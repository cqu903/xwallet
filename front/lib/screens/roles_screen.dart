import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/role_detail.dart';
import '../models/user.dart';
import '../providers/role_provider.dart';
import '../widgets/role_form_dialog.dart';

/// 角色管理页面
class RolesScreen extends StatefulWidget {
  const RolesScreen({super.key});

  @override
  State<RolesScreen> createState() => _RolesScreenState();
}

class _RolesScreenState extends State<RolesScreen> {
  @override
  void initState() {
    super.initState();
    // 加载角色列表
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<RoleProvider>().loadRoles();
    });
  }

  Future<void> _showRoleForm(RoleDetail? role) async {
    final result = await showDialog<dynamic>(
      context: context,
      builder: (context) => RoleFormDialog(role: role),
    );

    if (result != null) {
      // 提交表单
      final roleProvider = context.read<RoleProvider>();
      bool success;

      if (result is CreateRoleRequest) {
        success = await roleProvider.createRole(result);
      } else if (result is UpdateRoleRequest) {
        success = await roleProvider.updateRole(role!.id, result);
      } else {
        return;
      }

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(success ? '操作成功' : roleProvider.errorMessage ?? '操作失败'),
            backgroundColor: success ? Colors.green : Colors.red,
          ),
        );
      }
    }
  }

  Future<void> _handleDelete(Role role) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('确认删除'),
        content: Text('确定要删除角色 "${role.roleName}" 吗？\n'
            '这将同时删除该角色与所有用户和菜单的关联。'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(false),
            child: const Text('取消'),
          ),
          FilledButton(
            onPressed: () => Navigator.of(context).pop(true),
            style: FilledButton.styleFrom(backgroundColor: Colors.red),
            child: const Text('删除'),
          ),
        ],
      ),
    );

    if (confirmed == true && mounted) {
      final roleProvider = context.read<RoleProvider>();
      final success = await roleProvider.deleteRole(role.id);

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(success ? '删除成功' : roleProvider.errorMessage ?? '删除失败'),
            backgroundColor: success ? Colors.green : Colors.red,
          ),
        );
      }
    }
  }

  Future<void> _handleToggleStatus(Role role) async {
    final newStatus = role.isEnabled ? 0 : 1;
    final roleProvider = context.read<RoleProvider>();
    final success = await roleProvider.toggleStatus(role.id, newStatus);

    if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            success ? (newStatus == 1 ? '角色已启用' : '角色已禁用')
                : (roleProvider.errorMessage ?? '操作失败'),
          ),
          backgroundColor: success ? Colors.green : Colors.red,
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // 标题栏
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text(
                    '角色管理',
                    style: TextStyle(
                      fontSize: 24,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  FilledButton.icon(
                    onPressed: () => _showRoleForm(null),
                    icon: const Icon(Icons.add),
                    label: const Text('新增角色'),
                  ),
                ],
              ),
              const SizedBox(height: 16),

              // 角色列表
              Expanded(
                child: Consumer<RoleProvider>(
                  builder: (context, roleProvider, child) {
                    if (roleProvider.isLoading) {
                      return const Center(child: CircularProgressIndicator());
                    }

                    if (roleProvider.errorMessage != null &&
                        roleProvider.roles.isEmpty) {
                      return Center(
                        child: Text(
                          '加载失败: ${roleProvider.errorMessage}',
                          style: const TextStyle(color: Colors.red),
                        ),
                      );
                    }

                    if (roleProvider.roles.isEmpty) {
                      return const Center(
                        child: Text('暂无角色数据'),
                      );
                    }

                    return SingleChildScrollView(
                      child: Card(
                        child: DataTable(
                          columns: const [
                            DataColumn(label: Text('角色编码')),
                            DataColumn(label: Text('角色名称')),
                            DataColumn(label: Text('角色描述')),
                            DataColumn(label: Text('关联用户')),
                            DataColumn(label: Text('状态')),
                            DataColumn(label: Text('操作')),
                          ],
                          rows: roleProvider.roles.map((role) {
                            return DataRow(
                              cells: [
                                DataCell(Text(role.roleCode)),
                                DataCell(Text(role.roleName)),
                                DataCell(
                                  Text(
                                    role.description ?? '无',
                                    overflow: TextOverflow.ellipsis,
                                  ),
                                ),
                                DataCell(Text('${role.userCount ?? 0}')),
                                DataCell(
                                  Chip(
                                    label: Text(
                                      role.isEnabled ? '启用' : '禁用',
                                      style: const TextStyle(fontSize: 12),
                                    ),
                                    backgroundColor: role.isEnabled
                                        ? Colors.green.shade100
                                        : Colors.grey.shade300,
                                  ),
                                ),
                                DataCell(
                                  Row(
                                    children: [
                                      IconButton(
                                        icon: const Icon(Icons.edit, size: 20),
                                        onPressed: () async {
                                          // 获取角色详情
                                          final (detail, error) =
                                              await roleProvider.getRoleDetail(role.id);
                                          if (detail != null) {
                                            _showRoleForm(detail);
                                          } else if (mounted) {
                                            ScaffoldMessenger.of(context).showSnackBar(
                                              SnackBar(
                                                content: Text(error ?? '获取角色详情失败'),
                                                backgroundColor: Colors.red,
                                              ),
                                            );
                                          }
                                        },
                                        tooltip: '编辑',
                                      ),
                                      IconButton(
                                        icon: Icon(
                                          role.isEnabled
                                              ? Icons.block
                                              : Icons.check_circle,
                                          size: 20,
                                          color: role.isEnabled
                                              ? Colors.orange
                                              : Colors.green,
                                        ),
                                        onPressed: () => _handleToggleStatus(role),
                                        tooltip: role.isEnabled ? '禁用' : '启用',
                                      ),
                                      IconButton(
                                        icon: const Icon(Icons.delete, size: 20),
                                        onPressed: () => _handleDelete(role),
                                        tooltip: '删除',
                                        color: Colors.red,
                                      ),
                                    ],
                                  ),
                                ),
                              ],
                            );
                          }).toList(),
                        ),
                      ),
                    );
                  },
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
