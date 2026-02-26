import 'package:flutter/material.dart';

import '../analytics/analytics_constants.dart';
import '../analytics/event_spec.dart';
import '../utils/design_scale.dart';
import 'analytics/analytics_tap.dart';

/// 快捷金额按钮组件
/// 提供预设的还款金额选项
class QuickAmountButtons extends StatelessWidget {
  /// 预设金额列表
  final List<double> amounts;

  /// 选中的金额
  final double? selectedAmount;

  /// 选择回调
  final ValueChanged<double>? onAmountSelected;

  /// 自定义金额按钮文本（如果需要）
  final String? customAmountLabel;

  const QuickAmountButtons({
    super.key,
    required this.amounts,
    this.selectedAmount,
    this.onAmountSelected,
    this.customAmountLabel,
  });

  @override
  Widget build(BuildContext context) {
    final scale = DesignScale.getScale(context);

    return Wrap(
      spacing: 12 * scale,
      runSpacing: 12 * scale,
      children: amounts.map((amount) {
        final isSelected = selectedAmount == amount;
        return _AmountButton(
          amount: amount,
          isSelected: isSelected,
          scale: scale,
          onTap: () => onAmountSelected?.call(amount),
        );
      }).toList(),
    );
  }
}

/// 单个金额按钮
class _AmountButton extends StatelessWidget {
  final double amount;
  final bool isSelected;
  final double scale;
  final VoidCallback onTap;

  const _AmountButton({
    required this.amount,
    required this.isSelected,
    required this.scale,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return AnalyticsTap(
      eventType: AnalyticsEventType.buttonClick,
      properties: AnalyticsEventProperties.click(
        page: AnalyticsPages.repayment,
        flow: AnalyticsFlows.repayment,
        elementId: AnalyticsIds.quickAmountButton,
        elementType: AnalyticsElementType.button,
        elementText: _formatAmount(amount),
      ),
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 200),
        padding: EdgeInsets.symmetric(
          horizontal: 20 * scale,
          vertical: 12 * scale,
        ),
        decoration: BoxDecoration(
          gradient: isSelected
              ? const LinearGradient(
                  colors: [Color(0xFF7424F5), Color(0xFF5A1DB8)],
                )
              : null,
          color: isSelected ? null : Colors.white,
          borderRadius: BorderRadius.circular(24 * scale),
          border: Border.all(
            color: isSelected ? Colors.transparent : const Color(0xFFE0E0E0),
            width: 1.5,
          ),
          boxShadow: isSelected
              ? [
                  BoxShadow(
                    color: const Color(0xFF7424F5).withValues(alpha: 0.3),
                    blurRadius: 8 * scale,
                    offset: Offset(0, 2 * scale),
                  ),
                ]
              : null,
        ),
        child: Text(
          _formatAmount(amount),
          style: TextStyle(
            color: isSelected ? Colors.white : const Color(0xFF1A1A1A),
            fontSize: 15 * scale,
            fontWeight: isSelected ? FontWeight.w600 : FontWeight.w500,
          ),
        ),
      ),
    );
  }

  String _formatAmount(double value) {
    if (value >= 10000) {
      return '\$${(value / 10000).toStringAsFixed(1)}万';
    }
    return '\$${value.toStringAsFixed(0)}';
  }
}
