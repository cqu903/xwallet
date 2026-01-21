/// 表单验证工具类
class Validators {
  /// 邮箱验证
  static String? validateEmail(String? value) {
    if (value == null || value.trim().isEmpty) {
      return '请输入邮箱';
    }
    final emailRegex = RegExp(r'^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$');
    if (!emailRegex.hasMatch(value)) {
      return '请输入有效的邮箱地址';
    }
    return null;
  }

  /// 密码验证
  static String? validatePassword(String? value) {
    if (value == null || value.isEmpty) {
      return '请输入密码';
    }
    if (value.length < 6) {
      return '密码长度至少6位';
    }
    if (value.length > 20) {
      return '密码长度不能超过20位';
    }
    return null;
  }

  /// 验证码验证
  static String? validateVerificationCode(String? value) {
    if (value == null || value.trim().isEmpty) {
      return '请输入验证码';
    }
    if (value.length != 6) {
      return '验证码必须是6位数字';
    }
    final codeRegex = RegExp(r'^\d{6}$');
    if (!codeRegex.hasMatch(value)) {
      return '验证码必须是6位数字';
    }
    return null;
  }

  /// 确认密码验证
  static String? validateConfirmPassword(String? value, String password) {
    if (value == null || value.trim().isEmpty) {
      return '请确认密码';
    }
    if (value != password) {
      return '两次输入的密码不一致';
    }
    return null;
  }
}
