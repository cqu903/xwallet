import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import '../models/menu_item.dart';
import 'api_service.dart';

/// 菜单服务类
/// 负责从后端获取菜单数据并管理缓存
class MenuService {
  static const String _menuCacheKey = 'cached_menu';
  final ApiService _apiService = ApiService();

  /// 从缓存获取菜单
  Future<List<MenuItem>> getCachedMenu() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final String? cached = prefs.getString(_menuCacheKey);
      if (cached != null) {
        final List<dynamic> decoded = jsonDecode(cached);
        return decoded
            .map((e) => MenuItem.fromJson(e as Map<String, dynamic>))
            .toList();
      }
    } catch (e) {
      // 缓存读取失败，返回空列表
      print('读取菜单缓存失败: $e');
    }
    return [];
  }

  /// 保存菜单到缓存
  Future<void> cacheMenu(List<MenuItem> menus) async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final String encoded = jsonEncode(menus.map((e) => e.toJson()).toList());
      await prefs.setString(_menuCacheKey, encoded);
    } catch (e) {
      print('保存菜单缓存失败: $e');
    }
  }

  /// 清除菜单缓存
  Future<void> clearCache() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      await prefs.remove(_menuCacheKey);
    } catch (e) {
      print('清除菜单缓存失败: $e');
    }
  }

  /// 从后端获取菜单（根据当前用户的权限）
  /// 返回: (菜单列表, 错误消息)
  Future<(List<MenuItem>?, String?)> fetchMenus() async {
    try {
      final token = await _apiService.getToken();
      if (token == null || token.isEmpty) {
        return (null, '未登录');
      }

      final url = Uri.parse('${ApiService.baseUrl}/menus');
      print('正在请求菜单数据: $url');

      final response = await http
          .get(
            url,
            headers: {
              'Content-Type': 'application/json',
              'Authorization': 'Bearer $token',
            },
          )
          .timeout(
            const Duration(seconds: 10),
            onTimeout: () {
              throw Exception('请求超时');
            },
          );

      print('菜单API响应状态码: ${response.statusCode}');
      print('菜单API响应体: ${response.body}');

      if (response.statusCode == 200) {
        final Map<String, dynamic> responseData = jsonDecode(response.body);

        // 检查响应格式
        final code = responseData['code'];
        if (code == 200) {
          final List<dynamic> data = responseData['data'] ?? [];
          final menus = data
              .map((e) => MenuItem.fromJson(e as Map<String, dynamic>))
              .toList();

          // 缓存菜单数据
          await cacheMenu(menus);

          return (menus, null);
        } else {
          final errorMsg = responseData['errmsg'] ?? '获取菜单失败';
          print('菜单API返回错误: code=$code, errmsg=$errorMsg');
          return (null, errorMsg as String?);
        }
      } else {
        // API 调用失败，尝试返回缓存
        final cached = await getCachedMenu();
        if (cached.isNotEmpty) {
          print('API调用失败，使用缓存菜单');
          return (cached, null);
        }
        print(
          '菜单API调用失败: statusCode=${response.statusCode}, body=${response.body}',
        );
        return (null, '服务器错误: ${response.statusCode}');
      }
    } catch (e, stackTrace) {
      print('获取菜单时发生异常: $e');
      print('堆栈跟踪: $stackTrace');

      // 发生异常，尝试返回缓存
      final cached = await getCachedMenu();
      if (cached.isNotEmpty) {
        print('发生异常，使用缓存菜单');
        return (cached, null);
      }
      return (null, '网络错误: $e');
    }
  }

  /// 根据路径查找菜单项
  MenuItem? findMenuByPath(List<MenuItem> menus, String path) {
    for (final menu in menus) {
      if (menu.path == path) {
        return menu;
      }
      if (menu.children.isNotEmpty) {
        final found = findMenuByPath(menu.children, path);
        if (found != null) {
          return found;
        }
      }
    }
    return null;
  }
}
