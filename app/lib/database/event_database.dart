import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart';
import 'event_entity.dart';

class EventDatabase {
  static final EventDatabase instance = EventDatabase._internal();
  static Database? _database;

  EventDatabase._internal();

  Future<Database> get database async {
    if (_database != null) return _database!;
    _database = await _initDatabase();
    return _database!;
  }

  Future<Database> _initDatabase() async {
    final dbPath = await getDatabasesPath();
    final path = join(dbPath, 'events.db');

    return await openDatabase(
      path,
      version: 1,
      onCreate: _onCreate,
    );
  }

  Future<void> _onCreate(Database db, int version) async {
    await db.execute('''
      CREATE TABLE events (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        event_id TEXT NOT NULL UNIQUE,
        device_id TEXT NOT NULL,
        user_id TEXT,
        event_type TEXT NOT NULL,
        topic TEXT NOT NULL,
        qos INTEGER NOT NULL DEFAULT 1,
        payload TEXT NOT NULL,
        status TEXT NOT NULL DEFAULT 'pending',
        retry_count INTEGER DEFAULT 0,
        created_at INTEGER NOT NULL,
        sent_at INTEGER,
        next_retry_at INTEGER,
        priority TEXT DEFAULT 'normal'
      )
    ''');

    // 创建索引
    await db.execute('''
      CREATE INDEX idx_status_created ON events(status, created_at)
    ''');
    
    await db.execute('''
      CREATE INDEX idx_next_retry ON events(next_retry_at)
    ''');

    await db.execute('''
      CREATE TABLE config (
        key TEXT PRIMARY KEY,
        value TEXT NOT NULL
      )
    ''');

    // 初始化配置
    await db.insert('config', {'key': 'max_events', 'value': '1000'});
    await db.insert('config', {'key': 'batch_size', 'value': '100'});
  }

  /// 插入事件
  Future<void> insertEvent(EventEntity event) async {
    final db = await database;
    await db.insert('events', event.toMap(), conflictAlgorithm: ConflictAlgorithm.replace);
  }

  /// 查询待重试事件
  Future<List<EventEntity>> getPendingEvents(int limit) async {
    final db = await database;
    final now = DateTime.now().millisecondsSinceEpoch;

    final List<Map<String, dynamic>> maps = await db.rawQuery('''
      SELECT * FROM events
      WHERE status = 'pending'
        AND (next_retry_at IS NULL OR next_retry_at < ?)
      ORDER BY priority DESC, created_at ASC
      LIMIT ?
    ''', [now, limit]);

    return maps.map((map) => EventEntity.fromMap(map)).toList();
  }

  /// 删除已成功的事件
  Future<void> deleteEvent(String eventId) async {
    final db = await database;
    await db.delete(
      'events',
      where: 'event_id = ?',
      whereArgs: [eventId],
    );
  }

  /// 更新重试次数和下次重试时间
  Future<void> updateRetryInfo(
    String eventId,
    int retryCount,
    int nextRetryAt,
  ) async {
    final db = await database;
    await db.update(
      'events',
      {
        'retry_count': retryCount,
        'next_retry_at': nextRetryAt,
      },
      where: 'event_id = ?',
      whereArgs: [eventId],
    );
  }

  /// 获取当前事件数量
  Future<int> getEventCount() async {
    final db = await database;
    final result = await db.rawQuery('SELECT COUNT(*) as count FROM events');
    return Sqflite.firstIntValue(result) ?? 0;
  }

  /// 清理旧事件（LRU）
  Future<void> enforceCapacityLimit() async {
    final db = await database;
    final config = await db.query('config', where: 'key = ?', whereArgs: ['max_events']);
    final maxEvents = int.parse(config.first['value'] as String);

    final count = await getEventCount();
    if (count > maxEvents) {
      // SQLite DELETE需要使用子查询来支持LIMIT
      final excess = count - maxEvents;
      await db.rawDelete('''
        DELETE FROM events
        WHERE rowid IN (
          SELECT rowid FROM events
          WHERE status = 'pending'
          ORDER BY created_at ASC
          LIMIT ?
        )
      ''', [excess]);
    }
  }

  Future<void> close() async {
    final db = await database;
    await db.close();
  }
}
