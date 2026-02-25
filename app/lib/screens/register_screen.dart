import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../analytics/analytics_constants.dart';
import '../analytics/event_spec.dart';
import '../models/analytics_event.dart';
import '../providers/auth_provider.dart';
import '../services/analytics_service.dart';
import '../utils/validators.dart';
import '../widgets/analytics/analytics_elevated_button.dart';
import '../widgets/analytics/analytics_icon_button.dart';
import '../widgets/analytics/analytics_text_button.dart';
import '../widgets/x_wallet_logo.dart';

// 设计稿颜色常量（与 login_screen.dart 一致）
const Color _kBgColor = Color(0xFFD4CCF5);
const Color _kPrimaryPurple = Color(0xFF7424F5);
const Color _kTextPrimary = Color(0xFF1A1A1A);
const Color _kTextSecondary = Color(0xFF666666);
const Color _kInputBg = Color(0xFFF8F9FA);
const Color _kDividerColor = Color(0xFFE0E0E0);
const Color _kCardGradientEnd = Color(0xFFFAF8FF);

/// 用户注册页面
class RegisterScreen extends StatefulWidget {
  const RegisterScreen({super.key});

  @override
  State<RegisterScreen> createState() => _RegisterScreenState();
}

class _RegisterScreenState extends State<RegisterScreen> {
  final _formKey = GlobalKey<FormState>();
  final _emailController = TextEditingController();
  final _passwordController = TextEditingController();
  final _confirmPasswordController = TextEditingController();
  final _codeController = TextEditingController();
  final _nicknameController = TextEditingController();

  bool _obscurePassword = true;
  bool _obscureConfirmPassword = true;
  int _countdown = 0; // 倒计时秒数

  @override
  void dispose() {
    _emailController.dispose();
    _passwordController.dispose();
    _confirmPasswordController.dispose();
    _codeController.dispose();
    _nicknameController.dispose();
    super.dispose();
  }

  /// 发送验证码
  Future<void> _handleSendCode() async {
    // 先验证邮箱
    final emailError = Validators.validateEmail(_emailController.text);
    if (emailError != null) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(emailError), backgroundColor: Colors.red),
        );
      }
      return;
    }

    final authProvider = context.read<AuthProvider>();
    final success = await authProvider.sendVerificationCode(
      _emailController.text.trim(),
    );

    if (!success && mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(authProvider.errorMessage ?? '发送失败'),
          backgroundColor: Colors.red,
        ),
      );
    } else if (mounted) {
      // 开始倒计时
      setState(() => _countdown = 60);
      _startCountdown();
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('验证码已发送到 ${_emailController.text}'),
          backgroundColor: Colors.green,
        ),
      );
    }
  }

  /// 倒计时
  void _startCountdown() {
    Future.delayed(const Duration(seconds: 1), () {
      if (_countdown > 0 && mounted) {
        setState(() => _countdown--);
        _startCountdown();
      }
    });
  }

  /// 注册
  Future<void> _handleRegister() async {
    if (!_formKey.currentState!.validate()) {
      return;
    }

    final authProvider = context.read<AuthProvider>();
    final success = await authProvider.register(
      email: _emailController.text.trim(),
      password: _passwordController.text,
      verificationCode: _codeController.text.trim(),
      nickname: _nicknameController.text.trim().isEmpty
          ? null
          : _nicknameController.text.trim(),
    );

    await AnalyticsService.instance.trackStandardEvent(
      eventType: AnalyticsEventType.formSubmit,
      properties: AnalyticsEventProperties.formSubmit(
        page: AnalyticsPages.register,
        flow: AnalyticsFlows.register,
        elementId: AnalyticsIds.registerSubmit,
        success: success,
        extra: {'registerMethod': 'email'},
      ),
      userId: success
          ? authProvider.currentUser?.userInfo.userId.toString()
          : null,
      category: success ? EventCategory.critical : EventCategory.behavior,
    );

    if (!success && mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(authProvider.errorMessage ?? '注册失败'),
          backgroundColor: Colors.red,
        ),
      );
    }
    // 如果成功，AuthProvider 会自动更新状态，Main.dart 会路由到 HomeScreen
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        leading: AnalyticsIconButton(
          icon: const Icon(Icons.arrow_back, color: Color(0xFF1A1A1A)),
          tooltip: '返回登录',
          eventType: AnalyticsEventType.linkClick,
          properties: AnalyticsEventProperties.click(
            page: AnalyticsPages.register,
            flow: AnalyticsFlows.register,
            elementId: AnalyticsIds.registerBackNav,
            elementType: AnalyticsElementType.icon,
            elementText: '返回登录',
          ),
          category: EventCategory.behavior,
          onPressed: () {
            Navigator.of(context).pop();
          },
        ),
        title: const Text(
          '注册 X Wallet 账号',
          style: TextStyle(
            color: Color(0xFF1A1A1A),
            fontSize: 18,
            fontWeight: FontWeight.w600,
          ),
        ),
        backgroundColor: Colors.white,
        foregroundColor: Color(0xFF1A1A1A),
        elevation: 0,
      ),
      body: Container(
        decoration: BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [_kBgColor, Colors.white],
          ),
        ),
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.symmetric(horizontal: 32.0),
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 400),
              child: Card(
                elevation: 8,
                shadowColor: Colors.black26,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(16),
                ),
                child: Padding(
                  padding: const EdgeInsets.all(32.0),
                  child: Form(
                    key: _formKey,
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.stretch,
                      children: [
                        // X Wallet Logo（与 www.xwallet.hk 品牌一致）- 靠上以给表单更多空间
                        const XWalletLogo(size: 56),
                        const SizedBox(height: 16),
                        const Text(
                          '创建新账号',
                          textAlign: TextAlign.center,
                          style: TextStyle(
                            fontSize: 24,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        const SizedBox(height: 20),

                        // 邮箱输入框
                        TextFormField(
                          controller: _emailController,
                          decoration: InputDecoration(
                            labelText: '邮箱',
                            hintText: '请输入邮箱',
                            prefixIcon: const Icon(Icons.email),
                            border: OutlineInputBorder(
                              borderRadius: BorderRadius.circular(12),
                            ),
                            filled: true,
                            fillColor: Colors.grey.shade50,
                          ),
                          keyboardType: TextInputType.emailAddress,
                          textInputAction: TextInputAction.next,
                          validator: Validators.validateEmail,
                        ),
                        const SizedBox(height: 16),

                        // 验证码输入框
                        Row(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Expanded(
                              child: TextFormField(
                                controller: _codeController,
                                decoration: InputDecoration(
                                  labelText: '验证码',
                                  hintText: '6位数字',
                                  prefixIcon: const Icon(Icons.verified),
                                  border: OutlineInputBorder(
                                    borderRadius: BorderRadius.circular(12),
                                  ),
                                  filled: true,
                                  fillColor: Colors.grey.shade50,
                                ),
                                keyboardType: TextInputType.number,
                                maxLength: 6,
                                textInputAction: TextInputAction.next,
                                validator: Validators.validateVerificationCode,
                              ),
                            ),
                            const SizedBox(width: 12),
                            SizedBox(
                              width: 120,
                              child: AnalyticsElevatedButton(
                                onPressed: (_countdown > 0)
                                    ? null
                                    : _handleSendCode,
                                style: ElevatedButton.styleFrom(
                                  padding: const EdgeInsets.symmetric(
                                    vertical: 16,
                                  ),
                                  shape: RoundedRectangleBorder(
                                    borderRadius: BorderRadius.circular(12),
                                  ),
                                  backgroundColor: Colors.green.shade700,
                                ),
                                eventType: AnalyticsEventType.buttonClick,
                                properties: AnalyticsEventProperties.click(
                                  page: AnalyticsPages.register,
                                  flow: AnalyticsFlows.register,
                                  elementId: AnalyticsIds.registerSendCode,
                                  elementType: AnalyticsElementType.button,
                                  elementText: '发送验证码',
                                ),
                                category: EventCategory.behavior,
                                child: Text(
                                  _countdown > 0 ? '${_countdown}s' : '发送验证码',
                                  style: const TextStyle(fontSize: 12),
                                ),
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 16),

                        // 密码输入框
                        TextFormField(
                          controller: _passwordController,
                          decoration: InputDecoration(
                            labelText: '密码',
                            hintText: '至少6位',
                            prefixIcon: const Icon(Icons.lock),
                            suffixIcon: AnalyticsIconButton(
                              icon: Icon(
                                _obscurePassword
                                    ? Icons.visibility_off
                                    : Icons.visibility,
                              ),
                              tooltip: _obscurePassword ? '显示密码' : '隐藏密码',
                              eventType: AnalyticsEventType.buttonClick,
                              properties: AnalyticsEventProperties.click(
                                page: AnalyticsPages.register,
                                flow: AnalyticsFlows.register,
                                elementId: AnalyticsIds.registerPasswordVisibility,
                                elementType: AnalyticsElementType.icon,
                                elementText: _obscurePassword ? '显示密码' : '隐藏密码',
                              ),
                              category: EventCategory.behavior,
                              onPressed: () {
                                setState(() {
                                  _obscurePassword = !_obscurePassword;
                                });
                              },
                            ),
                            border: OutlineInputBorder(
                              borderRadius: BorderRadius.circular(12),
                            ),
                            filled: true,
                            fillColor: Colors.grey.shade50,
                          ),
                          obscureText: _obscurePassword,
                          textInputAction: TextInputAction.next,
                          validator: Validators.validatePassword,
                        ),
                        const SizedBox(height: 16),

                        // 确认密码输入框
                        TextFormField(
                          controller: _confirmPasswordController,
                          decoration: InputDecoration(
                            labelText: '确认密码',
                            hintText: '再次输入密码',
                            prefixIcon: const Icon(Icons.lock_outline),
                            suffixIcon: AnalyticsIconButton(
                              icon: Icon(
                                _obscureConfirmPassword
                                    ? Icons.visibility_off
                                    : Icons.visibility,
                              ),
                              tooltip: _obscureConfirmPassword
                                  ? '显示确认密码'
                                  : '隐藏确认密码',
                              eventType: AnalyticsEventType.buttonClick,
                              properties: AnalyticsEventProperties.click(
                                page: AnalyticsPages.register,
                                flow: AnalyticsFlows.register,
                                elementId:
                                    AnalyticsIds.registerConfirmPasswordVisibility,
                                elementType: AnalyticsElementType.icon,
                                elementText: _obscureConfirmPassword
                                    ? '显示确认密码'
                                    : '隐藏确认密码',
                              ),
                              category: EventCategory.behavior,
                              onPressed: () {
                                setState(() {
                                  _obscureConfirmPassword =
                                      !_obscureConfirmPassword;
                                });
                              },
                            ),
                            border: OutlineInputBorder(
                              borderRadius: BorderRadius.circular(12),
                            ),
                            filled: true,
                            fillColor: Colors.grey.shade50,
                          ),
                          obscureText: _obscureConfirmPassword,
                          textInputAction: TextInputAction.next,
                          validator: (value) =>
                              Validators.validateConfirmPassword(
                                value,
                                _passwordController.text,
                              ),
                        ),
                        const SizedBox(height: 16),

                        // 昵称输入框（可选）
                        TextFormField(
                          controller: _nicknameController,
                          decoration: InputDecoration(
                            labelText: '昵称 (可选)',
                            hintText: '请输入昵称',
                            prefixIcon: const Icon(Icons.person),
                            border: OutlineInputBorder(
                              borderRadius: BorderRadius.circular(12),
                            ),
                            filled: true,
                            fillColor: Colors.grey.shade50,
                          ),
                          textInputAction: TextInputAction.done,
                          onFieldSubmitted: (_) => _handleRegister(),
                        ),
                        const SizedBox(height: 24),

                        // 注册按钮
                        Consumer<AuthProvider>(
                          builder: (context, authProvider, child) {
                            final isRegistering = authProvider.isLoggingIn;
                            return AnalyticsElevatedButton(
                              onPressed: isRegistering ? null : _handleRegister,
                              style: ElevatedButton.styleFrom(
                                padding: const EdgeInsets.symmetric(
                                  vertical: 16,
                                ),
                                shape: RoundedRectangleBorder(
                                  borderRadius: BorderRadius.circular(12),
                                ),
                                backgroundColor: Colors.green.shade700,
                                foregroundColor: Colors.white,
                              ),
                              eventType: AnalyticsEventType.buttonClick,
                              properties: AnalyticsEventProperties.click(
                                page: AnalyticsPages.register,
                                flow: AnalyticsFlows.register,
                                elementId: AnalyticsIds.registerSubmit,
                                elementType: AnalyticsElementType.button,
                                elementText: '注册',
                              ),
                              category: EventCategory.behavior,
                              child: isRegistering
                                  ? const SizedBox(
                                      height: 20,
                                      width: 20,
                                      child: CircularProgressIndicator(
                                        strokeWidth: 2,
                                        valueColor:
                                            AlwaysStoppedAnimation<Color>(
                                              Colors.white,
                                            ),
                                      ),
                                    )
                                  : const Text(
                                      '注册',
                                      style: TextStyle(
                                        fontSize: 18,
                                        fontWeight: FontWeight.bold,
                                      ),
                                    ),
                            );
                          },
                        ),
                        const SizedBox(height: 16),

                        // 返回登录链接
                        AnalyticsTextButton(
                          onPressed: () {
                            Navigator.of(context).pop();
                          },
                          eventType: AnalyticsEventType.linkClick,
                          properties: AnalyticsEventProperties.click(
                            page: AnalyticsPages.register,
                            flow: AnalyticsFlows.register,
                            elementId: AnalyticsIds.registerBackToLogin,
                            elementType: AnalyticsElementType.link,
                            elementText: '已有账号？返回登录',
                          ),
                          category: EventCategory.behavior,
                          child: Text(
                            '已有账号？返回登录',
                            style: TextStyle(
                              color: Colors.green.shade700,
                              fontWeight: FontWeight.w500,
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
