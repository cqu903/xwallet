# 交易记录页面实现计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 为 xWallet 移动端添加完整的交易记录页面，支持从首页"查看全部"跳转，显示所有交易记录，点击可查看详情。

**Architecture:** HistoryScreen 作为底部导航栏的"记录"标签页，使用 TransactionProvider 获取数据，点击交易项通过 showModalBottomSheet 弹出详情抽屉。

**Tech Stack:** Flutter 3.10+, Provider 状态管理, Material Design 3

---

## 前置准备

### 阅读设计文档
- 文件: `docs/plans/2026-02-25-transaction-history-page-design.md`
- 理解 UI 设计、数据流和埋点事件

### 了解现有代码
- `app/lib/screens/home_screen.dart` - 了解首页如何处理"查看全部"
- `app/lib/widgets/transaction_list.dart` - 了解 TransactionData 和 TransactionItem 样式
- `app/lib/providers/transaction_provider.dart` - 了解数据获取方式
- `app/lib/models/loan_transaction.dart` - 了解交易数据模型
- `app/lib/main.dart` - 了解 MainNavigation 和 NavigationProvider

---

## Task 1: 修改首页导航跳转逻辑

**Files:**
- Modify: `app/lib/screens/home_screen.dart:154-162`

**Step 1: 修改 _handleViewAllTransactions 方法**

将当前显示 SnackBar 的逻辑改为切换到底部导航栏的"记录"标签：

```dart
/// 查看全部交易
void _handleViewAllTransactions() {
  context.read<NavigationProvider>().setIndex(2); // 切换到"记录"标签
}
```

**Step 2: 验证编译**

Run: `cd app && flutter analyze`
Expected: No errors

**Step 3: 提交**

```bash
git add app/lib/screens/home_screen.dart
git commit -m "feat(home): navigate to history tab on view all tap"
```

---

## Task 2: 扩展 TransactionProvider

**Files:**
- Modify: `app/lib/providers/transaction_provider.dart`
- Read: `app/lib/models/loan_transaction.dart`

**Step 1: 读取现有 TransactionProvider**

查看 TransactionProvider 的实现，了解现有的 `recentTransactions` 和数据加载逻辑。

**Step 2: 添加全部交易数据支持**

在 TransactionProvider 中添加以下内容：

```dart
// 全部交易列表（用于历史记录页面）
List<LoanTransactionItem> get allTransactions => _allTransactions;
List<LoanTransactionItem> _allTransactions = [];

// 是否正在加载全部交易
bool get isLoadingAll => _isLoadingAll;
bool _isLoadingAll = false;

// 加载全部交易记录
Future<void> loadAllTransactions() async {
  if (_isLoadingAll) return;

  _isLoadingAll = true;
  notifyListeners();

  try {
    final customerId = context?.read<AuthProvider>().customer?.customerId;
    if (customerId == null || customerId.isEmpty) {
      throw Exception('Customer not logged in');
    }

    final response = await _apiClient.get('/customer/transactions?customerId=$customerId');

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      final List<dynamic> list = data['data'] ?? [];
      _allTransactions = list.map((item) => LoanTransactionItem.fromJson(item)).toList();
      _errorMessage = null;
    } else {
      throw Exception('Failed to load transactions: ${response.statusCode}');
    }
  } catch (e) {
    _errorMessage = e.toString();
  } finally {
    _isLoadingAll = false;
    notifyListeners();
  }
}

// 刷新全部交易记录
Future<void> refreshAllTransactions() async {
  _allTransactions = [];
  await loadAllTransactions();
}
```

**Step 3: 验证编译**

Run: `cd app && flutter analyze`
Expected: No errors

**Step 4: 提交**

```bash
git add app/lib/providers/transaction_provider.dart
git commit -m "feat(provider): add allTransactions support to TransactionProvider"
```

---

## Task 3: 创建交易详情抽屉组件

**Files:**
- Create: `app/lib/widgets/transaction_detail_sheet.dart`

**Step 1: 创建 TransactionDetailSheet 组件**

```dart
import 'package:flutter/material.dart';
import '../analytics/analytics_constants.dart';
import '../analytics/event_spec.dart';
import '../models/loan_transaction.dart';
import '../utils/design_scale.dart';
import 'analytics/analytics_tap.dart';

/// 交易详情抽屉
class TransactionDetailSheet extends StatelessWidget {
  final LoanTransactionItem transaction;

  const TransactionDetailSheet({
    super.key,
    required this.transaction,
  });

  @override
  Widget build(BuildContext context) {
    final scale = DesignScale.getScale(context);

    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.vertical(top: Radius.circular(24 * scale)),
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          // 拖拽指示条
          _buildDragIndicator(scale),
          // 标题
          _buildTitle(scale),
          // 基本信息
          _buildBasicInfo(scale),
          // 业务信息
          _buildBusinessInfo(scale),
          // 关闭按钮
          _buildCloseButton(context, scale),
          SizedBox(height: 16 * scale),
        ],
      ),
    );
  }

  /// 拖拽指示条
  Widget _buildDragIndicator(double scale) {
    return Container(
      margin: EdgeInsets.only(top: 12 * scale),
      width: 40 * scale,
      height: 4 * scale,
      decoration: BoxDecoration(
        color: const Color(0xFFCCCCCC),
        borderRadius: BorderRadius.circular(2 * scale),
      ),
    );
  }

  /// 标题
  Widget _buildTitle(double scale) {
    return Padding(
      padding: EdgeInsets.all(16 * scale),
      child: Text(
        '交易详情',
        style: TextStyle(
          color: const Color(0xFF1A1A1A),
          fontSize: 18 * scale,
          fontWeight: FontWeight.w600,
        ),
      ),
    );
  }

  /// 基本信息
  Widget _buildBasicInfo(double scale) {
    final typeConfig = _getTypeConfig(transaction.type);
    final formattedAmount = _formatAmount(transaction.amount, transaction.type);
    final formattedTime = _formatTime(transaction.occurredAt);

    return Container(
      margin: EdgeInsets.symmetric(horizontal: 16 * scale),
      padding: EdgeInsets.all(16 * scale),
      decoration: BoxDecoration(
        color: const Color(0xFFF8F5FF),
        borderRadius: BorderRadius.circular(12 * scale),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            '基本信息',
            style: TextStyle(
              color: const Color(0xFF666666),
              fontSize: 13 * scale,
              fontWeight: FontWeight.w500,
            ),
          ),
          SizedBox(height: 12 * scale),
          _buildInfoRow('交易类型', typeConfig, scale),
          SizedBox(height: 8 * scale),
          _buildInfoRow('交易时间', formattedTime, scale),
          SizedBox(height: 8 * scale),
          _buildInfoRow('交易金额', formattedAmount, scale),
        ],
      ),
    );
  }

  /// 业务信息
  Widget _buildBusinessInfo(double scale) {
    return Container(
      margin: EdgeInsets.all(16 * scale),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            '业务信息',
            style: TextStyle(
              color: const Color(0xFF666666),
              fontSize: 13 * scale,
              fontWeight: FontWeight.w500,
            ),
          ),
          SizedBox(height: 12 * scale),
          _buildInfoRow('交易单号', transaction.transactionId, scale),
          if (transaction.loanId.isNotEmpty) ...[
            SizedBox(height: 8 * scale),
            _buildInfoRow('关联贷款', transaction.loanId, scale),
          ],
        ],
      ),
    );
  }

  /// 信息行
  Widget _buildInfoRow(String label, String value, double scale) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        SizedBox(
          width: 80 * scale,
          child: Text(
            label,
            style: TextStyle(
              color: const Color(0xFF999999),
              fontSize: 14 * scale,
            ),
          ),
        ),
        Expanded(
          child: Text(
            value,
            style: TextStyle(
              color: const Color(0xFF1A1A1A),
              fontSize: 14 * scale,
              fontWeight: FontWeight.w500,
            ),
          ),
        ),
      ],
    );
  }

  /// 关闭按钮
  Widget _buildCloseButton(BuildContext context, double scale) {
    return AnalyticsTap(
      eventType: AnalyticsEventType.buttonClick,
      properties: AnalyticsEventProperties.click(
        page: AnalyticsPages.history,
        flow: AnalyticsFlows.history,
        elementId: AnalyticsIds.closeDetail,
        elementType: AnalyticsElementType.button,
        elementText: '关闭',
      ),
      onTap: () => Navigator.of(context).pop(),
      child: Container(
        width: double.infinity,
        margin: EdgeInsets.symmetric(horizontal: 16 * scale),
        padding: EdgeInsets.symmetric(vertical: 14 * scale),
        decoration: BoxDecoration(
          color: const Color(0xFF7424F5),
          borderRadius: BorderRadius.circular(12 * scale),
        ),
        child: Text(
          '关闭',
          textAlign: TextAlign.center,
          style: TextStyle(
            color: Colors.white,
            fontSize: 16 * scale,
            fontWeight: FontWeight.w600,
          ),
        ),
      ),
    );
  }

  String _getTypeConfig(String type) {
    switch (type.toUpperCase()) {
      case 'INITIAL_DISBURSEMENT':
        return '放款到账';
      case 'REDRAW_DISBURSEMENT':
        return '再次提款';
      case 'REPAYMENT':
        return '还款扣除';
      default:
        return '交易变动';
    }
  }

  String _formatAmount(double amount, String type) {
    final absolute = amount.abs();
    final formatted = absolute.toStringAsFixed(2);
    final sign = type.toUpperCase() == 'REPAYMENT' ? '-' : '+';
    return '$sign¥$formatted';
  }

  String _formatTime(DateTime? dateTime) {
    if (dateTime == null) return '--';
    final local = dateTime.toLocal();
    return '${local.year}-${local.month.toString().padLeft(2, '0')}-${local.day.toString().padLeft(2, '0')} '
        '${local.hour.toString().padLeft(2, '0')}:${local.minute.toString().padLeft(2, '0')}:${local.second.toString().padLeft(2, '0')}';
  }
}
```

**Step 2: 验证编译**

Run: `cd app && flutter analyze`
Expected: No errors

**Step 3: 提交**

```bash
git add app/lib/widgets/transaction_detail_sheet.dart
git commit -m "feat(widget): add transaction detail bottom sheet"
```

---

## Task 4: 创建 HistoryScreen 页面

**Files:**
- Create: `app/lib/screens/history_screen.dart`

**Step 1: 创建完整的 HistoryScreen**

```dart
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../analytics/analytics_constants.dart';
import '../analytics/event_spec.dart';
import '../models/loan_transaction.dart';
import '../providers/transaction_provider.dart';
import '../utils/design_scale.dart';
import '../widgets/transaction_detail_sheet.dart';
import '../widgets/analytics/analytics_tap.dart';

/// 交易记录页面
class HistoryScreen extends StatefulWidget {
  const HistoryScreen({super.key});

  @override
  State<HistoryScreen> createState() => _HistoryScreenState();
}

class _HistoryScreenState extends State<HistoryScreen> {
  final ScrollController _scrollController = ScrollController();

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<TransactionProvider>().loadAllTransactions();
    });
  }

  @override
  void dispose() {
    _scrollController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final scale = DesignScale.getScale(context);

    return Scaffold(
      backgroundColor: const Color(0xFFF5F3FF),
      appBar: AppBar(
        title: Text(
          '交易记录',
          style: TextStyle(
            fontSize: 18 * scale,
            fontWeight: FontWeight.w600,
          ),
        ),
        backgroundColor: const Color(0xFF7424F5),
        foregroundColor: Colors.white,
        elevation: 0,
        centerTitle: true,
      ),
      body: Consumer<TransactionProvider>(
        builder: (context, txProvider, child) {
          return RefreshIndicator(
            onRefresh: _handleRefresh,
            color: const Color(0xFF7424F5),
            child: _buildContent(txProvider, scale),
          );
        },
      ),
    );
  }

  Widget _buildContent(TransactionProvider txProvider, double scale) {
    if (txProvider.isLoadingAll) {
      return const Center(
        child: CircularProgressIndicator(
          color: Color(0xFF7424F5),
        ),
      );
    }

    if (txProvider.errorMessage != null && txProvider.errorMessage!.isNotEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.error_outline,
              size: 48 * scale,
              color: const Color(0xFF999999),
            ),
            SizedBox(height: 12 * scale),
            Text(
              txProvider.errorMessage!,
              style: TextStyle(
                color: const Color(0xFF666666),
                fontSize: 14 * scale,
              ),
            ),
            SizedBox(height: 16 * scale),
            ElevatedButton(
              onPressed: _handleRetry,
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFF7424F5),
                foregroundColor: Colors.white,
                padding: EdgeInsets.symmetric(
                  horizontal: 24 * scale,
                  vertical: 12 * scale,
                ),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(8 * scale),
                ),
              ),
              child: Text(
                '重试',
                style: TextStyle(fontSize: 14 * scale),
              ),
            ),
          ],
        ),
      );
    }

    final transactions = txProvider.allTransactions;

    if (transactions.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.receipt_long,
              size: 64 * scale,
              color: const Color(0xFF7424F5).withOpacity(0.3),
            ),
            SizedBox(height: 16 * scale),
            Text(
              '暂无交易记录',
              style: TextStyle(
                fontSize: 16 * scale,
                color: const Color(0xFF666666),
              ),
            ),
          ],
        ),
      );
    }

    return ListView.separated(
      controller: _scrollController,
      padding: EdgeInsets.all(16 * scale),
      itemCount: transactions.length,
      separatorBuilder: (context, index) => SizedBox(height: 8 * scale),
      itemBuilder: (context, index) {
        final transaction = transactions[index];
        return _TransactionItem(
          transaction: transaction,
          scale: scale,
          onTap: () => _showTransactionDetail(transaction),
        );
      },
    );
  }

  Future<void> _handleRefresh() async {
    await context.read<TransactionProvider>().refreshAllTransactions();
  }

  void _handleRetry() {
    context.read<TransactionProvider>().loadAllTransactions();
  }

  void _showTransactionDetail(LoanTransactionItem transaction) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (context) => TransactionDetailSheet(transaction: transaction),
    );
  }
}

/// 单个交易列表项
class _TransactionItem extends StatelessWidget {
  final LoanTransactionItem transaction;
  final double scale;
  final VoidCallback onTap;

  const _TransactionItem({
    required this.transaction,
    required this.scale,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final typeConfig = _getTypeConfig(transaction.type);
    final formattedAmount = _formatAmount(transaction.amount, transaction.type);
    final formattedTime = _formatTime(transaction.occurredAt);
    final isPositive = transaction.type.toUpperCase() != 'REPAYMENT';

    return AnalyticsTap(
      eventType: AnalyticsEventType.transactionClick,
      properties: AnalyticsEventProperties.itemClick(
        page: AnalyticsPages.history,
        flow: AnalyticsFlows.history,
        elementId: AnalyticsIds.transactionItem,
        elementType: AnalyticsElementType.listItem,
        itemType: 'transaction',
        itemId: transaction.transactionId,
        itemName: typeConfig,
      ),
      onTap: onTap,
      child: Container(
        padding: EdgeInsets.all(16 * scale),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(12 * scale),
          boxShadow: [
            BoxShadow(
              color: const Color(0xFF000000).withOpacity(0.04),
              blurRadius: 8 * scale,
              offset: Offset(0, 2 * scale),
            ),
          ],
        ),
        child: Row(
          children: [
            // 图标
            Container(
              width: 40 * scale,
              height: 40 * scale,
              decoration: BoxDecoration(
                color: _getIconColor(transaction.type).withOpacity(0.1),
                borderRadius: BorderRadius.circular(10 * scale),
              ),
              child: Icon(
                _getIcon(transaction.type),
                color: _getIconColor(transaction.type),
                size: 20 * scale,
              ),
            ),
            SizedBox(width: 12 * scale),
            // 名称和时间
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    typeConfig,
                    style: TextStyle(
                      color: const Color(0xFF1A1A1A),
                      fontSize: 15 * scale,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                  SizedBox(height: 4 * scale),
                  Text(
                    formattedTime,
                    style: TextStyle(
                      color: const Color(0xFF999999),
                      fontSize: 12 * scale,
                    ),
                  ),
                ],
              ),
            ),
            // 金额
            Text(
              formattedAmount,
              style: TextStyle(
                color: isPositive ? const Color(0xFF22C55E) : const Color(0xFF1A1A1A),
                fontSize: 16 * scale,
                fontWeight: FontWeight.w600,
              ),
            ),
          ],
        ),
      ),
    );
  }

  String _getTypeConfig(String type) {
    switch (type.toUpperCase()) {
      case 'INITIAL_DISBURSEMENT':
        return '放款到账';
      case 'REDRAW_DISBURSEMENT':
        return '再次提款';
      case 'REPAYMENT':
        return '还款扣除';
      default:
        return '交易变动';
    }
  }

  String _formatAmount(double amount, String type) {
    final absolute = amount.abs();
    final formatted = absolute.toStringAsFixed(2);
    final sign = type.toUpperCase() == 'REPAYMENT' ? '-' : '+';
    return '$sign¥$formatted';
  }

  String _formatTime(DateTime? dateTime) {
    if (dateTime == null) return '--';
    final now = DateTime.now();
    final local = dateTime.toLocal();
    final today = DateTime(now.year, now.month, now.day);
    final target = DateTime(local.year, local.month, local.day);
    final diffDays = today.difference(target).inDays;
    final hour = local.hour.toString().padLeft(2, '0');
    final minute = local.minute.toString().padLeft(2, '0');

    if (diffDays == 0) {
      return '今天 $hour:$minute';
    }
    if (diffDays == 1) {
      return '昨天 $hour:$minute';
    }
    final month = local.month.toString().padLeft(2, '0');
    final day = local.day.toString().padLeft(2, '0');
    return '$month/$day $hour:$minute';
  }

  IconData _getIcon(String type) {
    switch (type.toUpperCase()) {
      case 'INITIAL_DISBURSEMENT':
        return Icons.arrow_downward;
      case 'REDRAW_DISBURSEMENT':
        return Icons.account_balance_wallet;
      case 'REPAYMENT':
        return Icons.arrow_upward;
      default:
        return Icons.receipt_long;
    }
  }

  Color _getIconColor(String type) {
    switch (type.toUpperCase()) {
      case 'INITIAL_DISBURSEMENT':
        return const Color(0xFF7424F5);
      case 'REDRAW_DISBURSEMENT':
        return const Color(0xFF11998E);
      case 'REPAYMENT':
        return const Color(0xFFFF6B6B);
      default:
        return const Color(0xFF666666);
    }
  }
}
```

**Step 2: 验证编译**

Run: `cd app && flutter analyze`
Expected: No errors

**Step 3: 提交**

```bash
git add app/lib/screens/history_screen.dart
git commit -m "feat(screen): add history screen with transaction list"
```

---

## Task 5: 更新 main.dart

**Files:**
- Modify: `app/lib/main.dart:164-278`

**Step 1: 移除占位 HistoryScreen 并导入新版本**

删除 main.dart 中的占位 HistoryScreen 类，并在顶部添加 import：

```dart
import 'screens/history_screen.dart';
```

然后在 MainNavigation 的 _pages 列表中，确保使用新的 HistoryScreen：

```dart
static const List<Widget> _pages = [
  HomeScreen(), // 首页
  AccountScreen(), // 钱包
  HistoryScreen(), // 记录 (使用独立的文件)
  ProfileScreen(), // 我的
];
```

删除 main.dart 中原来的 HistoryScreen 占位类（第 234-278 行）。

**Step 2: 验证编译**

Run: `cd app && flutter analyze`
Expected: No errors

**Step 3: 提交**

```bash
git add app/lib/main.dart
git commit -m "refactor(main): use standalone HistoryScreen"
```

---

## Task 6: 添加缺失的 Analytics 常量

**Files:**
- Modify: `app/lib/analytics/analytics_constants.dart`

**Step 1: 检查并添加缺失的常量**

确保 `AnalyticsIds` 中包含以下常量：

```dart
static const String closeDetail = 'close_detail';
```

**Step 2: 验证编译**

Run: `cd app && flutter analyze`
Expected: No errors

**Step 3: 提交**

```bash
git add app/lib/analytics/analytics_constants.dart
git commit -m "feat(analytics): add closeDetail constant"
```

---

## Task 7: 运行测试并验证

**Step 1: 运行 Flutter 分析**

Run: `cd app && flutter analyze`
Expected: No issues found

**Step 2: 运行测试**

Run: `cd app && flutter test`
Expected: All tests pass

**Step 3: 手动测试功能**

1. 启动应用: `cd app && flutter run`
2. 登录后进入首页
3. 点击"最近交易"板块的"查看全部" → 应切换到"记录"标签
4. 在"记录"页面查看交易列表
5. 下拉刷新
6. 点击交易项 → 弹出详情抽屉
7. 点击"关闭"按钮 → 抽屉关闭

**Step 4: 最终提交**

```bash
git add app/
git commit -m "feat(transaction history): complete implementation and verification"
```

---

## 验收标准

- [x] 首页"查看全部"按钮切换到"记录"标签
- [x] HistoryScreen 显示所有交易记录
- [x] 支持下拉刷新
- [x] 点击交易项弹出详情抽屉
- [x] 详情显示基本信息和业务信息
- [x] 埋点事件正确上报
- [x] 代码通过 Flutter analyze
- [x] 所有测试通过

## 相关文档

- 设计文档: `docs/plans/2026-02-25-transaction-history-page-design.md`
- TransactionProvider: `app/lib/providers/transaction_provider.dart`
- 埋点规范: `app/lib/analytics/event_spec.dart`
