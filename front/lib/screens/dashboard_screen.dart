import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/layout_provider.dart';

/// 仪表盘页面
/// 登录后的默认首页
class DashboardScreen extends StatelessWidget {
  const DashboardScreen({super.key});

  @override
  Widget build(BuildContext context) {
    // 设置当前激活菜单
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<LayoutProvider>().setActiveMenu('dashboard');
    });

    return Scaffold(
      body: Padding(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // 页面标题
            const Text(
              '仪表盘',
              style: TextStyle(
                fontSize: 32,
                fontWeight: FontWeight.bold,
                color: Colors.black87,
              ),
            ),
            const SizedBox(height: 24),

            // 统计卡片网格
            Expanded(
              child: GridView.count(
                crossAxisCount: _calculateColumns(context),
                crossAxisSpacing: 16,
                mainAxisSpacing: 16,
                children: const [
                  _DashboardCard(
                    title: '总用户数',
                    value: '1,234',
                    icon: Icons.people,
                    color: Colors.blue,
                  ),
                  _DashboardCard(
                    title: '交易总额',
                    value: '¥ 89,432',
                    icon: Icons.account_balance_wallet,
                    color: Colors.green,
                  ),
                  _DashboardCard(
                    title: '活跃钱包',
                    value: '456',
                    icon: Icons.wallet,
                    color: Colors.orange,
                  ),
                  _DashboardCard(
                    title: '待处理',
                    value: '12',
                    icon: Icons.pending,
                    color: Colors.red,
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  /// 根据屏幕宽度计算列数
  int _calculateColumns(BuildContext context) {
    final width = MediaQuery.of(context).size.width;
    if (width > 1600) return 4;
    if (width > 1200) return 3;
    if (width > 800) return 2;
    return 1;
  }
}

/// 仪表盘统计卡片
class _DashboardCard extends StatelessWidget {
  final String title;
  final String value;
  final IconData icon;
  final Color color;

  const _DashboardCard({
    required this.title,
    required this.value,
    required this.icon,
    required this.color,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 2,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
      ),
      child: Padding(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              icon,
              size: 48,
              color: color,
            ),
            const SizedBox(height: 16),
            Text(
              value,
              style: TextStyle(
                fontSize: 36,
                fontWeight: FontWeight.bold,
                color: color,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              title,
              style: TextStyle(
                fontSize: 16,
                color: Colors.grey.shade600,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
