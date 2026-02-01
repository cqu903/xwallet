import 'dart:async';
import 'package:flutter/foundation.dart';
import '../models/analytics_event.dart';
import 'event_collector.dart';
import 'event_reporter.dart';
import 'mqtt_client_wrapper.dart';
import '../database/event_database.dart';
import '../utils/device_info_collector.dart';
import '../utils/platform_config.dart';

class AnalyticsService {
  static final AnalyticsService instance = AnalyticsService._internal();

  late MqttClientWrapper _mqttClient;
  late EventReporter _reporter;
  Timer? _retryTimer;

  AnalyticsService._internal();

  /// 初始化
  Future<void> initialize() async {
    final deviceId = await DeviceInfoCollector.getDeviceId();
    final clientId = 'xwallet-$deviceId';

    _mqttClient = MqttClientWrapper(
      broker: PlatformConfig.mqttBroker,
      clientId: clientId,
      port: PlatformConfig.mqttPort,
      useSSL: PlatformConfig.mqttUseSSL,
      username: null, // 开发环境允许匿名连接
      password: null,
    );

    try {
      await _mqttClient.connect();
      print('✅ MQTT connected to ${PlatformConfig.mqttBroker}:${PlatformConfig.mqttPort}');
    } catch (e) {
      print('⚠️  MQTT connection failed: $e');
      print('📦 Events will be saved to SQLite for retry');
      // 即使MQTT连接失败也继续,事件会存入SQLite
    }

    _reporter = EventReporter(
      mqttClient: _mqttClient,
      database: EventDatabase.instance,
    );

    // 启动定时重试任务（每 10 秒）
    _startRetryTask();
  }

  /// 上报事件
  Future<void> trackEvent({
    required String eventType,
    required Map<String, dynamic> properties,
    String? userId,
    EventCategory category = EventCategory.behavior,
    Map<String, dynamic>? riskContext,
  }) async {
    final event = await EventCollector.collect(
      eventType: eventType,
      properties: properties,
      userId: userId,
      riskContext: riskContext,
    );

    await _reporter.report(event, category);
  }

  /// 启动定时重试任务
  void _startRetryTask() {
    _retryTimer = Timer.periodic(Duration(seconds: 10), (timer) {
      _reporter.retryPendingEvents();
    });
  }

  /// 销毁
  Future<void> dispose() async {
    _retryTimer?.cancel();
    await _mqttClient.disconnect();
  }
}
