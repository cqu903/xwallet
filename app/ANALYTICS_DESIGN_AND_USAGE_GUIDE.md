# xWallet App 埋点设计与抽象层使用指南

> 本文档用于沉淀本轮埋点改造的设计决策、架构变更与落地用法，作为后续功能开发的统一参考。

## 1. 改造目标

本轮改造目标：

1. **规范化**：统一事件定义、页面定义、属性校验与命名。
2. **自动化**：尽可能通过拦截器/观察器/基础组件自动采集，而不是在业务页手写。
3. **声明式**：页面层只保留业务逻辑，埋点通过组件一行声明。
4. **可扩展**：通过公共抽象（按钮、导航、Tab 等）支持后续页面快速接入。

---

## 2. 端到端埋点架构（当前）

### 2.1 数据流

```text
UI交互/系统事件
  -> AnalyticsService.trackStandardEvent(...)
  -> EventCollector.collect(...)
      - 自动补充 eventId/deviceId/context/environment
  -> EventReporter.report(...)
      - 先发 MQTT
      - 失败写 SQLite pending 队列
  -> 定时 retryPendingEvents 指数退避重试
```

### 2.2 MQTT 与可靠性策略

- Topic 规则：`app/{environment}/{category}`
  - 例：`app/dev/behavior`、`app/prod/critical`
- `environment` 来自 `AppConfig.instance.environment`
- 优先实时发送 MQTT；失败则落盘 SQLite，后台定时重试
- 重试策略：指数退避（上限 60s）

---

## 3. 分层设计（本轮新增/收敛）

## 3.1 规范层（Spec Layer）

### 文件

- `lib/analytics/event_spec.dart`
- `lib/analytics/app_routes.dart`
- `lib/analytics/analytics_constants.dart`

### 职责

- `AnalyticsEventType`：事件类型枚举（`page_view`、`button_click`、`api_request` 等）
- `AnalyticsElementType`：交互元素类型（`button/link/tab/icon/list_item/card`）
- `AnalyticsPages`：页面与路由映射
- `AnalyticsEventProperties`：属性构造器（`click/formSubmit/itemClick/apiRequest/error`）
- `AnalyticsEventValidator`：上报前校验必要字段
- `AnalyticsFlows / AnalyticsIds`：统一 flow 与 elementId 常量，杜绝魔法字符串

---

## 3.2 自动采集层（Auto Collection Layer）

### a) 路由自动 page_view

- 文件：`lib/analytics/analytics_route_observer.dart`
- 接入：`MaterialApp.navigatorObservers = [analyticsRouteObserver]`
- 行为：`didPush/didReplace` 自动上报 page_view

### b) 全局错误自动上报

- 文件：`lib/analytics/analytics_error_handler.dart`
- 接入：`main()` 启动时 `AnalyticsErrorHandler.install()`
- 行为：捕获 `FlutterError` 和 `PlatformDispatcher` 错误，统一上报 `error`

### c) API 请求自动上报

- 文件：`lib/services/api_service.dart`
- 机制：通过 `_trackedRequest` 包装 `_get/_post`
- 上报：`api_request`（method/path/success/duration/statusCode/errorType/message）
- 异常：通过 `AnalyticsErrorHandler.trackCaughtError(...)` 自动收敛

---

## 3.3 声明式交互组件层（UI Abstraction Layer）

### 基础组件

- `AnalyticsTap`
  - 最底层 Gesture 包装器
  - 支持任意 child + 一行事件声明

### 通用包装组件

- `AnalyticsPressable`：通用点击容器
- `AnalyticsIconButton`：图标按钮埋点
- `AnalyticsListTile`：列表项埋点
- `AnalyticsTextButton`：文本按钮埋点（新增）
- `AnalyticsElevatedButton`：主按钮埋点（新增）

### Tab 组件

- `TrackedBottomNavBar` + `TrackedTabItem`
  - 自动处理 `tab_click`
  - 自动处理 Tab 切换后的 `page_view`

---

## 3.4 导航埋点抽象层（Navigation Layer）

- 文件：`lib/analytics/navigation_tracking.dart`
- 扩展：`BuildContext` 上提供
  - `pushTracked(...)`
  - `pushReplacementNamedTracked(...)`
  - `popTracked(...)`

用途：把“点击跳转”与“埋点上报”合并，减少路由处重复代码。

---

## 4. 页面落地结果（本轮）

## 4.1 MainNavigation / Tab

- 主导航已切为 `TrackedBottomNavBar`
- Tab 事件和 Tab page_view 统一由组件托管

## 4.2 Home

- 页面 handler 中移除了手写埋点，保留业务行为（提示/刷新）
- 埋点下沉到组件：
  - `LoanCard`（申请）
  - `RewardMiniCard`（推荐）
  - `ActivityCarousel`（活动卡/查看更多）
  - `QuickActionsSection`（快捷操作）
  - `TransactionListSection`（重试/查看全部/交易项）

## 4.3 Profile

- 菜单项统一改为 `AnalyticsListTile`
- 右上关于入口改为 `AnalyticsIconButton`
- 退出按钮改为 `AnalyticsPressable`

## 4.4 Login / Register

- Login
  - 密码显隐：`AnalyticsIconButton`
  - 忘记密码 / 立即注册：`AnalyticsTextButton`
  - 登录按钮：`AnalyticsElevatedButton`
  - 登录结果：保留 `form_submit`（success/fail）
  - 登录成功跳转：`pushReplacementNamedTracked`
- Register
  - 发送验证码 / 注册：`AnalyticsElevatedButton`
  - 密码显隐 / 确认密码显隐：`AnalyticsIconButton`
  - 返回登录：`AnalyticsTextButton` + AppBar `AnalyticsIconButton`
  - 注册结果：`form_submit`（success/fail）

---

## 5. 推荐埋点模式（重要）

建议统一采用“双事件语义”：

1. **用户意图事件**（button_click/link_click/tab_click）
2. **业务结果事件**（form_submit success/fail、api_request success/fail）

示例：登录

- 点“登录”按钮 -> `button_click`
- 登录请求返回 -> `form_submit(success=true/false)`

这样可以同时衡量“点击行为”和“转化结果”。

---

## 6. 新页面接入模板

## 6.1 页面常量准备

1. 在 `AnalyticsPages` 增加页面定义
2. 在 `AppRoutes` 增加命名路由
3. 在 `AnalyticsIds` 增加页面内 elementId
4. 在 `AnalyticsFlows` 增加 flow（如需要）

## 6.2 组件接入优先级

优先顺序：

1. `AnalyticsElevatedButton / AnalyticsTextButton / AnalyticsIconButton`
2. `AnalyticsListTile`
3. `AnalyticsPressable`
4. `AnalyticsTap`（兜底）

原则：**优先用语义化组件，不要直接手写 GestureDetector + track 调用**。

## 6.3 跳转接入

- 页面跳转优先用 `pushTracked / pushReplacementNamedTracked`
- 返回可用 `popTracked`（若已在按钮层埋点则避免重复埋）

---

## 7. 组件使用示例

### 7.1 主按钮

```dart
AnalyticsElevatedButton(
  onPressed: onSubmit,
  eventType: AnalyticsEventType.buttonClick,
  properties: AnalyticsEventProperties.click(
    page: AnalyticsPages.login,
    flow: AnalyticsFlows.login,
    elementId: AnalyticsIds.loginSubmit,
    elementType: AnalyticsElementType.button,
    elementText: '登录',
  ),
  child: const Text('登录'),
)
```

### 7.2 文本链接按钮

```dart
AnalyticsTextButton(
  onPressed: onTap,
  eventType: AnalyticsEventType.linkClick,
  properties: AnalyticsEventProperties.click(
    page: AnalyticsPages.login,
    flow: AnalyticsFlows.login,
    elementId: AnalyticsIds.loginGoRegister,
    elementType: AnalyticsElementType.link,
    elementText: '立即注册',
  ),
  child: const Text('立即注册'),
)
```

### 7.3 导航埋点

```dart
await context.pushReplacementNamedTracked<void>(
  AppRoutes.main,
  page: AnalyticsPages.login,
  flow: AnalyticsFlows.login,
  elementId: AnalyticsIds.loginSuccessRedirect,
  elementType: AnalyticsElementType.button,
  eventType: AnalyticsEventType.buttonClick,
  elementText: '登录成功跳转',
);
```

---

## 8. 常见问题与约束

1. **避免重复埋点**
   - 如果按钮组件已经埋点，回调里不要再重复发同一个 click 事件。
2. **elementId 必须常量化**
   - 新增 elementId 先加到 `AnalyticsIds`，不要在页面里写字符串。
3. **flow 要稳定**
   - 同一业务链路用同一个 flow（如 `login` / `loan_apply` / `history`）。
4. **事件属性必须过校验**
   - 用 `trackStandardEvent + AnalyticsEventProperties`，不要绕开 validator。
5. **页面层尽量无埋点细节**
   - 页面只保留业务逻辑，埋点通过基础组件与 helper 托管。

---

## 9. 当前关键文件索引

- 规范与常量
  - `lib/analytics/event_spec.dart`
  - `lib/analytics/app_routes.dart`
  - `lib/analytics/analytics_constants.dart`
- 自动采集
  - `lib/analytics/analytics_route_observer.dart`
  - `lib/analytics/analytics_error_handler.dart`
  - `lib/services/api_service.dart`
- 服务与上报
  - `lib/services/analytics_service.dart`
  - `lib/services/event_collector.dart`
  - `lib/services/event_reporter.dart`
  - `lib/services/mqtt_client_wrapper.dart`
- 抽象组件
  - `lib/widgets/analytics/analytics_tap.dart`
  - `lib/widgets/analytics/analytics_pressable.dart`
  - `lib/widgets/analytics/analytics_icon_button.dart`
  - `lib/widgets/analytics/analytics_list_tile.dart`
  - `lib/widgets/analytics/analytics_text_button.dart`
  - `lib/widgets/analytics/analytics_elevated_button.dart`
  - `lib/widgets/analytics/tracked_bottom_nav_bar.dart`
- 导航 helper
  - `lib/analytics/navigation_tracking.dart`

---

## 10. 后续演进建议

1. 新增 `AnalyticsSwitch / AnalyticsCheckbox / AnalyticsSegmentedControl`
2. 将“结果事件”进一步抽成 helper（如 `trackFormResult(...)`）
3. 增加埋点单测（属性完整性与 eventType 规范）
4. 增加“页面接入检查清单”到 PR 模板

## 11. 变更记录

- 2026-02-09：统一命名为 `AnalyticsPressable`，并删除 `TrackButton` 兼容别名文件 `lib/widgets/analytics/track_button.dart`。
