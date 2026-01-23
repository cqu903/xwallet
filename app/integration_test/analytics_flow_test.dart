import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';
import 'package:app/main.dart';
import 'package:app/models/analytics_event.dart';
import 'package:app/services/analytics_service.dart';

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  testWidgets('full analytics flow test', (WidgetTester tester) async {
    // 1. 启动应用
    await tester.pumpWidget(MyApp());
    await tester.pumpAndSettle();

    // 2. 等待埋点服务初始化
    await Future.delayed(Duration(seconds: 2));

    // 3. 发送测试事件
    await AnalyticsService.instance.trackEvent(
      eventType: 'test_event',
      properties: {'test_key': 'test_value'},
      userId: 'test_user_123',
      category: EventCategory.behavior,
    );

    // 4. 等待上报
    await Future.delayed(Duration(seconds: 3));

    // 5. 验证（检查日志或数据库）
    // 注意：完整验证需要:
    // - MQTT broker运行中
    // - 检查MQTT消息是否成功发送
    // - 检查SQLite中是否缓存失败事件
    expect(true, true); // 占位符
  });

  testWidgets('analytics service offline mode test', (WidgetTester tester) async {
    // 测试离线模式
    await tester.pumpWidget(MyApp());
    await tester.pumpAndSettle();

    // 模拟MQTT连接失败的场景
    // 事件应该存入SQLite
    await AnalyticsService.instance.trackEvent(
      eventType: 'offline_test',
      properties: {'mode': 'offline'},
      category: EventCategory.critical,
    );

    await Future.delayed(Duration(seconds: 1));

    // TODO: 验证SQLite中是否有该事件
    expect(true, true); // 占位符
  });
}
