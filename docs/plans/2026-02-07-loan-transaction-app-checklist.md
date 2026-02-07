# 贷款交易实施清单（App）

**日期**: 2026-02-07  
**状态**: Ready for implementation  
**目标**: 将首页最近交易从样例切换为真实交易，完成还款/再提款交互接入与状态治理  
**关联文档**: `docs/product/loan-transaction-product-spec.md`

---

## 前置准备

- [ ] 确认交易接口契约与错误码
- [ ] 准备测试账号与测试数据（首放、还款、再提款）
- [ ] 确认埋点字段与事件名

---

## Phase 1：数据模型与服务接入

### Task 1：新增交易与账户模型

**目标**: 定义 App 端交易与账户摘要数据结构  
**范围文件**:
- `app/lib/models/*`

**子任务**:
- [ ] 新增交易列表模型（type/amount/time/components）
- [ ] 新增账户摘要模型（creditLimit/availableLimit/principalOutstanding/interestOutstanding）
- [ ] 增加统一解析与容错

**完成定义（DoD）**:
- [ ] 模型可解析后端返回
- [ ] 关键字段缺失时可安全降级

### Task 2：扩展 API 服务

**目标**: 在现有 `ApiService` 中新增贷款交易接口  
**范围文件**:
- `app/lib/services/api_service.dart`

**子任务**:
- [ ] 新增账户摘要查询
- [ ] 新增最近交易查询
- [ ] 新增还款接口
- [ ] 新增再次提款接口

**完成定义（DoD）**:
- [ ] 接口调用统一走 token 请求头
- [ ] 错误信息可直接用于 UI 展示

---

## Phase 2：状态管理与首页替换

### Task 3：新增交易 Provider

**目标**: 建立交易状态管理（loading/error/data）  
**范围文件**:
- `app/lib/providers/*`
- `app/lib/main.dart`

**子任务**:
- [ ] 新增 TransactionProvider
- [ ] 注册到 `MultiProvider`
- [ ] 支持刷新与失败重试

**完成定义（DoD）**:
- [ ] Provider 生命周期与现有模式一致
- [ ] 页面切换后状态行为符合预期

### Task 4：首页最近交易改造

**目标**: `TransactionListSection` 使用真实数据  
**范围文件**:
- `app/lib/screens/home_screen.dart`
- `app/lib/widgets/transaction_list.dart`

**子任务**:
- [ ] 替换默认样例数据来源
- [ ] 显示 loading/empty/error 三类状态
- [ ] 保留既有 UI 结构与样式

**完成定义（DoD）**:
- [ ] 首页展示真实最近交易
- [ ] 接口异常时有可读提示且可重试

---

## Phase 3：交互联动与可观测性

### Task 5：交易相关交互

**目标**: 打通“查看全部/点击交易/还款/再提款”关键交互  
**范围文件**:
- `app/lib/screens/home_screen.dart`
- `app/lib/widgets/quick_actions.dart`
- 相关页面文件（按实际新增）

**子任务**:
- [ ] 查看全部交易跳转到记录页
- [ ] 点击交易项进入详情（或统一详情入口）
- [ ] 快捷还款入口接入还款流程
- [ ] 再提款入口接入提款流程

**完成定义（DoD）**:
- [ ] 交互路径完整可用
- [ ] 成功/失败反馈一致

### Task 6：埋点与异常追踪

**目标**: 保持关键行为可观测  
**范围文件**:
- `app/lib/screens/home_screen.dart`
- `app/lib/services/analytics_service.dart`

**子任务**:
- [ ] 交易列表曝光与点击事件
- [ ] 还款/再提款成功与失败事件
- [ ] 刷新与重试事件

**完成定义（DoD）**:
- [ ] 核心行为均有埋点
- [ ] 异常路径可定位

---

## Phase 4：测试与验收

### Task 7：功能与回归测试

**目标**: 验证交易展示与交互闭环  
**范围文件**:
- `app/test/**`

**子任务**:
- [ ] 最近交易列表渲染测试
- [ ] loading/empty/error 状态测试
- [ ] 还款成功与失败流程测试
- [ ] 再提款额度不足场景测试

**完成定义（DoD）**:
- [ ] 关键测试通过
- [ ] 不影响登录、导航与现有页面

---

## 验收标准

### 功能验收

- [ ] 首页最近交易按真实数据倒序展示
- [ ] 查看全部、点击明细、还款、再提款链路可用
- [ ] 还款后页面展示的本金/利息拆分与后端一致

### 体验验收

- [ ] 首屏加载有统一 loading 状态
- [ ] 空列表与错误状态有明确引导
- [ ] 重试后可恢复正常展示

### 测试验收

- [ ] `app` 相关测试通过

---

## 里程碑建议

- M1（1天）: 模型 + API 服务接入
- M2（1天）: Provider + 首页数据替换
- M3（1天）: 交互联动 + 埋点
- M4（1天）: 测试与联调
