# 贷款交易与还款清分产品文档

**文档版本**: v1.1  
**状态**: Draft（可进入评审）  
**最后更新**: 2026-02-07  
**适用范围**: `app/`（Flutter 客户端）、`backend/`（Spring Boot 后端）、`front-web/`（管理后台）

## 1. 文档目标

本文档定义 xWallet 贷款交易核心规则与实现边界，确保以下目标：

- 首页“最近交易”从样例数据切换为真实交易数据。
- 交易能力覆盖：发放贷款、再次提款、还款。
- 还款清分模块独立化，对外封装实现细节，支持后续扩展（罚息、费用、减免等）。
- 业务规则、接口契约、状态变更与测试口径形成统一参照，后续变更必须同步文档。

## 2. 业务范围与非范围

### 2.1 本期范围

- 合同签署后自动发放首笔贷款（金额=合同签署金额）。
- 首放后可用额度变更为 0。
- 用户还款后仅按“本金部分”恢复可用额度。
- 用户可在可用额度内再次提款。
- 首页与记录页展示真实交易流水。
- 管理后台提供“交易记录管理”页面，支持交易记录的查询、创建、纠错与作废（遵循不可变流水规则）。

### 2.2 非本期范围

- 复杂罚息与费用优先级规则（本期仅预留扩展点，不完整落地）。
- 多产品线差异化清分策略（本期先实现单策略模板）。
- 跨系统清结算、总账对接、监管报送等外部系统能力。
- 管理后台批量导入、批量冲正、对账导出等高级运营能力。

## 3. 领域术语

- **授信额度（creditLimit）**: 合同生效后可循环使用的总额度。
- **可用额度（availableLimit）**: 当前可再次提款的额度。
- **在贷本金（principalOutstanding）**: 尚未归还的本金余额。
- **首放（Initial Disbursement）**: 合同签署成功后自动触发的第一笔放款。
- **再次提款（Redraw）**: 在可用额度内新增提款。
- **还款清分（Repayment Allocation）**: 将还款金额按规则拆分到利息、本金（后续可含罚息、费用）。

## 4. 核心业务规则（强约束）

1. 合同签署成功必须触发首放，且首放金额 = 合同签署金额。
2. 首放完成后：`availableLimit = 0`。
3. 还款清分默认顺序：`应还利息 -> 应还本金`。
4. 仅“归还本金”会恢复额度：`availableLimit += repaidPrincipal`。
5. 再次提款必须满足：`redrawAmount <= availableLimit`。
6. 再次提款成功后：`availableLimit -= redrawAmount`。
7. 每笔交易必须生成不可变流水，不允许物理修改历史流水（纠错采用冲正/反向流水）。

## 5. 账户不变量

系统在每次交易后必须满足：

- `0 <= availableLimit <= creditLimit`
- `0 <= principalOutstanding <= creditLimit`
- `availableLimit + principalOutstanding = creditLimit`

> 说明：利息余额不参与额度恢复恒等式，但参与还款清分。

## 6. 还款清分模块设计（独立模块）

## 6.1 设计目标

- 规则复杂度与交易主流程解耦。
- 对外仅暴露稳定输入/输出，不泄漏内部策略细节。
- 支持策略扩展与版本演进，避免未来重写交易主流程。

### 6.2 模块边界

模块名称建议：`RepaymentAllocationEngine`

- **输入**
  - `repaymentAmount`
  - `accountSnapshot`（应还利息、应还本金、可用额度、在贷本金等）
  - `allocationContext`（产品、合同、日期、策略版本）
- **输出**
  - `allocationResult`
    - `interestPaid`
    - `principalPaid`
    - `unallocatedAmount`（可选）
    - `lineItems[]`（清分明细）

### 6.3 对外契约（封装）

交易服务仅依赖：

- `allocate(repaymentRequest, accountSnapshot) -> allocationResult`

交易服务不感知：

- 清分内部顺序实现细节
- 规则参数存储结构
- 未来罚息/费用的内部处理逻辑

### 6.4 规则扩展机制

采用策略链（Waterfall）并支持版本化：

- v1: `interest -> principal`
- v2+（预留）: `penalty -> interest -> principal -> fee`（示例）

每次还款都记录策略版本，保证历史可追溯。

## 7. 交易类型与状态机

### 7.1 交易类型

- `INITIAL_DISBURSEMENT`（首放）
- `REDRAW_DISBURSEMENT`（再次提款）
- `REPAYMENT`（还款）
- `REVERSAL`（冲正，预留）

### 7.2 状态流转（简化）

- `PENDING` -> `POSTED` -> `SETTLED`（可选）
- 异常纠错走 `REVERSAL`，不回写修改原交易。

## 8. 接口契约（产品级）

> 具体 URL 以后端实现为准，本文档定义产品语义与字段约束。

### 8.1 账户摘要查询

- 返回：`creditLimit`, `availableLimit`, `principalOutstanding`, `interestOutstanding`
- 用途：首页额度卡片、钱包页、风险提示。

### 8.2 最近交易查询

- 返回按时间倒序的交易列表，字段至少包括：
  - `transactionId`
  - `type`
  - `occurredAt`
  - `amount`
  - `principalComponent`（还款类可见）
  - `interestComponent`（还款类可见）
  - `balanceAfter`（可选）

### 8.3 合同签署并触发首放

- 请求包含：合同标识、签署金额、幂等键。
- 成功后返回：首放交易摘要 + 最新账户摘要。

### 8.4 还款

- 请求包含：还款金额、幂等键。
- 服务端调用清分引擎并返回：
  - `interestPaid`
  - `principalPaid`
  - `availableLimitAfter`
  - `principalOutstandingAfter`

### 8.5 再次提款

- 请求包含：提款金额、幂等键。
- 校验通过后返回交易摘要与账户摘要。

### 8.6 交易记录管理（Admin）

> 管理后台接口仅用于运营/测试/纠错，不可绕过交易不变量与幂等规则。

- 查询：支持按客户、合同、类型、状态、时间区间、金额区间过滤，默认按时间倒序分页。
- 创建：允许创建“运营录入交易”（仅限 `REPAYMENT` 与 `REDRAW_DISBURSEMENT`），必须校验额度不变量与幂等键。
- 修改：禁止直接修改历史流水，仅允许“纠错/备注”字段更新与“冲正”操作。
- 删除：禁止物理删除，支持“作废/冲正”形成反向流水。

返回字段至少包括：
- `transactionId`
- `type`
- `status`
- `occurredAt`
- `amount`
- `principalComponent`
- `interestComponent`
- `customerId`
- `contractId`
- `idempotencyKey`
- `source`（SYSTEM/APP/ADMIN）
- `createdBy`（可选）
- `note`（可选）

## 9. 技术与数据约束

### 9.1 幂等

- 所有写接口必须要求幂等键。
- 同幂等键重复请求返回同一业务结果，禁止重复记账。

### 9.2 并发一致性

- 涉及额度变更与清分入账的操作在同一事务完成。
- 同一账户并发写需串行化控制，避免额度穿透。

### 9.3 审计与追踪

- 每笔交易保留可追溯明细（含清分结果、策略版本、操作来源）。
- 禁止删除历史流水。

## 10. 前后端落地映射

## 10.1 App（Flutter）

- 首页最近交易来源由 mock 切换为后端接口。
- 新增交易数据 Provider（沿用现有 Provider 架构）。
- 保留 UI 结构，替换数据来源与点击行为。

### 10.2 Backend（Spring Boot）

- 新增贷款域实体、服务、仓储与控制器。
- 引入 `RepaymentAllocationEngine` 独立模块。
- 交易服务编排：校验 -> 清分/入账 -> 额度更新 -> 流水落库。

### 10.3 Front-web（管理后台）

- 新增“交易记录管理”页面：列表、筛选、详情抽屉、创建/冲正弹窗。
- 列表默认按 `occurredAt` 倒序，支持分页与导出按钮占位。
- CRUD 语义遵循不可变流水规则：
  - Create：创建运营交易（受限类型）。
  - Read：列表/详情。
  - Update：仅允许更新备注与标签类字段。
  - Delete：转为“冲正/作废”生成反向流水。

## 11. 验收标准（产品）

- 合同签署后自动首放成功，且可用额度变为 0。
- 还款后利息与本金拆分清晰可见。
- 仅本金导致可用额度恢复。
- 再次提款会正确扣减可用额度，且不可超过可用额度。
- 首页最近交易显示真实流水，顺序与金额正确。
- 管理后台可完成交易记录查询、创建运营交易、冲正/作废操作，且不破坏交易不变量。

## 12. 演进路线

### 12.1 Phase 1（当前）

- 完成 `interest -> principal` 清分策略。
- 完成首放、还款、再次提款三类交易闭环。

### 12.2 Phase 2（扩展）

- 引入罚息、费用、减免等清分项。
- 支持多策略模板与按产品版本路由。

### 12.3 Phase 3（增强）

- 增强对账、冲正、异常恢复与离线重试治理。

## 13. 文档治理与变更同步（强制）

本节为团队执行约束：

1. 任一影响以下内容的代码变更，必须同步更新本文档：
   - 业务规则
   - 清分顺序
   - 接口字段/语义
   - 状态流转
   - 不变量与验收标准
2. PR 评审清单必须包含：`是否更新 docs/product/loan-transaction-product-spec.md`。
3. 若未更新文档，涉及上述内容的 PR 不允许合并。

## 14. 变更记录

| 日期 | 版本 | 变更内容 | 责任人 |
|---|---|---|---|
| 2026-02-07 | v1.0 | 首版：定义首放/还款/再提款与独立清分模块边界 | AI + 产品讨论 |
| 2026-02-07 | v1.1 | 增加管理后台交易记录管理与 CRUD 语义 | AI + 产品讨论 |

## 15. 实施清单索引

- Backend: `docs/plans/2026-02-07-loan-transaction-backend-checklist.md`
- App: `docs/plans/2026-02-07-loan-transaction-app-checklist.md`
