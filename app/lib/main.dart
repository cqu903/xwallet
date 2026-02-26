import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'providers/auth_provider.dart';
import 'providers/contract_provider.dart';
import 'providers/loan_application_provider.dart';
import 'providers/navigation_provider.dart';
import 'providers/transaction_provider.dart';
import 'screens/login_screen.dart';
import 'screens/loan/loan_apply_flow_screen.dart';
import 'screens/register_screen.dart';
import 'screens/home_screen.dart';
import 'screens/account_screen.dart';
import 'screens/profile_screen.dart';
import 'screens/history_screen.dart';
import 'screens/contract_list_page.dart';
import 'screens/repayment_page.dart';
import 'services/analytics_service.dart';
import 'widgets/x_wallet_logo.dart';
import 'config/app_config.dart';
import 'analytics/analytics_route_observer.dart';
import 'analytics/app_routes.dart';
import 'analytics/analytics_constants.dart';
import 'analytics/event_spec.dart';
import 'analytics/analytics_error_handler.dart';
import 'widgets/analytics/tracked_bottom_nav_bar.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  AnalyticsErrorHandler.install();

  // 加载配置
  await AppConfig.load();

  // 初始化埋点服务
  try {
    await AnalyticsService.instance.initialize();
  } catch (e) {
    debugPrint('Analytics initialization failed: $e');
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
        ChangeNotifierProvider(create: (_) => TransactionProvider()),
        ChangeNotifierProvider(create: (_) => LoanApplicationProvider()),
        ChangeNotifierProvider(create: (_) => ContractProvider()),
      ],
      child: MaterialApp(
        title: 'X Wallet',
        debugShowCheckedModeBanner: false,
        navigatorObservers: [analyticsRouteObserver],
        theme: ThemeData(
          colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xFF7424F5)),
          useMaterial3: true,
        ),
        initialRoute: AppRoutes.splash,
        onGenerateRoute: (settings) {
          switch (settings.name) {
            case AppRoutes.splash:
              return MaterialPageRoute(
                builder: (_) => const SplashScreen(),
                settings: settings,
              );
            case AppRoutes.login:
              return MaterialPageRoute(
                builder: (_) => const LoginScreen(),
                settings: settings,
              );
            case AppRoutes.register:
              return MaterialPageRoute(
                builder: (_) => const RegisterScreen(),
                settings: settings,
              );
            case AppRoutes.main:
              return MaterialPageRoute(
                builder: (_) => const MainNavigation(),
                settings: settings,
              );
            case AppRoutes.loanApply:
              return MaterialPageRoute(
                builder: (_) => const LoanApplyFlowScreen(),
                settings: settings,
              );
            case AppRoutes.contractList:
              return MaterialPageRoute(
                builder: (_) => const ContractListPage(),
                settings: settings,
              );
            case AppRoutes.repayment:
              return MaterialPageRoute(
                builder: (_) => const RepaymentPage(),
                settings: settings,
              );
            default:
              return MaterialPageRoute(
                builder: (_) => const SplashScreen(),
                settings: settings,
              );
          }
        },
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
        Navigator.of(context).pushReplacementNamed(AppRoutes.main);
      }
    } else {
      if (mounted) {
        Navigator.of(context).pushReplacementNamed(AppRoutes.login);
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
class MainNavigation extends StatefulWidget {
  const MainNavigation({super.key});

  static const List<Widget> _pages = [
    HomeScreen(), // 首页
    AccountScreen(), // 钱包
    HistoryScreen(), // 记录
    ProfileScreen(), // 我的
  ];

  static const List<TrackedTabItem> _tabItems = [
    TrackedTabItem(
      icon: Icons.home,
      label: '首页',
      elementId: AnalyticsIds.tabHome,
      page: AnalyticsPages.home,
      flow: AnalyticsFlows.loanApply,
    ),
    TrackedTabItem(
      icon: Icons.account_balance_wallet,
      label: '钱包',
      elementId: AnalyticsIds.tabAccount,
      page: AnalyticsPages.account,
      flow: AnalyticsFlows.account,
    ),
    TrackedTabItem(
      icon: Icons.history,
      label: '记录',
      elementId: AnalyticsIds.tabHistory,
      page: AnalyticsPages.history,
      flow: AnalyticsFlows.history,
    ),
    TrackedTabItem(
      icon: Icons.person,
      label: '我的',
      elementId: AnalyticsIds.tabProfile,
      page: AnalyticsPages.profile,
      flow: AnalyticsFlows.account,
    ),
  ];

  @override
  State<MainNavigation> createState() => _MainNavigationState();
}

class _MainNavigationState extends State<MainNavigation> {
  void _onTabTap(int index) {
    final currentIndex = context.read<NavigationProvider>().currentIndex;
    if (currentIndex == index) {
      return;
    }

    context.read<NavigationProvider>().setIndex(index);
  }

  @override
  Widget build(BuildContext context) {
    final navigationProvider = context.watch<NavigationProvider>();

    return Scaffold(
      body: MainNavigation._pages[navigationProvider.currentIndex],
      bottomNavigationBar: TrackedBottomNavBar(
        currentIndex: navigationProvider.currentIndex,
        items: MainNavigation._tabItems,
        onTap: _onTabTap,
      ),
    );
  }
}
