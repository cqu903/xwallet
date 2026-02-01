import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/login_request.dart';
import '../models/login_response.dart';
import '../models/register_request.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../utils/platform_config.dart';

/// API服务类
/// 封装所有与后端API的交互
class ApiService {
  // 后端API地址 - 自动检测平台
  static String get baseUrl => PlatformConfig.apiBaseUrl;

  // Token存储key
  static const String _tokenKey = 'auth_token';
  static const String _userInfoKey = 'user_info';

  // 单例模式
  static final ApiService _instance = ApiService._internal();
  factory ApiService() => _instance;
  ApiService._internal();

  /// 获取存储的token
  Future<String?> getToken() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_tokenKey);
  }

  /// 保存token
  Future<void> saveToken(String token) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_tokenKey, token);
  }

  /// 清除token
  Future<void> clearToken() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_tokenKey);
  }

  /// 保存用户信息
  Future<void> saveUserInfo(LoginResponse userInfo) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_userInfoKey, jsonEncode(userInfo.toJson()));
  }

  /// 获取用户信息
  Future<LoginResponse?> getUserInfo() async {
    final prefs = await SharedPreferences.getInstance();
    final userInfoStr = prefs.getString(_userInfoKey);
    if (userInfoStr == null) return null;

    try {
      final Map<String, dynamic> userData = jsonDecode(userInfoStr);
      return LoginResponse.fromJson(userData);
    } catch (e) {
      return null;
    }
  }

  /// 清除用户信息
  Future<void> clearUserInfo() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_userInfoKey);
  }

  /// 获取请求头（包含Authorization）
  Future<Map<String, String>> _getHeaders() async {
    final token = await getToken();
    return {
      'Content-Type': 'application/json',
      if (token != null) 'Authorization': 'Bearer $token',
    };
  }

  /// 用户登录
  /// 返回: (是否成功, 错误消息)
  Future<(bool, String?)> login(LoginRequest request) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/auth/login'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode(request.toJson()),
      );

      if (response.statusCode == 200) {
        final Map<String, dynamic> responseData = jsonDecode(response.body);
        final result = ResponseResult<LoginResponse>.fromJson(
          responseData,
          (data) => LoginResponse.fromJson(data),
        );

        if (result.isSuccess && result.data != null) {
          // 保存token和用户信息
          await saveToken(result.data!.token);
          await saveUserInfo(result.data!);
          return (true, null);
        } else {
          return (false, result.message ?? '登录失败');
        }
      } else {
        return (false, '服务器错误: ${response.statusCode}');
      }
    } catch (e) {
      return (false, '网络错误: $e');
    }
  }

  /// 用户登出
  /// 返回: (是否成功, 错误消息)
  Future<(bool, String?)> logout() async {
    try {
      final headers = await _getHeaders();
      final response = await http.post(
        Uri.parse('$baseUrl/auth/logout'),
        headers: headers,
      );

      // 无论后端返回什么，都清除本地token和用户信息
      await clearToken();
      await clearUserInfo();

      if (response.statusCode == 200) {
        return (true, null);
      } else {
        return (false, '登出失败');
      }
    } catch (e) {
      // 即使网络错误，也清除本地token
      await clearToken();
      return (false, '网络错误: $e');
    }
  }

  /// 验证token
  /// 返回: (是否有效, 错误消息)
  Future<(bool, String?)> validateToken() async {
    try {
      final headers = await _getHeaders();
      final response = await http.get(
        Uri.parse('$baseUrl/auth/validate'),
        headers: headers,
      );

      if (response.statusCode == 200) {
        final Map<String, dynamic> responseData = jsonDecode(response.body);
        final isValid = responseData['data'] == true;
        return (isValid, null);
      } else {
        // 如果验证失败，清除本地token
        await clearToken();
        return (false, 'Token验证失败');
      }
    } catch (e) {
      await clearToken();
      return (false, '网络错误: $e');
    }
  }

  /// 检查是否已登录
  Future<bool> isLoggedIn() async {
    final token = await getToken();
    if (token == null || token.isEmpty) return false;

    final (isValid, _) = await validateToken();
    return isValid;
  }

  /// 发送验证码
  /// 返回: (是否成功, 错误消息)
  Future<(bool, String?)> sendVerificationCode(String email) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/auth/send-code'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({'email': email}),
      );

      if (response.statusCode == 200) {
        return (true, null);
      } else {
        final Map<String, dynamic> responseData = jsonDecode(response.body);
        final message = responseData['message']?.toString() ?? '发送验证码失败';
        return (false, message);
      }
    } catch (e) {
      return (false, '网络错误: $e');
    }
  }

  /// 用户注册
  /// 返回: (是否成功, 错误消息)
  Future<(bool, String?)> register(RegisterRequest request) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/auth/register'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode(request.toJson()),
      );

      if (response.statusCode == 200) {
        final Map<String, dynamic> responseData = jsonDecode(response.body);
        final result = ResponseResult<LoginResponse>.fromJson(
          responseData,
          (data) => LoginResponse.fromJson(data),
        );

        if (result.isSuccess && result.data != null) {
          // 保存token和用户信息 (注册成功自动登录)
          await saveToken(result.data!.token);
          await saveUserInfo(result.data!);
          return (true, null);
        } else {
          return (false, result.message ?? '注册失败');
        }
      } else {
        return (false, '服务器错误: ${response.statusCode}');
      }
    } catch (e) {
      return (false, '网络错误: $e');
    }
  }
}
