import 'package:flutter/material.dart';
import '../utils/design_scale.dart';

/// 活动数据模型
class ActivityData {
  final String id;
  final String title;
  final String subtitle;
  final String badgeText;
  final List<Color> gradientColors;
  final String? imageUrl;
  /// 本地图片资源路径 (design/images/ 下的资源)
  final String? imageAsset;

  const ActivityData({
    required this.id,
    required this.title,
    required this.subtitle,
    required this.badgeText,
    required this.gradientColors,
    this.imageUrl,
    this.imageAsset,
  });

  /// 默认活动数据（使用 design/images 下的真实图片）
  static List<ActivityData> getDefaultActivities() {
    return [
      const ActivityData(
        id: '1',
        title: '新用户免息券',
        subtitle: '限时发放中',
        badgeText: '限时优惠',
        gradientColors: [Color(0xFFFF6B6B), Color(0xFFFF8E8E)],
        imageAsset: 'design/images/generated-1769999557017.png',
      ),
      const ActivityData(
        id: '2',
        title: '邀请好友赚现金',
        subtitle: '每推荐一人奖\$1,000',
        badgeText: '推荐有礼',
        gradientColors: [Color(0xFF11998E), Color(0xFF38EF7D)],
        imageAsset: 'design/images/generated-1769999565802.png',
      ),
      const ActivityData(
        id: '3',
        title: '会员专享特权',
        subtitle: '尊享低息 优先审批',
        badgeText: 'VIP专属',
        gradientColors: [Color(0xFF7424F5), Color(0xFFB066FF)],
        imageAsset: 'design/images/generated-1769999557017.png',
      ),
    ];
  }
}

/// 活动轮播组件
/// 设计稿基准宽度: 402px
class ActivityCarousel extends StatefulWidget {
  final List<ActivityData>? activities;
  final Function(ActivityData)? onActivityTap;
  final VoidCallback? onMoreTap;

  const ActivityCarousel({
    super.key,
    this.activities,
    this.onActivityTap,
    this.onMoreTap,
  });

  @override
  State<ActivityCarousel> createState() => _ActivityCarouselState();
}

class _ActivityCarouselState extends State<ActivityCarousel> {
  late PageController _pageController;
  int _currentPage = 0;

  List<ActivityData> get _activities =>
      widget.activities ?? ActivityData.getDefaultActivities();

  @override
  void initState() {
    super.initState();
    _pageController = PageController(viewportFraction: 0.92, initialPage: 0);
  }

  @override
  void dispose() {
    _pageController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final scale = DesignScale.getScale(context);

    return Container(
      margin: EdgeInsets.symmetric(horizontal: 16 * scale),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          colors: [
            const Color(0xFF7424F5).withOpacity(0.02),
            Colors.white.withOpacity(0.9),
          ],
        ),
      ),
      child: Stack(
        children: [
          // 主内容
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisSize: MainAxisSize.min,
            children: [
              // 顶部辉光

              // 标题栏（设计稿 activitiesContainer gap 8）
              _buildHeader(scale),
              SizedBox(height: 8 * scale),
              // 轮播卡片
              _buildCarousel(scale),
              SizedBox(height: 8 * scale),
              // 指示器
              _buildIndicators(scale),
              SizedBox(height: 12 * scale),
            ],
          ),
        ],
      ),
    );
  }

  /// 标题栏
  Widget _buildHeader(double scale) {
    return Padding(
      padding: EdgeInsets.fromLTRB(16 * scale, 12 * scale, 16 * scale, 0),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            '热门活动',
            style: TextStyle(
              color: const Color(0xFF1A1A1A),
              fontSize: 18 * scale,
              fontWeight: FontWeight.w600,
            ),
          ),
          GestureDetector(
            onTap: widget.onMoreTap,
            child: Text(
              '查看更多',
              style: TextStyle(
                color: const Color(0xFF7424F5),
                fontSize: 13 * scale,
                fontWeight: FontWeight.w500,
              ),
            ),
          ),
        ],
      ),
    );
  }

  /// 轮播卡片
  Widget _buildCarousel(double scale) {
    return SizedBox(
      height: 200 * scale, // 设计稿: height: 200
      child: PageView.builder(
        controller: _pageController,
        itemCount: _activities.length,
        onPageChanged: (index) {
          setState(() {
            _currentPage = index;
          });
        },
        itemBuilder: (context, index) {
          return _ActivityCard(
            activity: _activities[index],
            scale: scale,
            onTap: () => widget.onActivityTap?.call(_activities[index]),
          );
        },
      ),
    );
  }

  /// 指示器
  Widget _buildIndicators(double scale) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: List.generate(
        _activities.length,
        (index) => Container(
          width: (index == _currentPage ? 8 : 6) * scale,
          height: (index == _currentPage ? 8 : 6) * scale,
          margin: EdgeInsets.symmetric(horizontal: 4 * scale),
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            color: index == _currentPage
                ? const Color(0xFF7424F5)
                : const Color(0xFF7424F5).withOpacity(0.4),
          ),
        ),
      ),
    );
  }
}

/// 单个活动卡片
class _ActivityCard extends StatelessWidget {
  final ActivityData activity;
  final double scale;
  final VoidCallback? onTap;

  const _ActivityCard({
    required this.activity,
    required this.scale,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        margin: EdgeInsets.symmetric(horizontal: 6 * scale),
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(16 * scale),
          gradient: LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: activity.gradientColors,
          ),
          boxShadow: [
            BoxShadow(
              color: activity.gradientColors.first.withOpacity(0.25),
              blurRadius: 24 * scale,
              offset: Offset(0, 8 * scale),
            ),
            BoxShadow(
              color: const Color(0xFF000000).withOpacity(0.08),
              blurRadius: 6 * scale,
              offset: Offset(0, 2 * scale),
            ),
          ],
        ),
        child: Stack(
          children: [
            // 背景图片（优先本地资源，其次网络）
            if (activity.imageAsset != null)
              Positioned.fill(
                child: ClipRRect(
                  borderRadius: BorderRadius.circular(16 * scale),
                  child: Image.asset(
                    activity.imageAsset!,
                    fit: BoxFit.cover,
                    errorBuilder: (_, __, ___) => const SizedBox.shrink(),
                  ),
                ),
              )
            else if (activity.imageUrl != null)
              Positioned.fill(
                child: ClipRRect(
                  borderRadius: BorderRadius.circular(16 * scale),
                  child: Image.network(
                    activity.imageUrl!,
                    fit: BoxFit.cover,
                    errorBuilder: (_, __, ___) => const SizedBox.shrink(),
                  ),
                ),
              ),
            // 图片上的渐变遮罩，保证文字可读
            if (activity.imageAsset != null || activity.imageUrl != null)
              Positioned.fill(
                child: DecoratedBox(
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(16 * scale),
                    gradient: LinearGradient(
                      begin: Alignment.topCenter,
                      end: Alignment.bottomCenter,
                      colors: [
                        Colors.black.withOpacity(0.2),
                        Colors.black.withOpacity(0.6),
                      ],
                    ),
                  ),
                ),
              ),
            // 内容覆盖层
            Padding(
              padding: EdgeInsets.all(20 * scale),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  // 徽章
                  Container(
                    padding: EdgeInsets.symmetric(
                      horizontal: 14 * scale,
                      vertical: 6 * scale,
                    ),
                    decoration: BoxDecoration(
                      color: Colors.white.withOpacity(0.25),
                      borderRadius: BorderRadius.circular(50 * scale),
                    ),
                    child: Text(
                      activity.badgeText,
                      style: TextStyle(
                        color: Colors.white,
                        fontSize: 12 * scale,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                  ),
                  // 标题和副标题
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        activity.title,
                        style: TextStyle(
                          color: Colors.white,
                          fontSize: 22 * scale,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      SizedBox(height: 4 * scale),
                      Text(
                        activity.subtitle,
                        style: TextStyle(
                          color: Colors.white.withOpacity(0.9),
                          fontSize: 14 * scale,
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
