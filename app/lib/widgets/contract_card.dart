import 'package:flutter/material.dart';

import '../models/loan_contract.dart';
import '../utils/design_scale.dart';
import 'analytics/analytics_tap.dart';
import '../analytics/analytics_constants.dart';
import '../analytics/event_spec.dart';

/// 合同卡片组件
/// 显示单个贷款合同的摘要信息
class ContractCard extends StatelessWidget {
  /// 合同数据
  final LoanContractSummary contract;

  /// 点击回调
  final VoidCallback? onTap;

  const ContractCard({
    super.key,
    required this.contract,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final scale = DesignScale.getScale(context);

    return AnalyticsTap(
      eventType: AnalyticsEventType.transactionClick,
      properties: AnalyticsEventProperties.itemClick(
        page: AnalyticsPages.contractList,
        flow: AnalyticsFlows.repayment,
        elementId: AnalyticsIds.contractCard,
        elementType: AnalyticsElementType.card,
        itemType: 'contract',
        itemId: contract.contractNo,
        itemName: contract.contractNo,
      ),
      onTap: onTap,
      child: Container(
        margin: EdgeInsets.only(bottom: 12 * scale),
        padding: EdgeInsets.all(16 * scale),
        decoration: BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: contract.hasOutstanding
                ? [const Color(0xFF7424F5), const Color(0xFF5A1DB8)]
                : [const Color(0xFF66BB6A), const Color(0xFF43A047)],
          ),
          borderRadius: BorderRadius.circular(16 * scale),
          boxShadow: [
            BoxShadow(
              color: contract.hasOutstanding
                  ? const Color(0xFF7424F5).withOpacity(0.2)
                  : const Color(0xFF66BB6A).withOpacity(0.2),
              blurRadius: 12 * scale,
              offset: Offset(0, 4 * scale),
            ),
          ],
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // 顶部：合同编号和状态
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Expanded(
                  child: Text(
                    '合同 ${contract.contractNo}',
                    style: TextStyle(
                      color: Colors.white,
                      fontSize: 16 * scale,
                      fontWeight: FontWeight.w600,
                    ),
                    overflow: TextOverflow.ellipsis,
                  ),
                ),
                Container(
                  padding: EdgeInsets.symmetric(
                    horizontal: 8 * scale,
                    vertical: 4 * scale,
                  ),
                  decoration: BoxDecoration(
                    color: Colors.white.withOpacity(0.2),
                    borderRadius: BorderRadius.circular(12 * scale),
                  ),
                  child: Text(
                    contract.statusLabel,
                    style: TextStyle(
                      color: Colors.white,
                      fontSize: 12 * scale,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                ),
              ],
            ),
            SizedBox(height: 16 * scale),

            // 额度信息
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                _AmountItem(
                  label: '授信额度',
                  value: _formatCurrency(contract.creditLimit),
                  scale: scale,
                ),
                _AmountItem(
                  label: '可用额度',
                  value: _formatCurrency(contract.availableLimit),
                  scale: scale,
                ),
              ],
            ),
            SizedBox(height: 12 * scale),

            // 未偿余额
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                _AmountItem(
                  label: '本金余额',
                  value: _formatCurrency(contract.principalOutstanding),
                  scale: scale,
                ),
                _AmountItem(
                  label: '利息余额',
                  value: _formatCurrency(contract.interestOutstanding),
                  scale: scale,
                ),
              ],
            ),

            // 下次还款信息（如果有）
            if (contract.nextRepaymentDate != null) ...[
              SizedBox(height: 12 * scale),
              Container(
                padding: EdgeInsets.all(12 * scale),
                decoration: BoxDecoration(
                  color: Colors.white.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(12 * scale),
                ),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      '下次还款',
                      style: TextStyle(
                        color: Colors.white.withOpacity(0.8),
                        fontSize: 13 * scale,
                      ),
                    ),
                    Text(
                      _formatDate(contract.nextRepaymentDate!),
                      style: TextStyle(
                        color: Colors.white,
                        fontSize: 14 * scale,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }

  String _formatCurrency(double value) {
    if (value >= 10000) {
      return '\$${(value / 10000).toStringAsFixed(1)}万';
    }
    return '\$${value.toStringAsFixed(0)}';
  }

  String _formatDate(DateTime date) {
    return '${date.month}/${date.day}';
  }
}

/// 金额显示组件
class _AmountItem extends StatelessWidget {
  final String label;
  final String value;
  final double scale;

  const _AmountItem({
    required this.label,
    required this.value,
    required this.scale,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          label,
          style: TextStyle(
            color: Colors.white.withOpacity(0.7),
            fontSize: 12 * scale,
          ),
        ),
        SizedBox(height: 4 * scale),
        Text(
          value,
          style: TextStyle(
            color: Colors.white,
            fontSize: 16 * scale,
            fontWeight: FontWeight.w600,
          ),
        ),
      ],
    );
  }
}

/// 合同空状态组件
/// 当没有合同时显示
class ContractEmptyState extends StatelessWidget {
  final String? message;
  final VoidCallback? onActionTap;

  const ContractEmptyState({
    super.key,
    this.message,
    this.onActionTap,
  });

  @override
  Widget build(BuildContext context) {
    final scale = DesignScale.getScale(context);

    return Center(
      child: Padding(
        padding: EdgeInsets.all(32 * scale),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              width: 80 * scale,
              height: 80 * scale,
              decoration: BoxDecoration(
                color: const Color(0xFF7424F5).withOpacity(0.1),
                shape: BoxShape.circle,
              ),
              child: Icon(
                Icons.description_outlined,
                size: 40 * scale,
                color: const Color(0xFF7424F5),
              ),
            ),
            SizedBox(height: 16 * scale),
            Text(
              message ?? '暂无贷款合同',
              style: TextStyle(
                color: const Color(0xFF666666),
                fontSize: 16 * scale,
                fontWeight: FontWeight.w500,
              ),
            ),
            SizedBox(height: 8 * scale),
            Text(
              '申请贷款并签署合同后即可查看',
              style: TextStyle(
                color: const Color(0xFF999999),
                fontSize: 14 * scale,
              ),
            ),
            if (onActionTap != null) ...[
              SizedBox(height: 24 * scale),
              ElevatedButton.icon(
                onPressed: onActionTap,
                icon: const Icon(Icons.add),
                label: const Text('申请贷款'),
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF7424F5),
                  foregroundColor: Colors.white,
                  padding: EdgeInsets.symmetric(
                    horizontal: 24 * scale,
                    vertical: 12 * scale,
                  ),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(24 * scale),
                  ),
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }
}
