import 'package:flutter/foundation.dart';
import '../models/user.dart';
import '../services/user_service.dart';

/// 用户状态管理
/// 负责用户列表数据和查询条件的管理
class UserProvider with ChangeNotifier {
  final UserService _userService = UserService();

  List<User> _users = [];
  int _total = 0;
  int _currentPage = 1;
  int _totalPages = 1;
  bool _isLoading = false;
  String? _errorMessage;

  // 查询条件
  String _keyword = '';
  List<int> _selectedRoleIds = [];
  int? _selectedStatus;

  // Getters
  List<User> get users => _users;
  int get total => _total;
  int get currentPage => _currentPage;
  int get totalPages => _totalPages;
  bool get isLoading => _isLoading;
  String? get errorMessage => _errorMessage;
  String get keyword => _keyword;
  List<int> get selectedRoleIds => List.unmodifiable(_selectedRoleIds);
  int? get selectedStatus => _selectedStatus;
  bool get hasFilters => _keyword.isNotEmpty || _selectedRoleIds.isNotEmpty || _selectedStatus != null;

  /// 加载用户列表
  Future<void> loadUsers({bool refresh = false}) async {
    if (refresh) {
      _currentPage = 1;
    }

    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    final (response, error) = await _userService.getUserList(
      keyword: _keyword.isEmpty ? null : _keyword,
      roleIds: _selectedRoleIds.isEmpty ? null : _selectedRoleIds,
      status: _selectedStatus,
      page: _currentPage,
      size: 10,
    );

    if (response != null) {
      _users = response.list;
      _total = response.total;
      _totalPages = response.totalPages;
      _errorMessage = null;
    } else {
      _errorMessage = error ?? '加载用户列表失败';
    }

    _isLoading = false;
    notifyListeners();
  }

  /// 搜索
  Future<void> search(String value) async {
    _keyword = value;
    _currentPage = 1;
    await loadUsers();
  }

  /// 清除搜索
  Future<void> clearSearch() async {
    _keyword = '';
    _currentPage = 1;
    await loadUsers();
  }

  /// 按角色筛选
  Future<void> filterByRole(List<int> roleIds) async {
    _selectedRoleIds = roleIds;
    _currentPage = 1;
    await loadUsers();
  }

  /// 按状态筛选
  Future<void> filterByStatus(int? status) async {
    _selectedStatus = status;
    _currentPage = 1;
    await loadUsers();
  }

  /// 清除所有筛选
  Future<void> clearFilters() async {
    _keyword = '';
    _selectedRoleIds = [];
    _selectedStatus = null;
    _currentPage = 1;
    await loadUsers();
  }

  /// 下一页
  Future<void> nextPage() async {
    if (_currentPage < _totalPages) {
      _currentPage++;
      await loadUsers();
    }
  }

  /// 上一页
  Future<void> previousPage() async {
    if (_currentPage > 1) {
      _currentPage--;
      await loadUsers();
    }
  }

  /// 跳转到指定页
  Future<void> goToPage(int page) async {
    if (page >= 1 && page <= _totalPages) {
      _currentPage = page;
      await loadUsers();
    }
  }

  /// 创建用户
  Future<(bool, String?)> createUser(CreateUserRequest request) async {
    final (success, error) = await _userService.createUser(request);
    if (success) {
      await loadUsers(refresh: true);
    }
    return (success, error);
  }

  /// 更新用户
  Future<(bool, String?)> updateUser(String id, UpdateUserRequest request) async {
    final (success, error) = await _userService.updateUser(id, request);
    if (success) {
      await loadUsers();
    }
    return (success, error);
  }

  /// 切换用户状态
  Future<(bool, String?)> toggleUserStatus(String id, int status) async {
    final (success, error) = await _userService.toggleUserStatus(id, status);
    if (success) {
      await loadUsers();
    }
    return (success, error);
  }

  /// 重置密码
  Future<(bool, String?)> resetPassword(String id, String password) async {
    final (success, error) = await _userService.resetPassword(id, password);
    return (success, error);
  }

  /// 清除错误消息
  void clearError() {
    _errorMessage = null;
    notifyListeners();
  }
}
