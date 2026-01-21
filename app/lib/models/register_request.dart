/// 用户注册请求
class RegisterRequest {
  final String email;
  final String password;
  final String verificationCode;
  final String? nickname;

  RegisterRequest({
    required this.email,
    required this.password,
    required this.verificationCode,
    this.nickname,
  });

  Map<String, dynamic> toJson() {
    return {
      'email': email,
      'password': password,
      'verificationCode': verificationCode,
      if (nickname != null) 'nickname': nickname,
    };
  }
}
