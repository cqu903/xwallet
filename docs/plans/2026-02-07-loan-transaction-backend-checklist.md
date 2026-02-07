# 贷款交易实施清单（Backend）

**日期**: 2026-02-07  
**状态**: Ready for implementation  
**目标**: 落地贷款交易后端闭环（首放、还款清分、再次提款），满足额度不变量与幂等一致性  
**关联文档**: `docs/product/loan-transaction-product-spec.md`

---

## 前置准备

- [ ] 确认数据库变更窗口与回滚策略
- [ ] 明确接口鉴权范围（CUSTOMER）
- [ ] 评审核心规则：`interest -> principal`、仅本金恢复额度

---

## Phase 1：数据模型与表结构

### Task 1：新增贷款核心表

**目标**: 建立账户快照 + 合同 + 交易流水模型  
**范围文件**:
- `backend/database/init_all.sql`

**子任务**:
- [ ] 新增 `loan_account`（额度快照）
- [ ] 新增 `loan_contract`（合同）
- [ ] 新增 `loan_transaction`（不可变流水）
- [ ] 增加必要唯一索引（合同号、幂等键等）

**完成定义（DoD）**:
- [ ] 表结构可支持首放、还款、再提款
- [ ] 约束可阻断重复交易与非法状态

### Task 2：新增实体与 Mapper

**目标**: 完成 MyBatis 持久层接入  
**范围文件**:
- `backend/src/main/java/com/zerofinance/xwallet/model/entity/*`
- `backend/src/main/java/com/zerofinance/xwallet/repository/*`
- `backend/src/main/resources/mapper/*`

**子任务**:
- [ ] 新增 LoanAccount/LoanContract/LoanTransaction 实体
- [ ] 新增 Mapper 接口与 XML
- [ ] 提供按客户查询账户与流水的基础查询

**完成定义（DoD）**:
- [ ] Mapper CRUD 可用
- [ ] 查询字段满足 App 展示需求

---

## Phase 2：还款清分引擎与交易编排

### Task 3：实现清分引擎（独立模块）

**目标**: 以独立模块实现 v1 清分策略（利息优先、本金其次）  
**范围文件**:
- `backend/src/main/java/com/zerofinance/xwallet/service/**/RepaymentAllocationEngine*`

**子任务**:
- [ ] 定义输入输出契约（request/snapshot/result）
- [ ] 实现 `interest -> principal` 清分逻辑
- [ ] 输出 `interestPaid`、`principalPaid`、`lineItems`
- [ ] 预留 penalty/fee 扩展点（不在本期落地逻辑）

**完成定义（DoD）**:
- [ ] 清分模块可独立单测
- [ ] 交易服务仅依赖引擎结果，不耦合内部细节

### Task 4：实现交易服务编排

**目标**: 完成首放、还款、再提款业务编排  
**范围文件**:
- `backend/src/main/java/com/zerofinance/xwallet/service/**`

**子任务**:
- [ ] 合同签署触发首放（金额=合同金额，首放后可用额度=0）
- [ ] 还款调用清分引擎并按本金恢复额度
- [ ] 再提款按可用额度校验并扣减
- [ ] 每笔写操作落不可变流水

**完成定义（DoD）**:
- [ ] 满足不变量：`available + principalOutstanding = creditLimit`
- [ ] 事务内完成校验、清分、额度更新、流水落库

---

## Phase 3：API 与一致性控制

### Task 5：新增贷款交易 API

**目标**: 提供 App 所需查询与写入接口  
**范围文件**:
- `backend/src/main/java/com/zerofinance/xwallet/controller/*`
- `backend/src/main/java/com/zerofinance/xwallet/model/dto/*`

**子任务**:
- [ ] 账户摘要查询接口
- [ ] 最近交易查询接口（倒序）
- [ ] 合同签署并首放接口
- [ ] 还款接口
- [ ] 再提款接口

**完成定义（DoD）**:
- [ ] 返回结构统一 `ResponseResult<T>`
- [ ] 错误码/错误信息可供 App 直接使用

### Task 6：幂等与并发控制

**目标**: 防重复记账与额度穿透  
**范围文件**:
- `backend/src/main/java/com/zerofinance/xwallet/service/**`
- `backend/src/main/resources/mapper/*`

**子任务**:
- [ ] 写接口要求幂等键
- [ ] 幂等重复请求返回相同业务结果
- [ ] 同账户写入并发串行化

**完成定义（DoD）**:
- [ ] 重试不产生重复交易
- [ ] 并发场景不破坏额度不变量

---

## Phase 4：测试与验收

### Task 7：单测与集成测试

**目标**: 验证核心规则与异常路径  
**范围文件**:
- `backend/src/test/java/**`

**子任务**:
- [ ] 首放规则测试
- [ ] 还款清分测试（利息+本金）
- [ ] 本金恢复额度测试
- [ ] 再提款额度校验测试
- [ ] 幂等重复请求测试
- [ ] 并发写入测试

**完成定义（DoD）**:
- [ ] 关键业务测试通过
- [ ] 回归不破坏现有认证与用户流程

---

## 验收标准

### 功能验收

- [ ] 合同签署自动首放成功，且首放后可用额度为 0
- [ ] 还款可拆分出利息/本金
- [ ] 仅本金部分恢复可用额度
- [ ] 再提款不能超过可用额度

### 一致性验收

- [ ] 每笔交易有不可变流水
- [ ] 幂等请求不重复记账
- [ ] 并发写入不破坏不变量

### 测试验收

- [ ] `backend` 相关测试通过

---

## 里程碑建议

- M1（2天）: 表结构 + 实体/Mapper
- M2（2天）: 清分引擎 + 交易编排
- M3（1天）: API + 幂等并发控制
- M4（1天）: 测试与联调支持
