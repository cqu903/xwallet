import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../analytics/analytics_constants.dart';
import '../analytics/app_routes.dart';
import '../analytics/event_spec.dart';
import '../analytics/navigation_tracking.dart';
import '../models/analytics_event.dart';
import '../providers/auth_provider.dart';
import '../services/analytics_service.dart';
import '../utils/design_scale.dart';
import '../widgets/analytics/analytics_elevated_button.dart';
import '../widgets/analytics/analytics_icon_button.dart';
import '../widgets/analytics/analytics_text_button.dart';
import '../widgets/x_wallet_logo.dart';
import 'register_screen.dart';

// 设计稿颜色常量
const Color _kBgColor = Color(0xFFD4CCF5);
const Color _kPrimaryPurple = Color(0xFF7424F5);
const Color _kTextPrimary = Color(0xFF1A1A1A);
const Color _kTextSecondary = Color(0xFF666666);
const Color _kInputBg = Color(0xFFF8F9FA);
const Color _kDividerColor = Color(0xFFE0E0E0);
const Color _kCardGradientEnd = Color(0xFFFAF8FF);

/// X Wallet 登录页面
///
/// 基于设计稿 首页.pen - Login Screen 实现
/// 设计稿基准宽度: 402px
/// - 背景: #D4CCF5 淡紫色（与主页一致）
/// - Logo: 页面正上方，保持比例
/// - 登录卡片: 白色渐变 #FFFFFF → #FAF8FF，24px 圆角
/// - 主按钮: 品牌色 #7424F5
class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final _formKey = GlobalKey<FormState>();
  final _emailController = TextEditingController();
  final _passwordController = TextEditingController();
  bool _obscurePassword = true;
  bool _isLoading = false;

  static const String _testEmail = 'customer@example.com';
  static const String _testPassword = 'customer123';

  @override
  void initState() {
    super.initState();
    _emailController.text = _testEmail;
    _passwordController.text = _testPassword;
  }

  @override
  void dispose() {
    _emailController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  Future<void> _handleLogin() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _isLoading = true);
    final authProvider = context.read<AuthProvider>();
    final email = _emailController.text.trim();

    try {
      final success = await authProvider.login(email, _passwordController.text);

      await AnalyticsService.instance.trackStandardEvent(
        eventType: AnalyticsEventType.formSubmit,
        properties: AnalyticsEventProperties.formSubmit(
          page: AnalyticsPages.login,
          flow: AnalyticsFlows.login,
          elementId: AnalyticsIds.loginSubmit,
          success: success,
          extra: {
            'loginMethod': 'email',
            'hasError': !success,
          },
        ),
        userId: success
            ? authProvider.currentUser?.userInfo.userId.toString()
            : null,
        category: success ? EventCategory.critical : EventCategory.behavior,
      );

      if (!success && mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Row(
              children: [
                const Icon(Icons.error_outline, color: Colors.white),
                const SizedBox(width: 12),
                Expanded(child: Text(authProvider.errorMessage ?? '登录失败')),
              ],
            ),
            backgroundColor: const Color(0xFFF44336),
            behavior: SnackBarBehavior.floating,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(8),
            ),
            duration: const Duration(seconds: 3),
          ),
        );
      } else if (success && mounted) {
        await context.pushReplacementNamedTracked<void>(
          AppRoutes.main,
          page: AnalyticsPages.login,
          flow: AnalyticsFlows.login,
          elementId: AnalyticsIds.loginSuccessRedirect,
          elementType: AnalyticsElementType.button,
          eventType: AnalyticsEventType.buttonClick,
          elementText: '登录成功跳转',
        );
      }
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final scale = DesignScale.getScale(context);

    return Scaffold(
      backgroundColor: _kBgColor,
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            physics: const BouncingScrollPhysics(),
            padding: EdgeInsets.symmetric(horizontal: 26 * scale),
            child: ConstrainedBox(
              constraints: BoxConstraints(maxWidth: 350 * scale),
              child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    // Logo - 设计稿: 小型横向 logo (140x28)，位于顶部
                    // 设计稿间距: logo底部(y=108) 到 loginCard顶部(y=165) = 57px
                    Padding(
                      padding: EdgeInsets.only(bottom: 70 * scale),
                      child: Image.asset(
                        'assets/images/xwallet_logo.png',
                        width: 140 * scale,
                        height: 28 * scale,
                        fit: BoxFit.contain,
                        errorBuilder: (context, error, stackTrace) {
                          // 如果图片加载失败，显示备用 logo
                          return XWalletLogo(size: 60 * scale);
                        },
                      ),
                    ),
                    // 登录卡片 - 设计稿: 350x, cornerRadius 24, padding 32x24, gap 24
                    _LoginCard(
                      scale: scale,
                      formKey: _formKey,
                      emailController: _emailController,
                      passwordController: _passwordController,
                      obscurePassword: _obscurePassword,
                      isLoading: _isLoading,
                      onTogglePassword: () =>
                          setState(() => _obscurePassword = !_obscurePassword),
                      onLogin: _handleLogin,
                      onForgotPassword: () {
                        // TODO: 实现忘记密码功能
                      },
                      onRegister: () {
                        Navigator.of(context).push(
                          PageRouteBuilder(
                            settings: const RouteSettings(
                              name: AppRoutes.register,
                            ),
                            pageBuilder:
                                (context, animation, secondaryAnimation) =>
                                    const RegisterScreen(),
                            transitionsBuilder:
                                (
                                  context,
                                  animation,
                                  secondaryAnimation,
                                  child,
                                ) => FadeTransition(
                                  opacity: animation,
                                  child: child,
                                ),
                            transitionDuration: const Duration(
                              milliseconds: 300,
                            ),
                          ),
                        );
                      },
                    ),
                  ],
                ),
            ),
          ),
        ),
      ),
    );
  }
}

/// 登录卡片 - 设计稿 loginCard
class _LoginCard extends StatelessWidget {
  final double scale;
  final GlobalKey<FormState> formKey;
  final TextEditingController emailController;
  final TextEditingController passwordController;
  final bool obscurePassword;
  final bool isLoading;
  final VoidCallback onTogglePassword;
  final VoidCallback onLogin;
  final VoidCallback onForgotPassword;
  final VoidCallback onRegister;

  const _LoginCard({
    required this.scale,
    required this.formKey,
    required this.emailController,
    required this.passwordController,
    required this.obscurePassword,
    required this.isLoading,
    required this.onTogglePassword,
    required this.onLogin,
    required this.onForgotPassword,
    required this.onRegister,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(24 * scale),
        gradient: const LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [Colors.white, _kCardGradientEnd],
        ),
        boxShadow: [
          BoxShadow(
            color: _kPrimaryPurple.withValues(alpha: 0.15),
            blurRadius: 32 * scale,
            offset: Offset(0, 16 * scale),
          ),
        ],
      ),
      padding: EdgeInsets.symmetric(
        horizontal: 32 * scale,
        vertical: 24 * scale,
      ),
      child: Form(
        key: formKey,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          mainAxisSize: MainAxisSize.min,
          children: [
            // 标题 - 设计稿: 24px, bold, #1A1A1A, 居中
            Text(
              '欢迎来到 X Wallet',
              textAlign: TextAlign.center,
              style: TextStyle(
                fontSize: 24 * scale,
                fontWeight: FontWeight.bold,
                color: _kTextPrimary,
              ),
            ),
            SizedBox(height: 24 * scale),
            // 邮箱输入 - 设计稿: 48px 高, 12px 圆角, #F8F9FA
            TextFormField(
              controller: emailController,
              enabled: !isLoading,
              keyboardType: TextInputType.emailAddress,
              textInputAction: TextInputAction.next,
              style: TextStyle(fontSize: 16 * scale, color: _kTextPrimary),
              decoration: InputDecoration(
                hintText: '请输入邮箱地址',
                prefixIcon: const Icon(
                  Icons.mail_outlined,
                  color: _kPrimaryPurple,
                  size: 20,
                ),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(12 * scale),
                  borderSide: BorderSide.none,
                ),
                filled: true,
                fillColor: _kInputBg,
                contentPadding: EdgeInsets.symmetric(
                  horizontal: 16 * scale,
                  vertical: 14 * scale,
                ),
              ),
              validator: (value) {
                if (value == null || value.trim().isEmpty) return '请输入邮箱';
                final emailRegex = RegExp(
                  r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$',
                );
                if (!emailRegex.hasMatch(value)) return '请输入有效的邮箱地址';
                return null;
              },
            ),
            SizedBox(height: 24 * scale),
            // 密码输入
            TextFormField(
              controller: passwordController,
              enabled: !isLoading,
              obscureText: obscurePassword,
              textInputAction: TextInputAction.done,
              onFieldSubmitted: (_) => onLogin(),
              style: TextStyle(fontSize: 16 * scale, color: _kTextPrimary),
              decoration: InputDecoration(
                hintText: '请输入密码',
                prefixIcon: const Icon(
                  Icons.lock_outlined,
                  color: _kPrimaryPurple,
                  size: 20,
                ),
                suffixIcon: AnalyticsIconButton(
                  icon: Icon(
                    obscurePassword
                        ? Icons.visibility_outlined
                        : Icons.visibility,
                    color: _kTextSecondary,
                    size: 20,
                  ),
                  tooltip: obscurePassword ? '显示密码' : '隐藏密码',
                  eventType: AnalyticsEventType.buttonClick,
                  properties: AnalyticsEventProperties.click(
                    page: AnalyticsPages.login,
                    flow: AnalyticsFlows.login,
                    elementId: AnalyticsIds.loginPasswordVisibility,
                    elementType: AnalyticsElementType.icon,
                    elementText: obscurePassword ? '显示密码' : '隐藏密码',
                  ),
                  category: EventCategory.behavior,
                  onPressed: isLoading ? null : onTogglePassword,
                ),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(12 * scale),
                  borderSide: BorderSide.none,
                ),
                filled: true,
                fillColor: _kInputBg,
                contentPadding: EdgeInsets.symmetric(
                  horizontal: 16 * scale,
                  vertical: 14 * scale,
                ),
              ),
              validator: (value) {
                if (value == null || value.isEmpty) return '请输入密码';
                if (value.length < 6) return '密码长度至少6位';
                return null;
              },
            ),
            SizedBox(height: 24 * scale),
            // 忘记密码 - 设计稿: 右对齐, 14px, #7424F5
            Align(
              alignment: Alignment.centerRight,
              child: AnalyticsTextButton(
                onPressed: isLoading ? null : onForgotPassword,
                style: TextButton.styleFrom(
                  foregroundColor: _kPrimaryPurple,
                  padding: EdgeInsets.zero,
                  minimumSize: Size.zero,
                  tapTargetSize: MaterialTapTargetSize.shrinkWrap,
                ),
                eventType: AnalyticsEventType.linkClick,
                properties: AnalyticsEventProperties.click(
                  page: AnalyticsPages.login,
                  flow: AnalyticsFlows.login,
                  elementId: AnalyticsIds.loginForgotPassword,
                  elementType: AnalyticsElementType.link,
                  elementText: '忘记密码？',
                ),
                category: EventCategory.behavior,
                child: Text('忘记密码？', style: TextStyle(fontSize: 14 * scale)),
              ),
            ),
            SizedBox(height: 24 * scale),
            // 登录按钮 - 设计稿: 52px 高, 12px 圆角, #7424F5
            SizedBox(
              width: double.infinity,
              height: 52 * scale,
              child: AnalyticsElevatedButton(
                onPressed: isLoading ? null : onLogin,
                style: ElevatedButton.styleFrom(
                  backgroundColor: _kPrimaryPurple,
                  foregroundColor: Colors.white,
                  disabledBackgroundColor: _kPrimaryPurple.withValues(
                    alpha: 0.5,
                  ),
                  elevation: 0,
                  shadowColor: Colors.transparent,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(12 * scale),
                  ),
                ),
                eventType: AnalyticsEventType.buttonClick,
                properties: AnalyticsEventProperties.click(
                  page: AnalyticsPages.login,
                  flow: AnalyticsFlows.login,
                  elementId: AnalyticsIds.loginSubmit,
                  elementType: AnalyticsElementType.button,
                  elementText: '登录',
                ),
                category: EventCategory.behavior,
                child: isLoading
                    ? SizedBox(
                        height: 20 * scale,
                        width: 20 * scale,
                        child: CircularProgressIndicator(
                          strokeWidth: 2,
                          valueColor: const AlwaysStoppedAnimation<Color>(
                            Colors.white,
                          ),
                        ),
                      )
                    : Text(
                        '登录',
                        style: TextStyle(
                          fontSize: 16 * scale,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
              ),
            ),
            SizedBox(height: 24 * scale),
            // 分隔线 - 设计稿: #E0E0E0
            Row(
              children: [
                Expanded(child: Container(height: 1, color: _kDividerColor)),
                Padding(
                  padding: EdgeInsets.symmetric(horizontal: 16 * scale),
                  child: Text(
                    '或',
                    style: TextStyle(
                      color: _kTextSecondary,
                      fontSize: 14 * scale,
                    ),
                  ),
                ),
                Expanded(child: Container(height: 1, color: _kDividerColor)),
              ],
            ),
            SizedBox(height: 24 * scale),
            // 注册链接 - 设计稿: "还没有账号？" #666666, "立即注册" #7424F5 bold
            AnalyticsTextButton(
              onPressed: isLoading ? null : onRegister,
              style: TextButton.styleFrom(
                foregroundColor: _kPrimaryPurple,
                padding: EdgeInsets.zero,
                minimumSize: Size.zero,
                tapTargetSize: MaterialTapTargetSize.shrinkWrap,
              ),
              eventType: AnalyticsEventType.linkClick,
              properties: AnalyticsEventProperties.click(
                page: AnalyticsPages.login,
                flow: AnalyticsFlows.login,
                elementId: AnalyticsIds.loginGoRegister,
                elementType: AnalyticsElementType.link,
                elementText: '立即注册',
              ),
              category: EventCategory.behavior,
              child: Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text(
                    '还没有账号？',
                    style: TextStyle(
                      fontSize: 14 * scale,
                      color: _kTextSecondary,
                    ),
                  ),
                  SizedBox(width: 4 * scale),
                  Text(
                    '立即注册',
                    style: TextStyle(
                      fontSize: 14 * scale,
                      fontWeight: FontWeight.w600,
                      color: _kPrimaryPurple,
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
}
