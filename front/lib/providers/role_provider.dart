import 'package:flutter/foundation.dart';
import '../models/user.dart';
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

  /// 清除错误消息
  void clearError() {
    _errorMessage = null;
    notifyListeners();
  }
}
