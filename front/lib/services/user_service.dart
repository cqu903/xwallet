import 'dart:convert';
import 'package:http/http.dart' as http;
import 'api_service.dart';
import '../models/user.dart';

/// 用户服务类
/// 负责用户管理相关的API调用
class UserService {
  final ApiService _apiService = ApiService();

  /// 获取用户列表
  /// 返回: (用户分页数据, 错误消息)
  Future<(UserListResponse?, String?)> getUserList({
    String? keyword,
    List<int>? roleIds,
    int? status,
    int page = 1,
    int size = 10,
  }) async {
    try {
      final token = await _apiService.getToken();
      if (token == null || token.isEmpty) {
        return (null, '未登录');
      }

      // 构建查询参数
      final queryParams = <String, String>{
        'page': page.toString(),
        'size': size.toString(),
      };
      if (keyword != null && keyword.isNotEmpty) {
        queryParams['keyword'] = keyword;
      }
      if (roleIds != null && roleIds.isNotEmpty) {
        queryParams['roleIds'] = roleIds.join(',');
      }
      if (status != null) {
        queryParams['status'] = status.toString();
      }

      final uri = Uri.parse('${ApiService.baseUrl}/user/list')
          .replace(queryParameters: queryParams);

      final response = await http.get(
        uri,
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
            return (UserListResponse.fromJson(data), null);
          }
        }
        final errorMsg = responseData['errmsg'] ?? '获取用户列表失败';
        return (null, errorMsg as String?);
      } else {
        return (null, '服务器错误: ${response.statusCode}');
      }
    } catch (e) {
      return (null, '网络错误: $e');
    }
  }

  /// 获取用户详情
  Future<(User?, String?)> getUserById(String id) async {
    try {
      final token = await _apiService.getToken();
      if (token == null || token.isEmpty) {
        return (null, '未登录');
      }

      final response = await http.get(
        Uri.parse('${ApiService.baseUrl}/user/$id'),
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer $token',
        },
      );

      if (response.statusCode == 200) {
        final Map<String, dynamic> responseData = jsonDecode(response.body);
        final code = responseData['code'];
        if (code == 200) {
          final userData = responseData['data'] as Map<String, dynamic>?;
          if (userData != null) {
            return (User.fromJson(userData), null);
          }
        }
        final errorMsg = responseData['errmsg'] ?? '获取用户详情失败';
        return (null, errorMsg as String?);
      } else if (response.statusCode == 404) {
        return (null, '用户不存在');
      } else {
        return (null, '服务器错误: ${response.statusCode}');
      }
    } catch (e) {
      return (null, '网络错误: $e');
    }
  }

  /// 创建用户
  Future<(bool, String?)> createUser(CreateUserRequest request) async {
    try {
      final token = await _apiService.getToken();
      if (token == null || token.isEmpty) {
        return (false, '未登录');
      }

      final response = await http.post(
        Uri.parse('${ApiService.baseUrl}/user'),
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
        final errorMsg = responseData['errmsg'] ?? '创建用户失败';
        return (false, errorMsg as String?);
      } else if (response.statusCode == 400) {
        final Map<String, dynamic> responseData = jsonDecode(response.body);
        return (false, responseData['errmsg'] as String? ?? '创建用户失败');
      } else {
        return (false, '服务器错误: ${response.statusCode}');
      }
    } catch (e) {
      return (false, '网络错误: $e');
    }
  }

  /// 更新用户
  Future<(bool, String?)> updateUser(String id, UpdateUserRequest request) async {
    try {
      final token = await _apiService.getToken();
      if (token == null || token.isEmpty) {
        return (false, '未登录');
      }

      final response = await http.put(
        Uri.parse('${ApiService.baseUrl}/user/$id'),
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
        final errorMsg = responseData['errmsg'] ?? '更新用户失败';
        return (false, errorMsg as String?);
      } else if (response.statusCode == 400) {
        final Map<String, dynamic> responseData = jsonDecode(response.body);
        return (false, responseData['errmsg'] as String? ?? '更新用户失败');
      } else {
        return (false, '服务器错误: ${response.statusCode}');
      }
    } catch (e) {
      return (false, '网络错误: $e');
    }
  }

  /// 切换用户状态（启用/禁用）
  Future<(bool, String?)> toggleUserStatus(String id, int status) async {
    try {
      final token = await _apiService.getToken();
      if (token == null || token.isEmpty) {
        return (false, '未登录');
      }

      final response = await http.put(
        Uri.parse('${ApiService.baseUrl}/user/$id/status?status=$status'),
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
        final errorMsg = responseData['errmsg'] ?? '更新用户状态失败';
        return (false, errorMsg as String?);
      } else if (response.statusCode == 400) {
        final Map<String, dynamic> responseData = jsonDecode(response.body);
        return (false, responseData['errmsg'] as String? ?? '更新用户状态失败');
      } else {
        return (false, '服务器错误: ${response.statusCode}');
      }
    } catch (e) {
      return (false, '网络错误: $e');
    }
  }

  /// 重置用户密码
  Future<(bool, String?)> resetPassword(String id, String password) async {
    try {
      final token = await _apiService.getToken();
      if (token == null || token.isEmpty) {
        return (false, '未登录');
      }

      final response = await http.put(
        Uri.parse('${ApiService.baseUrl}/user/$id/password'),
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer $token',
        },
        body: jsonEncode({'password': password}),
      );

      if (response.statusCode == 200) {
        final Map<String, dynamic> responseData = jsonDecode(response.body);
        final code = responseData['code'];
        if (code == 200) {
          return (true, null);
        }
        final errorMsg = responseData['errmsg'] ?? '重置密码失败';
        return (false, errorMsg as String?);
      } else {
        return (false, '服务器错误: ${response.statusCode}');
      }
    } catch (e) {
      return (false, '网络错误: $e');
    }
  }
}

/// 用户列表响应
class UserListResponse {
  final List<User> list;
  final int total;
  final int page;
  final int size;
  final int totalPages;

  UserListResponse({
    required this.list,
    required this.total,
    required this.page,
    required this.size,
    required this.totalPages,
  });

  factory UserListResponse.fromJson(Map<String, dynamic> json) {
    final listData = json['list'] as List<dynamic>? ?? [];
    return UserListResponse(
      list: listData.map((e) => User.fromJson(e as Map<String, dynamic>)).toList(),
      total: json['total'] as int? ?? 0,
      page: json['page'] as int? ?? 1,
      size: json['size'] as int? ?? 10,
      totalPages: json['totalPages'] as int? ?? 0,
    );
  }
}

/// 创建用户请求
class CreateUserRequest {
  final String employeeNo;
  final String username;
  final String email;
  final String password;
  final List<int> roleIds;

  CreateUserRequest({
    required this.employeeNo,
    required this.username,
    required this.email,
    required this.password,
    required this.roleIds,
  });

  Map<String, dynamic> toJson() {
    return {
      'employeeNo': employeeNo,
      'username': username,
      'email': email,
      'password': password,
      'roleIds': roleIds,
    };
  }
}

/// 更新用户请求
class UpdateUserRequest {
  final String username;
  final String email;
  final List<int> roleIds;

  UpdateUserRequest({
    required this.username,
    required this.email,
    required this.roleIds,
  });

  Map<String, dynamic> toJson() {
    return {
      'username': username,
      'email': email,
      'roleIds': roleIds,
    };
  }
}
