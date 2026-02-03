import 'package:flutter/material.dart';
import '../utils/design_scale.dart';

/// 推荐奖励迷你卡片组件
/// 设计稿基准宽度: 402px
class RewardMiniCard extends StatelessWidget {
  final VoidCallback? onShareTap;
  final String rewardAmount;
  final String title;

  const RewardMiniCard({
    super.key,
    this.onShareTap,
    this.rewardAmount = '1,000',
    this.title = '推荐友奖赏',
  });

  @override
  Widget build(BuildContext context) {
    final scale = DesignScale.getScale(context);

    return Container(
      width: double.infinity,
      margin: EdgeInsets.symmetric(horizontal: 16 * scale),
      height: 100 * scale, // 设计稿: height: 100
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [Colors.white, Color(0xFFFAF8FF)],
        ),
        borderRadius: BorderRadius.circular(20 * scale),
        boxShadow: [
          BoxShadow(
            color: const Color(0xFF7424F5).withOpacity(0.15),
            blurRadius: 30 * scale,
            offset: Offset(0, 10 * scale),
          ),
          BoxShadow(
            color: const Color(0xFF000000).withOpacity(0.05),
            blurRadius: 6 * scale,
            offset: Offset(0, 2 * scale),
          ),
        ],
      ),
      child: Stack(
        children: [
          // 装饰圆形
          Positioned(
            right: 0,
            top: -30 * scale,
            child: Container(
              width: 100 * scale, // 设计稿: width: 100
              height: 100 * scale, // 设计稿: height: 100
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                gradient: RadialGradient(
                  colors: [
                    const Color(0xFF7424F5).withOpacity(0.08),
                    const Color(0xFF7424F5).withOpacity(0),
                  ],
                ),
              ),
            ),
          ),
          // 主要内容
          Padding(
            padding: EdgeInsets.all(16 * scale),
            child: Row(
              children: [
                // 左侧内容
                Expanded(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        title,
                        style: TextStyle(
                          color: const Color(0xFF1A1A1A),
                          fontSize: 16 * scale,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                      SizedBox(height: 4 * scale),
                      Row(
                        crossAxisAlignment: CrossAxisAlignment.baseline,
                        textBaseline: TextBaseline.alphabetic,
                        children: [
                          Text(
                            '\$',
                            style: TextStyle(
                              color: const Color(0xFFFFD700),
                              fontSize: 18 * scale,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                          Text(
                            rewardAmount,
                            style: TextStyle(
                              color: const Color(0xFFFFD700),
                              fontSize: 24 * scale,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
                // 推荐按钮
                GestureDetector(
                  onTap: onShareTap,
                  child: Container(
                    width: 100 * scale, // 设计稿: width: 100
                    height: 40 * scale, // 设计稿: height: 40
                    decoration: BoxDecoration(
                      color: const Color(0xFF7424F5).withOpacity(0.1),
                      borderRadius: BorderRadius.circular(12 * scale),
                    ),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Icon(
                          Icons.share,
                          color: const Color(0xFF7424F5),
                          size: 16 * scale,
                        ),
                        SizedBox(width: 6 * scale),
                        Text(
                          '推荐',
                          style: TextStyle(
                            color: const Color(0xFF7424F5),
                            fontSize: 13 * scale,
                            fontWeight: FontWeight.w500,
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
