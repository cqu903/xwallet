import 'package:flutter/material.dart';
import '../widgets/loan_card.dart';
import '../widgets/activity_grid.dart';
import '../models/activity.dart';
import '../services/analytics_service.dart';
import '../models/analytics_event.dart';

/// 主页 - 贷款申请落地页
class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  @override
  void initState() {
    super.initState();
    // 上报页面浏览事件
    WidgetsBinding.instance.addPostFrameCallback((_) {
      AnalyticsService.instance.trackEvent(
        eventType: 'page_view',
        properties: {
          'pageName': 'HomeScreen',
        },
        category: EventCategory.behavior,
      );
    });
  }

  @override
  Widget build(BuildContext context) {
    final activities = Activity.getPlaceholderActivities();

    return Scaffold(
      backgroundColor: const Color(0xFFF5F5F5),
      body: SafeArea(
        child: SingleChildScrollView(
          physics: const BouncingScrollPhysics(),
          child: Column(
            children: [
              // 顶部状态栏区域（绿色背景）
              Container(
                height: 56,
                decoration: const BoxDecoration(
                  gradient: LinearGradient(
                    colors: [Color(0xFF43A047), Color(0xFF2E7D32)],
                  ),
                ),
                child: const Center(
                  child: Text(
                    'xWallet',
                    style: TextStyle(
                      color: Colors.white,
                      fontSize: 20,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
              ),

              // 贷款申请卡片
              LoanCard(
                onApply: () {
                  // 上报按钮点击事件
                  AnalyticsService.instance.trackEvent(
                    eventType: 'button_click',
                    properties: {
                      'buttonName': 'apply_loan',
                      'page': 'HomeScreen',
                    },
                    category: EventCategory.behavior,
                  );

                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(
                      content: Text('跳转到贷款申请页面...'),
                      duration: Duration(seconds: 1),
                    ),
                  );
                },
              ),

              // 活动宣传网格
              ActivityGrid(
                activities: activities,
                onActivityTap: (activity) {
                  // 上报活动点击事件
                  AnalyticsService.instance.trackEvent(
                    eventType: 'activity_click',
                    properties: {
                      'activityId': activity.id,
                      'activityTitle': activity.title,
                      'page': 'HomeScreen',
                    },
                    category: EventCategory.behavior,
                  );

                  ScaffoldMessenger.of(context).showSnackBar(
                    SnackBar(
                      content: Text('查看活动: ${activity.title}'),
                      duration: const Duration(seconds: 1),
                    ),
                  );
                },
              ),

              // 底部留白（为导航栏留出空间）
              const SizedBox(height: 80),
            ],
          ),
        ),
      ),
    );
  }
}
