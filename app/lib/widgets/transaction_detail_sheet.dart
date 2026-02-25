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
        elementId: 'close_detail',
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
