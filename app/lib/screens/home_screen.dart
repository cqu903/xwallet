import 'package:flutter/material.dart';
import '../widgets/loan_card.dart';
import '../widgets/reward_mini_card.dart';
import '../widgets/activity_carousel.dart';
import '../widgets/quick_actions.dart';
import '../widgets/transaction_list.dart';
import '../services/analytics_service.dart';
import '../models/analytics_event.dart';
import '../utils/design_scale.dart';

/// 主页 - 贷款申请落地页（基于设计稿重构）
/// 设计稿基准宽度: 402px
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
        properties: {'pageName': 'HomeScreen'},
        category: EventCategory.behavior,
      );
    });
  }

  @override
  Widget build(BuildContext context) {
    final scale = DesignScale.getScale(context);

    return Scaffold(
      backgroundColor: const Color(0xFFD4CCF5), // 淡紫色背景
      body: Stack(
        children: [
          // 主要内容：用 Positioned.fill 确保获得完整约束，避免 Stack 导致无法滚动
          Positioned.fill(
            child: SafeArea(
              child: SingleChildScrollView(
                physics: const BouncingScrollPhysics(),
                child: Column(
                  children: [
                    // 状态栏占位（模拟设计稿中的状态栏区域）
                    SizedBox(height: 16 * scale),

                    // 贷款英雄卡片
                    LoanCard(onApply: _handleApplyLoan),

                    SizedBox(height: 12 * scale),

                    // 推荐奖励迷你卡片
                    RewardMiniCard(
                      onShareTap: _handleShare,
                      rewardAmount: '1,000',
                      title: '推荐友奖赏',
                    ),

                    SizedBox(height: 12 * scale),

                    // 活动轮播区域
                    ActivityCarousel(
                      onActivityTap: _handleActivityTap,
                      onMoreTap: _handleViewMoreActivities,
                    ),

                    SizedBox(height: 12 * scale),

                    // 快捷服务区域
                    QuickActionsSection(onActionTap: _handleQuickAction),

                    SizedBox(height: 12 * scale),

                    // 最近交易区域
                    TransactionListSection(
                      onViewAllTap: _handleViewAllTransactions,
                      onTransactionTap: _handleTransactionTap,
                    ),

                    // 底部留白（为导航栏留出空间）
                    SizedBox(height: 100 * scale),
                  ],
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  /// 处理贷款申请
  void _handleApplyLoan() {
    // 上报按钮点击事件
    AnalyticsService.instance.trackEvent(
      eventType: 'button_click',
      properties: {'buttonName': 'apply_loan', 'page': 'HomeScreen'},
      category: EventCategory.behavior,
    );

    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text('跳转到贷款申请页面...'),
        duration: Duration(seconds: 1),
      ),
    );
  }

  /// 处理推荐分享
  void _handleShare() {
    AnalyticsService.instance.trackEvent(
      eventType: 'button_click',
      properties: {'buttonName': 'share_referral', 'page': 'HomeScreen'},
      category: EventCategory.behavior,
    );

    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text('打开分享面板...'),
        duration: Duration(seconds: 1),
      ),
    );
  }

  /// 处理活动点击
  void _handleActivityTap(ActivityData activity) {
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
  }

  /// 查看更多活动
  void _handleViewMoreActivities() {
    AnalyticsService.instance.trackEvent(
      eventType: 'link_click',
      properties: {'linkName': 'view_more_activities', 'page': 'HomeScreen'},
      category: EventCategory.behavior,
    );

    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text('跳转到活动列表...'),
        duration: Duration(seconds: 1),
      ),
    );
  }

  /// 处理快捷操作
  void _handleQuickAction(QuickActionData action) {
    AnalyticsService.instance.trackEvent(
      eventType: 'quick_action_click',
      properties: {
        'actionId': action.id,
        'actionLabel': action.label,
        'page': 'HomeScreen',
      },
      category: EventCategory.behavior,
    );

    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('打开: ${action.label}'),
        duration: const Duration(seconds: 1),
      ),
    );
  }

  /// 查看全部交易
  void _handleViewAllTransactions() {
    AnalyticsService.instance.trackEvent(
      eventType: 'link_click',
      properties: {'linkName': 'view_all_transactions', 'page': 'HomeScreen'},
      category: EventCategory.behavior,
    );

    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text('跳转到交易记录...'),
        duration: Duration(seconds: 1),
      ),
    );
  }

  /// 处理交易点击
  void _handleTransactionTap(TransactionData transaction) {
    AnalyticsService.instance.trackEvent(
      eventType: 'transaction_click',
      properties: {
        'transactionId': transaction.id,
        'transactionName': transaction.name,
        'page': 'HomeScreen',
      },
      category: EventCategory.behavior,
    );

    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('查看交易详情: ${transaction.name}'),
        duration: const Duration(seconds: 1),
      ),
    );
  }
}
