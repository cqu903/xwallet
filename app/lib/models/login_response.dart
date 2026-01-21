/// 登录响应数据模型
class LoginResponse {
  final String token;
  final UserInfo userInfo;

  LoginResponse({
    required this.token,
    required this.userInfo,
  });

  /// 从JSON创建实例
  factory LoginResponse.fromJson(Map<String, dynamic> json) {
    return LoginResponse(
      token: json['token'] ?? '',
      userInfo: UserInfo.fromJson(json['userInfo'] ?? {}),
    );
  }
}

/// 用户信息
class UserInfo {
  final int userId;
  final String username;
  final String userType; // "SYSTEM" 或 "CUSTOMER"
  final String? role; // 仅系统用户有值

  UserInfo({
    required this.userId,
    required this.username,
    required this.userType,
    this.role,
  });

  /// 从JSON创建实例
  factory UserInfo.fromJson(Map<String, dynamic> json) {
    return UserInfo(
      userId: json['userId'] ?? 0,
      username: json['username'] ?? '',
      userType: json['userType'] ?? '',
      role: json['role'],
    );
  }
}

/// 通用响应结果
class ResponseResult<T> {
  final int? code;
  final String? message;
  final T? data;

  ResponseResult({
    this.code,
    this.message,
    this.data,
  });

  /// 从JSON创建实例
  factory ResponseResult.fromJson(Map<String, dynamic> json, T Function(dynamic)? dataParser) {
    return ResponseResult(
      code: json['code'],
      message: json['message'],
      data: dataParser != null && json['data'] != null ? dataParser(json['data']) : json['data'],
    );
  }

  /// 是否成功
  bool get isSuccess => code == null || code == 200;
}
