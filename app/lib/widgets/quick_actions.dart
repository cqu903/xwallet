import 'package:flutter/material.dart';
import '../utils/design_scale.dart';

/// 快捷操作数据模型
class QuickActionData {
  final String id;
  final String label;
  final IconData icon;
  final Color iconColor;
  final List<Color> bgGradient;

  const QuickActionData({
    required this.id,
    required this.label,
    required this.icon,
    required this.iconColor,
    required this.bgGradient,
  });

  /// 默认快捷操作
  static List<QuickActionData> getDefaultActions() {
    return [
      QuickActionData(
        id: 'repay',
        label: '还款',
        icon: Icons.credit_score,
        iconColor: const Color(0xFF7424F5),
        bgGradient: [
          const Color(0xFF7424F5).withOpacity(0.1),
          const Color(0xFF7424F5).withOpacity(0.05),
        ],
      ),
      QuickActionData(
        id: 'transfer',
        label: '转账',
        icon: Icons.swap_horiz,
        iconColor: const Color(0xFF11998E),
        bgGradient: [
          const Color(0xFF11998E).withOpacity(0.1),
          const Color(0xFF11998E).withOpacity(0.05),
        ],
      ),
      QuickActionData(
        id: 'bill',
        label: '账单',
        icon: Icons.receipt_long,
        iconColor: const Color(0xFFFF6B6B),
        bgGradient: [
          const Color(0xFFFF6B6B).withOpacity(0.1),
          const Color(0xFFFF6B6B).withOpacity(0.05),
        ],
      ),
      QuickActionData(
        id: 'more',
        label: '更多',
        icon: Icons.grid_view,
        iconColor: const Color(0xFFFFA726),
        bgGradient: [
          const Color(0xFFFFC163).withOpacity(0.15),
          const Color(0xFFFFC163).withOpacity(0.05),
        ],
      ),
    ];
  }
}

/// 快捷服务组件
/// 设计稿基准宽度: 402px
class QuickActionsSection extends StatelessWidget {
  final List<QuickActionData>? actions;
  final Function(QuickActionData)? onActionTap;

  const QuickActionsSection({super.key, this.actions, this.onActionTap});

  List<QuickActionData> get _actions =>
      actions ?? QuickActionData.getDefaultActions();

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
          colors: [Colors.white, Color(0xFFFAF8FF)],
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
          Text(
            '快捷服务',
            style: TextStyle(
              color: const Color(0xFF1A1A1A),
              fontSize: 18 * scale,
              fontWeight: FontWeight.w600,
            ),
          ),
          SizedBox(height: 16 * scale),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: _actions
                .map(
                  (action) => _QuickActionItem(
                    action: action,
                    scale: scale,
                    onTap: () => onActionTap?.call(action),
                  ),
                )
                .toList(),
          ),
        ],
      ),
    );
  }
}

/// 单个快捷操作项
class _QuickActionItem extends StatelessWidget {
  final QuickActionData action;
  final double scale;
  final VoidCallback? onTap;

  const _QuickActionItem({
    required this.action,
    required this.scale,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: SizedBox(
        width: 70 * scale, // 设计稿: width: 70
        child: Column(
          children: [
            // 图标容器
            Container(
              width: 48 * scale, // 设计稿: width: 48
              height: 48 * scale, // 设计稿: height: 48
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(16 * scale),
                gradient: LinearGradient(
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                  colors: action.bgGradient,
                ),
              ),
              child: Icon(
                action.icon,
                color: action.iconColor,
                size: 24 * scale, // 设计稿: size: 24
              ),
            ),
            SizedBox(height: 8 * scale),
            // 标签
            Text(
              action.label,
              style: TextStyle(
                color: const Color(0xFF1A1A1A),
                fontSize: 13 * scale,
                fontWeight: FontWeight.w500,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
