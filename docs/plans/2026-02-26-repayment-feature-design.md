# 还款功能设计文档

**日期**: 2026-02-26
**状态**: 设计已批准
**关联**: 还款功能 MVP 实现

---

## 1. 需求概述

实现 App 端真实还款功能，支持多合同选择还款，并在管理后台提供完整的台账查询能力。

### 1.1 核心需求

| 需求 | 描述 |
|------|------|
| **多合同支持** | 用户可能有多个合同，还款时选择相应合同 |
| **还款输入** | 快捷按钮（还全部、还利息）+ 自由输入金额 |
| **合同展示** | 卡片展示所有合同，点击进入还款 |
| **管理后台** | 查询 + 导出 + 操作（冲正、备注） |
| **成功反馈** | 完整回单（清分明细、交易号、时间、可分享/保存）|

---

## 2. 架构设计

### 2.1 技术选型：最小改动方案

选择方案 B（最小改动），保持后端聚合账户架构，通过交易流水实时计算合同级余额。

**理由：**
- 当前 `loan_account` 表按 `customer_id` 唯一，改为合同级需重构核心数据结构
- 可通过 `loan_transaction` 表按合同聚合计算余额
- 快速上线 MVP，后续按需演进
- 降低风险，保证还款核心功能稳定

### 2.2 API 设计

```
新增：
GET  /api/loan/contracts                    - 获取用户合同列表
GET  /api/loan/contracts/{no}/summary       - 获取合同余额（实时计算）
GET  /api/admin/loan/contracts              - 管理后台查询合同列表
GET  /api/admin/loan/transactions/export    - 导出交易记录（Excel）

修改：
POST /api/loan/repayments                   - 新增可选 contractNo 参数
```

### 2.3 数据流

```
┌─────────┐     ┌─────────┐     ┌──────────┐
│   App   │────▶│ Backend │────▶│ Database │
└─────────┘     └─────────┘     └──────────┘
    │               │
    │ 1. 获取合同列表
    │──────────────▶│ GET /contracts
    │               │
    │ 2. 获取合同余额
    │──────────────▶│ GET /contracts/{no}/summary
    │               │
    │ 3. 提交还款
    │──────────────▶│ POST /repayments {contractNo, amount}
    │               │
    │ 4. 返回回单   │
    │◀──────────────│
```

---

## 3. 数据模型

### 3.1 后端新增 DTO

```java
// 合同摘要响应
public class LoanContractSummaryResponse {
    private String contractNo;           // 合同号
    private BigDecimal contractAmount;   // 合同金额
    private BigDecimal principalOutstanding;  // 在贷本金（实时计算）
    private BigDecimal interestOutstanding;   // 应还利息（实时计算）
    private LocalDateTime signedAt;      // 签署时间
    private String status;               // 合同状态
}

// 合同列表响应
public class LoanContractListResponse {
    private List<LoanContractSummaryResponse> contracts;
}

// 还款请求（新增字段）
public class LoanRepaymentRequest {
    private BigDecimal amount;
    private String idempotencyKey;
    private String contractNo;  // 新增：可选，指定还款合同
}
```

### 3.2 App 端新增模型

```dart
// 合同卡片数据
class ContractCardData {
  final String contractNo;
  final double contractAmount;
  final double principalOutstanding;
  final double interestOutstanding;
  final DateTime signedAt;
  final String status;
  final double totalOutstanding;  // 本金 + 利息
}

// 还款回单数据
class ReceiptData {
  final String transactionNo;
  final DateTime occurredAt;
  final double amount;
  final double principalPaid;
  final double interestPaid;
  final double remainingPrincipal;
  final double remainingInterest;
  final String contractNo;
}
```

---

## 4. UI/UX 设计

### 4.1 页面流程

```
快捷操作 → 点击"还款"
    ↓
合同列表页（ContractListPage）
    └─ 卡片展示所有合同
    └─ 空状态处理
    ↓
点击某合同卡片
    ↓
还款详情页（RepaymentPage）
    ├─ 合同信息卡片
    ├─ 当前欠款显示
    ├─ 快捷按钮：[还全部 ¥XXX] [还利息 ¥XXX]
    ├─ 金额输入框
    └─ [确认还款] 按钮
    ↓
确认对话框
    ├─ 展示清分预览
    └─ [确认] [取消]
    ↓
支付中（Loading）
    ↓
还款成功回单（ReceiptDialog）
    ├─ 成功图标 + "还款成功"
    ├─ 交易号
    ├─ 时间
    ├─ 本金/利息拆分明细
    ├─ 剩余欠款
    ├─ [保存回单] 按钮（生成图片）
    └─ [返回首页] 按钮
```

### 4.2 核心组件

| 组件 | 文件路径 | 功能描述 |
|------|----------|----------|
| `ContractListPage` | `app/lib/screens/contract_list_page.dart` | 合同列表页面 |
| `ContractCard` | `app/lib/widgets/contract_card.dart` | 合同卡片组件 |
| `RepaymentPage` | `app/lib/screens/repayment_page.dart` | 还款详情页面 |
| `RepaymentSuccessDialog` | `app/lib/widgets/repayment_success_dialog.dart` | 还款成功回单 |
| `QuickAmountButtons` | `app/lib/widgets/quick_amount_buttons.dart` | 快捷金额按钮组 |

### 4.3 UI 示意

**合同卡片：**
```
┌─────────────────────────────────┐
│  合同号: CT202501010001         │
│  合同金额: ¥10,000              │
│                                 │
│  在贷本金: ¥5,000               │
│  应还利息: ¥50.25               │
│  ─────────────────              │
│  应还总额: ¥5,050.25            │
│                                 │
│  签署日期: 2025-01-01           │
│  [去还款 →]                     │
└─────────────────────────────────┘
```

**还款页面：**
```
┌─────────────────────────────────┐
│  还款详情                        │
├─────────────────────────────────┤
│  合同号: CT202501010001         │
│  当前欠款: ¥5,050.25            │
│                                 │
│  快捷还款：                      │
│  [ 还全部 ¥5,050.25 ]           │
│  [ 还利息   ¥50.25 ]            │
│                                 │
│  还款金额：                      │
│  ┌───────────────────────────┐ │
│  │  ¥                        │ │
│  └───────────────────────────┘ │
│                                 │
│  [     确认还款     ]           │
└─────────────────────────────────┘
```

---

## 5. 管理后台改动

### 5.1 交易记录页面增强

**已有功能（保留）：**
- 分页查询交易记录
- 多条件筛选（客户、合同号、类型、状态、时间等）
- 创建运营交易
- 更新交易备注
- 冲正交易

**新增功能：**
| 功能 | 描述 |
|------|------|
| **导出 Excel** | 导出当前筛选条件的交易记录 |
| **合同号筛选** | 增强现有筛选器 |

### 5.2 导出功能设计

```
Excel 列：
- 交易号
- 客户邮箱
- 合同号
- 交易类型
- 交易金额
- 本金拆分
- 利息拆分
- 交易后可用额度
- 交易后在贷本金
- 交易来源
- 交易状态
- 创建时间
- 备注
```

---

## 6. 后端实现

### 6.1 新增 Service 方法

```java
// LoanContractService
public interface LoanContractService {
    // 获取用户合同列表
    List<LoanContractSummaryResponse> getCustomerContracts(Long customerId);

    // 获取合同余额（实时计算）
    LoanContractSummaryResponse getContractSummary(Long customerId, String contractNo);

    // 获取所有合同（管理后台）
    PageResult<LoanContractSummaryResponse> getAllContracts(ContractQueryRequest request);
}
```

### 6.2 合同级余额计算

```java
// 通过交易流水实时计算
public BigDecimal getPrincipalOutstanding(String contractNo) {
    // 获取该合同最新交易的本金余额
    return transactionMapper
        .findLatestByContractNo(contractNo)
        .getPrincipalOutstandingAfter();
}
```

### 6.3 还款逻辑调整

```java
// LoanTransactionService.repay()
public LoanRepaymentResponse repay(Long customerId, LoanRepaymentRequest request) {
    // 如果指定了合同号，验证合同属于该用户
    if (request.getContractNo() != null) {
        validateContractOwnership(customerId, request.getContractNo());
    }

    // 其余逻辑保持不变...
}
```

---

## 7. App 端实现

### 7.1 新增 Provider

```dart
// ContractProvider
class ContractProvider with ChangeNotifier {
  final ApiService _apiService = ApiService();

  List<ContractCardData> _contracts = [];
  bool _isLoading = false;

  List<ContractCardData> get contracts => _contracts;
  bool get isLoading => _isLoading;

  Future<void> loadContracts() async {
    _isLoading = true;
    notifyListeners();

    final (contracts, error) = await _apiService.getLoanContracts();
    _contracts = contracts ?? [];
    _isLoading = false;
    notifyListeners();
  }

  Future<ContractCardData?> getContractSummary(String contractNo) async {
    final (summary, error) = await _apiService.getContractSummary(contractNo);
    return summary;
  }
}
```

### 7.2 新增 API 方法

```dart
// ApiService
Future<(List<ContractCardData>?, String?)> getLoanContracts() async { ... }
Future<(ContractCardData?, String?)> getContractSummary(String contractNo) async { ... }
```

### 7.3 导航路由

```dart
// AppRoutes
static const String repayment = '/repayment';
static const String contractList = '/contracts';
static const String repaymentSuccess = '/repayment/success';
```

---

## 8. 错误处理

| 场景 | 处理方式 |
|------|----------|
| 无可用合同 | 显示空状态插画 + "暂无合同，先去申请吧" |
| 还款金额 ≤ 0 | 前端禁用确认按钮 |
| 还款金额 > 欠款 | 前端提示 + 后端校验 |
| 网络请求失败 | 显示错误提示 + 重试按钮 |
| 重复提交 | 幂等键防护（后端已支持） |
| 合同不存在 | 返回 404 + 提示用户 |

---

## 9. 测试计划

### 9.1 单元测试

| 测试项 | 覆盖范围 |
|--------|----------|
| 合同余额计算 | `LoanContractService` |
| 还款逻辑 | `LoanTransactionService` |
| DTO 序列化 | Model 类 |

### 9.2 集成测试

| 测试场景 |
|----------|
| 单合同全额还款 |
| 单合同部分还款 |
| 多合同选择还款 |
| 还款冲正 |
| 导出功能 |

### 9.3 UI 测试

| 测试场景 |
|----------|
| 合同列表展示 |
| 还款流程完整路径 |
| 快捷金额按钮 |
| 回单保存功能 |
| 错误状态展示 |

---

## 10. 实现优先级

### Phase 1 - 核心功能（P0）
1. 后端合同查询 API
2. App 合同列表页
3. App 还款详情页
4. 还款成功回单

### Phase 2 - 增强功能（P1）
1. 快捷金额按钮
2. 管理后台导出功能
3. 回单保存/分享

### Phase 3 - 优化（P2）
1. 合同列表刷新机制
2. 离线缓存
3. 动画效果优化

---

## 11. 验收标准

- [ ] 用户可以查看所有合同列表
- [ ] 用户可以选择合同进行还款
- [ ] 支持快捷金额按钮（还全部、还利息）
- [ ] 支持自定义金额输入
- [ ] 还款成功后显示完整回单
- [ ] 回单可保存为图片
- [ ] 管理后台可查询所有还款记录
- [ ] 管理后台可导出交易记录 Excel
- [ ] 所有边界情况有正确错误提示
