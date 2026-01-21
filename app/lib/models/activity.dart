/// 活动数据模型
class Activity {
  final String id;
  final String title;
  final String imageUrl;
  final String? description;

  Activity({
    required this.id,
    required this.title,
    required this.imageUrl,
    this.description,
  });

  /// 创建静态活动数据（占位用）
  static List<Activity> getPlaceholderActivities() {
    return [
      Activity(
        id: '1',
        title: '新用户专享',
        imageUrl: 'https://via.placeholder.com/300x200/43A047/FFFFFF?text=新用户专享',
        description: '首次借款免息7天',
      ),
      Activity(
        id: '2',
        title: '限时活动',
        imageUrl: 'https://via.placeholder.com/300x200/2E7D32/FFFFFF?text=限时活动',
        description: '借款即送优惠券',
      ),
      Activity(
        id: '3',
        title: '邀请好友',
        imageUrl: 'https://via.placeholder.com/300x200/66BB6A/FFFFFF?text=邀请好友',
        description: '双方各得50元现金',
      ),
      Activity(
        id: '4',
        title: '会员福利',
        imageUrl: 'https://via.placeholder.com/300x200/81C784/FFFFFF?text=会员福利',
        description: '会员专属低息',
      ),
    ];
  }
}
