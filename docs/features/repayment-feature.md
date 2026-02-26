# 还款功能说明

## 用户端（App）

### 功能入口
- 主页快捷操作 → "还款"
- 合同列表 → 选择合同 → "还款"

### 操作流程
1. 选择需要还款的合同
2. 输入还款金额（支持快捷按钮：还全部、还利息）
3. 确认还款
4. 查看回单（包含交易号、时间、本金/利息拆分）

### 快捷金额按钮
- **还全部**：自动计算并填入全部应还金额（本金 + 利息）
- **还利息**：自动计算并填入当前应还利息金额

### 还款清分规则
还款金额按照以下顺序清分：
1. 优先偿还逾期利息
2. 其次偿还正常利息
3. 剩余金额偿还本金，恢复可用额度

### 回单信息
成功还款后，回单显示：
- 交易号
- 交易时间
- 还款金额
- 本金金额
- 利息金额
- 还款后可用额度
- 还款后在贷本金

## 管理端

### 功能入口
- 贷款管理 → 交易记录

### 功能说明

#### 查询
支持多条件筛选：
- 客户邮箱
- 合同号
- 交易类型（首放/再提款/还款/冲正）
- 交易状态（已入账/已冲正）
- 时间区间（创建时间）
- 分页查询

#### 导出
- 点击"导出"按钮
- 根据当前查询条件导出交易记录为 Excel 文件
- Excel 包含所有匹配的记录

#### 操作
- **更新备注**：为交易记录添加备注信息
- **冲正交易**：
  - 生成一笔冲正交易（REVERSAL 类型）
  - 将原交易状态标记为 REVERSED
  - 恢复账户余额（可用额度、在贷本金、应还利息）
  - 只有 REPAYMENT 和 REDRAW_DISBURSEMENT 类型的交易可以冲正
  - INITIAL_DISBURSEMENT 和已冲正的交易不可冲正

### 交易类型说明
| 类型 | 说明 | 可冲正 |
|------|------|--------|
| INITIAL_DISBURSEMENT | 首次放款（合同签署后） | 否 |
| REDRAW_DISBURSEMENT | 再次提款 | 是 |
| REPAYMENT | 还款 | 是 |
| REVERSAL | 冲正交易 | 否 |

### 权限要求
- 查询交易记录：`loan:transaction:read`
- 创建运营交易：`loan:transaction:create`
- 更新交易备注：`loan:transaction:update_note`
- 冲正交易：`loan:transaction:reverse`

## API 端点

### 用户端 API

#### 账户摘要查询
```
GET /api/loan/account/summary
Authorization: Bearer <token>
```

响应：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "creditLimit": 50000.00,
    "availableLimit": 5000.00,
    "principalOutstanding": 45000.00,
    "interestOutstanding": 500.00
  }
}
```

#### 还款
```
POST /api/loan/repayments
Authorization: Bearer <token>
Content-Type: application/json

{
  "amount": 1000.00,
  "idempotencyKey": "unique-key-001",
  "contractNo": "CONTRACT-001"
}
```

响应：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "transaction": {
      "transactionId": "TXN-123",
      "type": "REPAYMENT",
      "amount": 1000.00,
      "principalComponent": 800.00,
      "interestComponent": 200.00,
      "availableLimitAfter": 5800.00,
      "principalOutstandingAfter": 44200.00,
      "occurredAt": "2026-02-26T10:30:00"
    },
    "accountSummary": {
      "creditLimit": 50000.00,
      "availableLimit": 5800.00,
      "principalOutstanding": 44200.00,
      "interestOutstanding": 300.00
    },
    "interestPaid": 200.00,
    "principalPaid": 800.00
  }
}
```

### 管理端 API

#### 查询交易记录
```
GET /api/admin/loan/transactions?page=1&size=10&customerEmail=xxx@example.com
Authorization: Bearer <token>
```

#### 导出交易记录
```
GET /api/admin/loan/transactions/export?customerEmail=xxx@example.com
Authorization: Bearer <token>
```
返回 Excel 文件（application/vnd.openxmlformats-officedocument.spreadsheetml.sheet）

#### 更新交易备注
```
PUT /api/admin/loan/transactions/{txnNo}/note
Authorization: Bearer <token>
Content-Type: application/json

{
  "note": "客户主动还款"
}
```

#### 冲正交易
```
POST /api/admin/loan/transactions/{txnNo}/reversal
Authorization: Bearer <token>
Content-Type: application/json

{
  "note": "操作失误冲正"
}
```

## 业务规则

### 还款规则
1. 只能对生效中（status=1）的合同进行还款
2. 还款金额必须大于 0
3. 还款后不能导致可用额度为负
4. 支持部分还款和全额还款
5. 使用幂等键保证不会重复扣款

### 冲正规则
1. 只能冲正 REPAYMENT 和 REDRAW_DISBURSEMENT 类型的交易
2. 不能冲正已冲正的交易
3. 不能冲正 INITIAL_DISBURSEMENT 类型的交易
4. 冲正后生成新的 REVERSAL 交易记录
5. 原交易状态变更为 REVERSED
6. 冲正会恢复账户余额，需要确保有足够的可用额度

## 错误码

| 错误码 | 说明 |
|--------|------|
| 400 | 参数错误（金额无效、合同不存在等） |
| 401 | 未登录 |
| 403 | 无权限或非顾客用户 |
| 409 | 账户更新失败（并发冲突） |
| 500 | 系统错误 |

## 注意事项

1. **幂等性**：所有还款请求必须提供唯一的幂等键，防止重复扣款
2. **并发控制**：账户更新使用乐观锁，如果并发更新会返回 409 错误
3. **交易不可修改**：交易一旦创建，除备注外不可修改
4. **冲正影响**：冲正操作会直接影响账户余额，需谨慎操作
5. **审计追踪**：所有交易操作都会记录操作人（createdBy 字段）
