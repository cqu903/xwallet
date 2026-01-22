import 'package:flutter_test/flutter_test.dart';
import 'package:sqflite_common_ffi/sqflite_ffi.dart';
import 'package:app/database/event_database.dart';
import 'package:app/database/event_entity.dart';

void main() {
  setUpAll(() {
    sqfliteFfiInit();
    databaseFactory = databaseFactoryFfi;
  });

  setUp(() async {
    // 每个测试前清空数据库
    final db = EventDatabase.instance;
    final database = await db.database;
    await database.delete('events');
  });

  test('should insert and retrieve event', () async {
    final db = EventDatabase.instance;
    await db.database; // 初始化

    final event = EventEntity(
      eventId: 'test-insert-123',
      deviceId: 'device-abc',
      eventType: 'login',
      topic: 'app/prod/critical',
      qos: 1,
      payload: '{"test": "data"}',
      status: 'pending',
      retryCount: 0,
      createdAt: DateTime.now().millisecondsSinceEpoch,
    );

    await db.insertEvent(event);

    final retrieved = await db.getPendingEvents(10);
    expect(retrieved.length, 1);
    expect(retrieved.first.eventId, 'test-insert-123');
  });

  test('should enforce capacity limit', () async {
    final db = EventDatabase.instance;

    // 插入超过限制的事件
    for (int i = 0; i < 20; i++) {
      final event = EventEntity(
        eventId: 'test-capacity-$i',
        deviceId: 'device-abc',
        eventType: 'login',
        topic: 'app/prod/critical',
        qos: 1,
        payload: '{"test": "data"}',
        status: 'pending',
        retryCount: 0,
        createdAt: DateTime.now().millisecondsSinceEpoch,
      );
      await db.insertEvent(event);
    }

    // 修改最大事件数为10以便快速测试
    final database = await db.database;
    await database.update('config', {'value': '10'}, where: 'key = ?', whereArgs: ['max_events']);

    await db.enforceCapacityLimit();
    final count = await db.getEventCount();
    expect(count, lessThanOrEqualTo(10));
  });
}
