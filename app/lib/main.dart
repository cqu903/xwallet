import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'providers/auth_provider.dart';
import 'providers/navigation_provider.dart';
import 'screens/login_screen.dart';
import 'screens/home_screen.dart';
import 'screens/account_screen.dart';
import 'screens/profile_screen.dart';
import 'services/analytics_service.dart';
import 'widgets/x_wallet_logo.dart';
import 'config/app_config.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // 加载配置
  await AppConfig.load();

  // 初始化埋点服务
  try {
    await AnalyticsService.instance.initialize();
  } catch (e) {
    print('Analytics initialization failed: $e');
    // 即使初始化失败也继续启动应用
  }

  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => AuthProvider()),
        ChangeNotifierProvider(create: (_) => NavigationProvider()),
      ],
      child: MaterialApp(
        title: 'X Wallet',
        debugShowCheckedModeBanner: false,
        theme: ThemeData(
          colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xFF7424F5)),
          useMaterial3: true,
        ),
        home: const SplashScreen(),
      ),
    );
  }
}

/// 启动闪屏/重定向页面
class SplashScreen extends StatefulWidget {
  const SplashScreen({super.key});

  @override
  State<SplashScreen> createState() => _SplashScreenState();
}

class _SplashScreenState extends State<SplashScreen> {
  @override
  void initState() {
    super.initState();
    _checkAuthStatus();
  }

  Future<void> _checkAuthStatus() async {
    final authProvider = context.read<AuthProvider>();

    // 等待 AuthProvider 初始化完成
    await Future.delayed(const Duration(milliseconds: 100));

    if (!mounted) return;

    // 根据登录状态跳转
    if (authProvider.isLoggedIn) {
      if (mounted) {
        Navigator.of(context).pushReplacement(
          PageRouteBuilder(
            pageBuilder: (context, animation, secondaryAnimation) =>
                const MainNavigation(),
            transitionDuration: Duration.zero,
            reverseTransitionDuration: Duration.zero,
          ),
        );
      }
    } else {
      if (mounted) {
        Navigator.of(context).pushReplacement(
          PageRouteBuilder(
            pageBuilder: (context, animation, secondaryAnimation) =>
                const LoginScreen(),
            transitionDuration: Duration.zero,
            reverseTransitionDuration: Duration.zero,
          ),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF7424F5),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const XWalletLogo(size: 80),
            const SizedBox(height: 16),
            const Text(
              'X Wallet',
              style: TextStyle(
                fontSize: 32,
                fontWeight: FontWeight.bold,
                color: Colors.white,
              ),
            ),
            const SizedBox(height: 32),
            const CircularProgressIndicator(color: Colors.white),
          ],
        ),
      ),
    );
  }
}

/// 主导航容器（含底部导航栏）- 匹配设计稿的4个Tab
class MainNavigation extends StatelessWidget {
  const MainNavigation({super.key});

  static const List<Widget> _pages = [
    HomeScreen(), // 首页
    AccountScreen(), // 钱包
    HistoryScreen(), // 记录
    ProfileScreen(), // 我的
  ];

  static const List<_TabItem> _tabItems = [
    _TabItem(icon: Icons.home, label: '首页'),
    _TabItem(icon: Icons.account_balance_wallet, label: '钱包'),
    _TabItem(icon: Icons.history, label: '记录'),
    _TabItem(icon: Icons.person, label: '我的'),
  ];

  @override
  Widget build(BuildContext context) {
    final navigationProvider = context.watch<NavigationProvider>();

    return Scaffold(
      body: _pages[navigationProvider.currentIndex],
      bottomNavigationBar: Container(
        decoration: BoxDecoration(
          gradient: const LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [Colors.white, Color(0xFFFDFCFF)],
          ),
          boxShadow: [
            BoxShadow(
              color: const Color(0xFF000000).withOpacity(0.08),
              blurRadius: 20,
              offset: const Offset(0, -4),
            ),
          ],
        ),
        child: SafeArea(
          child: Padding(
            padding: const EdgeInsets.only(top: 12, bottom: 8),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceAround,
              children: List.generate(_tabItems.length, (index) {
                final isSelected = navigationProvider.currentIndex == index;
                final item = _tabItems[index];
                return GestureDetector(
                  onTap: () {
                    context.read<NavigationProvider>().setIndex(index);
                  },
                  child: SizedBox(
                    width: 80,
                    child: Column(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Icon(
                          item.icon,
                          size: 24,
                          color: isSelected
                              ? const Color(0xFF7424F5)
                              : const Color(0xFF666666),
                        ),
                        const SizedBox(height: 4),
                        Text(
                          item.label,
                          style: TextStyle(
                            fontSize: 11,
                            fontWeight: isSelected
                                ? FontWeight.w600
                                : FontWeight.w500,
                            color: isSelected
                                ? const Color(0xFF7424F5)
                                : const Color(0xFF666666),
                          ),
                        ),
                      ],
                    ),
                  ),
                );
              }),
            ),
          ),
        ),
      ),
    );
  }
}

/// Tab项数据类
class _TabItem {
  final IconData icon;
  final String label;

  const _TabItem({required this.icon, required this.label});
}

/// 记录页面（占位）
class HistoryScreen extends StatelessWidget {
  const HistoryScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF5F3FF),
      appBar: AppBar(
        title: const Text('交易记录'),
        backgroundColor: const Color(0xFF7424F5),
        foregroundColor: Colors.white,
        elevation: 0,
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.history,
              size: 80,
              color: const Color(0xFF7424F5).withOpacity(0.3),
            ),
            const SizedBox(height: 16),
            Text(
              '交易记录',
              style: TextStyle(
                fontSize: 18,
                color: const Color(0xFF666666).withOpacity(0.8),
              ),
            ),
            const SizedBox(height: 8),
            Text(
              '暂无交易记录',
              style: TextStyle(
                fontSize: 14,
                color: const Color(0xFF999999).withOpacity(0.8),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
