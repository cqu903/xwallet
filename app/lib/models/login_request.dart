/// 登录请求数据模型
class LoginRequest {
  final String userType; // "SYSTEM" 或 "CUSTOMER"
  final String account; // 工号或邮箱
  final String password;

  LoginRequest({
    required this.userType,
    required this.account,
    required this.password,
  });

  /// 转换为JSON
  Map<String, dynamic> toJson() {
    return {
      'userType': userType,
      'account': account,
      'password': password,
    };
  }
}
