# 交易记录页面设计

**日期**: 2026-02-25
**状态**: 已批准

## 概述

为 xWallet 移动端（Flutter）添加完整的交易记录页面，用户可以从首页"最近交易"板块点击"查看全部"跳转到该页面。

## 需求

1. **入口**: 首页"最近交易"板块的"查看全部"按钮 → 切换到底部导航栏的"记录"标签
2. **记录页面**: 显示所有历史交易记录（放款、还款、再次提款等），按时间倒序排列
3. **交互**: 支持下拉刷新，点击交易项弹出 Bottom Sheet 抽屉显示详情
4. **详情内容**: 基本信息（交易时间、类型、金额）+ 关联业务信息（贷款单号等）

## 文件结构

```
app/lib/
├── screens/
│   └── history_screen.dart              # 新建：完整的交易记录页面
├── widgets/
│   ├── transaction_list.dart            # 现有：保持不变
│   └── transaction_detail_sheet.dart    # 新建：交易详情抽屉组件
├── providers/
│   └── transaction_provider.dart        # 扩展：添加 allTransactions
├── models/
│   └── loan_transaction.dart            # 现有：可能需要扩展
└── main.dart                            # 修改：移除占位 HistoryScreen
```

## UI 设计

### HistoryScreen 页面

```
┌─────────────────────────────┐
│ 交易记录          [筛选图标] │  AppBar (紫色主题)
├─────────────────────────────┤
│  ┌─────────────────────┐    │
│  │ 放款到账   +¥50,000  │    │  交易列表项
│  │ 今天 14:30           │    │  (复用 TransactionItem 样式)
│  └─────────────────────┘    │
│  ┌─────────────────────┐    │
│  │ 还款扣除   -¥2,500   │    │
│  │ 昨天 10:00           │    │
│  └─────────────────────┘    │
│        ...                   │
│                              │
│  [支持下拉刷新]              │
└─────────────────────────────┘
```

### 交易详情抽屉 (TransactionDetailSheet)

```
┌─────────────────────────────┐
│ ═══════════════════════════ │  ┃ 可拖拽指示条
│                             │
│  交易详情                   │  标题
│                             │
│  ┌─────────────────────┐   │
│  │ 基本信息             │   │  分组标题
│  ├─────────────────────┤   │
│  │ 交易类型: 放款到账    │   │
│  │ 交易时间: 2025-02-25  │   │
│  │          14:30:00     │   │
│  │ 交易金额: +¥50,000.00 │   │
│  └─────────────────────┘   │
│                             │
│  ┌─────────────────────┐   │
│  │ 业务信息             │   │  分组标题
│  ├─────────────────────┤   │
│  │ 贷款单号: LN20250... │   │
│  │ 期数信息: 第 1/12 期  │   │
│  └─────────────────────┘   │
│                             │
│  [关闭]                     │  关闭按钮
└─────────────────────────────┘
```

## 数据流

```
HomePage (点击"查看全部")
    │
    ▼ NavigationProvider.setIndex(2)
    │
    ▼ HistoryScreen 加载
    │
    ▼ TransactionProvider.getAllTransactions()
    │
    ▼ 显示交易列表
    │
    ▼ 点击交易项 → showModalBottomSheet(TransactionDetailSheet)
```

## API 接口

交易记录数据来自现有的 TransactionProvider，可能需要添加：

```dart
// TransactionProvider
List<LoanTransactionItem> get allTransactions;
Future<void> loadAllTransactions();
Future<void> refreshAllTransactions();
```

后端 API: `GET /api/customer/transactions` (如需分页)

## 埋点事件

| 事件名称 | 描述 | 属性 |
|---------|------|------|
| `view_all_transactions` | 点击"查看全部" | page, flow, element_id |
| `transaction_list_refresh` | 下拉刷新 | page, flow |
| `transaction_detail_view` | 查看交易详情 | page, flow, item_id, item_type |
| `transaction_detail_close` | 关闭详情抽屉 | page, flow |

## 实现任务

1. 创建 `screens/history_screen.dart` - 交易记录页面
2. 创建 `widgets/transaction_detail_sheet.dart` - 交易详情抽屉
3. 修改 `screens/home_screen.dart` - 更新导航跳转
4. 扩展 `providers/transaction_provider.dart` - 添加全部交易数据
5. 修改 `main.dart` - 移除占位 HistoryScreen
6. 添加埋点事件

## 设计风格

- 遵循现有绿色主题 (`Color(0xFF7424F5)` 为紫色，实际主题色)
- 使用 `DesignScale` 进行响应式缩放
- 圆角、阴影等与首页保持一致
