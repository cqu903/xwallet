# 贷款申请全流程设计（App + Backend）

**日期**: 2026-02-14  
**状态**: Approved  
**作者**: Codex + User  
**范围**: `app/` + `backend/`

## 1. 目标与范围

构建用户端贷款申请完整流程，覆盖：

- KYC 前置校验
- 多页申请向导（2 页）
- 自动审批（外部风控网关，首版 mock）
- 审批通过后合同生成
- 短信验证码签署（首版 mock）
- 签署成功后触发首放

本期不包含人工审批流。

## 2. 关键业务决策（已确认）

- 端到端主链路：`申请 -> 自动审批 -> 合同签署 -> 放款`
- 审批机制：全自动审批
- 风控来源：外部风控接口（首版 mock）
- 审批结果：`APPROVED / REJECTED`
- 拒绝后冷却：24 小时
- KYC：申请前置，未完成不允许提交申请
- 放款时机：审批通过后，用户完成合同签署再放款
- 合同生成：后端按模板自动生成，App 仅展示与签署
- 签署方式：勾选同意 + 短信验证码
- OTP：首版 mock（固定码/日志）
- 合同过期：审批通过后 14 天未签自动失效

## 3. 总体架构

### 3.1 分层职责

- `app`：贷款申请流程引导、表单校验、签署交互、状态恢复。
- `backend` 申请域：申请建单、审批编排、合同管理、OTP 校验、状态机推进。
- `backend` 交易域（已存在）：首放、还款、再提款、账户快照与流水不变量。

### 3.2 外部能力抽象

- `RiskGateway`：审批决策来源；首版使用 mock 实现。
- `SmsOtpGateway`：验证码发送能力；首版使用 mock 实现。

### 3.3 申请状态机

- `SUBMITTED`：申请已提交，待审批完成。
- `REJECTED`：审批拒绝，记录 `cooldownUntil`。
- `APPROVED_PENDING_SIGN`：审批通过，合同已生成，待签署。
- `SIGNED`：合同已签，待放款编排完成。
- `DISBURSED`：首放完成。
- `EXPIRED`：审批通过后超过 14 天未签。

状态约束：

- `SUBMITTED/APPROVED_PENDING_SIGN/SIGNED` 期间不可重复建单。
- `REJECTED` 且未过 `cooldownUntil` 不可建单。
- `EXPIRED` 后可重新申请。

## 4. App 交互流程（修订版）

### 4.1 入口与恢复

- 点击申请入口先调用 `GET /loan/applications/current`。
- 若存在进行中申请，恢复到对应节点（待签、冷却、过期提示）。
- 无有效申请时进入新申请向导。

### 4.2 申请向导（2 页）

第 1 页：基本信息

- `fullName`（客户姓名）
- `hkid`（身份证号码，HKID）
- `homeAddress`（家庭住址）
- `age`（年龄）

校验要求：

- 姓名、住址必填
- HKID 格式合法（含校验位）
- 年龄范围合法（建议 18-70，可配置）

第 2 页：职业与财务

- `occupation`（职业，基础数据选择）
- `monthlyIncome`（月收入）
- `monthlyDebtPayment`（每月应还负债，含房贷及其他贷款）

校验要求：

- 职业必选
- 月收入 `> 0`
- 月负债 `>= 0`

建单时机：

- 两页全部完成后，用户点击提交才调用后端建单。
- 提交前不创建贷款申请实体。

### 4.3 审批与签署

- 提交后若 `REJECTED`：展示拒绝信息与 24h 冷却倒计时。
- 提交后若 `APPROVED_PENDING_SIGN`：进入合同预览。
- 用户发送 OTP 并输入验证码，勾选协议后发起签署。
- 签署成功后进入放款成功页，并刷新首页账户摘要/最近交易。

### 4.4 Provider 设计

新增 `LoanApplicationProvider`：

- 状态：`currentApplication`、`wizardDraft`、`otpState`、`countdown`、`isLoading`、`errorMessage`
- 行为：`loadCurrent()`、`submitApplication()`、`sendOtp()`、`signContract()`、`cancelApplication()`

复用 `TransactionProvider`：

- 在签署放款成功后刷新或直接应用返回的交易/账户摘要。

## 5. Backend API 设计

沿用 `loan` 领域路由。

### 5.1 提交申请

- `POST /loan/applications`
- 功能：KYC 前置校验、冷却校验、进行中申请校验、风控决策、状态推进。
- 请求：
  - `basicInfo`: `fullName`, `hkid`, `homeAddress`, `age`
  - `financialInfo`: `occupation`, `monthlyIncome`, `monthlyDebtPayment`
  - `idempotencyKey`
- 响应：
  - 拒绝：`REJECTED` + `cooldownUntil`
  - 通过：`APPROVED_PENDING_SIGN` + `applicationId` + `contractPreview`

### 5.2 查询当前申请

- `GET /loan/applications/current`
- 功能：返回用户当前有效申请状态与可执行动作。

### 5.3 发送签署 OTP

- `POST /loan/applications/{id}/contracts/send-otp`
- 功能：发送验证码（首版 mock）
- 响应：`otpToken`、`otpExpiresAt`、`resendAfter`

### 5.4 签署合同并放款

- `POST /loan/applications/{id}/contracts/sign`
- 请求：`otpToken`, `otpCode`, `agreeTerms`, `idempotencyKey`
- 功能：校验 OTP 与协议勾选，签署成功后调用交易域触发首放。
- 响应：签署结果 + 首放交易摘要 + 账户摘要。

### 5.5 取消申请（可选）

- `POST /loan/applications/{id}/cancel`
- 功能：用户取消未签申请。

## 6. 数据模型

建议新增三张表：

1. `loan_application`
- 关键字段：`id`, `customer_id`, `status`, `product_code`, `requested_payload(json)`, `risk_decision`, `risk_reference_id`, `cooldown_until`, `approved_at`, `expires_at`, `signed_at`, `disbursed_at`, `reject_reason`, `created_at`, `updated_at`

2. `loan_contract_document`
- 关键字段：`id`, `application_id`, `contract_no`, `template_version`, `contract_content(json/text)`, `digest`, `status(DRAFT/SIGNED/EXPIRED)`, `signed_at`, `created_at`

3. `loan_application_otp`
- 关键字段：`id`, `application_id`, `otp_token`, `otp_code_hash`, `expires_at`, `verify_attempts`, `verified_at`, `created_at`

## 7. 与现有交易域衔接

- 申请域不直接实现记账。
- 合同签署成功后，统一调用现有交易服务（`LoanTransactionService`）执行首放。
- 保持“申请域负责状态流转，交易域负责金额与流水一致性”。

## 8. 错误处理与安全

### 8.1 错误码建议

- `LOAN_KYC_REQUIRED`
- `LOAN_APPLICATION_COOLDOWN`
- `LOAN_APPLICATION_ACTIVE_EXISTS`
- `LOAN_OTP_INVALID`
- `LOAN_OTP_EXPIRED`
- `LOAN_OTP_TOO_MANY_ATTEMPTS`
- `LOAN_CONTRACT_EXPIRED`
- `LOAN_RISK_GATEWAY_UNAVAILABLE`

### 8.2 安全要求

- HKID、住址、收入按敏感数据处理，日志禁止明文输出。
- OTP 仅存哈希，限制有效期和尝试次数。
- 签署接口强制 `agreeTerms=true`。
- 建单与签署接口均要求幂等键。

## 9. 测试与验收

### 9.1 Backend

- KYC 前置拦截测试
- 双页字段完整性校验测试
- 审批通过/拒绝分支测试
- 24h 冷却拦截测试
- 14 天过期测试
- OTP 正常/过期/次数超限测试
- 签署幂等测试（避免重复放款）

### 9.2 App

- 两页向导校验、前后翻页与草稿恢复
- 提交前不建单验证
- 审批结果页路由正确性
- OTP 倒计时与错误提示

### 9.3 联调路径

- KYC 完成 -> 两页填写 -> 审批通过 -> 签署 -> 放款成功 -> 首页交易更新
- KYC 完成 -> 两页填写 -> 审批拒绝 -> 冷却展示与拦截

## 10. 里程碑建议

- M1：申请域数据模型 + 基础 API 骨架
- M2：风控网关 mock + 审批状态机 + 合同生成
- M3：OTP mock + 合同签署 + 首放衔接
- M4：App 双页向导 + 状态恢复 + 联调验收
