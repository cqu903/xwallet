import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import '../models/menu_item.dart';
import '../providers/layout_provider.dart';

/// 单个菜单项组件
/// 支持一级菜单和二级菜单展开/折叠
class SidebarItem extends StatelessWidget {
  final MenuItem menu;
  final int indent;
  final String? activeMenuId;

  const SidebarItem({
    super.key,
    required this.menu,
    this.indent = 0,
    this.activeMenuId,
  });

  @override
  Widget build(BuildContext context) {
    final layoutProvider = context.watch<LayoutProvider>();
    final isExpanded = layoutProvider.isSidebarExpanded;
    final isActive = activeMenuId == menu.id;
    final hasChildren = menu.hasChildren;

    // 折叠状态且是一级菜单时，只显示简短文本或图标
    if (!isExpanded && indent == 0) {
      return _buildCollapsedItem(context, isActive, hasChildren);
    }

    // 展开状态或二级菜单
    if (hasChildren) {
      return _buildExpansionItem(context, isExpanded, isActive);
    } else {
      return _buildLeafItem(context, isExpanded, isActive);
    }
  }

  /// 构建折叠状态的菜单项
  Widget _buildCollapsedItem(BuildContext context, bool isActive, bool hasChildren) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8.0),
      child: Tooltip(
        message: menu.name,
        child: InkWell(
          onTap: hasChildren ? _handleToggle : () => _handleTap(context),
          child: Container(
            height: 48,
            alignment: Alignment.center,
            child: Text(
              _getShortText(menu.name),
              style: TextStyle(
                fontSize: 14,
                fontWeight: isActive ? FontWeight.bold : FontWeight.normal,
                color: isActive ? Colors.blue.shade700 : Colors.grey.shade700,
              ),
              textAlign: TextAlign.center,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
            ),
          ),
        ),
      ),
    );
  }

  /// 构建叶子节点菜单项（无子菜单）
  Widget _buildLeafItem(BuildContext context, bool isExpanded, bool isActive) {
    return Container(
      margin: EdgeInsets.only(
        left: isExpanded ? 8.0 : 0,
        right: 8.0,
        top: 2.0,
        bottom: 2.0,
      ),
      decoration: BoxDecoration(
        color: isActive ? Colors.blue.shade50 : Colors.transparent,
        borderRadius: BorderRadius.circular(8),
      ),
      child: InkWell(
        onTap: () => _handleTap(context),
        borderRadius: BorderRadius.circular(8),
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 12.0),
          child: Row(
            children: [
              if (indent > 0) ...[
                SizedBox(width: 16.0 * indent),
              ],
              Expanded(
                child: Text(
                  menu.name,
                  style: TextStyle(
                    fontSize: 14,
                    fontWeight: isActive ? FontWeight.bold : FontWeight.normal,
                    color: isActive ? Colors.blue.shade700 : Colors.grey.shade800,
                  ),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  /// 构建有子菜单的展开项
  Widget _buildExpansionItem(BuildContext context, bool isExpanded, bool isActive) {
    return Theme(
      data: Theme.of(context).copyWith(
        dividerColor: Colors.transparent,
        splashColor: Colors.transparent,
        highlightColor: Colors.transparent,
      ),
      child: ExpansionTile(
        leading: null,
        title: Padding(
          padding: const EdgeInsets.only(left: 0),
          child: Text(
            menu.name,
            style: TextStyle(
              fontSize: 14,
              fontWeight: isActive ? FontWeight.bold : FontWeight.normal,
              color: isActive ? Colors.blue.shade700 : Colors.grey.shade800,
            ),
          ),
        ),
        tilePadding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 0),
        childrenPadding: const EdgeInsets.only(left: 16.0),
        children: menu.children.map((child) {
          return SidebarItem(
            menu: child,
            indent: indent + 1,
            activeMenuId: activeMenuId,
          );
        }).toList(),
      ),
    );
  }

  /// 获取短文本（用于折叠状态）
  String _getShortText(String text) {
    if (text.length <= 2) return text;
    return text.substring(0, 2);
  }

  /// 处理菜单点击
  void _handleTap(BuildContext context) {
    if (menu.path != null) {
      context.read<LayoutProvider>().setActiveMenu(menu.id);
      context.go(menu.path!);
    }
  }

  /// 处理有子菜单的点击（展开/折叠）
  void _handleToggle() {
    // ExpansionTile 会自动处理展开/折叠
  }
}
