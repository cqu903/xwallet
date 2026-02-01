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
          MaterialPageRoute(builder: (_) => const MainNavigation()),
        );
      }
    } else {
      if (mounted) {
        Navigator.of(context).pushReplacement(
          MaterialPageRoute(builder: (_) => const LoginScreen()),
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

/// 主导航容器（含底部导航栏）
class MainNavigation extends StatelessWidget {
  const MainNavigation({super.key});

  static const List<Widget> _pages = [
    HomeScreen(),
    AccountScreen(),
    ProfileScreen(),
  ];

  static const List<IconData> _icons = [
    Icons.home,
    Icons.account_balance_wallet,
    Icons.person,
  ];

  @override
  Widget build(BuildContext context) {
    final navigationProvider = context.watch<NavigationProvider>();

    return Scaffold(
      body: _pages[navigationProvider.currentIndex],
      bottomNavigationBar: Container(
        decoration: BoxDecoration(
          color: Colors.white,
          border: Border(
            top: BorderSide(color: Colors.grey[300]!, width: 1),
          ),
        ),
        child: SafeArea(
          child: SizedBox(
            height: 60,
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceAround,
              children: List.generate(_icons.length, (index) {
                final isSelected = navigationProvider.currentIndex == index;
                return GestureDetector(
                  onTap: () {
                    context.read<NavigationProvider>().setIndex(index);
                  },
                  child: AnimatedContainer(
                    duration: const Duration(milliseconds: 200),
                    padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
                    child: Icon(
                      _icons[index],
                      size: 28,
                      color: isSelected ? const Color(0xFF7424F5) : const Color(0xFF9E9E9E),
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
