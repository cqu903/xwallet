import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/layout_provider.dart';
import '../providers/menu_provider.dart';
import '../theme/app_theme.dart';
import 'sidebar_item.dart';

/// 侧边栏组件
/// 显示系统Logo、菜单列表和折叠按钮
class Sidebar extends StatelessWidget {
  const Sidebar({super.key});

  @override
  Widget build(BuildContext context) {
    final layoutProvider = context.watch<LayoutProvider>();
    final menuProvider = context.watch<MenuProvider>();
    final width = layoutProvider.sidebarWidth;

    return Container(
      width: width,
      decoration: const BoxDecoration(
        gradient: AppTheme.sidebarGradient,
        boxShadow: [
          BoxShadow(
            color: Colors.black12,
            blurRadius: 8,
            offset: Offset(2, 0),
          ),
        ],
      ),
      child: Column(
        children: [
          // Logo区
          _buildLogoArea(layoutProvider.isSidebarExpanded),

          // 菜单列表
          Expanded(
            child: _buildMenuArea(menuProvider, layoutProvider.activeMenuId),
          ),

          // 底部控制区
          _buildBottomArea(context, layoutProvider, menuProvider),
        ],
      ),
    );
  }

  /// 构建Logo区域
  Widget _buildLogoArea(bool isExpanded) {
    return Container(
      height: 64,
      padding: EdgeInsets.symmetric(horizontal: isExpanded ? 16.0 : 0),
      decoration: BoxDecoration(
        border: Border(
          bottom: BorderSide(
            color: Colors.white.withOpacity(0.2),
            width: 1,
          ),
        ),
      ),
      child: Row(
        mainAxisAlignment: isExpanded ? MainAxisAlignment.start : MainAxisAlignment.center,
        children: [
          Container(
            padding: const EdgeInsets.all(8),
            decoration: BoxDecoration(
              color: Colors.white.withOpacity(0.2),
              borderRadius: BorderRadius.circular(8),
            ),
            child: const Icon(
              Icons.account_balance_wallet,
              color: Colors.white,
              size: 24,
            ),
          ),
          if (isExpanded) ...[
            const SizedBox(width: 12),
            const Text(
              'xWallet',
              style: TextStyle(
                fontSize: 20,
                fontWeight: FontWeight.bold,
                color: Colors.white,
              ),
            ),
          ],
        ],
      ),
    );
  }

  /// 构建菜单区域
  Widget _buildMenuArea(MenuProvider menuProvider, String? activeMenuId) {
    if (menuProvider.isLoading) {
      return const Center(
        child: CircularProgressIndicator(),
      );
    }

    if (menuProvider.errorMessage != null) {
      return Center(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(
                Icons.error_outline,
                color: Colors.white.withOpacity(0.8),
                size: 48,
              ),
              const SizedBox(height: 16),
              Text(
                menuProvider.errorMessage!,
                style: TextStyle(
                  fontSize: 14,
                  color: Colors.white.withOpacity(0.9),
                ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 16),
              ElevatedButton(
                onPressed: () => menuProvider.refresh(),
                child: const Text('重试'),
              ),
            ],
          ),
        ),
      );
    }

    if (menuProvider.menus.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.menu_outlined,
              color: Colors.white.withOpacity(0.5),
              size: 48,
            ),
            const SizedBox(height: 16),
            Text(
              '暂无菜单',
              style: TextStyle(
                fontSize: 14,
                color: Colors.white.withOpacity(0.7),
              ),
            ),
          ],
        ),
      );
    }

    return ListView.builder(
      padding: const EdgeInsets.symmetric(vertical: 8.0),
      itemCount: menuProvider.menus.length,
      itemBuilder: (context, index) {
        return SidebarItem(
          menu: menuProvider.menus[index],
          activeMenuId: activeMenuId,
        );
      },
    );
  }

  /// 构建底部控制区
  Widget _buildBottomArea(BuildContext context, LayoutProvider layoutProvider, MenuProvider menuProvider) {
    final isExpanded = layoutProvider.isSidebarExpanded;

    return Column(
      children: [
        Divider(
          color: Colors.white.withOpacity(0.2),
          height: 1,
        ),
        // 折叠/展开按钮
        ListTile(
          leading: Icon(
            isExpanded ? Icons.menu_open : Icons.menu,
            color: Colors.white.withOpacity(0.9),
          ),
          title: isExpanded
              ? Text(
                  '折叠菜单',
                  style: TextStyle(
                    fontSize: 14,
                    color: Colors.white.withOpacity(0.9),
                  ),
                )
              : null,
          onTap: layoutProvider.toggleSidebar,
        ),
      ],
    );
  }
}
