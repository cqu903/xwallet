# App 注册页面 UI 完善 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 将注册页面从绿色主题更新为紫色主题，与登录页面保持视觉一致性

**Architecture:** 直接修改 `app/lib/screens/register_screen.dart`，参考 `login_screen.dart` 的设计模式，更新颜色常量、卡片样式、输入框样式、按钮样式等，同时保留现有验证码注册功能和 Analytics 埋点。

**Tech Stack:** Flutter, Provider, Material Design

---

## Task 1: 更新颜色常量和页面背景

**Files:**
- Modify: `app/lib/screens/register_screen.dart:1-300`

**Step 1: 添加与登录页一致的颜色常量**

在文件顶部的 `_RegisterScreenState` 类之前添加颜色常量：

```dart
// 设计稿颜色常量（与 login_screen.dart 一致）
const Color _kBgColor = Color(0xFFD4CCF5);
const Color _kPrimaryPurple = Color(0xFF7424F5);
const Color _kTextPrimary = Color(0xFF1A1A1A);
const Color _kTextSecondary = Color(0xFF666666);
const Color _kInputBg = Color(0xFFF8F9FA);
const Color _kDividerColor = Color(0xFFE0E0E0);
const Color _kCardGradientEnd = Color(0xFFFAF8FF);
```

**Step 2: 更新 AppBar 样式**

将 `AppBar` 的 `backgroundColor` 从 `Colors.green.shade700` 改为 `Colors.white`，更新 `foregroundColor`：

```dart
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
```

**Step 3: 更新页面背景渐变**

将 `body` 中的 `Container` 背景渐变从绿色改为淡紫色：

```dart
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
      // ... 其余内容保持不变
    ),
  ),
),
```

**Step 4: 测试运行**

Run: `cd app && flutter run lib/screens/register_screen.dart`
Expected: 页面背景显示为淡紫色渐变，AppBar 为白色

**Step 5: Commit**

```bash
git add app/lib/screens/register_screen.dart
git commit -m "style(register): update color constants and page background to purple theme"
```

---

## Task 2: 更新卡片样式

**Files:**
- Modify: `app/lib/screens/register_screen.dart:100-200`

**Step 1: 更新 Card 样式**

将现有的 `Card` 替换为带渐变和紫色阴影的 `Container`：

```dart
child: SingleChildScrollView(
  padding: const EdgeInsets.symmetric(horizontal: 26.0),
  child: ConstrainedBox(
    constraints: const BoxConstraints(maxWidth: 400),
    child: Container(
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(24),
        gradient: const LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [Colors.white, _kCardGradientEnd],
        ),
        boxShadow: [
          BoxShadow(
            color: _kPrimaryPurple.withValues(alpha: 0.15),
            blurRadius: 32,
            offset: const Offset(0, 16),
          ),
        ],
      ),
      padding: const EdgeInsets.symmetric(horizontal: 32.0, vertical: 24.0),
      child: Form(
        key: _formKey,
        child: Column(
          // ... 表单内容
        ),
      ),
    ),
  ),
),
```

**Step 2: 测试运行**

Run: `cd app && flutter run lib/screens/register_screen.dart`
Expected: 卡片显示白色到淡紫渐变，带紫色阴影

**Step 3: Commit**

```bash
git add app/lib/screens/register_screen.dart
git commit -m "style(register): update card style with gradient and purple shadow"
```

---

## Task 3: 更新输入框样式

**Files:**
- Modify: `app/lib/screens/register_screen.dart:130-250`

**Step 1: 更新邮箱输入框样式**

```dart
// 邮箱输入框
TextFormField(
  controller: _emailController,
  decoration: InputDecoration(
    labelText: '邮箱',
    hintText: '请输入邮箱地址',
    prefixIcon: const Icon(Icons.mail_outlined, color: _kPrimaryPurple),
    border: OutlineInputBorder(
      borderRadius: BorderRadius.circular(12),
      borderSide: BorderSide.none,
    ),
    filled: true,
    fillColor: _kInputBg,
    contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
  ),
  keyboardType: TextInputType.emailAddress,
  textInputAction: TextInputAction.next,
  style: const TextStyle(fontSize: 16, color: _kTextPrimary),
  validator: Validators.validateEmail,
),
```

**Step 2: 更新验证码输入框和按钮样式**

```dart
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
          prefixIcon: const Icon(Icons.verified_outlined, color: _kPrimaryPurple),
          border: OutlineInputBorder(
            borderRadius: BorderRadius.circular(12),
            borderSide: BorderSide.none,
          ),
          filled: true,
          fillColor: _kInputBg,
          contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
        ),
        keyboardType: TextInputType.number,
        maxLength: 6,
        textInputAction: TextInputAction.next,
        style: const TextStyle(fontSize: 16, color: _kTextPrimary),
        validator: Validators.validateVerificationCode,
      ),
    ),
    const SizedBox(width: 12),
    SizedBox(
      width: 120,
      height: 52,
      child: AnalyticsElevatedButton(
        onPressed: (_countdown > 0) ? null : _handleSendCode,
        style: ElevatedButton.styleFrom(
          backgroundColor: _kPrimaryPurple,
          foregroundColor: Colors.white,
          disabledBackgroundColor: _kPrimaryPurple.withValues(alpha: 0.5),
          elevation: 0,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(12),
          ),
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
```

**Step 3: 更新密码输入框样式**

```dart
// 密码输入框
TextFormField(
  controller: _passwordController,
  decoration: InputDecoration(
    labelText: '密码',
    hintText: '至少6位',
    prefixIcon: const Icon(Icons.lock_outlined, color: _kPrimaryPurple),
    suffixIcon: AnalyticsIconButton(
      icon: Icon(
        _obscurePassword ? Icons.visibility_outlined : Icons.visibility,
        color: _kTextSecondary,
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
        setState(() => _obscurePassword = !_obscurePassword);
      },
    ),
    border: OutlineInputBorder(
      borderRadius: BorderRadius.circular(12),
      borderSide: BorderSide.none,
    ),
    filled: true,
    fillColor: _kInputBg,
    contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
  ),
  obscureText: _obscurePassword,
  textInputAction: TextInputAction.next,
  style: const TextStyle(fontSize: 16, color: _kTextPrimary),
  validator: Validators.validatePassword,
),
```

**Step 4: 更新确认密码输入框样式**

```dart
// 确认密码输入框
TextFormField(
  controller: _confirmPasswordController,
  decoration: InputDecoration(
    labelText: '确认密码',
    hintText: '再次输入密码',
    prefixIcon: const Icon(Icons.lock_outline, color: _kPrimaryPurple),
    suffixIcon: AnalyticsIconButton(
      icon: Icon(
        _obscureConfirmPassword ? Icons.visibility_outlined : Icons.visibility,
        color: _kTextSecondary,
      ),
      tooltip: _obscureConfirmPassword ? '显示确认密码' : '隐藏确认密码',
      eventType: AnalyticsEventType.buttonClick,
      properties: AnalyticsEventProperties.click(
        page: AnalyticsPages.register,
        flow: AnalyticsFlows.register,
        elementId: AnalyticsIds.registerConfirmPasswordVisibility,
        elementType: AnalyticsElementType.icon,
        elementText: _obscureConfirmPassword ? '显示确认密码' : '隐藏确认密码',
      ),
      category: EventCategory.behavior,
      onPressed: () {
        setState(() => _obscureConfirmPassword = !_obscureConfirmPassword);
      },
    ),
    border: OutlineInputBorder(
      borderRadius: BorderRadius.circular(12),
      borderSide: BorderSide.none,
    ),
    filled: true,
    fillColor: _kInputBg,
    contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
  ),
  obscureText: _obscureConfirmPassword,
  textInputAction: TextInputAction.next,
  style: const TextStyle(fontSize: 16, color: _kTextPrimary),
  validator: (value) => Validators.validateConfirmPassword(
    value,
    _passwordController.text,
  ),
),
```

**Step 5: 更新昵称输入框样式**

```dart
// 昵称输入框（可选）
TextFormField(
  controller: _nicknameController,
  decoration: InputDecoration(
    labelText: '昵称 (可选)',
    hintText: '请输入昵称',
    prefixIcon: const Icon(Icons.person_outline, color: _kPrimaryPurple),
    border: OutlineInputBorder(
      borderRadius: BorderRadius.circular(12),
      borderSide: BorderSide.none,
    ),
    filled: true,
    fillColor: _kInputBg,
    contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
  ),
  textInputAction: TextInputAction.done,
  style: const TextStyle(fontSize: 16, color: _kTextPrimary),
  onFieldSubmitted: (_) => _handleRegister(),
),
```

**Step 6: 测试运行**

Run: `cd app && flutter run lib/screens/register_screen.dart`
Expected: 所有输入框显示为浅灰色背景，聚焦时紫色边框

**Step 7: Commit**

```bash
git add app/lib/screens/register_screen.dart
git commit -m "style(register): update input field styles to purple theme"
```

---

## Task 4: 更新按钮样式

**Files:**
- Modify: `app/lib/screens/register_screen.dart:230-280`

**Step 1: 更新注册按钮样式**

```dart
// 注册按钮
Consumer<AuthProvider>(
  builder: (context, authProvider, child) {
    final isRegistering = authProvider.isLoggingIn;
    return SizedBox(
      width: double.infinity,
      height: 52,
      child: AnalyticsElevatedButton(
        onPressed: isRegistering ? null : _handleRegister,
        style: ElevatedButton.styleFrom(
          backgroundColor: _kPrimaryPurple,
          foregroundColor: Colors.white,
          disabledBackgroundColor: _kPrimaryPurple.withValues(alpha: 0.5),
          elevation: 0,
          shadowColor: Colors.transparent,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(12),
          ),
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
                  valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
                ),
              )
            : const Text(
                '注册',
                style: TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.w600,
                ),
              ),
      ),
    );
  },
),
```

**Step 2: 更新返回登录链接样式**

```dart
// 返回登录链接
AnalyticsTextButton(
  onPressed: () {
    Navigator.of(context).pop();
  },
  style: TextButton.styleFrom(
    foregroundColor: _kPrimaryPurple,
    padding: EdgeInsets.zero,
    minimumSize: Size.zero,
    tapTargetSize: MaterialTapTargetSize.shrinkWrap,
  ),
  eventType: AnalyticsEventType.linkClick,
  properties: AnalyticsEventProperties.click(
    page: AnalyticsPages.register,
    flow: AnalyticsFlows.register,
    elementId: AnalyticsIds.registerBackToLogin,
    elementType: AnalyticsElementType.link,
    elementText: '已有账号？返回登录',
  ),
  category: EventCategory.behavior,
  child: Row(
    mainAxisSize: MainAxisSize.min,
    children: [
      Text(
        '已有账号？',
        style: TextStyle(
          fontSize: 14,
          color: _kTextSecondary,
        ),
      ),
      const SizedBox(width: 4),
      const Text(
        '返回登录',
        style: TextStyle(
          fontSize: 14,
          fontWeight: FontWeight.w600,
          color: _kPrimaryPurple,
        ),
      ),
    ],
  ),
),
```

**Step 3: 测试运行**

Run: `cd app && flutter run lib/screens/register_screen.dart`
Expected: 按钮为紫色，返回登录链接紫色高亮

**Step 4: Commit**

```bash
git add app/lib/screens/register_screen.dart
git commit -m "style(register): update button styles to purple theme"
```

---

## Task 5: 更新成功/错误提示样式

**Files:**
- Modify: `app/lib/screens/register_screen.dart:50-100`

**Step 1: 更新验证码发送成功提示**

```dart
// 在 _handleSendCode 方法中
if (success && mounted) {
  setState(() => _countdown = 60);
  _startCountdown();
  ScaffoldMessenger.of(context).showSnackBar(
    SnackBar(
      content: Row(
        children: [
          const Icon(Icons.check_circle, color: Colors.white),
          const SizedBox(width: 12),
          Expanded(child: Text('验证码已发送到 ${_emailController.text}')),
        ],
      ),
      backgroundColor: const Color(0xFF22C55E),
      behavior: SnackBarBehavior.floating,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(8),
      ),
    ),
  );
}
```

**Step 2: 更新错误提示样式**

```dart
// 在 _handleSendCode 方法中
if (!success && mounted) {
  ScaffoldMessenger.of(context).showSnackBar(
    SnackBar(
      content: Row(
        children: [
          const Icon(Icons.error_outline, color: Colors.white),
          const SizedBox(width: 12),
          Expanded(child: Text(authProvider.errorMessage ?? '发送失败')),
        ],
      ),
      backgroundColor: const Color(0xFFF44336),
      behavior: SnackBarBehavior.floating,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(8),
      ),
    ),
  );
}
```

**Step 3: 测试运行**

Run: `cd app && flutter run lib/screens/register_screen.dart`
Expected: SnackBar 显示为绿色（成功）或红色（错误）

**Step 4: Commit**

```bash
git add app/lib/screens/register_screen.dart
git commit -m "style(register): update snackbar styles for consistency"
```

---

## Task 6: 更新标题和 Logo 区域样式

**Files:**
- Modify: `app/lib/screens/register_screen.dart:110-130`

**Step 1: 更新标题样式**

```dart
// Logo 和标题
const XWalletLogo(size: 56),
const SizedBox(height: 16),
const Text(
  '创建新账号',
  textAlign: TextAlign.center,
  style: TextStyle(
    fontSize: 24,
    fontWeight: FontWeight.bold,
    color: _kTextPrimary,
  ),
),
const SizedBox(height: 24),
```

**Step 2: 测试运行**

Run: `cd app && flutter run lib/screens/register_screen.dart`
Expected: 标题显示正确，颜色为深灰色

**Step 3: Commit**

```bash
git add app/lib/screens/register_screen.dart
git commit -m "style(register): update title and logo styles"
```

---

## Task 7: 最终测试和验证

**Files:**
- Test: `app/test/screens/register_screen_test.dart` (如存在)

**Step 1: 运行所有测试**

Run: `cd app && flutter test`
Expected: 所有测试通过

**Step 2: 运行代码分析**

Run: `cd app && flutter analyze`
Expected: 无警告和错误

**Step 3: 手动测试注册流程**

1. 启动应用: `cd app && flutter run`
2. 导航到注册页面
3. 验证以下内容：
   - [ ] 页面背景为淡紫色渐变
   - [ ] 卡片显示白色到淡紫渐变
   - [ ] 输入框背景为浅灰色 `#F8F9FA`
   - [ ] 按钮为紫色 `#7424F5`
   - [ ] 图标为紫色
   - [ ] 验证码倒计时正常工作
   - [ ] 密码可见性切换正常
   - [ ] 表单验证正常
   - [ ] 成功/错误提示显示正确

**Step 4: 对比登录页面**

Run: `cd app && flutter run`
Expected: 注册和登录页面视觉风格一致

**Step 5: 最终 Commit**

```bash
git add app/lib/screens/register_screen.dart docs/plans/2026-02-25-*.md
git commit -m "feat(register): complete purple theme update for register screen"
```

---

## 参考文件

- `app/DESIGN_GUIDE.md` - 设计规范
- `app/lib/screens/login_screen.dart` - 登录页面参考
- `app/lib/utils/validators.dart` - 验证器工具
- `app/lib/providers/auth_provider.dart` - 认证状态管理
