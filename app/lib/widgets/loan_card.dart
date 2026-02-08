import 'package:flutter/material.dart';
import '../utils/design_scale.dart';

/// 贷款英雄卡片 - 紫色渐变主题 + 金色按钮（严格遵循设计稿）
/// 设计稿: 首页.pen - loanHeroCard
/// 设计稿基准宽度: 402px
/// 用户需求: 仅保留从右上到左下的紫色渐变背景，无其他装饰
class LoanCard extends StatelessWidget {
  final VoidCallback onApply;

  const LoanCard({super.key, required this.onApply});

  @override
  Widget build(BuildContext context) {
    // 使用设计稿比例计算实际尺寸
    final scale = DesignScale.getScale(context);

    return Container(
      width: 370 * scale, // 设计稿宽度 370，按比例缩放
      height: 380 * scale, // 设计稿高度 380，按比例缩放
      margin: EdgeInsets.symmetric(horizontal: 16 * scale),
      decoration: BoxDecoration(
        // 设计稿: 渐变 #7424F5 → #4A148C → #2D0E5A, rotation: 135 (右上到左下)
        gradient: const LinearGradient(
          begin: Alignment.topRight, // rotation: 135 = 从右上
          end: Alignment.bottomLeft, // 到左下
          colors: [
            Color(0xFF7424F5), // 主紫色
            Color(0xFF4A148C), // 深紫色
            Color(0xFF2D0E5A), // 最深紫色
          ],
          stops: [0.0, 0.5, 1.0],
        ),
        borderRadius: BorderRadius.circular(
          24 * scale,
        ), // 设计稿: cornerRadius: 24
        boxShadow: [
          // 设计稿: blur:48, color:rgba(116,36,245,0.4), offset:(0,16)
          BoxShadow(
            color: const Color(0xFF7424F5).withOpacity(0.4),
            blurRadius: 48 * scale,
            offset: Offset(0, 16 * scale),
          ),
          // 设计稿: blur:16, color:rgba(0,0,0,0.15), offset:(0,4)
          BoxShadow(
            color: const Color(0xFF000000).withOpacity(0.15),
            blurRadius: 16 * scale,
            offset: Offset(0, 4 * scale),
          ),
        ],
      ),
      // 主要内容（设计稿 contentWrapper: padding [24,16], gap: 12）
      child: Padding(
        padding: EdgeInsets.symmetric(
          horizontal: 16 * scale,
          vertical: 24 * scale,
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.center,
          mainAxisSize: MainAxisSize.min,
          children: [
            // 徽章
            _buildBadge(context, scale),
            SizedBox(height: 12 * scale), // gap: 12
            // 金额区域
            _buildAmountSection(context, scale),
            SizedBox(height: 24 * scale), // gap: 12
            // 特点标签
            _buildFeatureTags(context, scale),
            SizedBox(height: 24 * scale), // gap: 12
            // 申请按钮
            _buildApplyButton(context, scale),
            SizedBox(height: 12 * scale), // gap: 12
            // 底部说明文字
            _buildFooterNote(context, scale),
          ],
        ),
      ),
    );
  }

  /// 徽章（设计稿: loanBadge - cornerRadius:25, padding:[12,24]）
  Widget _buildBadge(BuildContext context, double scale) {
    return Center(
      child: Container(
        // 设计稿: padding: [12, 24] (上下12, 左右24)
        padding: EdgeInsets.symmetric(
          horizontal: 24 * scale,
          vertical: 12 * scale,
        ),
        child: Text(
          '🎉 X Wallet极速闪贷', // 设计稿: content
          textAlign: TextAlign.center,
          style: TextStyle(
            color: const Color(0xFFFFD700), // 设计稿: fill: #FFD700
            fontSize: 14 * scale, // 设计稿: fontSize: 14
            fontWeight: FontWeight.w600, // 设计稿: fontWeight: 600
          ),
        ),
      ),
    );
  }

  /// 金额区域（设计稿: loanAmountArea - gap:8, layout:vertical）
  Widget _buildAmountSection(BuildContext context, double scale) {
    const gold = Color(0xFFFFD700);
    const goldDark = Color(0xFFFFA500);
    const amountGradient = LinearGradient(
      begin: Alignment.topCenter,
      end: Alignment.bottomCenter,
      colors: [Colors.white, gold, goldDark],
      stops: [0.0, 0.5, 1.0],
    );
    // 设计稿: fontSize:72, fontWeight:900, letterSpacing:-3
    final amountStyle = TextStyle(
      color: Colors.white,
      fontSize: 72 * scale,
      fontWeight: FontWeight.w900,
      fontFamily: 'Montserrat',
      letterSpacing: -3 * scale,
      height: 1.0,
    );
    return Center(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start, // 左对齐
        children: [
          // 设计稿: loanLabel - "最高可借", fill:#ffffff99, fontSize:18, fontWeight:500
          Text(
            '最高可借',
            style: TextStyle(
              color: Colors.white.withOpacity(0.6), // #ffffff99
              fontSize: 16 * scale,
              fontWeight: FontWeight.w500,
            ),
          ),
          SizedBox(height: 8 * scale), // 设计稿: gap: 8
          // 设计稿: loanAmountRow - gap:2
          Row(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // 设计稿: currencySymbol - "¥", 渐变 #FFD700 → #FFA500, fontSize:32, fontWeight:800
              _buildGradientText(
                text: '¥',
                style: TextStyle(
                  color: Colors.white,
                  fontSize: 32 * scale,
                  fontWeight: FontWeight.w800,
                  fontFamily: 'Montserrat',
                ),
                gradient: const LinearGradient(
                  begin: Alignment.topCenter,
                  end: Alignment.bottomCenter,
                  colors: [gold, goldDark],
                ),
              ),
              SizedBox(width: 2 * scale), // 设计稿: gap: 2
              // 设计稿: loanAmount - "180,000", 渐变 #FFFFFF → #FFD700 → #FFA500
              _buildGradientText(
                text: '180,000',
                style: amountStyle,
                gradient: amountGradient,
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildGradientText({
    required String text,
    required TextStyle style,
    required Gradient gradient,
  }) {
    return LayoutBuilder(
      builder: (context, constraints) {
        final textPainter = TextPainter(
          text: TextSpan(text: text, style: style),
          textDirection: TextDirection.ltr,
          maxLines: 1,
        )..layout();

        final shader = gradient.createShader(
          Rect.fromLTWH(0, 0, textPainter.width, textPainter.height),
        );

        return Text(
          text,
          style: style.copyWith(
            foreground: Paint()..shader = shader,
            color: null,
          ),
        );
      },
    );
  }

  /// 特点标签（设计稿: loanFeatures - gap:8）
  Widget _buildFeatureTags(BuildContext context, double scale) {
    return Center(
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          // 设计稿: feature1 - check_circle
          _buildFeatureTag(context, scale, Icons.check_circle, '5秒批核'),
          SizedBox(width: 8 * scale), // 设计稿: gap: 8
          // 设计稿: feature2 - schedule
          _buildFeatureTag(context, scale, Icons.schedule, '秒级到账'),
        ],
      ),
    );
  }

  /// 特点标签组件（设计稿: cornerRadius:12, padding:[8,12], gap:6）
  Widget _buildFeatureTag(
    BuildContext context,
    double scale,
    IconData icon,
    String text,
  ) {
    return Container(
      // 设计稿: padding: [8, 12] (上下8, 左右12)
      padding: EdgeInsets.symmetric(
        horizontal: 12 * scale,
        vertical: 8 * scale,
      ),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.15),
        borderRadius: BorderRadius.circular(
          12 * scale,
        ), // 设计稿: cornerRadius: 12
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          // 设计稿: icon size:14, fill:#FFD700
          Icon(icon, color: const Color(0xFFFFD700), size: 14 * scale),
          SizedBox(width: 6 * scale), // 设计稿: gap: 6
          // 设计稿: text fontSize:13, fontWeight:500, fill:#FFFFFF, lineHeight:1
          Text(
            text,
            style: TextStyle(
              color: Colors.white,
              fontSize: 13 * scale,
              fontWeight: FontWeight.w500,
              height: 1.0,
            ),
          ),
        ],
      ),
    );
  }

  /// 申请按钮（设计稿: applyBtn - width:338, height:60, cornerRadius:20）
  Widget _buildApplyButton(BuildContext context, double scale) {
    return GestureDetector(
      onTap: onApply,
      child: Container(
        width: 338 * scale, // 设计稿: width: 338
        height: 60 * scale, // 设计稿: height: 60
        decoration: BoxDecoration(
          // 设计稿: 渐变 #FFD700 → #FFC107 → #FFB300, rotation:90
          gradient: const LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [Color(0xFFFFD700), Color(0xFFFFC107), Color(0xFFFFB300)],
          ),
          borderRadius: BorderRadius.circular(
            20 * scale,
          ), // 设计稿: cornerRadius: 20
        ),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            // 设计稿: bolt icon, fill:#1A1A1A, size:28
            Icon(Icons.bolt, color: const Color(0xFF1A1A1A), size: 28 * scale),
            SizedBox(width: 8 * scale), // 设计稿: gap: 8
            // 设计稿: "立即申请 →", fill:#1A1A1A, fontSize:20, fontWeight:800
            Text(
              '立即申请 →',
              style: TextStyle(
                color: const Color(0xFF1A1A1A),
                fontSize: 20 * scale,
                fontWeight: FontWeight.w800,
              ),
            ),
          ],
        ),
      ),
    );
  }

  /// 底部说明文字（设计稿: loanNote - fill:#ffffff99, fontSize:12）
  Widget _buildFooterNote(BuildContext context, double scale) {
    return Text(
      '最低日息 0.02% · 具体额度以审批为准', // 设计稿: content
      style: TextStyle(
        color: Colors.white.withOpacity(0.6), // 设计稿: fill: #ffffff99
        fontSize: 12 * scale, // 设计稿: fontSize: 12
        fontWeight: FontWeight.normal, // 设计稿: fontWeight: normal
      ),
    );
  }
}
