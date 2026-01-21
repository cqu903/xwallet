import 'dart:convert';
import 'package:http/http.dart' as http;
import 'api_service.dart';
import '../models/user.dart';

/// 角色服务类
/// 负责角色相关的API调用
class RoleService {
  final ApiService _apiService = ApiService();

  /// 获取所有角色列表
  Future<(List<Role>, String?)> getAllRoles() async {
    try {
      final token = await _apiService.getToken();
      if (token == null || token.isEmpty) {
        return (const [], '未登录');
      }

      final response = await http.get(
        Uri.parse('${ApiService.baseUrl}/user/roles/all'),
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer $token',
        },
      );

      if (response.statusCode == 200) {
        final Map<String, dynamic> responseData = jsonDecode(response.body);
        final code = responseData['code'];
        if (code == 200) {
          final data = responseData['data'] as List<dynamic>? ?? [];
          final roles = data.map((e) => Role.fromJson(e as Map<String, dynamic>)).toList();
          return (roles, null);
        }
        final errorMsg = responseData['errmsg'] ?? '获取角色列表失败';
        return (const [], errorMsg as String?);
      } else {
        return (const [], '服务器错误: ${response.statusCode}');
      }
    } catch (e) {
      return (const [], '网络错误: $e');
    }
  }
}
