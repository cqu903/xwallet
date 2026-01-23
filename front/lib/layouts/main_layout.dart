import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/layout_provider.dart';
import '../providers/auth_provider.dart';
import '../providers/menu_provider.dart';
import '../theme/app_theme.dart';
import '../widgets/sidebar.dart';

/// 主布局组件
/// 包含左侧侧边栏和右侧内容区域
class MainLayout extends StatelessWidget {
  final Widget child;

  const MainLayout({super.key, required this.child});

  @override
  Widget build(BuildContext context) {
    final layoutProvider = context.watch<LayoutProvider>();
    final authProvider = context.watch<AuthProvider>();
    final menuProvider = context.read<MenuProvider>();

    // 当用户已登录且菜单为空时，尝试加载菜单
    // 这可以处理页面刷新后重新登录的情况
    if (authProvider.isLoggedIn &&
        menuProvider.menus.isEmpty &&
        !menuProvider.isLoading) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        menuProvider.refresh();
      });
    }

    return Scaffold(
      body: Row(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          // 左侧侧边栏
          AnimatedContainer(
            duration: const Duration(milliseconds: 300),
            curve: Curves.easeInOut,
            width: layoutProvider.sidebarWidth,
            child: const Sidebar(),
          ),

          // 右侧内容区
          Expanded(child: _buildContentArea(context, child)),
        ],
      ),
    );
  }

  /// 构建内容区域
  Widget _buildContentArea(BuildContext context, Widget child) {
    return Container(
      decoration: const BoxDecoration(gradient: AppTheme.lightGradient),
      child: child,
    );
  }
}
