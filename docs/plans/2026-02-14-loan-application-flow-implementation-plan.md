# 贷款申请全流程实施计划（App + Backend）

**日期**: 2026-02-14  
**来源设计**: `docs/plans/2026-02-14-loan-application-flow-design.md`  
**目标**: 交付 KYC 前置 + 双页申请 + 自动审批 + 合同签署 + 首放闭环

## 1. 实施范围

- `backend`：申请域模型、审批编排、合同与 OTP、签署放款衔接
- `app`：双页向导、审批结果页、合同签署页、状态恢复
- 保持现有交易域不变量与幂等约束

## 2. 里程碑与任务

### M1：Backend 申请域骨架（2 天）

1. 数据库与实体
- 新增表：`loan_application`、`loan_contract_document`、`loan_application_otp`
- 新增实体与 Mapper/XML
- 建唯一索引（customer active application、idempotency key）

2. DTO 与 API 骨架
- 新增 `LoanApplication*` DTO（提交、查询、签署、OTP）
- 新增 `LoanApplicationController` 路由：
  - `POST /loan/applications`
  - `GET /loan/applications/current`
  - `POST /loan/applications/{id}/contracts/send-otp`
  - `POST /loan/applications/{id}/contracts/sign`

3. 服务层骨架
- `LoanApplicationService` + `impl`
- `RiskGateway`/`SmsOtpGateway` 接口与 mock 实现

完成标准：
- 接口可编译并可返回基础 mock 响应
- DB 表与 Mapper CRUD 可用

### M2：审批状态机与合同生成（2 天）

1. 提交申请编排
- KYC 前置校验
- 冷却拦截（24h）
- 进行中申请拦截
- 调用 `RiskGateway` 并推进状态

2. 合同生成
- 审批通过自动生成合同草稿（模板版本 + 摘要）
- 写入 `loan_contract_document`
- 设置 `expiresAt=approvedAt+14d`

3. 查询当前申请
- 聚合返回状态、倒计时、合同预览摘要

完成标准：
- 申请提交后可得到 `REJECTED` 或 `APPROVED_PENDING_SIGN`
- 通过单可读取到合同草稿信息

### M3：OTP 与签署放款衔接（2 天）

1. OTP 能力
- `send-otp`：生成 token/code（mock），保存 hash、过期时间、重发限制
- `sign`：校验 token/code、尝试次数、有效期

2. 签署与放款
- 校验 `agreeTerms=true`
- 签署成功推进状态为 `SIGNED`
- 调用交易域首放编排（复用现有 `LoanTransactionService`）
- 成功后推进 `DISBURSED`

3. 幂等与事务
- 建单与签署接口均支持幂等
- 签署+首放保证“最多一次成功入账”

完成标准：
- 签署成功返回交易摘要和账户摘要
- 重复签署请求不重复放款

### M4：App 双页向导与签署体验（2 天）

1. 新增 Provider
- `LoanApplicationProvider`：草稿、状态、OTP、错误、加载态

2. 页面实现
- 第 1 页基本信息（含 HKID 校验）
- 第 2 页职业与财务
- 审批结果页（通过/拒绝）
- 合同签署页（发送 OTP、验证码输入、协议勾选）
- 放款成功页

3. 状态恢复
- 入口先调用 `current`，恢复进行中申请
- 拒绝场景展示冷却倒计时

完成标准：
- 两页完成前不会建单
- 通过后可签署并完成放款闭环

### M5：测试与联调（1-2 天）

1. Backend 单测/集成
- KYC 拦截、字段校验、冷却、过期、OTP 异常、签署幂等

2. App 测试
- 向导校验、状态恢复、错误映射、OTP 倒计时

3. 联调回归
- 成功主路径与拒绝路径
- 回归现有还款/再提款能力不受影响

完成标准：
- 相关测试通过
- 联调用例通过并形成记录

## 3. 风险与缓解

1. 外部风控真实接入波动
- 缓解：`RiskGateway` 抽象 + 超时/降级策略（首版 mock）

2. 敏感信息合规风险（HKID/收入）
- 缓解：日志脱敏、字段加密、最小化返回

3. 签署重复提交导致重复放款
- 缓解：签署幂等键 + 事务边界 + 唯一约束

## 4. 交付清单

- 设计文档：`docs/plans/2026-02-14-loan-application-flow-design.md`
- 实施计划：`docs/plans/2026-02-14-loan-application-flow-implementation-plan.md`
- Backend 代码与测试
- App 代码与测试
- 联调验收记录
