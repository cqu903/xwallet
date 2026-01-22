import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/layout_provider.dart';
import '../providers/user_provider.dart';
import '../providers/role_provider.dart';
import '../models/user.dart' as model;
import '../theme/app_theme.dart';
import '../widgets/user_dialog.dart';
import '../widgets/reset_password_dialog.dart';

/// 用户管理页面
class UsersScreen extends StatefulWidget {
  const UsersScreen({super.key});

  @override
  State<UsersScreen> createState() => _UsersScreenState();
}

class _UsersScreenState extends State<UsersScreen> {
  final TextEditingController _searchController = TextEditingController();

  @override
  void initState() {
    super.initState();
    // 初始化加载数据
    Future.microtask(() {
      context.read<UserProvider>().loadUsers();
      context.read<RoleProvider>().loadRoles();
    });
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Padding(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // 页面标题和操作栏
            _buildHeader(context),
            const SizedBox(height: 24),

            // 搜索和筛选栏
            _buildFilterBar(context),
            const SizedBox(height: 16),

            // 用户列表表格
            Expanded(
              child: _buildUserTable(context),
            ),

            // 分页组件
            _buildPagination(),
          ],
        ),
      ),
    );
  }

  /// 构建页面标题和操作栏
  Widget _buildHeader(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        const Text(
          '用户管理',
          style: TextStyle(
            fontSize: 32,
            fontWeight: FontWeight.bold,
            color: Colors.black87,
          ),
        ),
        ElevatedButton.icon(
          onPressed: () => _showUserDialog(context),
          icon: const Icon(Icons.add),
          label: const Text('新增用户'),
          style: ElevatedButton.styleFrom(
            padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
            backgroundColor: AppTheme.primaryPurple,
            foregroundColor: Colors.white,
          ),
        ),
      ],
    );
  }

  /// 构建搜索和筛选栏
  Widget _buildFilterBar(BuildContext context) {
    final userProvider = context.watch<UserProvider>();
    final roleProvider = context.watch<RoleProvider>();

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Row(
          children: [
            // 搜索框
            Expanded(
              child: TextField(
                controller: _searchController,
                decoration: InputDecoration(
                  hintText: '搜索工号或姓名...',
                  prefixIcon: const Icon(Icons.search),
                  suffixIcon: _searchController.text.isNotEmpty
                      ? IconButton(
                          icon: const Icon(Icons.clear),
                          onPressed: () {
                            _searchController.clear();
                            userProvider.clearSearch();
                          },
                        )
                      : null,
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(8),
                  ),
                  contentPadding: const EdgeInsets.symmetric(
                    horizontal: 16,
                    vertical: 12,
                  ),
                ),
                onSubmitted: (value) => userProvider.search(value),
              ),
            ),
            const SizedBox(width: 16),

            // 角色筛选
            DropdownButton<int?>(
              value: userProvider.selectedStatus,
              hint: const Text('状态筛选'),
              items: const [
                DropdownMenuItem(value: null, child: Text('全部')),
                DropdownMenuItem(value: 1, child: Text('正常')),
                DropdownMenuItem(value: 0, child: Text('已禁用')),
              ],
              onChanged: (value) => userProvider.filterByStatus(value),
            ),
            const SizedBox(width: 16),

            // 清除筛选按钮
            if (userProvider.hasFilters)
              TextButton.icon(
                onPressed: () {
                  _searchController.clear();
                  userProvider.clearFilters();
                },
                icon: const Icon(Icons.clear),
                label: const Text('清除筛选'),
              ),
          ],
        ),
      ),
    );
  }

  /// 构建用户表格
  Widget _buildUserTable(BuildContext context) {
    final userProvider = context.watch<UserProvider>();

    if (userProvider.isLoading) {
      return const Center(child: CircularProgressIndicator());
    }

    if (userProvider.errorMessage != null) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.error_outline, size: 48, color: Colors.red),
            const SizedBox(height: 16),
            Text(userProvider.errorMessage!),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: () => userProvider.loadUsers(),
              child: const Text('重试'),
            ),
          ],
        ),
      );
    }

    if (userProvider.users.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.people_outline, size: 64, color: Colors.grey.shade300),
            const SizedBox(height: 16),
            Text('暂无用户数据', style: TextStyle(color: Colors.grey.shade500)),
          ],
        ),
      );
    }

    return Card(
      child: SingleChildScrollView(
        child: DataTable(
          columns: const [
            DataColumn(label: Text('工号')),
            DataColumn(label: Text('姓名')),
            DataColumn(label: Text('邮箱')),
            DataColumn(label: Text('角色')),
            DataColumn(label: Text('状态')),
            DataColumn(label: Text('创建时间')),
            DataColumn(label: Text('操作')),
          ],
          rows: userProvider.users.map((user) {
            return _buildDataRow(context, user);
          }).toList(),
        ),
      ),
    );
  }

  /// 构建数据行
  DataRow _buildDataRow(BuildContext context, model.User user) {
    return DataRow(
      cells: [
        DataCell(Text(user.employeeNo)),
        DataCell(Text(user.username)),
        DataCell(Text(user.email)),
        DataCell(
          Wrap(
            spacing: 4,
            children: user.roles
                .map((role) => Chip(
                      label: Text(role.roleName),
                      labelStyle: const TextStyle(fontSize: 12),
                      padding: const EdgeInsets.symmetric(horizontal: 4),
                      backgroundColor: AppTheme.lightPurple.withOpacity(0.1),
                    ))
                .toList(),
          ),
        ),
        DataCell(
          _buildStatusChip(user),
        ),
        DataCell(Text(
          user.createdAt != null
              ? '${user.createdAt!.year}-${user.createdAt!.month.toString().padLeft(2, '0')}-${user.createdAt!.day.toString().padLeft(2, '0')}'
              : '-',
        )),
        DataCell(
          Wrap(
            spacing: 8,
            children: [
              IconButton(
                icon: const Icon(Icons.edit, size: 18),
                onPressed: () => _showUserDialog(context, user: user),
                tooltip: '编辑',
              ),
              IconButton(
                icon: Icon(
                    user.isEnabled ? Icons.block : Icons.check_circle,
                    size: 18,
                    color: user.isEnabled ? Colors.orange : Colors.green),
                onPressed: () => _toggleUserStatus(context, user),
                tooltip: user.isEnabled ? '禁用' : '启用',
              ),
              IconButton(
                icon: const Icon(Icons.lock_reset, size: 18),
                onPressed: () => _showResetPasswordDialog(context, user),
                tooltip: '重置密码',
              ),
            ],
          ),
        ),
      ],
    );
  }

  /// 构建状态标签
  Widget _buildStatusChip(model.User user) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
      decoration: BoxDecoration(
        color: user.isEnabled ? Colors.green.shade50 : Colors.red.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(
          color: user.isEnabled ? Colors.green.shade200 : Colors.red.shade200,
        ),
      ),
      child: Text(
        user.isEnabled ? '正常' : '已禁用',
        style: TextStyle(
          color: user.isEnabled ? Colors.green.shade700 : Colors.red.shade700,
          fontSize: 12,
        ),
      ),
    );
  }

  /// 构建分页组件
  Widget _buildPagination() {
    final userProvider = context.watch<UserProvider>();

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text('共 ${userProvider.total} 条记录'),
            Row(
              children: [
                IconButton(
                  icon: const Icon(Icons.chevron_left),
                  onPressed: userProvider.currentPage > 1
                      ? () => userProvider.previousPage()
                      : null,
                ),
                Text(
                    '第 ${userProvider.currentPage} / ${userProvider.totalPages} 页'),
                IconButton(
                  icon: const Icon(Icons.chevron_right),
                  onPressed: userProvider.currentPage < userProvider.totalPages
                      ? () => userProvider.nextPage()
                      : null,
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  /// 显示用户编辑弹窗
  void _showUserDialog(BuildContext context, {model.User? user}) {
    showDialog(
      context: context,
      builder: (context) => UserDialog(user: user),
    );
  }

  /// 显示重置密码弹窗
  void _showResetPasswordDialog(BuildContext context, model.User user) {
    showDialog(
      context: context,
      builder: (context) => ResetPasswordDialog(user: user),
    );
  }

  /// 切换用户状态
  Future<void> _toggleUserStatus(BuildContext context, model.User user) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: Text(user.isEnabled ? '禁用用户' : '启用用户'),
        content: Text('确定要${user.isEnabled ? "禁用" : "启用"}用户 ${user.username} 吗？'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('取消'),
          ),
          TextButton(
            onPressed: () => Navigator.pop(context, true),
            child: const Text('确定'),
          ),
        ],
      ),
    );

    if (confirmed == true && mounted) {
      final provider = context.read<UserProvider>();
      final (success, error) = await provider.toggleUserStatus(
            user.id,
            user.isEnabled ? 0 : 1,
          );

      if (!mounted) return;

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(success ? '操作成功' : error ?? '操作失败'),
          backgroundColor: success ? Colors.green : Colors.red,
        ),
      );
    }
  }
}
