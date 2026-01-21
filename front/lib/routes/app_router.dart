import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import '../layouts/main_layout.dart';
import '../providers/auth_provider.dart';
import '../screens/dashboard_screen.dart';
import '../screens/login_screen.dart';
import '../screens/placeholder_screen.dart';
import '../screens/users_screen.dart';

/// 应用路由配置
/// 使用 go_router 管理所有路由
final appRouter = GoRouter(
  initialLocation: '/dashboard',
  redirect: (context, state) => _redirect(context, state),
  routes: _appRoutes,
);

/// 路由重定向逻辑
/// 根据登录状态决定是否跳转到登录页
String? _redirect(BuildContext context, GoRouterState state) {
  final auth = context.watch<AuthProvider>();
  final isLoggedIn = auth.isLoggedIn;

  // 如果未登录且不在登录页，跳转到登录页
  if (!isLoggedIn && state.uri.path != '/login') {
    return '/login';
  }

  // 如果已登录且在登录页，跳转到首页
  if (isLoggedIn && state.uri.path == '/login') {
    return '/dashboard';
  }

  // 不需要重定向
  return null;
}

/// 所有路由配置
final _appRoutes = [
  // 加载页（仅在登录过程中使用）
  GoRoute(
    path: '/loading',
    pageBuilder: (context, state) => const MaterialPage(
      child: Scaffold(
        body: Center(
          child: CircularProgressIndicator(),
        ),
      ),
    ),
  ),
  // 登录页
  GoRoute(
    path: '/login',
    pageBuilder: (context, state) => const MaterialPage(
      child: LoginScreen(),
    ),
  ),
  // 主应用路由（使用 ShellRoute 实现侧边栏固定）
  ShellRoute(
    builder: (context, state, child) {
      return MainLayout(child: child);
    },
    routes: [
      // 仪表盘（首页）
      GoRoute(
        path: '/dashboard',
        pageBuilder: (context, state) => const NoTransitionPage(
          child: DashboardScreen(),
        ),
      ),
      // 用户管理
      GoRoute(
        path: '/users',
        pageBuilder: (context, state) => const NoTransitionPage(
          child: UsersScreen(),
        ),
      ),
      // 通用占位页（匹配所有未定义路由）
      GoRoute(
        path: '/:path',
        pageBuilder: (context, state) {
          final path = state.pathParameters['path'] ?? '未知页面';
          return NoTransitionPage(
            child: PlaceholderScreen(
              title: _formatTitle(path),
            ),
          );
        },
      ),
    ],
  ),
];

/// 格式化路径为标题
/// 例如: 'user-management' -> 'User Management'
String _formatTitle(String path) {
  return path
      .split('-')
      .where((word) => word.isNotEmpty) // 过滤空字符串
      .map((word) => word[0].toUpperCase() + word.substring(1))
      .join(' ');
}
