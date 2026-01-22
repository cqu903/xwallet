import 'package:uuid/uuid.dart';
import '../models/analytics_event.dart';
import '../models/event_context.dart';
import '../utils/device_info_collector.dart';
import '../config/analytics_config.dart';

class EventCollector {
  static final Uuid _uuid = Uuid();

  /// 采集事件
  static Future<AnalyticsEvent> collect({
    required String eventType,
    required Map<String, dynamic> properties,
    String? userId,
    Map<String, dynamic>? riskContext,
  }) async {
    final deviceId = await DeviceInfoCollector.getDeviceId();
    final context = await EventContext.collect();

    return AnalyticsEvent(
      eventId: _uuid.v4(),
      deviceId: deviceId,
      userId: userId,
      eventType: eventType,
      timestamp: DateTime.now().millisecondsSinceEpoch,
      environment: AnalyticsConfig.environment,
      context: context,
      properties: properties,
      riskContext: riskContext,
    );
  }
}
