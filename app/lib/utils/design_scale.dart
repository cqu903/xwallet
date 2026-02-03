import 'package:flutter/material.dart';

/// 设计稿适配工具类
/// 设计稿基准宽度: 402px
class DesignScale {
  /// 设计稿基准宽度
  static const double designWidth = 402.0;

  /// 私有构造函数，防止实例化
  DesignScale._();

  /// 获取缩放比例
  static double getScale(BuildContext context) {
    final screenWidth = MediaQuery.of(context).size.width;
    return screenWidth / designWidth;
  }

  /// 根据设计稿尺寸获取实际尺寸（宽度和通用尺寸）
  /// [designValue] 设计稿中的尺寸值
  static double w(BuildContext context, double designValue) {
    return designValue * getScale(context);
  }

  /// 获取屏幕宽度
  static double screenWidth(BuildContext context) {
    return MediaQuery.of(context).size.width;
  }

  /// 获取屏幕高度
  static double screenHeight(BuildContext context) {
    return MediaQuery.of(context).size.height;
  }

  /// 根据屏幕宽度比例获取实际宽度
  /// [ratio] 0.0 ~ 1.0 的比例值
  static double widthRatio(BuildContext context, double ratio) {
    return MediaQuery.of(context).size.width * ratio;
  }

  /// 获取边距（根据设计稿比例缩放）
  static EdgeInsets paddingSymmetric(
    BuildContext context, {
    double horizontal = 0,
    double vertical = 0,
  }) {
    final scale = getScale(context);
    return EdgeInsets.symmetric(
      horizontal: horizontal * scale,
      vertical: vertical * scale,
    );
  }

  /// 获取全部边距（根据设计稿比例缩放）
  static EdgeInsets paddingAll(BuildContext context, double value) {
    return EdgeInsets.all(value * getScale(context));
  }

  /// 获取自定义边距（根据设计稿比例缩放）
  static EdgeInsets paddingOnly(
    BuildContext context, {
    double left = 0,
    double top = 0,
    double right = 0,
    double bottom = 0,
  }) {
    final scale = getScale(context);
    return EdgeInsets.only(
      left: left * scale,
      top: top * scale,
      right: right * scale,
      bottom: bottom * scale,
    );
  }

  /// 获取圆角（根据设计稿比例缩放）
  static BorderRadius borderRadius(BuildContext context, double radius) {
    return BorderRadius.circular(radius * getScale(context));
  }

  /// 获取字体大小（根据设计稿比例缩放，但有最小值限制）
  static double fontSize(BuildContext context, double designFontSize) {
    final scaled = designFontSize * getScale(context);
    // 字体最小不小于设计值的 0.8 倍，防止在小屏幕上字体过小
    return scaled.clamp(designFontSize * 0.8, designFontSize * 1.5);
  }

  /// 获取图标大小（根据设计稿比例缩放）
  static double iconSize(BuildContext context, double designIconSize) {
    return designIconSize * getScale(context);
  }
}

/// 便捷扩展方法
extension DesignScaleExtension on num {
  /// 将设计稿尺寸转换为实际尺寸
  /// 使用方式: 370.w(context)
  double w(BuildContext context) {
    return DesignScale.w(context, toDouble());
  }

  /// 将设计稿字体大小转换为实际大小
  /// 使用方式: 16.sp(context)
  double sp(BuildContext context) {
    return DesignScale.fontSize(context, toDouble());
  }
}
