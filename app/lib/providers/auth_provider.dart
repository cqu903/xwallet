import 'package:flutter/foundation.dart';
import '../models/login_request.dart';
import '../models/login_response.dart';
import '../models/register_request.dart';
import '../services/api_service.dart';

/// 认证状态
enum AuthStatus {
  notLoggedIn,    // 未登录
  loggingIn,      // 登录中
  loggedIn,       // 已登录
  error,          // 错误
}

/// 认证Provider
/// 管理用户登录状态和认证相关数据
class AuthProvider with ChangeNotifier {
  final ApiService _apiService = ApiService();

  AuthStatus _status = AuthStatus.notLoggedIn;
  LoginResponse? _currentUser;
  String? _errorMessage;

  /// 当前认证状态
  AuthStatus get status => _status;

  /// 当前登录用户信息
  LoginResponse? get currentUser => _currentUser;

  /// 错误消息
  String? get errorMessage => _errorMessage;

  /// 是否已登录
  bool get isLoggedIn => _status == AuthStatus.loggedIn;

  /// 是否正在登录
  bool get isLoggingIn => _status == AuthStatus.loggingIn;

  /// 构造函数：检查本地存储的token
  AuthProvider() {
    _checkLoginStatus();
  }

  /// 检查登录状态
  Future<void> _checkLoginStatus() async {
    _status = AuthStatus.loggingIn;
    notifyListeners();

    final isValid = await _apiService.isLoggedIn();

    if (isValid) {
      _status = AuthStatus.loggedIn;
      // 注意：这里只设置了状态，没有设置用户信息
      // 如果需要完整的用户信息，需要额外的API获取
    } else {
      _status = AuthStatus.notLoggedIn;
      _currentUser = null;
    }

    notifyListeners();
  }

  /// 登录
  /// [email] 邮箱 (顾客登录使用)
  /// [password] 密码
  /// 返回是否登录成功
  Future<bool> login(String email, String password) async {
    _status = AuthStatus.loggingIn;
    _errorMessage = null;
    notifyListeners();

    final request = LoginRequest(
      userType: 'CUSTOMER', // App端固定使用CUSTOMER用户类型
      account: email,
      password: password,
    );

    final (success, error) = await _apiService.login(request);

    if (success) {
      _status = AuthStatus.loggedIn;
      // 注意：这里没有保存完整的用户信息
      // 如果需要，可以从token解析或调用额外的API
      _currentUser = LoginResponse(
        token: (await _apiService.getToken())!,
        userInfo: UserInfo(
          userId: 0, // 暂时设为0，实际应从后端获取
          username: email,
          userType: 'CUSTOMER',
          role: null,
        ),
      );
      _errorMessage = null;
    } else {
      _status = AuthStatus.error;
      _errorMessage = error ?? '登录失败';
      _currentUser = null;
    }

    notifyListeners();
    return success;
  }

  /// 登出
  Future<void> logout() async {
    _status = AuthStatus.loggingIn;
    notifyListeners();

    await _apiService.logout();

    _status = AuthStatus.notLoggedIn;
    _currentUser = null;
    _errorMessage = null;
    notifyListeners();
  }

  /// 清除错误消息
  void clearError() {
    _errorMessage = null;
    notifyListeners();
  }

  /// 发送验证码
  Future<bool> sendVerificationCode(String email) async {
    final (success, error) = await _apiService.sendVerificationCode(email);

    if (!success) {
      _errorMessage = error ?? '发送验证码失败';
      notifyListeners();
    }

    return success;
  }

  /// 注册
  Future<bool> register({
    required String email,
    required String password,
    required String verificationCode,
    String? nickname,
  }) async {
    _status = AuthStatus.loggingIn;
    _errorMessage = null;
    notifyListeners();

    final request = RegisterRequest(
      email: email,
      password: password,
      verificationCode: verificationCode,
      nickname: nickname,
    );

    final (success, error) = await _apiService.register(request);

    if (success) {
      _status = AuthStatus.loggedIn;
      // 注册成功，自动登录
      final token = await _apiService.getToken();
      _currentUser = LoginResponse(
        token: token!,
        userInfo: UserInfo(
          userId: 0, // 暂时设为0，实际应从后端获取
          username: nickname ?? email,
          userType: 'CUSTOMER',
          role: null,
        ),
      );
      _errorMessage = null;
    } else {
      _status = AuthStatus.error;
      _errorMessage = error ?? '注册失败';
      _currentUser = null;
    }

    notifyListeners();
    return success;
  }
}
