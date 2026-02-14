# 埋点规范草案（v0.1）

适用范围：xWallet Flutter App（MQTT 上报）

## 目标

确保事件在“页面、控件、流程”维度可唯一定位，避免文案变动导致统计失真，支持后续分析与漏斗追踪。

## 1. 命名与基本原则

1. 事件类型使用固定枚举，避免自由拼接。
2. 页面标识使用类名 + 路由路径，流程复用时必须带 `flow`。
3. 控件标识必须稳定，文案不作为唯一标识。
4. 事件字段必须可回溯到具体页面与控件。

## 2. 事件类型（eventType）

1. `page_view` 页面曝光
2. `button_click` 按钮点击
3. `link_click` 链接点击
4. `tab_click` Tab 点击
5. `form_submit` 表单提交
6. `error` 业务错误/异常

## 3. 核心字段规范

### 公共字段（每个事件必须包含）

1. `page`: 页面类名，例如 `HomeScreen`
2. `route`: 路由路径，例如 `/home`
3. `eventType`: 事件类型枚举
4. `timestamp`: 事件时间戳（由 SDK 生成）

### 页面复用场景字段

1. `flow`: 业务流程标识，例如 `loan_apply`、`repay`
2. `entry`: 进入方式，例如 `banner`、`push`、`tab`、`deeplink`

### 控件点击类事件字段

1. `elementId`: 控件稳定标识，例如 `apply_loan`
2. `elementType`: `button | link | tab | card | list_item | icon`
3. `elementText`: 显示文案（可选，仅辅助排查）

### 列表/卡片/动态对象事件字段

1. `itemId`: 业务对象 ID
2. `itemType`: `transaction | activity | loan | product`
3. `itemName`: 业务对象名称（可选）

## 4. 字段示例

### 页面曝光

```dart
eventType: 'page_view',
properties: {
  'page': 'HomeScreen',
  'route': '/home',
  'flow': 'loan_apply',
  'entry': 'tab',
}
```

### 按钮点击

```dart
eventType: 'button_click',
properties: {
  'page': 'HomeScreen',
  'route': '/home',
  'flow': 'loan_apply',
  'entry': 'banner',
  'elementId': 'apply_loan',
  'elementType': 'button',
  'elementText': '立即申请',
}
```

### 列表项点击

```dart
eventType: 'transaction_click',
properties: {
  'page': 'HomeScreen',
  'route': '/home',
  'flow': 'history',
  'elementId': 'transaction_item',
  'elementType': 'list_item',
  'itemType': 'transaction',
  'itemId': transaction.id,
  'itemName': transaction.name,
}
```

## 5. 命名规则建议

1. `page` 使用 `Screen` 类名。
2. `route` 使用统一路由路径。
3. `elementId` 采用 `snake_case`，语义化且稳定。
4. `flow` 使用业务流程名，不使用文案。

## 6. 落地清单（模板 + 初版映射）

以下为模板与当前代码可识别的初版映射，后续可逐步补齐。

### 6.1 页面与路由映射表

字段说明：

1. `page`: 页面类名
2. `route`: 路由路径（当前项目未统一配置命名路由时可先留空）
3. `flow`: 默认业务流程（可选）
4. `entry`: 默认入口（可选）

| page | route | flow | entry | 备注 |
| --- | --- | --- | --- | --- |
| SplashScreen |  |  |  | 启动闪屏 |
| LoginScreen |  | login |  | 登录页 |
| RegisterScreen |  | register |  | 注册页 |
| HomeScreen |  | loan_apply | tab | 首页/贷款落地 |
| LoanApplyFlowScreen | /loan/apply | loan_apply | push | 贷款申请向导流程 |
| AccountScreen |  |  | tab | 钱包页 |
| HistoryScreen |  | history | tab | 记录页 |
| ProfileScreen |  |  | tab | 我的页 |

### 6.2 控件 elementId 映射表

字段说明：

1. `elementId`: 稳定标识（snake_case）
2. `elementType`: `button | link | tab | card | list_item | icon`
3. `eventType`: 事件类型
4. `status`: `已埋点 | 未埋点`

| page | elementId | elementType | eventType | status | 说明 |
| --- | --- | --- | --- | --- | --- |
| HomeScreen | apply_loan | button | button_click | 已埋点 | 立即申请按钮 |
| HomeScreen | share_referral | button | button_click | 已埋点 | 推荐分享按钮 |
| HomeScreen | activity_item | card | activity_click | 已埋点 | 活动卡片 |
| HomeScreen | view_more_activities | link | link_click | 已埋点 | 查看更多活动 |
| HomeScreen | quick_action_item | card | quick_action_click | 已埋点 | 快捷服务入口 |
| HomeScreen | retry_transactions | button | button_click | 已埋点 | 交易加载失败重试 |
| HomeScreen | view_all_transactions | link | link_click | 已埋点 | 查看全部交易 |
| HomeScreen | transaction_item | list_item | transaction_click | 已埋点 | 交易记录条目 |
| LoanApplyFlowScreen | loan_apply_retry_occupations | link | link_click | 已埋点 | 职业字典重试 |
| LoanApplyFlowScreen | loan_apply_prev_step | button | button_click | 已埋点 | 向导上一步 |
| LoanApplyFlowScreen | loan_apply_next_step | button | button_click | 已埋点 | 向导下一步 |
| LoanApplyFlowScreen | loan_apply_defer | button | button_click | 已埋点 | 向导稍后再说 |
| LoanApplyFlowScreen | loan_apply_submit | button | form_submit | 已埋点 | 申请提交（含 success） |
| LoanApplyFlowScreen | loan_apply_reject_acknowledge | button | button_click | 已埋点 | 拒绝页我知道了 |
| LoanApplyFlowScreen | loan_apply_contact_support | button | button_click | 已埋点 | 拒绝页联系客服 |
| LoanApplyFlowScreen | loan_apply_start_signing | button | button_click | 已埋点 | 审批通过后进入签署 |
| LoanApplyFlowScreen | loan_apply_defer_signing | button | button_click | 已埋点 | 审批通过后稍后处理 |
| LoanApplyFlowScreen | loan_apply_view_contract | link | link_click | 已埋点 | 查看完整合同 |
| LoanApplyFlowScreen | loan_apply_send_otp | button | button_click | 已埋点 | 发送短信验证码 |
| LoanApplyFlowScreen | loan_apply_agree_terms | button | button_click | 已埋点 | 勾选同意协议 |
| LoanApplyFlowScreen | loan_apply_sign_contract | button | form_submit | 已埋点 | 合同签署并放款（含 success） |
| LoanApplyFlowScreen | loan_apply_back_home | button | button_click | 已埋点 | 放款成功后返回首页 |
| MainNavigation | tab_home | tab | tab_click | 未埋点 | 底部导航-首页 |
| MainNavigation | tab_account | tab | tab_click | 未埋点 | 底部导航-钱包 |
| MainNavigation | tab_history | tab | tab_click | 未埋点 | 底部导航-记录 |
| MainNavigation | tab_profile | tab | tab_click | 未埋点 | 底部导航-我的 |
| LoginScreen | login_submit | button | form_submit | 已埋点 | 登录提交（当前事件类型为 login，可统一为 form_submit） |

### 6.3 业务流程 flow 词典

| flow | 说明 | 典型入口 |
| --- | --- | --- |
| loan_apply | 贷款申请流程 | tab / banner / push |
| history | 交易记录浏览 | tab |
| login | 登录流程 | splash / deeplink |
| register | 注册流程 | login_screen |

### 6.4 入口 entry 词典（建议）

| entry | 说明 |
| --- | --- |
| tab | 底部/顶部 Tab 进入 |
| banner | 轮播/运营位进入 |
| push | 推送/通知进入 |
| deeplink | 外部链接/深链进入 |
| shortcut | 快捷入口进入 |
