import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/login_request.dart';
import '../models/login_response.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// API服务类
/// 封装所有与后端API的交互
class ApiService {
  // 后端API地址 (本地开发环境)
  static const String baseUrl = 'http://localhost:8080/api';

  // Token存储key
  static const String _tokenKey = 'auth_token';

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
          // 保存token
          await saveToken(result.data!.token);
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

      // 无论后端返回什么，都清除本地token
      await clearToken();

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
}
