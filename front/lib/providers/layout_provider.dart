import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// 侧边栏状态常量
class SidebarState {
  static const double expandedWidth = 280.0;
  static const double collapsedWidth = 80.0;
  static const String _expandedKey = 'sidebar_expanded';
}

/// 布局状态管理
/// 负责侧边栏展开/折叠状态和当前激活菜单
class LayoutProvider with ChangeNotifier {
  bool _isSidebarExpanded = true;
  String? _activeMenuId;

  bool get isSidebarExpanded => _isSidebarExpanded;
  String? get activeMenuId => _activeMenuId;

  /// 侧边栏宽度
  double get sidebarWidth =>
      _isSidebarExpanded ? SidebarState.expandedWidth : SidebarState.collapsedWidth;

  LayoutProvider() {
    _loadSidebarState();
  }

  /// 从本地存储加载侧边栏状态
  Future<void> _loadSidebarState() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      _isSidebarExpanded = prefs.getBool(SidebarState._expandedKey) ?? true;
      notifyListeners();
    } catch (e) {
      // 读取失败，使用默认值
      _isSidebarExpanded = true;
    }
  }

  /// 保存侧边栏状态到本地存储
  Future<void> _saveSidebarState() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      await prefs.setBool(SidebarState._expandedKey, _isSidebarExpanded);
    } catch (e) {
      // 保存失败，忽略
    }
  }

  /// 切换侧边栏展开/折叠
  void toggleSidebar() {
    _isSidebarExpanded = !_isSidebarExpanded;
    _saveSidebarState();
    notifyListeners();
  }

  /// 设置激活的菜单ID
  void setActiveMenu(String? menuId) {
    _activeMenuId = menuId;
    notifyListeners();
  }

  /// 设置侧边栏展开状态
  void setSidebarExpanded(bool expanded) {
    if (_isSidebarExpanded != expanded) {
      _isSidebarExpanded = expanded;
      _saveSidebarState();
      notifyListeners();
    }
  }
}
