# 贷款申请单据管理（front-web）设计文档

日期：2026-02-16  
适用项目：`front-web`（Next.js 管理后台）  
关联后端：`backend`（新增 admin 贷款申请查询接口）

## 1. 背景

当前仓库已具备贷款申请流程（顾客侧）与贷款交易管理（管理后台）。  
下一步需要在管理后台提供“贷款申请单据台账”，用于运营人员检索、查看和排查申请单据与合同信息。

## 2. 目标与范围

### 2.1 本期目标（V1）

- 提供贷款申请单据分页列表（台账）。
- 提供多条件筛选能力。
- 提供申请详情查看能力（抽屉）。
- 支持查看合同相关信息（含合同正文）。

### 2.2 非目标（V1 不做）

- 不做人工审批动作（通过/驳回/改额）。
- 不做备注、标签、补件流程。
- 不做导出、批量操作、审计报表。

## 3. 已确认决策

- 业务档位：`只读台账`
- 数据来源：`新增 admin 接口`
- 敏感字段：`完整展示`（包括 `hkid`、`contractContent`）
- 详情交互：`列表 + 右侧抽屉`

## 4. 方案对比与选型

## 4.1 方案 A（选中）

单页台账 + 抽屉详情 + 按需拉取详情数据

- 优点：实现快；和 `loan/transactions` 现有模式一致；运营检索效率高。
- 缺点：详情不可通过 URL 直达。

## 4.2 方案 B

列表页 + 独立详情页路由

- 优点：可分享详情链接，扩展审批动作更自然。
- 缺点：用户频繁跳转，操作节奏弱于抽屉。

## 4.3 方案 C

主从分栏常驻详情

- 优点：高频审核体验更连续。
- 缺点：首版成本高，移动端适配更复杂。

结论：V1 采用方案 A。

## 5. 信息架构与页面结构

新增页面：

- `front-web/src/app/[locale]/(dashboard)/loan/applications/page.tsx`

页面分区：

- 筛选区（顶部）
- 列表区（中部）
- 分页区（底部）
- 详情抽屉（右侧）

## 6. 后端接口契约（front 接入预期）

> 说明：以下为管理后台新增接口约定，前端按此接入。

### 6.1 分页查询

`GET /admin/loan/applications`

查询参数建议：

- `page`, `size`
- `applicationNo`
- `customerId`
- `status`
- `riskDecision`
- `contractNo`
- `contractStatus`
- `startTime`, `endTime`

返回结构建议：

- `list`
- `total`
- `page`
- `size`
- `totalPages`

列表项字段建议：

- `applicationId`
- `applicationNo`
- `customerId`
- `fullName`
- `status`
- `riskDecision`
- `approvedAmount`
- `contractNo`
- `contractStatus`
- `createdAt`
- `updatedAt`

### 6.2 详情查询

`GET /admin/loan/applications/{applicationId}`

详情字段建议：

- 申请信息：`applicationId`, `applicationNo`, `status`, `productCode`
- 客户信息：`customerId`, `fullName`, `hkid`, `homeAddress`, `age`, `occupation`
- 财务信息：`monthlyIncome`, `monthlyDebtPayment`
- 风险信息：`riskDecision`, `riskReferenceId`, `rejectReason`
- 金额时间：`approvedAmount`, `cooldownUntil`, `approvedAt`, `expiresAt`, `signedAt`, `disbursedAt`, `createdAt`, `updatedAt`
- 合同信息：`contractNo`, `templateVersion`, `contractStatus`, `digest`, `contractContent`

### 6.3 权限建议

- 读取权限：`loan:application:read`

## 7. 前端实现设计

## 7.1 API 封装

新增文件：

- `front-web/src/lib/api/loan-applications-admin.ts`

导出方法：

- `fetchAdminLoanApplications(params)`
- `fetchAdminLoanApplicationDetail(applicationId)`

与现有 `loan-transactions-admin.ts` 保持一致的 `unwrap` 与错误处理策略。

## 7.2 页面状态管理

核心状态：

- `page`
- `filters`（提交态）
- `searchInput`（输入态）
- `selectedApplicationId`
- `isDetailOpen`

请求策略：

- 列表：`useSWR(['admin-loan-applications', queryParams], fetcher)`
- 详情：`useSWR(['admin-loan-application-detail', selectedApplicationId], fetcher)`（仅抽屉打开且有 ID 时触发）

## 7.3 列表与筛选

筛选项：

- 申请编号、客户 ID、申请状态、风险决策、合同号、合同状态、时间区间

列表列：

- 申请编号
- 客户（ID + 姓名）
- 申请状态
- 风险结果
- 核准金额
- 合同状态
- 创建时间
- 更新时间
- 操作（查看详情）

## 7.4 详情抽屉

分区展示：

- 基本信息
- 风险与财务信息
- 时间轴信息
- 合同信息（含 `contractContent` 完整内容）

## 8. 状态映射与展示文案

申请状态：

- `SUBMITTED`
- `REJECTED`
- `APPROVED_PENDING_SIGN`
- `SIGNED`
- `DISBURSED`
- `EXPIRED`

合同状态：

- `DRAFT`
- `SIGNED`

风险结果：

- `APPROVED`
- `REJECTED`

对于未知枚举值，前端按原值回显并采用中性样式，避免信息丢失。

## 9. 错误处理与安全

- 统一沿用 `client.ts` 鉴权处理：`401` 清理鉴权并跳转登录。
- 列表请求失败：列表区错误态 + 重试入口。
- 详情请求失败：抽屉内错误态，不影响列表继续使用。
- 本期按确认要求展示敏感字段；不在前端日志打印完整合同内容。

## 10. 测试计划

新增测试：

- `front-web/src/__tests__/lib/api/loan-applications-admin.test.ts`
- `front-web/src/app/[locale]/(dashboard)/loan/applications/__tests__/page.test.tsx`

覆盖重点：

- 参数拼接与响应解包
- 列表加载态/空态/错误态
- 列表渲染与筛选提交
- 打开抽屉并拉取详情
- 详情异常兜底

## 11. 里程碑（建议）

1. 后端提供并联调 admin 查询接口。
2. 前端完成 API 封装 + 页面骨架。
3. 完成详情抽屉与状态映射。
4. 补齐单测并回归 `loan/transactions` 相关功能。
