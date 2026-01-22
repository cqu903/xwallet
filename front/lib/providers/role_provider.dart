import 'package:flutter/foundation.dart';
import '../models/user.dart';
import '../models/role_detail.dart';
import '../services/role_service.dart';

/// 角色状态管理
/// 负责角色列表数据的管理
class RoleProvider with ChangeNotifier {
  final RoleService _roleService = RoleService();

  List<Role> _roles = [];
  bool _isLoading = false;
  String? _errorMessage;

  List<Role> get roles => _roles;
  bool get isLoading => _isLoading;
  String? get errorMessage => _errorMessage;

  /// 加载角色列表
  Future<void> loadRoles() async {
    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    final (roles, error) = await _roleService.getAllRoles();

    if (error == null) {
      _roles = roles;
      _errorMessage = null;
    } else {
      _errorMessage = error;
    }

    _isLoading = false;
    notifyListeners();
  }

  /// 根据ID获取角色
  Role? getRoleById(String id) {
    for (final role in _roles) {
      if (role.id == id) return role;
    }
    return null;
  }

  /// 根据ID列表获取角色
  List<Role> getRolesByIds(List<String> ids) {
    return _roles.where((r) => ids.contains(r.id)).toList();
  }

  /// 创建角色
  Future<bool> createRole(CreateRoleRequest request) async {
    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    final (roleId, error) = await _roleService.createRole(request);

    _isLoading = false;

    if (error != null) {
      _errorMessage = error;
      notifyListeners();
      return false;
    }

    // 重新加载列表
    await loadRoles();
    return true;
  }

  /// 更新角色
  Future<bool> updateRole(String id, UpdateRoleRequest request) async {
    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    final (success, error) = await _roleService.updateRole(id, request);

    _isLoading = false;

    if (error != null || !success) {
      _errorMessage = error ?? '更新失败';
      notifyListeners();
      return false;
    }

    // 重新加载列表
    await loadRoles();
    return true;
  }

  /// 删除角色
  Future<bool> deleteRole(String id) async {
    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    final (success, error) = await _roleService.deleteRole(id);

    _isLoading = false;

    if (error != null || !success) {
      _errorMessage = error ?? '删除失败';
      notifyListeners();
      return false;
    }

    // 重新加载列表
    await loadRoles();
    return true;
  }

  /// 切换角色状态
  Future<bool> toggleStatus(String id, int status) async {
    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    final (success, error) = await _roleService.toggleRoleStatus(id, status);

    _isLoading = false;

    if (error != null || !success) {
      _errorMessage = error ?? '切换状态失败';
      notifyListeners();
      return false;
    }

    // 重新加载列表
    await loadRoles();
    return true;
  }

  /// 获取角色详情
  Future<(RoleDetail?, String?)> getRoleDetail(String id) async {
    return await _roleService.getRoleDetail(id);
  }

  /// 清除错误消息
  void clearError() {
    _errorMessage = null;
    notifyListeners();
  }
}
