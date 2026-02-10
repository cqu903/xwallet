import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../analytics/analytics_constants.dart';
import '../analytics/event_spec.dart';
import '../models/analytics_event.dart';
import '../providers/auth_provider.dart';
import '../widgets/analytics/analytics_icon_button.dart';
import '../widgets/analytics/analytics_list_tile.dart';
import '../widgets/analytics/analytics_pressable.dart';
import '../widgets/x_wallet_logo.dart';

/// 个人页面 - 占位（包含登出功能）
class ProfileScreen extends StatelessWidget {
  const ProfileScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final authProvider = context.watch<AuthProvider>();
    final userInfo = authProvider.currentUser?.userInfo;

    return Scaffold(
      backgroundColor: const Color(0xFFF5F5F5),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(24),
          child: Column(
            children: [
              const SizedBox(height: 40),

              // 头像
              CircleAvatar(
                radius: 50,
                backgroundColor: const Color(0xFF2E7D32),
                child: Text(
                  _getInitial(userInfo?.username),
                  style: const TextStyle(
                    fontSize: 36,
                    fontWeight: FontWeight.bold,
                    color: Colors.white,
                  ),
                ),
              ),

              const SizedBox(height: 16),

              // 用户名
              Text(
                userInfo?.username ?? '顾客',
                style: const TextStyle(
                  fontSize: 24,
                  fontWeight: FontWeight.bold,
                  color: Color(0xFF424242),
                ),
              ),

              const SizedBox(height: 8),

              // 用户类型
              Text(
                userInfo?.userType == 'CUSTOMER' ? '顾客用户' : '系统用户',
                style: TextStyle(fontSize: 14, color: Colors.grey[600]),
              ),

              const SizedBox(height: 12),

              Align(
                alignment: Alignment.centerRight,
                child: AnalyticsIconButton(
                  tooltip: '关于我们',
                  icon: const Icon(
                    Icons.info_outline,
                    color: Color(0xFF7424F5),
                  ),
                  eventType: AnalyticsEventType.linkClick,
                  properties: AnalyticsEventProperties.click(
                    page: AnalyticsPages.profile,
                    flow: AnalyticsFlows.account,
                    elementId: AnalyticsIds.profileAboutIcon,
                    elementType: AnalyticsElementType.icon,
                    elementText: '关于我们',
                  ),
                  category: EventCategory.behavior,
                  onPressed: () => _showAboutDialog(context),
                ),
              ),

              const SizedBox(height: 28),

              // 菜单项列表
              _MenuItem(
                icon: Icons.settings_outlined,
                title: '设置',
                elementId: AnalyticsIds.profileSettings,
                onTap: () {
                  ScaffoldMessenger.of(
                    context,
                  ).showSnackBar(const SnackBar(content: Text('设置功能开发中...')));
                },
              ),
              _MenuItem(
                icon: Icons.help_outline,
                title: '帮助中心',
                elementId: AnalyticsIds.profileHelpCenter,
                onTap: () {
                  ScaffoldMessenger.of(
                    context,
                  ).showSnackBar(const SnackBar(content: Text('帮助中心开发中...')));
                },
              ),
              _MenuItem(
                icon: Icons.info_outline,
                title: '关于我们',
                elementId: AnalyticsIds.profileAbout,
                onTap: () {
                  _showAboutDialog(context);
                },
              ),

              const SizedBox(height: 24),

              // 登出按钮
              SizedBox(
                width: double.infinity,
                child: AnalyticsPressable(
                  eventType: AnalyticsEventType.buttonClick,
                  properties: AnalyticsEventProperties.click(
                    page: AnalyticsPages.profile,
                    flow: AnalyticsFlows.account,
                    elementId: AnalyticsIds.logout,
                    elementType: AnalyticsElementType.button,
                    elementText: '退出登录',
                  ),
                  category: EventCategory.behavior,
                  onPressed: () => _handleLogout(context),
                  child: Container(
                    padding: const EdgeInsets.symmetric(vertical: 16),
                    decoration: BoxDecoration(
                      color: Colors.red,
                      borderRadius: BorderRadius.circular(12),
                    ),
                    alignment: Alignment.center,
                    child: const Text(
                      '退出登录',
                      style: TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.w500,
                        color: Colors.white,
                      ),
                    ),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Future<void> _handleLogout(BuildContext context) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: const Text('确认登出'),
        content: const Text('确定要退出登录吗？'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialogContext, false),
            child: const Text('取消'),
          ),
          TextButton(
            onPressed: () => Navigator.pop(dialogContext, true),
            child: const Text('确定'),
          ),
        ],
      ),
    );

    if (confirmed == true && context.mounted) {
      await context.read<AuthProvider>().logout();
      // 主导航容器会处理跳转
    }
  }

  String _getInitial(String? name) {
    if (name == null || name.isEmpty) return 'U';
    return name.substring(0, 1).toUpperCase();
  }

  void _showAboutDialog(BuildContext context) {
    showAboutDialog(
      context: context,
      applicationName: 'X Wallet',
      applicationVersion: '1.0.0',
      applicationIcon: const XWalletLogo(size: 48),
      children: const [Text('X Wallet 是一款便捷的移动钱包应用')],
    );
  }
}

/// 菜单项组件
class _MenuItem extends StatelessWidget {
  final IconData icon;
  final String title;
  final String elementId;
  final VoidCallback onTap;

  const _MenuItem({
    required this.icon,
    required this.title,
    required this.elementId,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
      ),
      child: AnalyticsListTile(
        leading: Icon(icon, color: const Color(0xFF7424F5)),
        title: Text(title),
        trailing: const Icon(Icons.chevron_right, color: Colors.grey),
        eventType: AnalyticsEventType.linkClick,
        properties: AnalyticsEventProperties.click(
          page: AnalyticsPages.profile,
          flow: AnalyticsFlows.account,
          elementId: elementId,
          elementType: AnalyticsElementType.listItem,
          elementText: title,
        ),
        category: EventCategory.behavior,
        onTap: onTap,
      ),
    );
  }
}
