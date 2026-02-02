import 'package:flutter/material.dart';
import '../utils/design_scale.dart';

/// 交易类型枚举
enum TransactionType {
  income, // 收入（绿色）
  expense, // 支出（黑色）
  reward, // 奖励（绿色）
}

/// 交易数据模型
class TransactionData {
  final String id;
  final String name;
  final String time;
  final String amount;
  final TransactionType type;
  final IconData icon;
  final Color iconColor;
  final Color bgColor;

  const TransactionData({
    required this.id,
    required this.name,
    required this.time,
    required this.amount,
    required this.type,
    required this.icon,
    required this.iconColor,
    required this.bgColor,
  });

  /// 金额是否为正数（收入）
  bool get isPositive =>
      type == TransactionType.income || type == TransactionType.reward;

  /// 默认交易数据
  static List<TransactionData> getDefaultTransactions() {
    return [
      TransactionData(
        id: '1',
        name: '放款到账',
        time: '今天 14:30',
        amount: '+¥50,000.00',
        type: TransactionType.income,
        icon: Icons.arrow_downward,
        iconColor: const Color(0xFF7424F5),
        bgColor: const Color(0xFF7424F5).withOpacity(0.1),
      ),
      TransactionData(
        id: '2',
        name: '还款扣除',
        time: '昨天 10:00',
        amount: '-¥2,500.00',
        type: TransactionType.expense,
        icon: Icons.arrow_upward,
        iconColor: const Color(0xFFFF6B6B),
        bgColor: const Color(0xFFFF6B6B).withOpacity(0.1),
      ),
      TransactionData(
        id: '3',
        name: '推荐奖励',
        time: '01/28 16:45',
        amount: '+¥50.00',
        type: TransactionType.reward,
        icon: Icons.redeem,
        iconColor: const Color(0xFF11998E),
        bgColor: const Color(0xFF11998E).withOpacity(0.1),
      ),
    ];
  }
}

/// 最近交易组件
/// 设计稿基准宽度: 402px
class TransactionListSection extends StatelessWidget {
  final List<TransactionData>? transactions;
  final VoidCallback? onViewAllTap;
  final Function(TransactionData)? onTransactionTap;

  const TransactionListSection({
    super.key,
    this.transactions,
    this.onViewAllTap,
    this.onTransactionTap,
  });

  List<TransactionData> get _transactions =>
      transactions ?? TransactionData.getDefaultTransactions();

  @override
  Widget build(BuildContext context) {
    final scale = DesignScale.getScale(context);

    return Container(
      margin: EdgeInsets.symmetric(horizontal: 16 * scale),
      padding: EdgeInsets.symmetric(
        horizontal: 16 * scale,
        vertical: 24 * scale,
      ),
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [Colors.white, Color(0xFFF8F5FF)],
        ),
        borderRadius: BorderRadius.circular(20 * scale),
        boxShadow: [
          BoxShadow(
            color: const Color(0xFF000000).withOpacity(0.06),
            blurRadius: 16 * scale,
            offset: Offset(0, 4 * scale),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // 标题栏
          _buildHeader(scale),
          SizedBox(height: 16 * scale),
          // 交易列表
          _buildTransactionList(scale),
        ],
      ),
    );
  }

  /// 标题栏
  Widget _buildHeader(double scale) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text(
          '最近交易',
          style: TextStyle(
            color: const Color(0xFF1A1A1A),
            fontSize: 18 * scale,
            fontWeight: FontWeight.w600,
          ),
        ),
        GestureDetector(
          onTap: onViewAllTap,
          child: Text(
            '查看全部',
            style: TextStyle(
              color: const Color(0xFF7424F5),
              fontSize: 13 * scale,
              fontWeight: FontWeight.w500,
            ),
          ),
        ),
      ],
    );
  }

  /// 交易列表
  Widget _buildTransactionList(double scale) {
    return Column(
      children: List.generate(_transactions.length, (index) {
        final transaction = _transactions[index];
        final isLast = index == _transactions.length - 1;

        return Column(
          children: [
            _TransactionItem(
              transaction: transaction,
              scale: scale,
              onTap: () => onTransactionTap?.call(transaction),
            ),
            if (!isLast)
              Container(
                height: 1,
                color: const Color(0xFF000000).withOpacity(0.05),
              ),
          ],
        );
      }),
    );
  }
}

/// 单个交易项
class _TransactionItem extends StatelessWidget {
  final TransactionData transaction;
  final double scale;
  final VoidCallback? onTap;

  const _TransactionItem({
    required this.transaction,
    required this.scale,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Padding(
        padding: EdgeInsets.symmetric(vertical: 16 * scale),
        child: Row(
          children: [
            // 图标
            Container(
              width: 40 * scale, // 设计稿: width: 40
              height: 40 * scale, // 设计稿: height: 40
              decoration: BoxDecoration(
                color: transaction.bgColor,
                borderRadius: BorderRadius.circular(12 * scale),
              ),
              child: Icon(
                transaction.icon,
                color: transaction.iconColor,
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
                    transaction.name,
                    style: TextStyle(
                      color: const Color(0xFF1A1A1A),
                      fontSize: 15 * scale,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                  SizedBox(height: 4 * scale),
                  Text(
                    transaction.time,
                    style: TextStyle(
                      color: const Color(0xFF666666),
                      fontSize: 12 * scale,
                    ),
                  ),
                ],
              ),
            ),
            // 金额
            Text(
              transaction.amount,
              style: TextStyle(
                color: transaction.isPositive
                    ? const Color(0xFF22C55E)
                    : const Color(0xFF1A1A1A),
                fontSize: 16 * scale,
                fontWeight: FontWeight.w600,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
