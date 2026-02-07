# 贷款交易实施清单（Front-web）

**日期**: 2026-02-07  
**状态**: Ready for implementation  
**目标**: 管理后台新增“交易记录管理”页面，支持交易记录增删改查并遵循不可变流水规则  
**关联文档**:
- `docs/product/loan-transaction-product-spec.md`
- `docs/plans/2026-02-07-loan-transaction-front-design.md`

---

## 前置准备

- [ ] 确认后台接口与权限点（read/create/update_note/reverse）
- [ ] 明确交易状态与类型枚举
- [ ] 准备测试数据（首放、还款、再提款、冲正）

---

## Phase 1：页面骨架与列表查询

### Task 1：新增交易记录页面

**目标**: 建立页面路由与布局  
**范围文件**:
- `front-web/src/app/[locale]/(dashboard)/**`
- `front-web/src/components/**`

**子任务**:
- [ ] 新增“交易记录”路由与菜单入口
- [ ] 页面布局：筛选区 + 表格 + 分页 + 详情抽屉
- [ ] 复用现有表格与表单组件

**完成定义（DoD）**:
- [ ] 页面可访问
- [ ] 布局符合设计稿

### Task 2：交易列表查询

**目标**: 接入交易列表 API  
**范围文件**:
- `front-web/src/lib/api/**`
- `front-web/src/app/[locale]/(dashboard)/**`

**子任务**:
- [ ] 实现列表查询与分页
- [ ] 支持默认按 occurredAt 倒序
- [ ] loading/empty/error 状态展示

**完成定义（DoD）**:
- [ ] 列表与分页可用
- [ ] 错误可读且可重试

---

## Phase 2：筛选、详情与备注更新

### Task 3：筛选区

**目标**: 支持关键字段过滤  
**范围文件**:
- `front-web/src/app/[locale]/(dashboard)/**`

**子任务**:
- [ ] 客户ID、合同号、类型、状态、来源
- [ ] 时间区间、金额区间、幂等键、创建人、备注关键词
- [ ] 重置与搜索

**完成定义（DoD）**:
- [ ] 筛选条件映射到查询参数
- [ ] 重置恢复默认

### Task 4：详情抽屉与备注更新

**目标**: 展示交易详情并允许更新 note  
**范围文件**:
- `front-web/src/app/[locale]/(dashboard)/**`
- `front-web/src/lib/api/**`

**子任务**:
- [ ] 点击行打开详情抽屉
- [ ] 展示摘要/清分明细/账户快照/审计信息
- [ ] note 更新接口接入

**完成定义（DoD）**:
- [ ] 详情信息完整
- [ ] note 更新生效并回刷列表

---

## Phase 3：新增交易与冲正

### Task 5：新增交易弹窗

**目标**: 支持创建运营交易  
**范围文件**:
- `front-web/src/app/[locale]/(dashboard)/**`
- `front-web/src/lib/api/**`

**子任务**:
- [ ] 表单字段与校验（仅 REPAYMENT / REDRAW_DISBURSEMENT）
- [ ] 幂等键必填与发生时间支持
- [ ] 成功后定位到新增交易

**完成定义（DoD）**:
- [ ] 可成功创建运营交易
- [ ] 错误提示清晰

### Task 6：冲正/作废操作

**目标**: 生成 REVERSAL 交易  
**范围文件**:
- `front-web/src/app/[locale]/(dashboard)/**`
- `front-web/src/lib/api/**`

**子任务**:
- [ ] 行内与详情抽屉入口
- [ ] 二次确认弹窗
- [ ] 成功后回刷状态

**完成定义（DoD）**:
- [ ] 冲正成功且原交易状态更新
- [ ] 操作权限控制有效

---

## Phase 4：权限、埋点与测试

### Task 7：权限控制

**目标**: 基于权限点控制可见性  
**范围文件**:
- `front-web/src/lib/**`
- `front-web/src/app/[locale]/(dashboard)/**`

**子任务**:
- [ ] read/create/update_note/reverse 权限控制
- [ ] 无权限提示与禁用状态

**完成定义（DoD）**:
- [ ] 权限控制符合预期

### Task 8：埋点与测试

**目标**: 保持核心行为可观测  
**范围文件**:
- `front-web/src/lib/**`
- `front-web/src/app/[locale]/(dashboard)/**`
- `front-web/src/**/__tests__/**`

**子任务**:
- [ ] 列表曝光、筛选、创建、冲正埋点（若已有体系）
- [ ] 关键交互的单测/组件测试

**完成定义（DoD）**:
- [ ] 关键路径可观测
- [ ] 相关测试通过

---

## 验收标准

### 功能验收

- [ ] 交易记录查询、分页、详情可用
- [ ] 运营交易创建可用且幂等可控
- [ ] 冲正/作废可生成反向流水
- [ ] 备注更新可生效

### 体验验收

- [ ] loading/empty/error 展示统一
- [ ] 操作反馈明确，失败可重试

### 测试验收

- [ ] `front-web` 相关测试通过

---

## 里程碑建议

- M1（1天）: 页面骨架 + 列表查询
- M2（1天）: 筛选 + 详情 + 备注更新
- M3（1天）: 新增交易 + 冲正
- M4（1天）: 权限 + 测试
