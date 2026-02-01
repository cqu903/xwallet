import 'dart:convert';
import 'package:json_annotation/json_annotation.dart';
import 'event_context.dart';
import 'event_properties.dart';

part 'analytics_event.g.dart';

@JsonSerializable()
class AnalyticsEvent {
  final String eventId;          // UUID
  final String deviceId;         // 设备ID
  final String? userId;          // 用户ID（可选）
  final String eventType;        // 事件类型
  final int timestamp;           // 时间戳（毫秒）
  final String environment;      // 环境：prod/dev/test
  final EventContext context;
  final Map<String, dynamic> properties;
  final Map<String, dynamic>? riskContext;

  AnalyticsEvent({
    required this.eventId,
    required this.deviceId,
    this.userId,
    required this.eventType,
    required this.timestamp,
    required this.environment,
    required this.context,
    required this.properties,
    this.riskContext,
  });

  factory AnalyticsEvent.fromJson(Map<String, dynamic> json) =>
      _$AnalyticsEventFromJson(json);

  Map<String, dynamic> toJson() => _$AnalyticsEventToJson(this);

  /// 转换为 MQTT payload
  String toPayload() {
    return jsonEncode(toJson());
  }

  /// 获取 MQTT Topic
  String getTopic(EventCategory category) {
    return 'app/$environment/${category.name}';
  }
}

enum EventCategory {
  critical,
  behavior,
  system,
}
