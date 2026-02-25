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
      context.read<TransactionProvider>().loadAllTransactionsIfNeeded();
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
    context.read<TransactionProvider>().loadAllTransactionsIfNeeded();
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
