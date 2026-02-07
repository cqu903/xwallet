# 贷款交易管理页面交互稿（Front-web）

**日期**: 2026-02-07  
**状态**: Draft  
**目标**: 管理后台新增“交易记录管理”页面，支持交易记录增删改查并遵循不可变流水规则  
**关联文档**: `docs/product/loan-transaction-product-spec.md`

---

## 1. 页面定位与入口

- 入口建议：`交易管理 > 交易记录`
- 面向角色：运营/测试/风控
- 关键约束：禁止物理删除与直接修改历史流水；仅允许备注更新与冲正/作废。

---

## 2. 页面结构

1. 顶部筛选区（可折叠）
2. 列表区（表格 + 分页）
3. 详情抽屉（点击行展开）
4. 操作弹窗
   - 新增交易
   - 冲正/作废确认

---

## 3. 筛选区（建议字段）

- 客户ID
- 合同号
- 交易类型（INITIAL_DISBURSEMENT / REDRAW_DISBURSEMENT / REPAYMENT / REVERSAL）
- 交易状态（PENDING / POSTED / SETTLED / REVERSED）
- 来源（SYSTEM / APP / ADMIN）
- 时间区间（occurredAt）
- 金额区间（amount）
- 幂等键（idempotencyKey）
- 创建人（createdBy）
- 备注关键词（note）

交互规则：
- 默认时间区间：近 30 天（可清空）
- 搜索按钮触发查询；重置恢复默认

---

## 4. 列表区（表格字段）

默认按 `occurredAt` 倒序。

字段建议（从左到右）：
- 交易ID
- 类型
- 状态
- 发生时间
- 金额
- 本金/利息（仅还款展示）
- 客户ID
- 合同号
- 来源
- 幂等键
- 创建人
- 备注

交互：
- 点击行打开详情抽屉
- 仅在允许操作时展示“冲正/作废”按钮
- 表格工具栏：`新增交易`、`导出（占位）`

---

## 5. 详情抽屉

内容分区：
- 交易摘要：基础字段（金额、类型、状态、时间、来源、幂等键）
- 清分明细：interestPaid / principalPaid / lineItems
- 账户快照：creditLimit / availableLimit / principalOutstanding / interestOutstanding
- 审计信息：createdAt / createdBy / note / strategyVersion

动作：
- 编辑备注（仅 note 字段）
- 冲正/作废（符合权限与状态）

备注规则：
- note 可多次修改，保留最后一次内容（是否保留历史由后端决定）

---

## 6. 新增交易弹窗

允许类型：
- `REPAYMENT`
- `REDRAW_DISBURSEMENT`

必填字段：
- 客户ID
- 合同号
- 金额
- 幂等键
- 发生时间（默认当前时间，不可调整）

可选字段：
- 备注

说明：
- `INITIAL_DISBURSEMENT` 仅由合同签署触发，后台不允许手动创建。

校验：
- 金额 > 0
- 幂等键不可为空
- 再提款需后端校验可用额度

成功提示：
- 展示交易ID
- 列表自动刷新并定位到新增交易

---

## 7. 冲正/作废流程

触发：
- 列表操作或详情抽屉

确认弹窗内容：
- 原交易摘要
- 冲正后的影响提示（由后端返回）
- 备注输入（可选）

结果：
- 生成 `REVERSAL` 交易
- 原交易标记为 `REVERSED`

---

## 8. 状态与异常处理

- Loading：表格骨架或 loading 状态
- Empty：空状态提示 + 清空筛选按钮
- Error：错误提示 + 重试按钮

错误展示：
- 直接展示后端错误信息（遵循统一错误格式）

---

## 9. 权限与可见性

建议权限：
- `loan:transaction:read`
- `loan:transaction:create`
- `loan:transaction:update_note`
- `loan:transaction:reverse`

无权限时：
- 操作按钮隐藏或禁用，并提示权限不足

---

## 10. 埋点（可选）

- 交易列表曝光
- 筛选查询次数
- 创建交易成功/失败
- 冲正成功/失败
