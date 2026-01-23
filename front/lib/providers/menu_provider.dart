import 'package:flutter/foundation.dart';
import '../models/menu_item.dart';
import '../services/menu_service.dart';

/// 菜单状态管理
/// 负责从后端获取菜单数据并管理加载状态
class MenuProvider with ChangeNotifier {
  final MenuService _menuService = MenuService();

  List<MenuItem> _menus = [];
  bool _isLoading = false;
  String? _errorMessage;

  List<MenuItem> get menus => _menus;
  bool get isLoading => _isLoading;
  String? get errorMessage => _errorMessage;

  /// 初始化：不立即加载菜单
  /// 菜单将在用户登录后通过 refresh() 方法加载
  MenuProvider() {
    // 不在构造函数中立即加载，避免在用户未登录时失败
    // 菜单将在登录成功后通过 refresh() 方法加载
  }

  /// 从后端加载菜单数据
  Future<void> loadMenus() async {
    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    final (menus, error) = await _menuService.fetchMenus();

    if (menus != null) {
      _menus = menus;
      _errorMessage = null;
    } else {
      _errorMessage = error ?? '加载菜单失败';
    }

    _isLoading = false;
    notifyListeners();
  }

  /// 重新加载菜单
  Future<void> refresh() async {
    await loadMenus();
  }

  /// 根据ID查找菜单
  MenuItem? findMenu(String id) {
    for (final menu in _menus) {
      if (menu.id == id) return menu;
      for (final sub in menu.children) {
        if (sub.id == id) return sub;
      }
    }
    return null;
  }

  /// 根据路径查找菜单
  MenuItem? findMenuByPath(String path) {
    return _menuService.findMenuByPath(_menus, path);
  }

  /// 获取菜单名称
  String getMenuName(String? id) {
    if (id == null) return '未知页面';
    final menu = findMenu(id);
    return menu?.name ?? '未知页面';
  }

  /// 清除错误消息
  void clearError() {
    _errorMessage = null;
    notifyListeners();
  }
}
