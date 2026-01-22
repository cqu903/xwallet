import 'dart:async';
import 'package:flutter/foundation.dart';
import '../models/analytics_event.dart';
import '../database/event_database.dart';
import '../database/event_entity.dart';
import 'mqtt_client_wrapper.dart';

class EventReporter {
  final MqttClientWrapper mqttClient;
  final EventDatabase database;

  final Set<String> _pendingEvents = {};

  EventReporter({
    required this.mqttClient,
    required this.database,
  });

  /// 上报事件：立即尝试，失败则存本地
  Future<void> report(
    AnalyticsEvent event,
    EventCategory category,
  ) async {
    final eventId = event.eventId;

    // 防止重复入队
    if (_pendingEvents.contains(eventId)) {
      return;
    }

    _pendingEvents.add(eventId);

    try {
      // 1. 立即尝试上报
      final topic = event.getTopic(category);
      final payload = event.toPayload();

      await mqttClient.publish(topic, payload);

      print('Event sent immediately: $eventId');
      // 成功则结束，不存 SQLite

    } catch (e) {
      print('Event send failed, saving to SQLite: $eventId, error: $e');

      // 2. 失败则存入本地
      final entity = EventEntity(
        eventId: eventId,
        deviceId: event.deviceId,
        userId: event.userId,
        eventType: event.eventType,
        topic: event.getTopic(category),
        qos: 1,
        payload: event.toPayload(),
        status: 'pending',
        retryCount: 0,
        createdAt: event.timestamp,
        nextRetryAt: _calculateNextRetry(0),
        priority: category == EventCategory.critical ? 'high' : 'normal',
      );

      await database.insertEvent(entity);
      await database.enforceCapacityLimit();
    } finally {
      _pendingEvents.remove(eventId);
    }
  }

  /// 重试待发送事件
  Future<void> retryPendingEvents() async {
    if (!mqttClient.isConnected) {
      return;
    }

    final pendingEvents = await database.getPendingEvents(100);

    for (final event in pendingEvents) {
      try {
        await mqttClient.publish(event.topic, event.payload);

        // 成功：删除
        await database.deleteEvent(event.eventId);
        print('Retry success: ${event.eventId}');

      } catch (e) {
        // 仍失败：更新重试次数
        final retryCount = event.retryCount + 1;
        final nextRetryAt = _calculateNextRetry(retryCount);

        await database.updateRetryInfo(
          event.eventId,
          retryCount,
          nextRetryAt,
        );

        print('Retry failed (${retryCount}x): ${event.eventId}');
      }
    }
  }

  /// 指数退避：2^n 秒，最大 60 秒
  int _calculateNextRetry(int retryCount) {
    final delay = (1 << retryCount) * 1000; // 毫秒
    final maxDelay = 60000;
    return DateTime.now().millisecondsSinceEpoch + (delay > maxDelay ? maxDelay : delay);
  }
}
