import 'package:flutter/material.dart';

import '../analytics/analytics_constants.dart';
import '../analytics/event_spec.dart';
import '../models/loan_transaction.dart';
import '../utils/design_scale.dart';
import 'analytics/analytics_tap.dart';

/// 还款成功对话框
/// 显示还款交易的详细回单信息
class RepaymentSuccessDialog extends StatelessWidget {
  /// 还款交易信息
  final LoanTransactionItem transaction;

  /// 利息支付金额
  final double interestPaid;

  /// 本金支付金额
  final double principalPaid;

  /// 关闭回调
  final VoidCallback? onClose;

  const RepaymentSuccessDialog({
    super.key,
    required this.transaction,
    required this.interestPaid,
    required this.principalPaid,
    this.onClose,
  });

  /// 显示对话框
  static Future<void> show({
    required BuildContext context,
    required LoanTransactionItem transaction,
    required double interestPaid,
    required double principalPaid,
    VoidCallback? onClose,
  }) {
    return showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      isDismissible: false,
      enableDrag: false,
      builder: (context) => RepaymentSuccessDialog(
        transaction: transaction,
        interestPaid: interestPaid,
        principalPaid: principalPaid,
        onClose: onClose ?? () => Navigator.of(context).pop(),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final scale = DesignScale.getScale(context);

    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.vertical(top: Radius.circular(24 * scale)),
      ),
      child: SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            // 顶部成功图标
            _buildSuccessHeader(context, scale),

            // 交易详情
            Padding(
              padding: EdgeInsets.all(24 * scale),
              child: Column(
                children: [
                  // 还款金额
                  _AmountItem(
                    label: '还款总额',
                    amount: transaction.amount,
                    scale: scale,
                    isPrimary: true,
                  ),
                  SizedBox(height: 16 * scale),

                  // 本金和利息明细
                  Row(
                    children: [
                      Expanded(
                        child: _AmountItem(
                          label: '本金',
                          amount: principalPaid,
                          scale: scale,
                        ),
                      ),
                      SizedBox(width: 24 * scale),
                      Expanded(
                        child: _AmountItem(
                          label: '利息',
                          amount: interestPaid,
                          scale: scale,
                        ),
                      ),
                    ],
                  ),
                  SizedBox(height: 24 * scale),

                  // 分隔线
                  Divider(height: 1, color: const Color(0xFFE0E0E0)),
                  SizedBox(height: 16 * scale),

                  // 账户余额更新
                  _BalanceItem(
                    label: '可用额度',
                    value: transaction.availableLimitAfter,
                    scale: scale,
                  ),
                  SizedBox(height: 12 * scale),
                  _BalanceItem(
                    label: '本金余额',
                    value: transaction.principalOutstandingAfter,
                    scale: scale,
                  ),
                  SizedBox(height: 32 * scale),

                  // 关闭按钮
                  AnalyticsTap(
                    eventType: AnalyticsEventType.buttonClick,
                    properties: AnalyticsEventProperties.click(
                      page: AnalyticsPages.repayment,
                      flow: AnalyticsFlows.repayment,
                      elementId: AnalyticsIds.repaymentSuccessClose,
                      elementType: AnalyticsElementType.button,
                      elementText: '完成',
                    ),
                    onTap: onClose,
                    child: Container(
                      width: double.infinity,
                      padding: EdgeInsets.symmetric(vertical: 16 * scale),
                      decoration: BoxDecoration(
                        gradient: const LinearGradient(
                          colors: [Color(0xFF7424F5), Color(0xFF5A1DB8)],
                        ),
                        borderRadius: BorderRadius.circular(24 * scale),
                      ),
                      child: Text(
                        '完成',
                        style: TextStyle(
                          color: Colors.white,
                          fontSize: 16 * scale,
                          fontWeight: FontWeight.w600,
                        ),
                        textAlign: TextAlign.center,
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSuccessHeader(BuildContext context, double scale) {
    return Container(
      width: double.infinity,
      padding: EdgeInsets.symmetric(vertical: 32 * scale),
      decoration: const BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [Color(0xFF66BB6A), Color(0xFF43A047)],
        ),
        borderRadius: BorderRadius.vertical(
          top: Radius.circular(24),
        ),
      ),
      child: Column(
        children: [
          Container(
            width: 64 * scale,
            height: 64 * scale,
            decoration: BoxDecoration(
              color: Colors.white,
              shape: BoxShape.circle,
            ),
            child: Icon(
              Icons.check_rounded,
              size: 40 * scale,
              color: const Color(0xFF66BB6A),
            ),
          ),
          SizedBox(height: 16 * scale),
          Text(
            '还款成功',
            style: TextStyle(
              color: Colors.white,
              fontSize: 20 * scale,
              fontWeight: FontWeight.w600,
            ),
          ),
        ],
      ),
    );
  }
}

/// 金额显示组件
class _AmountItem extends StatelessWidget {
  final String label;
  final double amount;
  final double scale;
  final bool isPrimary;

  const _AmountItem({
    required this.label,
    required this.amount,
    required this.scale,
    this.isPrimary = false,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          label,
          style: TextStyle(
            color: const Color(0xFF999999),
            fontSize: 13 * scale,
          ),
        ),
        SizedBox(height: 4 * scale),
        Text(
          '\$${amount.toStringAsFixed(2)}',
          style: TextStyle(
            color: isPrimary ? const Color(0xFF7424F5) : const Color(0xFF1A1A1A),
            fontSize: isPrimary ? 24 * scale : 18 * scale,
            fontWeight: FontWeight.w600,
          ),
        ),
      ],
    );
  }
}

/// 余额显示组件
class _BalanceItem extends StatelessWidget {
  final String label;
  final double value;
  final double scale;

  const _BalanceItem({
    required this.label,
    required this.value,
    required this.scale,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text(
          label,
          style: TextStyle(
            color: const Color(0xFF666666),
            fontSize: 14 * scale,
          ),
        ),
        Text(
          '\$${value.toStringAsFixed(2)}',
          style: TextStyle(
            color: const Color(0xFF1A1A1A),
            fontSize: 16 * scale,
            fontWeight: FontWeight.w600,
          ),
        ),
      ],
    );
  }
}
