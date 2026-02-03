import 'package:flutter/material.dart';

/// X Wallet 品牌 Logo（官网 www.xwallet.hk 正式 logo 图片）
/// Logo 保持原始宽高比，仅通过 width 控制缩放
class XWalletLogo extends StatelessWidget {
  const XWalletLogo({super.key, this.size = 80, this.cornerRadius});

  /// Logo 宽度（高度按图片比例自动计算）
  final double size;

  /// 圆角，默认 size * 0.25（约 20 当 size=80）
  final double? cornerRadius;

  static const String _assetPath = 'design/images/xwallet_logo.png';

  /// Logo 原始宽高比 1844:364 ≈ 5.07
  static const double _aspectRatio = 1844 / 364;

  @override
  Widget build(BuildContext context) {
    final radius = cornerRadius ?? size * 0.25;
    return ClipRRect(
      borderRadius: BorderRadius.circular(radius),
      child: SizedBox(
        width: size,
        height: size / _aspectRatio,
        child: Image.asset(
          _assetPath,
          fit: BoxFit.contain,
          errorBuilder: (context, error, stackTrace) =>
              _FallbackLogo(size: size),
        ),
      ),
    );
  }
}

/// 资源加载失败时显示的备用 logo（紫色底 + 白色 X）
class _FallbackLogo extends StatelessWidget {
  const _FallbackLogo({required this.size});

  final double size;

  static const Color _primary = Color(0xFF7424F5);

  @override
  Widget build(BuildContext context) {
    return Container(
      width: size,
      height: size,
      alignment: Alignment.center,
      decoration: BoxDecoration(
        color: _primary,
        borderRadius: BorderRadius.circular(size * 0.25),
      ),
      child: Text(
        'X',
        style: TextStyle(
          color: Colors.white,
          fontSize: size * 0.6,
          fontWeight: FontWeight.bold,
          height: 1,
        ),
      ),
    );
  }
}
