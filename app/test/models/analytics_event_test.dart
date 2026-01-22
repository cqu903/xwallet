import 'package:flutter_test/flutter_test.dart';
import 'package:app/models/analytics_event.dart';
import 'package:app/models/event_context.dart';

void main() {
  group('AnalyticsEvent', () {
    test('should serialize to JSON correctly', () {
      final event = AnalyticsEvent(
        eventId: 'test-123',
        deviceId: 'device-abc',
        eventType: 'login',
        timestamp: 1705210800000,
        environment: 'prod',
        context: EventContext(
          appVersion: '1.0.0',
          os: 'iOS',
          osVersion: '17.2',
          deviceModel: 'iPhone 15',
          networkType: 'wifi',
          screenSize: '393x852',
          timezone: 'Asia/Shanghai',
          language: 'zh-CN',
        ),
        properties: {'loginMethod': 'email', 'success': true},
      );

      final json = event.toJson();

      expect(json['eventId'], 'test-123');
      expect(json['eventType'], 'login');
      expect(json['environment'], 'prod');
    });

    test('should generate correct MQTT topic', () {
      final event = AnalyticsEvent(
        eventId: 'test-123',
        deviceId: 'device-abc',
        eventType: 'payment',
        timestamp: 1705210800000,
        environment: 'prod',
        context: EventContext(
          appVersion: '1.0.0',
          os: 'iOS',
          osVersion: '17.2',
          deviceModel: 'iPhone 15',
          networkType: 'wifi',
          screenSize: '393x852',
          timezone: 'Asia/Shanghai',
          language: 'zh-CN',
        ),
        properties: {},
      );

      expect(event.getTopic(EventCategory.critical), 'app/prod/critical');
      expect(event.getTopic(EventCategory.behavior), 'app/prod/behavior');
    });
  });
}
