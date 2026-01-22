import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/user.dart';
import '../providers/user_provider.dart';
import '../providers/role_provider.dart';
import '../services/user_service.dart';

/// 用户编辑弹窗
/// 用于创建和编辑用户
class UserDialog extends StatefulWidget {
  final User? user;

  const UserDialog({super.key, this.user});

  @override
  State<UserDialog> createState() => _UserDialogState();
}

class _UserDialogState extends State<UserDialog> {
  final _formKey = GlobalKey<FormState>();
  final _employeeNoController = TextEditingController();
  final _usernameController = TextEditingController();
  final _emailController = TextEditingController();
  final _passwordController = TextEditingController();

  final Set<int> _selectedRoleIds = {};
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    if (widget.user != null) {
      // 编辑模式
      _employeeNoController.text = widget.user!.employeeNo;
      _usernameController.text = widget.user!.username;
      _emailController.text = widget.user!.email;
      _selectedRoleIds.addAll(
        widget.user!.roles.map((r) => int.tryParse(r.id) ?? 0).where((id) => id > 0),
      );
    }
  }

  @override
  void dispose() {
    _employeeNoController.dispose();
    _usernameController.dispose();
    _emailController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final isEditMode = widget.user != null;

    return AlertDialog(
      title: Text(isEditMode ? '编辑用户' : '新增用户'),
      content: SizedBox(
        width: 500,
        child: Form(
          key: _formKey,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextFormField(
                controller: _employeeNoController,
                decoration: const InputDecoration(
                  labelText: '工号',
                  border: OutlineInputBorder(),
                ),
                enabled: !isEditMode,
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return '请输入工号';
                  }
                  if (!RegExp(r'^[A-Z0-9]{3,20}$').hasMatch(value)) {
                    return '工号必须是3-20位大写字母或数字';
                  }
                  return null;
                },
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _usernameController,
                decoration: const InputDecoration(
                  labelText: '姓名',
                  border: OutlineInputBorder(),
                ),
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return '请输入姓名';
                  }
                  return null;
                },
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _emailController,
                decoration: const InputDecoration(
                  labelText: '邮箱',
                  border: OutlineInputBorder(),
                ),
                keyboardType: TextInputType.emailAddress,
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return '请输入邮箱';
                  }
                  if (!RegExp(r'^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$').hasMatch(value)) {
                    return '邮箱格式不正确';
                  }
                  return null;
                },
              ),
              const SizedBox(height: 16),
              if (!isEditMode)
                TextFormField(
                  controller: _passwordController,
                  decoration: const InputDecoration(
                    labelText: '密码',
                    border: OutlineInputBorder(),
                  ),
                  obscureText: true,
                  validator: (value) {
                    if (value == null || value.isEmpty) {
                      return '请输入密码';
                    }
                    if (value.length < 6 || value.length > 20) {
                      return '密码长度必须是6-20位';
                    }
                    return null;
                  },
                ),
              if (!isEditMode) const SizedBox(height: 16),
              _buildRoleSelector(),
            ],
          ),
        ),
      ),
      actions: [
        TextButton(
          onPressed: _isLoading ? null : () => Navigator.pop(context),
          child: const Text('取消'),
        ),
        ElevatedButton(
          onPressed: _isLoading ? null : _handleSubmit,
          child: _isLoading
              ? const SizedBox(
                  width: 16,
                  height: 16,
                  child: CircularProgressIndicator(strokeWidth: 2),
                )
              : const Text('保存'),
        ),
      ],
    );
  }

  /// 构建角色选择器
  Widget _buildRoleSelector() {
    return Consumer<RoleProvider>(
      builder: (context, roleProvider, child) {
        if (roleProvider.isLoading) {
          return const Center(child: CircularProgressIndicator());
        }

        if (roleProvider.roles.isEmpty) {
          return const Text('暂无可用角色');
        }

        return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text('角色（至少选择一个）',
                style: TextStyle(fontWeight: FontWeight.bold)),
            const SizedBox(height: 8),
            Wrap(
              spacing: 8,
              children: roleProvider.roles.map((role) {
                final roleId = int.tryParse(role.id) ?? 0;
                final isSelected = _selectedRoleIds.contains(roleId);
                return FilterChip(
                  label: Text(role.roleName),
                  selected: isSelected,
                  onSelected: (selected) {
                    setState(() {
                      if (selected) {
                        _selectedRoleIds.add(roleId);
                      } else {
                        _selectedRoleIds.remove(roleId);
                      }
                    });
                  },
                  selectedColor: Colors.blue.shade100,
                  checkmarkColor: Colors.blue,
                );
              }).toList(),
            ),
          ],
        );
      },
    );
  }

  /// 处理提交
  Future<void> _handleSubmit() async {
    if (!_formKey.currentState!.validate()) {
      return;
    }

    if (_selectedRoleIds.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('请至少选择一个角色')),
      );
      return;
    }

    setState(() {
      _isLoading = true;
    });

    final userProvider = context.read<UserProvider>();

    final (success, error) = widget.user == null
        ? await userProvider.createUser(CreateUserRequest(
            employeeNo: _employeeNoController.text,
            username: _usernameController.text,
            email: _emailController.text,
            password: _passwordController.text,
            roleIds: _selectedRoleIds.toList(),
          ))
        : await userProvider.updateUser(
            widget.user!.id,
            UpdateUserRequest(
              username: _usernameController.text,
              email: _emailController.text,
              roleIds: _selectedRoleIds.toList(),
            ),
          );

    setState(() {
      _isLoading = false;
    });

    if (mounted) {
      if (success) {
        Navigator.pop(context);
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(widget.user == null ? '用户创建成功' : '用户更新成功'),
            backgroundColor: Colors.green,
          ),
        );
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(error ?? '操作失败'),
            backgroundColor: Colors.red,
          ),
        );
      }
    }
  }
}
