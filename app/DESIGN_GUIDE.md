# xWallet APP 设计风格指南

## 品牌与官网对齐

- **产品官网**: [www.xwallet.hk](https://www.xwallet.hk)（X Wallet 贷款产品主页）
- **正式品牌名**: **X Wallet**（官網及產品線均以「X」開頭：X Cash、X 現金 A.I. 貸款、X 大額私人貸款、X 結餘轉戶、X 物業貸款、X Pay 等）
- **Logo 建議**: 以字母 **「X」** 為核心識別——登錄頁與 App 圖標建議使用 **X 字形圖標** 或 **大寫「X」字樣**，與官網主視覺一致；可從官網獲取正式 logo 資源後替換佔位
- **文案與數字可對齊官網**: 迎新獎賞高達 **$3,000**、友獎賞每推薦 **$1,000**、X Cash 每日利息 **$0.032/每$1,000**、A.I. 即時審批約 **5 秒** 等，設計稿中的獎賞金額建議與官網一致

---

## 设计理念

**X Wallet APP**（移动端贷款应用）采用年轻化、激励感的设计风格，强调：
- **激励感**: 通过高饱和度色彩和清晰的价值传递，让用户感受到"轻松赚钱"的机会
- **信任感**: 紫色主题传递尊贵、专业、可信赖的品牌形象
- **易用性**: 简化的操作流程和直观的信息层级，降低用户理解成本
- **年轻化**: 现代化的视觉语言，符合 Z 世代/新中产用户的审美偏好

---

## 配色方案

### 主色（Primary）
- **紫色**: `#7424F5` → HEX `#7424F5` / RGB `(116, 36, 245)`
- 用于：主按钮、品牌元素、重要图标、进度指示
- **深紫色变体**: `#4A148C` - 用于背景、强调区域

### 辅助色（Secondary）- 紫色色系
- **亮紫色**: `#9C4DFF` (RGB: 156, 77, 255) - 用于渐变过渡、次要按钮
- **浅紫色**: `#B066FF` (RGB: 176, 102, 255) - 用于轻量级强调
- **淡紫色背景**: `#F5F3FF` / `#EDE9FE` / `#E9E5FF` - 用于页面背景渐变
- **紫色透明**: `rgba(116, 36, 245, 0.1)` - 用于图标背景、徽章背景

### 装饰色（Accent）- 仅用于个别元素
- **金色**: `#FFD700` (RGB: 255, 215, 0) - **仅用于**：奖励金额数字、特殊徽章、重要数值高亮
- 注意：金色不作为主要辅助色，仅在需要突出金额/奖励时少量使用

### 功能色
- **成功**: 绿色 `#22C55E` / `#4CAF50` - 用于成功状态、正向反馈、收入金额
- **警告**: 橙色 `#FF9800` - 用于警告提示、限时优惠
- **错误**: 红色 `#F44336` / `#FF6B6B` - 用于错误状态、失败提示
- **信息**: 蓝色 `#2196F3` - 用于信息提示、链接

### 背景色
- **主背景渐变**: `#F5F3FF` → `#EDE9FE` → `#E9E5FF` → `#F3F0FF` (紫色色系渐变)
- **次要背景**: `#FAF8FF` / `#F8F5FF` (淡紫色)
- **深色背景**: `#4A148C` → `#7424F5` (紫色渐变背景)
- **卡片背景**: `#FFFFFF` → `#FAF8FF` (白色到淡紫渐变) + 阴影
- **装饰光晕**: `rgba(116, 36, 245, 0.05~0.08)` (紫色光晕装饰)

### 文字色
- **主文字**: `#1A1A1A` (深灰近黑)
- **次要文字**: `#666666` (中灰)
- **辅助文字**: `#999999` (浅灰)
- **白色文字**: `#FFFFFF` (用于深色背景)

---

## 字体系统

### 字体家族
```dart
// Flutter 字体设置
// 使用系统默认字体 + SF Pro (iOS) / Roboto (Android)

// 主字体
fontFamily: 'SF Pro Text',  // iOS
           'Roboto',         // Android
           'PingFang SC',    // 中文 fallback
           system default

// 显示字体（标题）
fontFamily: 'SF Pro Display',  // iOS
           'Roboto Medium',     // Android
           'PingFang SC',       // 中文 fallback
```

### 字体层级
- **大标题**: `font-size: 28px, font-weight: bold` - 页面主标题
- **标题**: `font-size: 24px, font-weight: 600` - 区块标题
- **副标题**: `font-size: 20px, font-weight: 600` - 次要标题
- **正文**: `font-size: 16px, font-weight: 400` - 常规内容
- **小文字**: `font-size: 14px, font-weight: 400` - 次要信息
- **辅助文字**: `font-size: 12px, font-weight: 400` - 元信息、说明

### 字体颜色使用规则
- **金额数字**: 使用金色 `#FFD700` 或主色 `#7424F5`
- **按钮文字**: 白色 `#FFFFFF`
- **标题文字**: 深色 `#1A1A1A`
- **说明文字**: 中灰 `#666666`
- **禁用文字**: 浅灰 `#CCCCCC`

---

## 视觉效果

### 渐变效果
```dart
// 主色渐变 - 紫色系
BoxDecoration(
  gradient: LinearGradient(
    colors: [
      Color(0xFF7424F5),  // 主紫色
      Color(0xFF9C4DFF),  // 亮紫色
      Color(0xFF6A1B9A),  // 深紫色
    ],
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
  ),
)

// 金色渐变 - 用于奖励卡片
BoxDecoration(
  gradient: LinearGradient(
    colors: [
      Color(0xFFFFD700),  // 金色
      Color(0xFFFFC107),  // 琥珀色
      Color(0xFFFFEB3B),  // 黄色
    ],
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
  ),
)

// 深色背景渐变
BoxDecoration(
  gradient: LinearGradient(
    colors: [
      Color(0xFF4A148C),  // 深紫色
      Color(0xFF7B1FA2),  // 紫色
      Color(0xFF6A1B9A),  // 更深紫色
    ],
    begin: Alignment.topCenter,
    end: Alignment.bottomCenter,
  ),
)
```

### 阴影系统
```dart
// 轻微阴影 - 用于卡片
BoxShadow(
  color: Color(0x1A000000),  // 黑色 10% 透明度
  blurRadius: 8,
  offset: Offset(0, 2),
)

// 中等阴影 - 用于浮动元素
BoxShadow(
  color: Color(0x1F000000),  // 黑色 12% 透明度
  blurRadius: 16,
  offset: Offset(0, 4),
)

// 强阴影 - 用于模态弹窗
BoxShadow(
  color: Color(0x33000000),  // 黑色 20% 透明度
  blurRadius: 24,
  offset: Offset(0, 8),
)

// 金色发光效果 - 用于奖励卡片
BoxShadow(
  color: Color(0x80FFD700),  // 金色 50% 透明度
  blurRadius: 16,
  spreadRadius: 0,
)
```

### 圆角规范
```dart
// 小圆角 - 按钮标签
BorderRadius.circular(8),

// 中圆角 - 卡片、输入框
BorderRadius.circular(12),

// 大圆角 - 大卡片、模态弹窗
BorderRadius.circular(16),

// 超大圆角 - 特殊组件
BorderRadius.circular(24),

// 圆形 - 头像、图标背景
BoxShape.circle,
```

---

## 动画系统

### 页面转场动画
```dart
// 淡入淡出
PageRouteBuilder(
  pageBuilder: (context, animation, secondaryAnimation) => NextPage(),
  transitionsBuilder: (context, animation, secondaryAnimation, child) {
    return FadeTransition(
      opacity: animation,
      child: child,
    );
  },
  transitionDuration: Duration(milliseconds: 300),
)

// 从右滑入
PageRouteBuilder(
  pageBuilder: (context, animation, secondaryAnimation) => NextPage(),
  transitionsBuilder: (context, animation, secondaryAnimation, child) {
    const begin = Offset(1.0, 0.0);
    const end = Offset.zero;
    final tween = Tween(begin: begin, end: end);
    final curvedAnimation = CurvedAnimation(
      parent: animation,
      curve: Curves.easeInOut,
    );
    return SlideTransition(
      position: tween.animate(curvedAnimation),
      child: child,
    );
  },
)
```

### 元素动画
```dart
// 按钮点击缩放效果
GestureDetector(
  onTapDown: (_) => setState(() => _scale = 0.95),
  onTapUp: (_) => setState(() => _scale = 1.0),
  onTapCancel: () => setState(() => _scale = 1.0),
  child: Transform.scale(
    scale: _scale,
    child: ElevatedButton(...),
  ),
)

// 列表项淡入动画
AnimationController(
  duration: Duration(milliseconds: 300),
  vsync: this,
)..forward();

FadeTransition(
  opacity: CurvedAnimation(
    parent: controller,
    curve: Curves.easeIn,
  ),
  child: YourWidget(),
)

// 数字滚动动画（用于金额显示）
// 使用 IntTween 等实现数字从 0 滚动到目标值
```

### 加载动画
```dart
// 旋转加载指示器
CircularProgressIndicator(
  valueColor: AlwaysStoppedAnimation<Color>(Color(0xFF7424F5)),
  strokeWidth: 3,
)

// 脉冲发光动画（用于推荐按钮）
AnimationController(
  duration: Duration(milliseconds: 1500),
  vsync: this,
)..repeat();

AnimatedBuilder(
  animation: controller,
  builder: (context, child) {
    return Container(
      decoration: BoxDecoration(
        boxShadow: [
          BoxShadow(
            color: Color(0xFF7424F5).withOpacity(
              0.3 + 0.2 * (controller.value as double),
            ),
            blurRadius: 16,
            spreadRadius: 4 * (controller.value as double),
          ),
        ],
      ),
      child: child,
    );
  },
)
```

---

## 组件样式规范

### 按钮（Button）

#### 主按钮（Primary Button）
```dart
ElevatedButton(
  style: ElevatedButton.styleFrom(
    backgroundColor: Color(0xFF7424F5),  // 紫色
    foregroundColor: Colors.white,
    minimumSize: Size(double.infinity, 52),  // 全宽，高度 52
    shape: RoundedRectangleBorder(
      borderRadius: BorderRadius.circular(12),
    ),
    elevation: 0,
    shadowColor: Colors.transparent,
    textStyle: TextStyle(
      fontSize: 16,
      fontWeight: FontWeight.w600,
    ),
  ),
  onPressed: () {},
  child: Text('立即申请'),
)
```

#### 次要按钮（Secondary Button）
```dart
OutlinedButton(
  style: OutlinedButton.styleFrom(
    foregroundColor: Color(0xFF7424F5),
    side: BorderSide(color: Color(0xFF7424F5), width: 1.5),
    minimumSize: Size(double.infinity, 52),
    shape: RoundedRectangleBorder(
      borderRadius: BorderRadius.circular(12),
    ),
    textStyle: TextStyle(
      fontSize: 16,
      fontWeight: FontWeight.w600,
    ),
  ),
  onPressed: () {},
  child: Text('了解更多'),
)
```

#### 文字按钮（Text Button）
```dart
TextButton(
  style: TextButton.styleFrom(
    foregroundColor: Color(0xFF7424F5),
    textStyle: TextStyle(
      fontSize: 14,
      fontWeight: FontWeight.w500,
    ),
  ),
  onPressed: () {},
  child: Text('查看详情'),
)
```

#### 金色强调按钮
```dart
Container(
  decoration: BoxDecoration(
    gradient: LinearGradient(
      colors: [Color(0xFFFFD700), Color(0xFFFFC107)],
    ),
    borderRadius: BorderRadius.circular(12),
    boxShadow: [
      BoxShadow(
        color: Color(0xFFFFD700).withOpacity(0.3),
        blurRadius: 8,
        offset: Offset(0, 4),
      ),
    ],
  ),
  child: ElevatedButton(
    style: ElevatedButton.styleFrom(
      backgroundColor: Colors.transparent,
      shadowColor: Colors.transparent,
      minimumSize: Size(double.infinity, 52),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
      ),
    ),
    onPressed: () {},
    child: Text(
      '立即推荐 赚取奖赏',
      style: TextStyle(
        color: Color(0xFF1A1A1A),
        fontSize: 16,
        fontWeight: FontWeight.bold,
      ),
    ),
  ),
)
```

### 卡片（Card）

#### 基础卡片
```dart
Container(
  decoration: BoxDecoration(
    color: Colors.white,
    borderRadius: BorderRadius.circular(12),
    boxShadow: [
      BoxShadow(
        color: Color(0x0D000000),
        blurRadius: 8,
        offset: Offset(0, 2),
      ),
    ],
  ),
  padding: EdgeInsets.all(16),
  child: Column(
    crossAxisAlignment: CrossAxisAlignment.start,
    children: [
      // 卡片内容
    ],
  ),
)
```

#### 紫色背景卡片（推荐奖励卡片）
```dart
Container(
  decoration: BoxDecoration(
    gradient: LinearGradient(
      colors: [Color(0xFF4A148C), Color(0xFF7B1FA2)],
      begin: Alignment.topLeft,
      end: Alignment.bottomRight,
    ),
    borderRadius: BorderRadius.circular(16),
    boxShadow: [
      BoxShadow(
        color: Color(0xFF4A148C).withOpacity(0.3),
        blurRadius: 12,
        offset: Offset(0, 4),
      ),
    ],
  ),
  padding: EdgeInsets.all(20),
  child: Column(
    crossAxisAlignment: CrossAxisAlignment.start,
    children: [
      Text(
        '推荐人奖赏',
        style: TextStyle(
          color: Colors.white,
          fontSize: 20,
          fontWeight: FontWeight.bold,
        ),
      ),
      SizedBox(height: 12),
      // 奖励详情
      Container(
        padding: EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: Colors.white.withOpacity(0.1),
          borderRadius: BorderRadius.circular(8),
        ),
        child: Row(
          children: [
            Icon(Icons.monetization_on, color: Color(0xFFFFD700)),
            SizedBox(width: 8),
            Text(
              '\$900',
              style: TextStyle(
                color: Color(0xFFFFD700),
                fontSize: 24,
                fontWeight: FontWeight.bold,
              ),
            ),
          ],
        ),
      ),
    ],
  ),
)
```

#### 金色奖励卡片
```dart
Container(
  decoration: BoxDecoration(
    gradient: LinearGradient(
      colors: [Color(0xFFFFD700), Color(0xFFFFC107)],
    ),
    borderRadius: BorderRadius.circular(16),
    boxShadow: [
      BoxShadow(
        color: Color(0xFFFFD700).withOpacity(0.4),
        blurRadius: 16,
        offset: Offset(0, 4),
      ),
    ],
  ),
  padding: EdgeInsets.all(20),
  child: Row(
    children: [
      Icon(Icons.card_giftcard, color: Color(0xFF1A1A1A), size: 32),
      SizedBox(width: 12),
      Expanded(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              '终极奖赏',
              style: TextStyle(
                color: Color(0xFF1A1A1A),
                fontSize: 16,
                fontWeight: FontWeight.bold,
              ),
            ),
            SizedBox(height: 4),
            Text(
              '\$300 现金奖赏',
              style: TextStyle(
                color: Color(0xFF1A1A1A).withOpacity(0.7),
                fontSize: 14,
              ),
            ),
          ],
        ),
      ),
    ],
  ),
)
```

### 输入框（Input Field）

```dart
TextField(
  decoration: InputDecoration(
    hintText: '请输入邮箱地址',
    hintStyle: TextStyle(
      color: Color(0xFF999999),
      fontSize: 16,
    ),
    filled: true,
    fillColor: Color(0xFFF8F9FA),
    border: OutlineInputBorder(
      borderRadius: BorderRadius.circular(12),
      borderSide: BorderSide.none,
    ),
    enabledBorder: OutlineInputBorder(
      borderRadius: BorderRadius.circular(12),
      borderSide: BorderSide(color: Color(0xFFE0E0E0), width: 1),
    ),
    focusedBorder: OutlineInputBorder(
      borderRadius: BorderRadius.circular(12),
      borderSide: BorderSide(color: Color(0xFF7424F5), width: 2),
    ),
    errorBorder: OutlineInputBorder(
      borderRadius: BorderRadius.circular(12),
      borderSide: BorderSide(color: Color(0xFFF44336), width: 1),
    ),
    contentPadding: EdgeInsets.symmetric(horizontal: 16, vertical: 16),
  ),
  style: TextStyle(
    color: Color(0xFF1A1A1A),
    fontSize: 16,
  ),
)
```

### 徽章（Badge）

```dart
Container(
  padding: EdgeInsets.symmetric(horizontal: 12, vertical: 6),
  decoration: BoxDecoration(
    color: Color(0xFF7424F5).withOpacity(0.1),
    borderRadius: BorderRadius.circular(20),
  ),
  child: Text(
    '限时优惠',
    style: TextStyle(
      color: Color(0xFF7424F5),
      fontSize: 12,
      fontWeight: FontWeight.w600,
    ),
  ),
)

// 金色徽章
Container(
  padding: EdgeInsets.symmetric(horizontal: 12, vertical: 6),
  decoration: BoxDecoration(
    color: Color(0xFFFFD700).withOpacity(0.15),
    borderRadius: BorderRadius.circular(20),
  ),
  child: Text(
    '新手专属',
    style: TextStyle(
      color: Color(0xFFF57F17),  // 深金色
      fontSize: 12,
      fontWeight: FontWeight.w600,
    ),
  ),
)
```

---

## 图标使用

### 图标库
```yaml
# pubspec.yaml
dependencies:
  flutter_svg: ^2.0.0  # SVG 图标支持
```

### 图标尺寸规范
- **小图标**: 16x16 - 用于列表、小按钮
- **默认图标**: 24x24 - 用于导航、卡片
- **大图标**: 32x32 - 用于首页功能入口
- **超大图标**: 48x48 - 用于空状态、引导页

### 图标背景样式
```dart
// 紫色背景图标
Container(
  width: 48,
  height: 48,
  decoration: BoxDecoration(
    color: Color(0xFF7424F5),
    borderRadius: BorderRadius.circular(12),
  ),
  child: Icon(
    Icons.account_balance_wallet,
    color: Colors.white,
    size: 24,
  ),
)

// 浅色背景图标
Container(
  width: 48,
  height: 48,
  decoration: BoxDecoration(
    color: Color(0xFF7424F5).withOpacity(0.1),
    borderRadius: BorderRadius.circular(12),
  ),
  child: Icon(
    Icons.trending_up,
    color: Color(0xFF7424F5),
    size: 24,
  ),
)

// 金色背景图标
Container(
  width: 48,
  height: 48,
  decoration: BoxDecoration(
    color: Color(0xFFFFD700).withOpacity(0.15),
    borderRadius: BorderRadius.circular(12),
  ),
  child: Icon(
    Icons.card_giftcard,
    color: Color(0xFFF57F17),
    size: 24,
  ),
)
```

---

## 布局模式

### 页面结构
```dart
Scaffold(
  backgroundColor: Color(0xFFF3F5F7),
  appBar: AppBar(
    // 顶部导航栏
  ),
  body: SafeArea(
    child: SingleChildScrollView(
      padding: EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // 页面内容
        ],
      ),
    ),
  ),
  bottomNavigationBar: BottomNavigationBar(
    // 底部导航栏
  ),
)
```

### 推荐奖励页面布局（重要参考）
```dart
Column(
  children: [
    // 1. 顶部标题区
    Container(
      padding: EdgeInsets.all(20),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [Color(0xFF4A148C), Color(0xFF7B1FA2)],
        ),
        borderRadius: BorderRadius.circular(16),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            '推荐友奖赏',
            style: TextStyle(
              color: Colors.white,
              fontSize: 28,
              fontWeight: FontWeight.bold,
            ),
          ),
          SizedBox(height: 8),
          Text(
            '推广期: 2024.01.01 - 2024.12.31',
            style: TextStyle(
              color: Colors.white.withOpacity(0.8),
              fontSize: 14,
            ),
          ),
          SizedBox(height: 16),
          Text(
            '每个成功推荐 奖赏高达',
            style: TextStyle(
              color: Colors.white,
              fontSize: 16,
            ),
          ),
          Text(
            '\$900',
            style: TextStyle(
              color: Color(0xFFFFD700),
              fontSize: 48,
              fontWeight: FontWeight.bold,
            ),
          ),
        ],
      ),
    ),

    SizedBox(height: 24),

    // 2. 推荐人奖赏卡片
    Container(
      padding: EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
            color: Color(0x0D000000),
            blurRadius: 8,
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(Icons.person, color: Color(0xFF7424F5)),
              SizedBox(width: 8),
              Text(
                '推荐人奖赏',
                style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ],
          ),
          SizedBox(height: 16),
          _buildRewardItem('首日奖赏', '\$300', '电子现金券'),
          _buildRewardItem('首月奖赏', '\$600', '现金 + 电子券'),
        ],
      ),
    ),

    SizedBox(height: 16),

    // 3. 受赠人奖赏卡片
    Container(
      padding: EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
            color: Color(0x0D000000),
            blurRadius: 8,
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(Icons.people, color: Color(0xFF4CAF50)),
              SizedBox(width: 8),
              Text(
                '受赠人奖赏',
                style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                ),
              ),
              SizedBox(width: 8),
              Container(
                padding: EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                decoration: BoxDecoration(
                  color: Color(0xFF4CAF50).withOpacity(0.1),
                  borderRadius: BorderRadius.circular(4),
                ),
                child: Text(
                  '好友亦有赏',
                  style: TextStyle(
                    color: Color(0xFF4CAF50),
                    fontSize: 12,
                  ),
                ),
              ),
            ],
          ),
          SizedBox(height: 16),
          _buildRewardItem('申请奖赏', '\$100', '电子现金券'),
          _buildRewardItem('首月奖赏', '\$200', '现金 + 电子券'),
          _buildRewardItem('终极奖赏', '\$300', '现金奖赏', isGold: true),
        ],
      ),
    ),

    SizedBox(height: 24),

    // 4. 推荐步骤三部曲
    Text(
      '友奖赏三部曲',
      style: TextStyle(
        fontSize: 18,
        fontWeight: FontWeight.bold,
      ),
    ),
    SizedBox(height: 16),
    Row(
      mainAxisAlignment: MainAxisAlignment.spaceAround,
      children: [
        _buildStep(Icons.share, '分享推荐码', '1'),
        _buildStep(Icons.edit, '好友申请时', '2'),
        _buildStep(Icons.monetization_on, '赚取奖励', '3'),
      ],
    ),

    SizedBox(height: 32),

    // 5. 底部按钮
    SizedBox(
      width: double.infinity,
      height: 52,
      child: ElevatedButton(
        style: ElevatedButton.styleFrom(
          backgroundColor: Color(0xFF7424F5),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(12),
          ),
        ),
        onPressed: () {},
        child: Text(
          '立即推荐 赚取奖赏',
          style: TextStyle(
            fontSize: 16,
            fontWeight: FontWeight.bold,
            color: Colors.white,
          ),
        ),
      ),
    ),
    SizedBox(height: 12),
    Center(
      child: TextButton(
        onPressed: () {},
        child: Text('查看我的奖赏'),
      ),
    ),
  ],
)

// 辅助方法：奖励项
Widget _buildRewardItem(String title, String amount, String subtitle, {bool isGold = false}) {
  return Container(
    margin: EdgeInsets.only(bottom: 12),
    padding: EdgeInsets.all(12),
    decoration: BoxDecoration(
      color: isGold
          ? Color(0xFFFFD700).withOpacity(0.1)
          : Color(0xFFF8F9FA),
      borderRadius: BorderRadius.circular(8),
      border: isGold
          ? Border.all(color: Color(0xFFFFD700), width: 1)
          : null,
    ),
    child: Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              title,
              style: TextStyle(
                fontSize: 14,
                fontWeight: FontWeight.w500,
              ),
            ),
            SizedBox(height: 4),
            Text(
              subtitle,
              style: TextStyle(
                fontSize: 12,
                color: Color(0xFF666666),
              ),
            ),
          ],
        ),
        Text(
          amount,
          style: TextStyle(
            color: isGold ? Color(0xFFF57F17) : Color(0xFF7424F5),
            fontSize: 20,
            fontWeight: FontWeight.bold,
          ),
        ),
      ],
    ),
  );
}

// 辅助方法：步骤
Widget _buildStep(IconData icon, String text, String number) {
  return Column(
    children: [
      Container(
        width: 64,
        height: 64,
        decoration: BoxDecoration(
          color: Color(0xFF7424F5).withOpacity(0.1),
          shape: BoxShape.circle,
        ),
        child: Icon(icon, color: Color(0xFF7424F5), size: 32),
      ),
      SizedBox(height: 8),
      Text(
        number,
        style: TextStyle(
          color: Color(0xFF7424F5),
          fontSize: 20,
          fontWeight: FontWeight.bold,
        ),
      ),
      SizedBox(height: 4),
      Text(
        text,
        style: TextStyle(
          fontSize: 12,
          color: Color(0xFF666666),
        ),
        textAlign: TextAlign.center,
      ),
    ],
  );
}
```

### 间距规范
- **页面边距**: 16px（安全区域外）
- **卡片间距**: 16px
- **元素间距**: 8px / 12px / 16px / 24px
- **圆角**: 12px（卡片）、8px（小元素）

---

## 响应式设计

### 屏幕适配
```dart
// 使用 MediaQuery 获取屏幕尺寸
final screenWidth = MediaQuery.of(context).size.width;
final screenHeight = MediaQuery.of(context).size.height;

// 使用百分比布局
Container(
  width: screenWidth * 0.9,  // 90% 屏幕宽度
)

// 使用 LayoutBuilder 自适应布局
LayoutBuilder(
  builder: (context, constraints) {
    if (constraints.maxWidth > 600) {
      return DesktopLayout();
    } else {
      return MobileLayout();
    }
  },
)
```

### 字体缩放
```dart
// 使用 TextTheme 和 MediaQuery.textScaleFactor
Text(
  '标题',
  style: Theme.of(context).textTheme.headlineMedium,
)

// 或使用 MediaQuery.of(context).textScaleFactor
Text(
  '自适应文字',
  style: TextStyle(
    fontSize: 16 * MediaQuery.of(context).textScaleFactor,
  ),
)
```

---

## 顶部导航（AppBar）

### 标准导航栏
```dart
AppBar(
  backgroundColor: Colors.white,
  elevation: 0,
  leading: IconButton(
    icon: Icon(Icons.arrow_back, color: Color(0xFF1A1A1A)),
    onPressed: () => Navigator.pop(context),
  ),
  title: Text(
    '页面标题',
    style: TextStyle(
      color: Color(0xFF1A1A1A),
      fontSize: 18,
      fontWeight: FontWeight.w600,
    ),
  ),
  centerTitle: true,
  actions: [
    IconButton(
      icon: Icon(Icons.notifications_outlined, color: Color(0xFF1A1A1A)),
      onPressed: () {},
    ),
  ],
)
```

### 紫色渐变导航栏
```dart
AppBar(
  flexibleSpace: Container(
    decoration: BoxDecoration(
      gradient: LinearGradient(
        colors: [Color(0xFF7424F5), Color(0xFF9C4DFF)],
      ),
    ),
  ),
  elevation: 0,
  title: Text(
    '推荐奖励',
    style: TextStyle(
      color: Colors.white,
      fontSize: 18,
      fontWeight: FontWeight.w600,
    ),
  ),
  centerTitle: true,
)
```

---

## 底部导航（Bottom Navigation）

```dart
BottomNavigationBar(
  type: BottomNavigationBarType.fixed,
  backgroundColor: Colors.white,
  selectedItemColor: Color(0xFF7424F5),
  unselectedItemColor: Color(0xFF999999),
  selectedFontSize: 12,
  unselectedFontSize: 12,
  currentIndex: _currentIndex,
  onTap: (index) {
    setState(() => _currentIndex = index);
  },
  items: [
    BottomNavigationBarItem(
      icon: Icon(Icons.home_outlined),
      activeIcon: Icon(Icons.home),
      label: '首页',
    ),
    BottomNavigationBarItem(
      icon: Icon(Icons.account_balance_wallet_outlined),
      activeIcon: Icon(Icons.account_balance_wallet),
      label: '贷款',
    ),
    BottomNavigationBarItem(
      icon: Icon(Icons.card_giftcard_outlined),
      activeIcon: Icon(Icons.card_giftcard),
      label: '奖励',
    ),
    BottomNavigationBarItem(
      icon: Icon(Icons.person_outline),
      activeIcon: Icon(Icons.person),
      label: '我的',
    ),
  ],
)
```

---

## 弹窗和模态（Dialog & Modal）

### 底部弹出式模态
```dart
showModalBottomSheet(
  context: context,
  backgroundColor: Colors.transparent,
  builder: (context) {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      padding: EdgeInsets.all(24),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          // 模态内容
        ],
      ),
    );
  },
)
```

### 紫色主题对话框
```dart
showDialog(
  context: context,
  builder: (context) {
    return AlertDialog(
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(16),
      ),
      title: Text(
        '推荐成功',
        style: TextStyle(
          color: Color(0xFF1A1A1A),
          fontSize: 20,
          fontWeight: FontWeight.bold,
        ),
      ),
      content: Text(
        '您的推荐链接已复制到剪贴板',
        style: TextStyle(
          color: Color(0xFF666666),
          fontSize: 16,
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: Text(
            '确定',
            style: TextStyle(
              color: Color(0xFF7424F5),
              fontSize: 16,
              fontWeight: FontWeight.w600,
            ),
          ),
        ),
      ],
    );
  },
)
```

---

## 列表和卡片（List & Cards）

### 奖励列表项
```dart
Container(
  margin: EdgeInsets.only(bottom: 12),
  padding: EdgeInsets.all(16),
  decoration: BoxDecoration(
    color: Colors.white,
    borderRadius: BorderRadius.circular(12),
    boxShadow: [
      BoxShadow(
        color: Color(0x0D000000),
        blurRadius: 8,
        offset: Offset(0, 2),
      ),
    ],
  ),
  child: Row(
    children: [
      Container(
        width: 48,
        height: 48,
        decoration: BoxDecoration(
          color: Color(0xFF7424F5).withOpacity(0.1),
          borderRadius: BorderRadius.circular(12),
        ),
        child: Icon(Icons.monetization_on, color: Color(0xFF7424F5)),
      ),
      SizedBox(width: 12),
      Expanded(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              '推荐奖励',
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.w600,
                color: Color(0xFF1A1A1A),
              ),
            ),
            SizedBox(height: 4),
            Text(
              '来自: user@example.com',
              style: TextStyle(
                fontSize: 14,
                color: Color(0xFF999999),
              ),
            ),
          ],
        ),
      ),
      Column(
        crossAxisAlignment: CrossAxisAlignment.end,
        children: [
          Text(
            '+\$300',
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.bold,
              color: Color(0xFF4CAF50),
            ),
          ),
          SizedBox(height: 4),
          Text(
            '2小时前',
            style: TextStyle(
              fontSize: 12,
              color: Color(0xFF999999),
            ),
          ),
        ],
      ),
    ],
  ),
)
```

---

## 表单设计

### 表单布局
```dart
Form(
  child: Column(
    children: [
      // 邮箱输入
      TextField(
        keyboardType: TextInputType.emailAddress,
        decoration: InputDecoration(
          labelText: '邮箱地址',
          hintText: '请输入邮箱地址',
          prefixIcon: Icon(Icons.email_outlined),
        ),
      ),
      SizedBox(height: 16),

      // 密码输入
      TextField(
        obscureText: _obscurePassword,
        decoration: InputDecoration(
          labelText: '密码',
          hintText: '请输入密码',
          prefixIcon: Icon(Icons.lock_outlined),
          suffixIcon: IconButton(
            icon: Icon(
              _obscurePassword
                  ? Icons.visibility_outlined
                  : Icons.visibility_off_outlined,
            ),
            onPressed: () {
              setState(() => _obscurePassword = !_obscurePassword);
            },
          ),
        ),
      ),
      SizedBox(height: 24),

      // 提交按钮
      SizedBox(
        width: double.infinity,
        height: 52,
        child: ElevatedButton(
          style: ElevatedButton.styleFrom(
            backgroundColor: Color(0xFF7424F5),
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(12),
            ),
          ),
          onPressed: () {},
          child: Text(
            '登录',
            style: TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.w600,
              color: Colors.white,
            ),
          ),
        ),
      ),
    ],
  ),
)
```

---

## 空状态（Empty State）

```dart
Center(
  child: Column(
    mainAxisAlignment: MainAxisAlignment.center,
    children: [
      Container(
        width: 120,
        height: 120,
        decoration: BoxDecoration(
          color: Color(0xFF7424F5).withOpacity(0.1),
          shape: BoxShape.circle,
        ),
        child: Icon(
          Icons.card_giftcard_outlined,
          color: Color(0xFF7424F5),
          size: 64,
        ),
      ),
      SizedBox(height: 24),
      Text(
        '暂无推荐记录',
        style: TextStyle(
          fontSize: 18,
          fontWeight: FontWeight.w600,
          color: Color(0xFF1A1A1A),
        ),
      ),
      SizedBox(height: 8),
      Text(
        '开始推荐好友，赚取丰厚奖励',
        style: TextStyle(
          fontSize: 14,
          color: Color(0xFF999999),
        ),
      ),
      SizedBox(height: 24),
      ElevatedButton(
        style: ElevatedButton.styleFrom(
          backgroundColor: Color(0xFF7424F5),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(12),
          ),
        ),
        onPressed: () {},
        child: Text('立即推荐'),
      ),
    ],
  ),
)
```

---

## 加载状态（Loading State）

### 页面加载
```dart
Center(
  child: Column(
    mainAxisAlignment: MainAxisAlignment.center,
    children: [
      CircularProgressIndicator(
        valueColor: AlwaysStoppedAnimation<Color>(Color(0xFF7424F5)),
      ),
      SizedBox(height: 16),
      Text(
        '加载中...',
        style: TextStyle(
          fontSize: 14,
          color: Color(0xFF999999),
        ),
      ),
    ],
  ),
)
```

### 按钮加载
```dart
ElevatedButton(
  style: ElevatedButton.styleFrom(
    backgroundColor: Color(0xFF7424F5),
    disabledBackgroundColor: Color(0xFF7424F5).withOpacity(0.5),
  ),
  onPressed: _isLoading ? null : () {},
  child: _isLoading
      ? SizedBox(
          width: 20,
          height: 20,
          child: CircularProgressIndicator(
            strokeWidth: 2,
            valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
          ),
        )
      : Text('提交'),
)
```

---

## 提示和通知（Toast & Snackbar）

### Snackbar 提示
```dart
ScaffoldMessenger.of(context).showSnackBar(
  SnackBar(
    content: Text('操作成功'),
    backgroundColor: Color(0xFF1A1A1A),
    behavior: SnackBarBehavior.floating,
    shape: RoundedRectangleBorder(
      borderRadius: BorderRadius.circular(8),
    ),
    duration: Duration(seconds: 2),
  ),
)
```

### 成功提示
```dart
ScaffoldMessenger.of(context).showSnackBar(
  SnackBar(
    content: Row(
      children: [
        Icon(Icons.check_circle, color: Color(0xFF4CAF50)),
        SizedBox(width: 12),
        Text('操作成功'),
      ],
    ),
    backgroundColor: Color(0xFF1A1A1A),
    behavior: SnackBarBehavior.floating,
    shape: RoundedRectangleBorder(
      borderRadius: BorderRadius.circular(8),
    ),
  ),
)
```

---

## 可访问性

### 语义化标签
```dart
Semantics(
  label: '推荐奖励金额',
  value: '900美元',
  child: Text(
    '\$900',
    style: TextStyle(fontSize: 48, fontWeight: FontWeight.bold),
  ),
)
```

### 触摸目标大小
- 最小触摸目标：44x44 dp（iOS）/ 48x48 dp（Android）
- 推荐：所有可点击元素至少 48x48 像素

---

## 性能优化

### 图片优化
```dart
// 使用 cached_network_image
CachedNetworkImage(
  imageUrl: 'https://example.com/image.jpg',
  placeholder: (context, url) => CircularProgressIndicator(),
  errorWidget: (context, url, error) => Icon(Icons.error),
  width: 100,
  height: 100,
  fit: BoxFit.cover,
)
```

### 列表优化
```dart
// 使用 ListView.builder 替代 ListView
ListView.builder(
  itemCount: items.length,
  itemBuilder: (context, index) {
    return ListItem(item: items[index]);
  },
)

// 使用 AutomaticKeepAliveClientMixin 混入保持状态
class ListItem extends StatefulWidget {
  @override
  _ListItemState createState() => _ListItemState();
}

class _ListItemState extends State<ListItem> with AutomaticKeepAliveClientMixin {
  @override
  bool get wantKeepAlive => true;

  @override
  Widget build(BuildContext context) {
    super.build(context);
    return ...;
  }
}
```

---

## 设计原则清单

在设计新页面时，遵循以下原则：

✅ **色彩**: 使用紫色主题色 `#7424F5` + 紫色色系辅色（`#9C4DFF`、`#B066FF`）
✅ **金色使用**: 金色 `#FFD700` **仅用于**奖励金额数字、特殊徽章等个别装饰元素
✅ **背景**: 使用紫色色系渐变背景（`#F5F3FF` → `#EDE9FE` → `#E9E5FF`）
✅ **字体**: 使用系统默认字体（SF Pro / Roboto），层级清晰
✅ **渐变**: 紫色渐变用于主卡片、背景；紫色淡色渐变用于次要卡片
✅ **卡片**: 白色/淡紫渐变卡片 + 紫色调阴影，圆角 12-20px
✅ **动画**: 淡入淡出转场，按钮点击缩放反馈
✅ **圆角**: 统一使用 8/12/16/20/24px 圆角
✅ **间距**: 遵循 8/12/16/24px 倍数系统
✅ **图标**: 使用 Material Symbols，配合紫色渐变背景
✅ **按钮**: 主按钮 52-56px 高度，全宽设计，紫色主题
✅ **输入框**: 圆角 12px，聚焦时紫色边框
✅ **激励感**: 金额使用紫色/绿色大字体显示，特殊奖励可用金色
✅ **层次感**: 使用装饰性光晕（紫色透明圆形）增加视觉层次
✅ **响应式**: 适配不同屏幕尺寸，使用百分比布局

---

## 页面模板

### 登录页模板
```dart
Scaffold(
  backgroundColor: Colors.white,
  body: SafeArea(
    child: SingleChildScrollView(
      padding: EdgeInsets.all(24),
      child: Column(
        children: [
          SizedBox(height: 40),

          // Logo 和标题
          Center(
            child: Column(
              children: [
                Container(
                  width: 80,
                  height: 80,
                  decoration: BoxDecoration(
                    color: Color(0xFF7424F5),
                    borderRadius: BorderRadius.circular(20),
                  ),
                  child: Icon(Icons.account_balance_wallet, color: Colors.white, size: 40),
                ),
                SizedBox(height: 24),
                Text(
                  '欢迎来到 xWallet',
                  style: TextStyle(
                    fontSize: 24,
                    fontWeight: FontWeight.bold,
                    color: Color(0xFF1A1A1A),
                  ),
                ),
                SizedBox(height: 8),
                Text(
                  '快速登录，开启财富之旅',
                  style: TextStyle(
                    fontSize: 14,
                    color: Color(0xFF999999),
                  ),
                ),
              ],
            ),
          ),

          SizedBox(height: 48),

          // 登录表单
          Form(...),

          SizedBox(height: 24),

          // 注册链接
          Center(
            child: TextButton(
              onPressed: () {},
              child: Text('还没有账号？立即注册'),
            ),
          ),
        ],
      ),
    ),
  ),
)
```

---

## Pencil 设计图

使用 [Pencil](https://pencil.dev) 根据本规范生成的可视化设计图：

- **登录页 (Login Screen)**：Logo（紫色圆角方块 + **X 字形图标/字母「X」**，與 [www.xwallet.hk](https://www.xwallet.hk) 品牌一致）、欢迎文案「**欢迎来到 X Wallet**」「快速登录，开启财富之旅」、邮箱/密码输入框、主按钮「登录」、注册链接「还没有账号？立即注册」。
- **主页 (Home Screen)**：顶部栏「首页」+ 通知图标、推荐友奖赏卡片（**奖赏高达 $1,000 / $3,000** 等，與官網迎新/友獎賞一致，金色强调）、快捷入口（贷款 / 奖励 / 我的）、底部导航（首页 / 贷款 / 奖励 / 我的）。

**建議改進（對齊官網）**：  
1）Logo 改為 **X 字形** 或從官網取正式 logo 替換；  
2）產品名統一為 **X Wallet**；  
3）獎賞金額改為官網數字（如 $3,000 迎新、$1,000 推薦）；  
4）若有空間可加入官網賣點（如「A.I. 即時審批」「每日利息 $0.032/千」）。

配色与组件均按本规范：主色 `#7424F5`、金色 `#FFD700`、背景 `#F3F5F7` / `#FFFFFF`、圆角 12px、间距 16/24px。设计图在 Pencil 中保存为 `pencil-new.pen`，可在 Pencil 编辑器中打开继续编辑或导出。

---

## 版本历史

- **v1.1** (2026-02-01): 配色方案优化
  - **辅助色调整**: 辅色从金色改为紫色色系（`#9C4DFF`、`#B066FF`）
  - **金色定位明确**: 金色 `#FFD700` 仅用于奖励金额、特殊徽章等个别装饰元素
  - **背景色增强**: 添加紫色色系渐变背景（`#F5F3FF` → `#EDE9FE` → `#E9E5FF`）
  - **层次感设计**: 添加装饰性紫色光晕元素，增强视觉层次
  - **卡片背景优化**: 卡片使用白色到淡紫渐变，配合紫色调阴影

- **v1.0** (2026-02-01): 初始 APP 设计规范
  - 主色设置为 #7424F5（紫色）
  - 金色强调色 #FFD700
  - 年轻化、激励感的设计风格
  - 推荐奖励页面完整布局参考
  - 组件样式规范（按钮、卡片、输入框）
  - 动画系统（页面转场、元素动画）
  - 详细的 Flutter 代码示例

---

## 与管理后台风格对比

| 维度 | APP（移动端） | 后台（Web 管理系统） |
|------|-------------|-------------------|
| **目标用户** | 普通顾客（Z世代/新中产） | 系统员工 |
| **设计风格** | 年轻化、激励感 | 专业、严肃、高效 |
| **主色** | #7424F5（亮紫色） | oklch(42% 0.22 295)（专业紫） |
| **强调色** | #FFD700（金色）- 用于金额 | 无专门强调色 |
| **字体** | SF Pro / Roboto（系统字体） | DM Sans（专业字体） |
| **圆角** | 12px（较大，更亲和） | 8px（较小，更紧凑） |
| **动画** | 丰富（淡入淡出、缩放） | 简洁（必要时使用） |
| **布局** | 移动优先，全宽设计 | 响应式，卡片网格 |
| **交互** | 按钮点击缩放反馈 | hover 效果（桌面端） |
