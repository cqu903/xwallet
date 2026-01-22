import 'package:flutter/material.dart';
import '../models/menu_item.dart';

/// 菜单权限树组件
/// 支持层级展示和复选框选择
class MenuPermissionTree extends StatefulWidget {
  final List<MenuItem> menus;
  final List<int> selectedMenuIds;
  final Function(List<int>) onChanged;

  const MenuPermissionTree({
    super.key,
    required this.menus,
    required this.selectedMenuIds,
    required this.onChanged,
  });

  @override
  State<MenuPermissionTree> createState() => _MenuPermissionTreeState();
}

class _MenuPermissionTreeState extends State<MenuPermissionTree> {
  late Set<int> _selectedIds;

  @override
  void initState() {
    super.initState();
    _selectedIds = widget.selectedMenuIds.toSet();
  }

  @override
  void didUpdateWidget(MenuPermissionTree oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.selectedMenuIds != widget.selectedMenuIds) {
      setState(() {
        _selectedIds = widget.selectedMenuIds.toSet();
      });
    }
  }

  void _onMenuTapped(int menuId, bool isParent, List<MenuItem> children) {
    setState(() {
      if (_selectedIds.contains(menuId)) {
        // 取消选中
        _selectedIds.remove(menuId);
        // 如果是父菜单，同时取消所有子菜单
        if (isParent) {
          for (final child in children) {
            _selectedIds.remove(int.tryParse(child.id) ?? 0);
          }
        }
      } else {
        // 选中
        _selectedIds.add(menuId);
        // 如果是父菜单，同时选中所有子菜单
        if (isParent) {
          for (final child in children) {
            _selectedIds.add(int.tryParse(child.id) ?? 0);
          }
        }
      }
      widget.onChanged(_selectedIds.toList());
    });
  }

  bool _isMenuChecked(int menuId) {
    return _selectedIds.contains(menuId);
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: widget.menus.map((menu) {
        final menuId = int.tryParse(menu.id) ?? 0;
        final isChecked = _isMenuChecked(menuId);

        return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // 父菜单
            CheckboxListTile(
              title: Text(
                menu.name,
                style: const TextStyle(fontWeight: FontWeight.bold),
              ),
              value: isChecked,
              onChanged: (value) {
                if (value != null) {
                  _onMenuTapped(menuId, menu.hasChildren, menu.children);
                }
              },
              contentPadding: const EdgeInsets.only(left: 8, right: 8),
              controlAffinity: ListTileControlAffinity.leading,
            ),

            // 子菜单
            if (menu.hasChildren)
              Padding(
                padding: const EdgeInsets.only(left: 32),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: menu.children.map((child) {
                    final childId = int.tryParse(child.id) ?? 0;
                    final isChildChecked = _isMenuChecked(childId);

                    return CheckboxListTile(
                      title: Text(child.name),
                      value: isChildChecked,
                      onChanged: (value) {
                        if (value != null) {
                          _onMenuTapped(childId, false, []);
                        }
                      },
                      contentPadding:
                          const EdgeInsets.only(left: 8, right: 8, top: 0),
                      controlAffinity: ListTileControlAffinity.leading,
                    );
                  }).toList(),
                ),
              ),

            const Divider(),
          ],
        );
      }).toList(),
    );
  }
}
