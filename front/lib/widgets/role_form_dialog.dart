import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/role_detail.dart';
import '../providers/menu_provider.dart';
import '../widgets/menu_permission_tree.dart';

/// 角色表单对话框
/// 用于创建和编辑角色
class RoleFormDialog extends StatefulWidget {
  final RoleDetail? role; // null 表示新增，非 null 表示编辑

  const RoleFormDialog({
    super.key,
    this.role,
  });

  @override
  State<RoleFormDialog> createState() => _RoleFormDialogState();
}

class _RoleFormDialogState extends State<RoleFormDialog> {
  final _formKey = GlobalKey<FormState>();
  final _roleCodeController = TextEditingController();
  final _roleNameController = TextEditingController();
  final _descriptionController = TextEditingController();
  int _status = 1;
  List<int> _selectedMenuIds = [];

  bool get _isEdit => widget.role != null;

  @override
  void initState() {
    super.initState();
    if (_isEdit) {
      _roleCodeController.text = widget.role!.roleCode;
      _roleNameController.text = widget.role!.roleName;
      _descriptionController.text = widget.role!.description ?? '';
      _status = widget.role!.status ?? 1;
      _selectedMenuIds = widget.role!.menuIds;
    }
  }

  @override
  void dispose() {
    _roleCodeController.dispose();
    _roleNameController.dispose();
    _descriptionController.dispose();
    super.dispose();
  }

  void _submit() {
    if (_formKey.currentState!.validate()) {
      if (_selectedMenuIds.isEmpty) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('请至少选择一个菜单权限')),
        );
        return;
      }

      final result = _isEdit
          ? UpdateRoleRequest(
              roleName: _roleNameController.text.trim(),
              description: _descriptionController.text.trim(),
              status: _status,
              menuIds: _selectedMenuIds,
            )
          : CreateRoleRequest(
              roleCode: _roleCodeController.text.trim().toUpperCase(),
              roleName: _roleNameController.text.trim(),
              description: _descriptionController.text.trim(),
              status: _status,
              menuIds: _selectedMenuIds,
            );

      Navigator.of(context).pop(result);
    }
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: Text(_isEdit ? '编辑角色' : '新增角色'),
      content: SizedBox(
        width: 600,
        height: 500,
        child: Form(
          key: _formKey,
          child: Column(
            children: [
              Expanded(
                child: SingleChildScrollView(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      // 角色编码
                      if (!_isEdit)
                        TextFormField(
                          controller: _roleCodeController,
                          decoration: const InputDecoration(
                            labelText: '角色编码',
                            hintText: '例如：ADMIN',
                            border: OutlineInputBorder(),
                          ),
                          textCapitalization: TextCapitalization.characters,
                          validator: (value) {
                            if (value == null || value.trim().isEmpty) {
                              return '请输入角色编码';
                            }
                            if (!RegExp(r'^[A-Z0-9]{2,50}$').hasMatch(value.trim())) {
                              return '角色编码必须是2-50位大写字母或数字';
                            }
                            return null;
                          },
                        ),
                      if (!_isEdit) const SizedBox(height: 16),

                      // 角色名称
                      TextFormField(
                        controller: _roleNameController,
                        decoration: const InputDecoration(
                          labelText: '角色名称',
                          hintText: '例如：超级管理员',
                          border: OutlineInputBorder(),
                        ),
                        validator: (value) {
                          if (value == null || value.trim().isEmpty) {
                            return '请输入角色名称';
                          }
                          return null;
                        },
                      ),
                      const SizedBox(height: 16),

                      // 角色描述
                      TextFormField(
                        controller: _descriptionController,
                        decoration: const InputDecoration(
                          labelText: '角色描述',
                          hintText: '请输入角色描述',
                          border: OutlineInputBorder(),
                        ),
                        maxLines: 3,
                      ),
                      const SizedBox(height: 16),

                      // 状态
                      Row(
                        children: [
                          const Text('状态：'),
                          const SizedBox(width: 8),
                          SegmentedButton<int>(
                            segments: const [
                              ButtonSegment(
                                value: 1,
                                label: Text('启用'),
                                icon: Icon(Icons.check_circle),
                              ),
                              ButtonSegment(
                                value: 0,
                                label: Text('禁用'),
                                icon: Icon(Icons.cancel),
                              ),
                            ],
                            selected: {_status},
                            onSelectionChanged: (Set<int> newSelection) {
                              setState(() {
                                _status = newSelection.first;
                              });
                            },
                          ),
                        ],
                      ),
                      const SizedBox(height: 16),

                      // 菜单权限
                      const Text(
                        '菜单权限',
                        style: TextStyle(
                          fontSize: 16,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      const SizedBox(height: 8),
                      Consumer<MenuProvider>(
                        builder: (context, menuProvider, child) {
                          if (menuProvider.isLoading) {
                            return const Center(child: CircularProgressIndicator());
                          }
                          if (menuProvider.errorMessage != null) {
                            return Text(
                              '加载菜单失败: ${menuProvider.errorMessage}',
                              style: const TextStyle(color: Colors.red),
                            );
                          }
                          return MenuPermissionTree(
                            menus: menuProvider.menus,
                            selectedMenuIds: _selectedMenuIds,
                            onChanged: (List<int> selectedIds) {
                              setState(() {
                                _selectedMenuIds = selectedIds;
                              });
                            },
                          );
                        },
                      ),
                    ],
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.of(context).pop(),
          child: const Text('取消'),
        ),
        FilledButton(
          onPressed: _submit,
          child: Text(_isEdit ? '保存' : '创建'),
        ),
      ],
    );
  }
}
