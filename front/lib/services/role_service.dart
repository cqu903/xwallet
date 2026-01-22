import 'dart:convert';
import 'package:http/http.dart' as http;
import 'api_service.dart';
import '../models/user.dart';
import '../models/role_detail.dart';

/// 角色服务类
/// 负责角色相关的API调用
class RoleService {
  final ApiService _apiService = ApiService();

  /// 获取所有角色列表
  Future<(List<Role>, String?)> getAllRoles() async {
    try {
      final token = await _apiService.getToken();
      if (token == null || token.isEmpty) {
        return (<Role>[], '未登录');
      }

      final response = await http.get(
        Uri.parse('${ApiService.baseUrl}/role/list'),
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
        return (<Role>[], errorMsg as String?);
      } else {
        return (<Role>[], '服务器错误: ${response.statusCode}');
      }
    } catch (e) {
      return (<Role>[], '网络错误: $e');
    }
  }

  /// 获取角色详情
  Future<(RoleDetail?, String?)> getRoleDetail(String id) async {
    try {
      final token = await _apiService.getToken();
      if (token == null || token.isEmpty) {
        return (null, '未登录');
      }

      final response = await http.get(
        Uri.parse('${ApiService.baseUrl}/role/$id'),
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer $token',
        },
      );

      if (response.statusCode == 200) {
        final Map<String, dynamic> responseData = jsonDecode(response.body);
        final code = responseData['code'];
        if (code == 200) {
          final data = responseData['data'] as Map<String, dynamic>?;
          if (data != null) {
            return (RoleDetail.fromJson(data), null);
          }
        }
        final errorMsg = responseData['errmsg'] ?? '获取角色详情失败';
        return (null, errorMsg as String?);
      } else {
        return (null, '服务器错误: ${response.statusCode}');
      }
    } catch (e) {
      return (null, '网络错误: $e');
    }
  }

  /// 创建角色
  Future<(String?, String?)> createRole(CreateRoleRequest request) async {
    try {
      final token = await _apiService.getToken();
      if (token == null || token.isEmpty) {
        return (null, '未登录');
      }

      final response = await http.post(
        Uri.parse('${ApiService.baseUrl}/role'),
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer $token',
        },
        body: jsonEncode(request.toJson()),
      );

      if (response.statusCode == 200) {
        final Map<String, dynamic> responseData = jsonDecode(response.body);
        final code = responseData['code'];
        if (code == 200) {
          final roleId = responseData['data']?.toString();
          return (roleId, null);
        }
        final errorMsg = responseData['errmsg'] ?? '创建角色失败';
        return (null, errorMsg as String?);
      } else {
        return (null, '服务器错误: ${response.statusCode}');
      }
    } catch (e) {
      return (null, '网络错误: $e');
    }
  }

  /// 更新角色
  Future<(bool, String?)> updateRole(String id, UpdateRoleRequest request) async {
    try {
      final token = await _apiService.getToken();
      if (token == null || token.isEmpty) {
        return (false, '未登录');
      }

      final response = await http.put(
        Uri.parse('${ApiService.baseUrl}/role/$id'),
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer $token',
        },
        body: jsonEncode(request.toJson()),
      );

      if (response.statusCode == 200) {
        final Map<String, dynamic> responseData = jsonDecode(response.body);
        final code = responseData['code'];
        if (code == 200) {
          return (true, null);
        }
        final errorMsg = responseData['errmsg'] ?? '更新角色失败';
        return (false, errorMsg as String?);
      } else {
        return (false, '服务器错误: ${response.statusCode}');
      }
    } catch (e) {
      return (false, '网络错误: $e');
    }
  }

  /// 删除角色
  Future<(bool, String?)> deleteRole(String id) async {
    try {
      final token = await _apiService.getToken();
      if (token == null || token.isEmpty) {
        return (false, '未登录');
      }

      final response = await http.delete(
        Uri.parse('${ApiService.baseUrl}/role/$id'),
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer $token',
        },
      );

      if (response.statusCode == 200) {
        final Map<String, dynamic> responseData = jsonDecode(response.body);
        final code = responseData['code'];
        if (code == 200) {
          return (true, null);
        }
        final errorMsg = responseData['errmsg'] ?? '删除角色失败';
        return (false, errorMsg as String?);
      } else {
        return (false, '服务器错误: ${response.statusCode}');
      }
    } catch (e) {
      return (false, '网络错误: $e');
    }
  }

  /// 切换角色状态
  Future<(bool, String?)> toggleRoleStatus(String id, int status) async {
    try {
      final token = await _apiService.getToken();
      if (token == null || token.isEmpty) {
        return (false, '未登录');
      }

      final response = await http.put(
        Uri.parse('${ApiService.baseUrl}/role/$id/status?status=$status'),
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer $token',
        },
      );

      if (response.statusCode == 200) {
        final Map<String, dynamic> responseData = jsonDecode(response.body);
        final code = responseData['code'];
        if (code == 200) {
          return (true, null);
        }
        final errorMsg = responseData['errmsg'] ?? '切换角色状态失败';
        return (false, errorMsg as String?);
      } else {
        return (false, '服务器错误: ${response.statusCode}');
      }
    } catch (e) {
      return (false, '网络错误: $e');
    }
  }
}
