# App 注册页面 UI 完善设计

**日期**: 2026-02-25
**作者**: Claude
**状态**: 设计阶段

---

## 1. 概述

### 1.1 背景

当前注册页面 (`app/lib/screens/register_screen.dart`) 使用绿色主题，与设计指南 (`app/DESIGN_GUIDE.md`) 规定的紫色主题 (`#7424F5`) 不一致。登录页面已按设计规范更新，注册页面需要同步更新以保持视觉一致性。

### 1.2 目标

- 将注册页面从绿色主题更新为紫色主题
- 与登录页面保持视觉风格一致（卡片布局、渐变背景、阴影效果）
- 保留现有验证码注册功能
- 遵循 `DESIGN_GUIDE.md` 中的设计规范

---

## 2. 当前状态分析

### 2.1 现有问题

| 问题 | 描述 |
|------|------|
| 主题色不一致 | 使用 `Colors.green.shade700` 而非规范中的 `#7424F5` |
| AppBar 风格 | 绿色 AppBar 与整体设计不协调 |
| 卡片样式 | 缺少渐变效果和紫色阴影 |
| 圆角不统一 | 使用 16px，登录页使用 24px |

### 2.2 现有功能（保留）

- 邮箱验证码发送（60秒倒计时）
- 密码可见性切换
- 表单验证（邮箱、密码、确认密码、验证码）
- Analytics 埋点事件
- 自动登录（注册成功后）

---

## 3. 设计规范

### 3.1 颜色方案

| 用途 | 颜色值 | 用途说明 |
|------|--------|----------|
| 主色 | `#7424F5` | 按钮、图标、强调元素 |
| 页面背景 | `#D4CCF5` | 淡紫色背景 |
| 卡片渐变 | `#FFFFFF` → `#FAF8FF` | 白色到淡紫 |
| 输入框背景 | `#F8F9FA` | 浅灰色 |
| 主文字 | `#1A1A1A` | 标题、正文 |
| 次要文字 | `#666666` | 说明、提示 |
| 分隔线 | `#E0E0E0` | 分隔线颜色 |

### 3.2 尺寸规范

| 元素 | 尺寸 |
|------|------|
| 卡片圆角 | 24px |
| 输入框圆角 | 12px |
| 按钮高度 | 52px |
| 按钮圆角 | 12px |
| 页面边距 | 26px |
| 卡片内边距 | 32px 水平 / 24px 垂直 |
| 元素间距 | 16px / 24px |

### 3.3 阴影规范

```dart
BoxShadow(
  color: Color(0xFF7424F5).withValues(alpha: 0.15),
  blurRadius: 32,
  offset: Offset(0, 16),
)
```

---

## 4. 页面结构设计

```
┌─────────────────────────────────────────────┐
│  ←  注册 X Wallet 账号              │  ← AppBar
├─────────────────────────────────────────────┤
│                                             │
│              [Logo - 56px]                   │
│            创建新账号                        │
│                                             │
│  ┌───────────────────────────────────────┐ │
│  │  邮箱                                  │ │
│  │  [请输入邮箱地址]              📧     │ │
│  ├───────────────────────────────────────┤ │
│  │  验证码        │ [发送验证码] 120px   │ │
│  │  [6位数字]      │                     │ │
│  ├───────────────────────────────────────┤ │
│  │  密码                          👁/👁‍   │ │
│  │  [至少6位]               🔒           │ │
│  ├───────────────────────────────────────┤ │
│  │  确认密码                      👁/👁‍   │ │
│  │  [再次输入密码]          🔒           │ │
│  ├───────────────────────────────────────┤ │
│  │  昵称 (可选)                          │ │
│  │  [请输入昵称]              👤         │ │
│  ├───────────────────────────────────────┤ │
│  │      [注册] - 紫色主按钮              │ │
│  ├───────────────────────────────────────┤ │
│  │  已有账号？返回登录                    │ │
│  └───────────────────────────────────────┘ │
│                                             │
└─────────────────────────────────────────────┘
```

---

## 5. 组件设计

### 5.1 AppBar

```dart
AppBar(
  leading: AnalyticsIconButton(...),
  title: Text('注册 X Wallet 账号'),
  backgroundColor: Colors.white,  // 或透明，跟随页面背景
  foregroundColor: Color(0xFF1A1A1A),
  elevation: 0,
)
```

### 5.2 页面背景

```dart
Container(
  decoration: BoxDecoration(
    gradient: LinearGradient(
      begin: Alignment.topCenter,
      end: Alignment.bottomCenter,
      colors: [Color(0xFFD4CCF5), Colors.white],
    ),
  ),
)
```

### 5.3 卡片样式

```dart
Container(
  decoration: BoxDecoration(
    borderRadius: BorderRadius.circular(24),
    gradient: LinearGradient(
      begin: Alignment.topLeft,
      end: Alignment.bottomRight,
      colors: [Colors.white, Color(0xFFFAF8FF)],
    ),
    boxShadow: [
      BoxShadow(
        color: Color(0xFF7424F5).withValues(alpha: 0.15),
        blurRadius: 32,
        offset: Offset(0, 16),
      ),
    ],
  ),
  padding: EdgeInsets.symmetric(horizontal: 32, vertical: 24),
  child: Form(...),
)
```

### 5.4 输入框样式

```dart
TextField(
  decoration: InputDecoration(
    hintText: '请输入邮箱地址',
    prefixIcon: Icon(Icons.mail_outlined, color: Color(0xFF7424F5)),
    border: OutlineInputBorder(
      borderRadius: BorderRadius.circular(12),
      borderSide: BorderSide.none,
    ),
    filled: true,
    fillColor: Color(0xFFF8F9FA),
    contentPadding: EdgeInsets.symmetric(horizontal: 16, vertical: 14),
  ),
)
```

### 5.5 验证码区域（并排布局）

```dart
Row(
  crossAxisAlignment: CrossAxisAlignment.start,
  children: [
    Expanded(
      child: TextField(
        controller: _codeController,
        decoration: InputDecoration(
          labelText: '验证码',
          hintText: '6位数字',
          prefixIcon: Icon(Icons.verified, color: Color(0xFF7424F5)),
          ...
        ),
        keyboardType: TextInputType.number,
        maxLength: 6,
      ),
    ),
    SizedBox(width: 12),
    SizedBox(
      width: 120,
      child: ElevatedButton(
        onPressed: _countdown > 0 ? null : _handleSendCode,
        style: ElevatedButton.styleFrom(
          backgroundColor: Color(0xFF7424F5),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(12),
          ),
        ),
        child: Text(_countdown > 0 ? '${_countdown}s' : '发送验证码'),
      ),
    ),
  ],
)
```

### 5.6 主按钮

```dart
SizedBox(
  width: double.infinity,
  height: 52,
  child: ElevatedButton(
    onPressed: _isLoading ? null : _handleRegister,
    style: ElevatedButton.styleFrom(
      backgroundColor: Color(0xFF7424F5),
      foregroundColor: Colors.white,
      disabledBackgroundColor: Color(0xFF7424F5).withValues(alpha: 0.5),
      elevation: 0,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
      ),
    ),
    child: _isLoading
        ? CircularProgressIndicator(...)
        : Text('注册', style: TextStyle(fontSize: 16, fontWeight: FontWeight.w600)),
  ),
)
```

---

## 6. 交互设计

### 6.1 状态管理

| 状态 | 表现 |
|------|------|
| 空闲 | 所有输入框和按钮可用 |
| 加载中 | 按钮显示旋转器，表单禁用 |
| 倒计时 | 验证码按钮显示秒数，禁用 |
| 错误 | 输入框显示红色边框，顶部显示错误提示 |

### 6.2 错误提示

使用 SnackBar 显示错误信息：

```dart
ScaffoldMessenger.of(context).showSnackBar(
  SnackBar(
    content: Row(
      children: [
        Icon(Icons.error_outline, color: Colors.white),
        SizedBox(width: 12),
        Expanded(child: Text(errorMessage)),
      ],
    ),
    backgroundColor: Color(0xFFF44336),
    behavior: SnackBarBehavior.floating,
    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
  ),
);
```

### 6.3 成功提示

```dart
ScaffoldMessenger.of(context).showSnackBar(
  SnackBar(
    content: Row(
      children: [
        Icon(Icons.check_circle, color: Colors.white),
        SizedBox(width: 12),
        Expanded(child: Text('验证码已发送到 $email')),
      ],
    ),
    backgroundColor: Color(0xFF22C55E),
    behavior: SnackBarBehavior.floating,
  ),
);
```

---

## 7. 响应式适配

使用 `DesignScale` 工具类进行屏幕适配（与登录页一致）：

```dart
final scale = DesignScale.getScale(context);
```

所有尺寸值乘以 `scale` 系数，确保在不同屏幕尺寸下的一致体验。

---

## 8. Analytics 埋点

保留现有埋点事件，确保数据收集不受影响：

| 事件 | Element ID |
|------|------------|
| 返回导航 | `registerBackNav` |
| 发送验证码 | `registerSendCode` |
| 密码可见性切换 | `registerPasswordVisibility` |
| 确认密码可见性 | `registerConfirmPasswordVisibility` |
| 提交注册 | `registerSubmit` |
| 返回登录 | `registerBackToLogin` |

---

## 9. 实现清单

- [ ] 更新颜色常量为紫色主题
- [ ] 更新页面背景为淡紫色渐变
- [ ] 更新卡片样式（渐变 + 圆角 + 阴影）
- [ ] 更新输入框样式
- [ ] 更新按钮样式
- [ ] 更新 AppBar 样式
- [ ] 调整间距和圆角
- [ ] 保持验证码倒计时功能
- [ ] 保持 Analytics 埋点

---

## 10. 参考资料

- `app/DESIGN_GUIDE.md` - 设计规范文档
- `app/lib/screens/login_screen.dart` - 登录页面参考实现
- `app/lib/utils/design_scale.dart` - 响应式缩放工具
