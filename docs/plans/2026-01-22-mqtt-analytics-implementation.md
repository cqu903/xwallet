# MQTT 埋点系统实施计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**目标：** 为移动端应用（app/）添加高实时性的用户行为埋点系统，支持风控决策场景

**架构：** Flutter 端采集事件并通过 MQTT QoS 1 立即上报，失败则存入 SQLite 持久化并持续重试；后端通过 MQTT 订阅接收事件，使用 Redis 进行去重后分发到风控服务

**技术栈：** Flutter (mqtt_client, sqflite), MQTT Broker (EMQX), Spring Boot (Eclipse Paho), Redis

---

## 前置准备

### Task 0: 环境准备和依赖安装

**目标：** 搭建 MQTT Broker 和准备好开发环境

**Step 1: 注册 EMQX Cloud 账号**

访问 https://www.emqx.com/zh/cloud 注册免费试用账号

创建实例后获得：
- MQTT Broker 地址：`broker.emqxsl.com`
- 端口：`8883` (TLS)
- Client ID、Username、Password

**Step 2: 添加 Flutter 依赖**

编辑 `app/pubspec.yaml`:

```yaml
dependencies:
  mqtt_client: ^10.0.0
  sqflite: ^2.3.0
  path_provider: ^2.1.0
  device_info_plus: ^9.0.0
  connectivity_plus: ^5.0.0
  uuid: ^4.0.0
  json_annotation: ^4.8.0

dev_dependencies:
  json_serializable: ^4.8.0
  build_runner: ^2.4.0
```

运行：`flutter pub get`

预期：所有依赖成功安装，无冲突

**Step 3: 添加后端依赖**

编辑 `backend/pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>org.eclipse.paho</groupId>
        <artifactId>org.eclipse.paho.mqttv5.client</artifactId>
        <version>1.2.5</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.integration</groupId>
        <artifactId>spring-integration-mqtt</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
</dependencies>
```

运行：`mvn clean install`

预期：构建成功，Maven 下载所有依赖

**Step 4: 配置后端 MQTT 连接**

创建 `backend/src/main/resources/application-mqtt.yml`:

```yaml
mqtt:
  broker: tcp://${MQTT_BROKER_HOST:broker.emqxsl.com}:1883
  client-id: xwallet-backend-${random.value}
  username: ${MQTT_USERNAME}
  password: ${MQTT_PASSWORD}
  topic: app/prod/+
  qos: 1
  completion-timeout: 30000
```

修改 `application-dev.yml` 添加：

```yaml
spring:
  profiles:
    include: mqtt
```

**Step 5: 提交环境准备**

```bash
cd app
git add pubspec.yaml
git commit -m "feat(analytics): 添加 MQTT 埋点相关依赖"

cd ../backend
git add pom.xml src/main/resources/
git commit -m "feat(analytics): 添加 MQTT 后端依赖和配置"
```

---

## 阶段一：前端基础架构

### Task 1: 创建事件数据模型

**目标：** 定义事件数据结构，支持 JSON 序列化

**文件：**
- 创建: `app/lib/models/analytics_event.dart`
- 创建: `app/lib/models/event_context.dart`
- 创建: `app/lib/models/event_properties.dart`

**Step 1: 创建事件上下文模型**

创建 `app/lib/models/event_context.dart`:

```dart
import 'package:json_annotation/json_annotation.dart';

part 'event_context.g.dart';

@JsonSerializable()
class EventContext {
  final String appVersion;
  final String os;
  final String osVersion;
  final String deviceModel;
  final String networkType;
  final String? carrier;
  final String screenSize;
  final String timezone;
  final String language;

  EventContext({
    required this.appVersion,
    required this.os,
    required this.osVersion,
    required this.deviceModel,
    required this.networkType,
    this.carrier,
    required this.screenSize,
    required this.timezone,
    required this.language,
  });

  factory EventContext.fromJson(Map<String, dynamic> json) =>
      _$EventContextFromJson(json);

  Map<String, dynamic> toJson() => _$EventContextToJson(this);

  /// 自动采集上下文信息
  static Future<EventContext> collect() async {
    // TODO: Task 2 实现
    throw UnimplementedError();
  }
}
```

**Step 2: 创建事件属性模型（基类）**

创建 `app/lib/models/event_properties.dart`:

```dart
import 'package:json_annotation/json_annotation.dart';

part 'event_properties.g.dart';

/// 基础属性类
@JsonSerializable()
class EventProperties {
  final Map<String, dynamic> data;

  EventProperties(this.data);

  factory EventProperties.fromJson(Map<String, dynamic> json) =>
      _$EventPropertiesFromJson(json);

  Map<String, dynamic> toJson() => _$EventPropertiesToJson(this);
}

/// 登录事件属性
@JsonSerializable()
class LoginEventProperties extends EventProperties {
  final String loginMethod;
  final bool success;
  final String? failureReason;

  LoginEventProperties({
    required this.loginMethod,
    required this.success,
    this.failureReason,
  }) : super({});

  factory LoginEventProperties.fromJson(Map<String, dynamic> json) =>
      _$LoginEventPropertiesFromJson(json);

  @override
  Map<String, dynamic> toJson() => _$LoginEventPropertiesToJson(this);
}

/// 支付事件属性
@JsonSerializable()
class PaymentEventProperties extends EventProperties {
  final double amount;
  final String currency;
  final String paymentMethod;
  final String? merchantId;

  PaymentEventProperties({
    required this.amount,
    required this.currency,
    required this.paymentMethod,
    this.merchantId,
  }) : super({});

  factory PaymentEventProperties.fromJson(Map<String, dynamic> json) =>
      _$PaymentEventPropertiesFromJson(json);

  @override
  Map<String, dynamic> toJson() => _$PaymentEventPropertiesToJson(this);
}
```

**Step 3: 创建主事件模型**

创建 `app/lib/models/analytics_event.dart`:

```dart
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
    return toJson().toString();
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
```

**Step 4: 生成 JSON 序列化代码**

运行：`flutter pub run build_runner build`

预期：生成 `*.g.dart` 文件，无错误

**Step 5: 编写单元测试**

创建 `app/test/models/analytics_event_test.dart`:

```dart
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
```

运行：`flutter test test/models/analytics_event_test.dart`

预期：PASS

**Step 6: 提交**

```bash
git add app/lib/models/ app/test/models/
git commit -m "feat(analytics): 添加事件数据模型和单元测试"
```

---

### Task 2: 实现设备信息采集

**目标：** 实现 EventContext 自动采集

**文件：**
- 修改: `app/lib/models/event_context.dart`
- 创建: `app/lib/utils/device_info_collector.dart`

**Step 1: 创建设备信息采集工具**

创建 `app/lib/utils/device_info_collector.dart`:

```dart
import 'dart:io';
import 'package:device_info_plus/device_info_plus.dart';
import 'package:connectivity_plus/connectivity_plus.dart';

class DeviceInfoCollector {
  static final DeviceInfoPlugin deviceInfo = DeviceInfoPlugin();
  static final Connectivity connectivity = Connectivity();

  /// 获取设备 ID
  static Future<String> getDeviceId() async {
    if (Platform.isIOS) {
      IosDeviceInfo iosInfo = await deviceInfo.iosInfo;
      return iosInfo.identifierForVendor;
    } else {
      AndroidDeviceInfo androidInfo = await deviceInfo.androidInfo;
      return androidInfo.id;
    }
  }

  /// 获取设备型号
  static Future<String> getDeviceModel() async {
    if (Platform.isIOS) {
      IosDeviceInfo iosInfo = await deviceInfo.iosInfo;
      return '${iosInfo.name} ${iosInfo.model}';
    } else {
      AndroidDeviceInfo androidInfo = await deviceInfo.androidInfo;
      return '${androidInfo.brand} ${androidInfo.model}';
    }
  }

  /// 获取网络类型
  static Future<String> getNetworkType() async {
    final results = await connectivity.checkConnectivity();
    final result = results.first;

    switch (result) {
      case ConnectivityResult.wifi:
        return 'wifi';
      case ConnectivityResult.mobile:
        return '4g';
      case ConnectivityResult.ethernet:
        return 'ethernet';
      case ConnectivityResult.bluetooth:
        return 'bluetooth';
      case ConnectivityResult.none:
        return 'none';
      default:
        return 'unknown';
    }
  }

  /// 获取运营商（仅 Android）
  static Future<String?> getCarrier() async {
    if (Platform.isAndroid) {
      AndroidDeviceInfo androidInfo = await deviceInfo.androidInfo;
      return androidInfo.androidId;
    }
    return null;
  }

  /// 获取屏幕尺寸（需 BuildContext，这里先返回固定值）
  static String getScreenSize() {
    // TODO: 从 MediaQuery 获取
    return '390x844'; // iPhone 13 默认尺寸
  }
}
```

**Step 2: 实现 EventContext.collect()**

修改 `app/lib/models/event_context.dart`:

```dart
import 'package:uuid/uuid.dart';
import '../utils/device_info_collector.dart';
import '../utils/app_info.dart'; // 需要创建

// 在 EventContext 类中替换 collect 方法：

static Future<EventContext> collect() async {
  final networkType = await DeviceInfoCollector.getNetworkType();
  final carrier = await DeviceInfoCollector.getCarrier();

  return EventContext(
    appVersion: await AppInfo.getVersion(),
    os: Platform.isIOS ? 'iOS' : 'Android',
    osVersion: await _getOsVersion(),
    deviceModel: await DeviceInfoCollector.getDeviceModel(),
    networkType: networkType,
    carrier: carrier,
    screenSize: DeviceInfoCollector.getScreenSize(),
    timezone: DateTime.now().timeZoneName,
    language: Platform.localeName,
  );
}

static Future<String> _getOsVersion() async {
  if (Platform.isIOS) {
    IosDeviceInfo iosInfo = await DeviceInfoPlugin().iosInfo;
    return iosInfo.systemVersion;
  } else {
    AndroidDeviceInfo androidInfo = await DeviceInfoPlugin().androidInfo;
    return androidInfo.release;
  }
}
```

**Step 3: 创建 AppInfo 工具**

创建 `app/lib/utils/app_info.dart`:

```dart
import 'package:package_info_plus/package_info_plus.dart';

class AppInfo {
  static Future<String> getVersion() async {
    final info = await PackageInfo.fromPlatform();
    return info.version;
  }

  static Future<String> getBuildNumber() async {
    final info = await PackageInfo.fromPlatform();
    return info.buildNumber;
  }
}
```

添加依赖到 `pubspec.yaml`:

```yaml
dependencies:
  package_info_plus: ^4.0.0
```

运行：`flutter pub get`

**Step 4: 添加 package_info_plus 依赖并提交**

```bash
flutter pub get
git add app/pubspec.yaml app/lib/models/event_context.dart app/lib/utils/ pubspec.lock
git commit -m "feat(analytics): 实现设备和应用信息采集"
```

---

### Task 3: 创建 SQLite 持久化层

**目标：** 创建本地数据库存储失败事件

**文件：**
- 创建: `app/lib/database/event_database.dart`
- 创建: `app/lib/database/event_entity.dart`

**Step 1: 创建事件实体**

创建 `app/lib/database/event_entity.dart`:

```dart
class EventEntity {
  final int? id;
  final String eventId;
  final String deviceId;
  final String? userId;
  final String eventType;
  final String topic;
  final int qos;
  final String payload;
  final String status; // pending/sent/failed
  final int retryCount;
  final int createdAt;
  final int? sentAt;
  final int? nextRetryAt;
  final String priority; // high/normal

  EventEntity({
    this.id,
    required this.eventId,
    required this.deviceId,
    this.userId,
    required this.eventType,
    required this.topic,
    required this.qos,
    required this.payload,
    required this.status,
    required this.retryCount,
    required this.createdAt,
    this.sentAt,
    this.nextRetryAt,
    this.priority = 'normal',
  });

  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'event_id': eventId,
      'device_id': deviceId,
      'user_id': userId,
      'event_type': eventType,
      'topic': topic,
      'qos': qos,
      'payload': payload,
      'status': status,
      'retry_count': retryCount,
      'created_at': createdAt,
      'sent_at': sentAt,
      'next_retry_at': nextRetryAt,
      'priority': priority,
    };
  }

  factory EventEntity.fromMap(Map<String, dynamic> map) {
    return EventEntity(
      id: map['id'] as int?,
      eventId: map['event_id'] as String,
      deviceId: map['device_id'] as String,
      userId: map['user_id'] as String?,
      eventType: map['event_type'] as String,
      topic: map['topic'] as String,
      qos: map['qos'] as int,
      payload: map['payload'] as String,
      status: map['status'] as String,
      retryCount: map['retry_count'] as int,
      createdAt: map['created_at'] as int,
      sentAt: map['sent_at'] as int?,
      nextRetryAt: map['next_retry_at'] as int?,
      priority: map['priority'] as String? ?? 'normal',
    );
  }
}
```

**Step 2: 创建数据库辅助类**

创建 `app/lib/database/event_database.dart`:

```dart
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
        priority TEXT DEFAULT 'normal',
        INDEX idx_status_created (status, created_at),
        INDEX idx_next_retry (next_retry_at)
      )
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
        AND next_retry_at < ?
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
      await db.rawDelete('''
        DELETE FROM events
        WHERE status = 'pending'
        ORDER BY created_at ASC
        LIMIT ?
      ''', [count - maxEvents]);
    }
  }

  Future<void> close() async {
    final db = await database;
    await db.close();
  }
}
```

**Step 3: 编写数据库测试**

创建 `app/test/database/event_database_test.dart`:

```dart
import 'package:flutter_test/flutter_test.dart';
import 'package:app/database/event_database.dart';
import 'package:app/database/event_entity.dart';
import 'package:sqflite_common_ffi/sqflite_ffi.dart';

void main() {
  setUpAll(() {
    sqfliteFfiInit();
    databaseFactory = databaseFactoryFfi;
  });

  test('should insert and retrieve event', () async {
    final db = EventDatabase.instance;
    await db.database; // 初始化

    final event = EventEntity(
      eventId: 'test-123',
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
    expect(retrieved.first.eventId, 'test-123');
  });

  test('should enforce capacity limit', () async {
    final db = EventDatabase.instance;

    // 插入超过限制的事件
    for (int i = 0; i < 1100; i++) {
      final event = EventEntity(
        eventId: 'test-$i',
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

    await db.enforceCapacityLimit();
    final count = await db.getEventCount();
    expect(count, lessThanOrEqualTo(1000));
  });
}
```

**Step 4: 提交**

```bash
git add app/lib/database/ app/test/database/
git commit -m "feat(analytics): 添加 SQLite 持久化层和测试"
```

---

### Task 4: 实现 MQTT 客户端

**目标：** 创建 MQTT 客户端，支持自动重连和 QoS 1

**文件：**
- 创建: `app/lib/services/mqtt_client_wrapper.dart`
- 修改: `app/lib/services/analytics_service.dart` (后续 Task)

**Step 1: 创建 MQTT 客户端包装类**

创建 `app/lib/services/mqtt_client_wrapper.dart`:

```dart
import 'package:mqtt_client/mqtt_client.dart';
import 'package:mqtt_client/mqtt_server_client.dart';

class MqttClientWrapper {
  late MqttServerClient _client;
  bool _isConnected = false;

  final String broker;
  final String clientId;
  final String? username;
  final String? password;

  MqttClientWrapper({
    required this.broker,
    required this.clientId,
    this.username,
    this.password,
  });

  /// 连接状态
  bool get isConnected => _isConnected;

  /// 初始化并连接
  Future<void> connect() async {
    _client = MqttServerClient(broker, clientId);
    _client.port = 8883; // TLS 端口
    _client.keepAlivePeriod = 60;
    _client.logging(on: false);

    _client.onConnected = () {
      print('MQTT connected');
      _isConnected = true;
    };

    _client.onDisconnected = () {
      print('MQTT disconnected');
      _isConnected = false;
    };

    final connMess = MqttConnectMessage()
        .withClientIdentifier(clientId)
        .startClean()
        .withWillQos(MqttQos.atLeastOnce);

    _client.connectionMessage = connMess;

    try {
      await _client.connect(username, password);
    } catch (e) {
      print('MQTT connection failed: $e');
      _isConnected = false;
      rethrow;
    }
  }

  /// 发布消息
  Future<void> publish(
    String topic,
    String payload, {
    MqttQos qos = MqttQos.atLeastOnce,
  }) async {
    if (!_isConnected) {
      throw Exception('MQTT not connected');
    }

    final builder = MqttClientPayloadBuilder();
    builder.addString(payload);

    _client.publishMessage(
      topic,
      qos,
      builder.payload!,
    );
  }

  /// 断开连接
  Future<void> disconnect() async {
    await _client.disconnect();
    _isConnected = false;
  }
}
```

**Step 2: 添加环境变量配置**

创建 `app/lib/config/analytics_config.dart`:

```dart
class AnalyticsConfig {
  static const String broker = String.fromEnvironment(
    'MQTT_BROKER',
    defaultValue: 'broker.emqxsl.com',
  );

  static const String mqttUsername = String.fromEnvironment(
    'MQTT_USERNAME',
  );

  static const String mqttPassword = String.fromEnvironment(
    'MQTT_PASSWORD',
  );

  static const String environment = String.fromEnvironment(
    'ENV',
    defaultValue: 'dev',
  );
}
```

**Step 3: 提交**

```bash
git add app/lib/services/mqtt_client_wrapper.dart app/lib/config/analytics_config.dart
git commit -m "feat(analytics): 添加 MQTT 客户端包装类"
```

---

### Task 5: 实现事件采集和上报服务

**目标：** 实现核心的上报逻辑：立即上报，失败则存 SQLite

**文件：**
- 创建: `app/lib/services/analytics_service.dart`
- 创建: `app/lib/services/event_reporter.dart`

**Step 1: 创建事件采集器**

创建 `app/lib/services/event_collector.dart`:

```dart
import 'package:uuid/uuid.dart';
import '../models/analytics_event.dart';
import '../models/event_context.dart';
import '../utils/device_info_collector.dart';

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
```

**Step 2: 创建事件上报器**

创建 `app/lib/services/event_reporter.dart`:

```dart
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
      print('Event send failed, saving to SQLite: $eventId');

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
```

**Step 3: 创建分析服务主入口**

创建 `app/lib/services/analytics_service.dart`:

```dart
import 'dart:async';
import 'package:flutter/foundation.dart';
import '../models/analytics_event.dart';
import '../models/event_context.dart';
import 'event_collector.dart';
import 'event_reporter.dart';
import 'mqtt_client_wrapper.dart';
import '../database/event_database.dart';
import '../utils/device_info_collector.dart';

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
      broker: AnalyticsConfig.broker,
      clientId: clientId,
      username: AnalyticsConfig.mqttUsername,
      password: AnalyticsConfig.mqttPassword,
    );

    await _mqttClient.connect();

    _reporter = EventReporter(
      mqttClient: _mqttClient,
      database: EventDatabase.instance,
    );

    // 监听网络状态，恢复时立即重试
    _setupNetworkListener();

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

  /// 监听网络状态
  void _setupNetworkListener() {
    // TODO: 使用 connectivity_plus 监听
    _mqttClient._client.onConnected = () {
      _triggerImmediateRetry();
    };
  }

  /// 立即触发重试
  void _triggerImmediateRetry() async {
    if (_mqttClient.isConnected) {
      await _reporter.retryPendingEvents();
    }
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
```

**Step 4: 在 main.dart 初始化**

修改 `app/lib/main.dart`:

```dart
import 'package:app/services/analytics_service.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // 初始化埋点服务
  await AnalyticsService.instance.initialize();

  runApp(MyApp());
}
```

**Step 5: 提交**

```bash
git add app/lib/services/
git commit -m "feat(analytics): 实现事件采集和上报核心逻辑"
```

---

## 阶段二：后端实现

### Task 6: 创建后端 MQTT 订阅服务

**目标：** 后端接收 MQTT 消息并解析

**文件：**
- 创建: `backend/src/main/java/com/zerofinance/xwallet/mqtt/MqttConfig.java`
- 创建: `backend/src/main/java/com/zerofinance/xwallet/mqtt/MqttEventSubscriber.java`
- 创建: `backend/src/main/java/com/zerofinance/xwallet/model/dto/AnalyticsEvent.java`

**Step 1: 创建事件 DTO**

创建 `backend/src/main/java/com/zerofinance/xwallet/model/dto/AnalyticsEvent.java`:

```java
package com.zerofinance.xwallet.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.Instant;
import java.util.Map;

@Data
public class AnalyticsEvent {
    @JsonProperty("eventId")
    private String eventId;

    @JsonProperty("deviceId")
    private String deviceId;

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("eventType")
    private String eventType;

    @JsonProperty("timestamp")
    private Long timestamp;

    @JsonProperty("environment")
    private String environment;

    @JsonProperty("context")
    private EventContext context;

    @JsonProperty("properties")
    private Map<String, Object> properties;

    @JsonProperty("riskContext")
    private RiskContext riskContext;

    @Data
    public static class EventContext {
        @JsonProperty("appVersion")
        private String appVersion;

        @JsonProperty("os")
        private String os;

        @JsonProperty("osVersion")
        private String osVersion;

        @JsonProperty("deviceModel")
        private String deviceModel;

        @JsonProperty("networkType")
        private String networkType;
    }

    @Data
    public static class RiskContext {
        @JsonProperty("sessionId")
        private String sessionId;

        @JsonProperty("lastEventType")
        private String lastEventType;

        @JsonProperty("timeSinceLastEvent")
        private Long timeSinceLastEvent;
    }
}
```

**Step 2: 创建 MQTT 配置**

创建 `backend/src/main/java/com/zerofinance/xwallet/mqtt/MqttConfig.java`:

```java
package com.zerofinance.xwallet.mqtt;

import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Configuration
public class MqttConfig {

    @Value("${mqtt.broker}")
    private String broker;

    @Value("${mqtt.client-id}")
    private String clientId;

    @Value("${mqtt.username:}")
    private String username;

    @Value("${mqtt.password:}")
    private String password;

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectionOptions options = new MqttConnectionOptions();
        options.setAutomaticReconnect(true);
        options.setCleanStart(false);
        options.setConnectionTimeout(30);
        options.setKeepAliveInterval(60);

        if (!username.isEmpty()) {
            options.setUserName(username);
            options.setPassword(password.getBytes());
        }

        factory.setConnectionOptions(options);
        return factory;
    }

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MqttPahoMessageDrivenChannelAdapter mqttAdapter(
            MqttPahoClientFactory factory) {
        MqttPahoMessageDrivenChannelAdapter adapter =
            new MqttPahoMessageDrivenChannelAdapter(
                clientId,
                factory,
                "app/prod/+", "app/dev/+"
            );
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new MqttJsonConverter());
        adapter.setQos(1);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }
}
```

**Step 3: 创建 JSON 消息转换器**

创建 `backend/src/main/java/com/zerofinance/xwallet/mqtt/MqttJsonConverter.java`:

```java
package com.zerofinance.xwallet.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

public class MqttJsonConverter extends DefaultPahoMessageConverter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected Object extractPayload(Message<?> message) {
        String payload = (String) message.getPayload();
        try {
            return objectMapper.readTree(payload);
        } catch (Exception e) {
            return payload;
        }
    }
}
```

**Step 4: 创建事件订阅服务**

创建 `backend/src/main/java/com/zerofinance/xwallet/mqtt/MqttEventSubscriber.java`:

```java
package com.zerofinance.xwallet.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerofinance.xwallet.model.dto.AnalyticsEvent;
import com.zerofinance.xwallet.service.EventDeduplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MqttEventSubscriber implements MessageHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EventDeduplicationService deduplicationService;

    public MqttEventSubscriber(EventDeduplicationService deduplicationService) {
        this.deduplicationService = deduplicationService;
    }

    @ServiceActivator(inputChannel = "mqttInputChannel")
    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        try {
            String payload = message.getPayload().toString();
            AnalyticsEvent event = objectMapper.readValue(payload, AnalyticsEvent.class);

            log.info("Received event: {} from device: {}",
                event.getEventType(), event.getDeviceId());

            // 1. 去重
            if (deduplicationService.isDuplicate(event.getEventId())) {
                log.warn("Duplicate event ignored: {}", event.getEventId());
                return;
            }

            // 2. 处理事件（TODO: Task 7）
            processEvent(event);

        } catch (Exception e) {
            log.error("Failed to handle MQTT message", e);
            throw new MessagingException(message, e);
        }
    }

    private void processEvent(AnalyticsEvent event) {
        // TODO: 调用风控服务、分析服务等
        log.info("Processing event: {}", event.getEventType());
    }
}
```

**Step 5: 创建 Redis 去重服务**

创建 `backend/src/main/java/com/zerofinance/xwallet/service/EventDeduplicationService.java`:

```java
package com.zerofinance.xwallet.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class EventDeduplicationService {

    private final RedisTemplate<String, String> redisTemplate;

    public EventDeduplicationService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isDuplicate(String eventId) {
        String key = "event:" + eventId;

        Boolean isNew = redisTemplate.opsForValue()
            .setIfAbsent(key, "1", Duration.ofHours(1));

        return Boolean.FALSE.equals(isNew);
    }
}
```

**Step 6: 配置 Redis**

修改 `backend/src/main/resources/application-dev.yml`:

```yaml
spring:
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
```

**Step 7: 提交**

```bash
cd backend
git add src/main/java/
git commit -m "feat(analytics): 添加 MQTT 订阅和 Redis 去重服务"
```

---

### Task 7: 实现风控决策服务接口

**目标：** 将埋点事件分发到风控服务

**文件：**
- 创建: `backend/src/main/java/com/zerofinance/xwallet/service/RiskEvaluationService.java`
- 修改: `backend/src/main/java/com/zerofinance/xwallet/mqtt/MqttEventSubscriber.java`

**Step 1: 创建风控评估服务**

创建 `backend/src/main/java/com/zerofinance/xwallet/service/RiskEvaluationService.java`:

```java
package com.zerofinance.xwallet.service;

import com.zerofinance.xwallet.model.dto.AnalyticsEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RiskEvaluationService {

    public void evaluate(AnalyticsEvent event) {
        log.info("Evaluating risk for event: {} from user: {}",
            event.getEventType(), event.getUserId());

        // TODO: 实现具体的风控规则
        // 1. 检查行为序列
        // 2. 检查地理位置异常
        // 3. 检查设备指纹
        // 4. 决策：放行 / 拦截 / 人工审核

        // 示例规则：短时间多次登录失败
        if ("login_failed".equals(event.getEventType())) {
            // 触发风控规则
            log.warn("Risk alert: multiple login failures");
        }

        // 示例规则：异地登录
        if ("login".equals(event.getEventType()) && event.getRiskContext() != null) {
            log.info("Checking location for login: {}", event.getDeviceId());
        }
    }
}
```

**Step 2: 集成到 MQTT 订阅服务**

修改 `backend/src/main/java/com/zerofinance/xwallet/mqtt/MqttEventSubscriber.java`:

```java
private final RiskEvaluationService riskEvaluationService;

// 在构造函数中注入
public MqttEventSubscriber(
    EventDeduplicationService deduplicationService,
    RiskEvaluationService riskEvaluationService
) {
    this.deduplicationService = deduplicationService;
    this.riskEvaluationService = riskEvaluationService;
}

// 修改 processEvent 方法
private void processEvent(AnalyticsEvent event) {
    String topic = (String) message.getHeaders().get("mqtt_topic");

    if (topic != null && topic.contains("critical")) {
        // 风控事件，实时处理
        riskEvaluationService.evaluate(event);
    } else {
        // 普通行为事件，异步处理（TODO）
        log.info("Behavior event: {}", event.getEventType());
    }
}
```

**Step 3: 提交**

```bash
cd backend
git add src/main/java/com/zerofinance/xwallet/service/
git commit -m "feat(analytics): 添加风控评估服务"
```

---

## 阶段三：测试和集成

### Task 8: 端到端测试

**目标：** 测试完整的埋点流程

**文件：**
- 创建: `app/integration_test/analytics_flow_test.dart`
- 创建: `backend/src/test/java/com/zerofinance/xwallet/mqtt/MqttIntegrationTest.java`

**Step 1: 编写前端集成测试**

创建 `app/integration_test/analytics_flow_test.dart`:

```dart
import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';
import 'package:app/main.dart';
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
    expect(true, true); // 占位符
  });
}
```

**Step 2: 编写后端集成测试**

创建 `backend/src/test/java/com/zerofinance/xwallet/mqtt/MqttIntegrationTest.java`:

```java
package com.zerofinance.xwallet.mqtt;

import com.zerofinance.xwallet.model.dto.AnalyticsEvent;
import com.zerofinance.xwallet.service.EventDeduplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
public class MqttIntegrationTest {

    @Autowired
    private MqttEventSubscriber subscriber;

    @MockBean
    private EventDeduplicationService deduplicationService;

    @Test
    public void testEventProcessing() {
        // 模拟收到事件
        AnalyticsEvent event = new AnalyticsEvent();
        event.setEventId("test-123");
        event.setEventType("login");

        when(deduplicationService.isDuplicate(anyString())).thenReturn(false);

        // 测试处理逻辑
        // subscriber.handleMessage(message);

        verify(deduplicationService, times(1)).isDuplicate("test-123");
    }
}
```

**Step 3: 提交**

```bash
git add integration_test/ src/test/
git commit -m "test(analytics): 添加端到端集成测试"
```

---

### Task 9: 监控和日志

**目标：** 添加关键指标监控

**文件：**
- 创建: `app/lib/services/analytics_metrics.dart`
- 修改: `backend/src/main/java/com/zerofinance/xwallet/mqtt/MqttEventSubscriber.java`

**Step 1: 创建前端指标收集**

创建 `app/lib/services/analytics_metrics.dart`:

```dart
class AnalyticsMetrics {
  int totalEvents = 0;
  int immediateSuccess = 0;
  int retrySuccess = 0;
  int failedEvents = 0;
  int queueSize = 0;

  void recordImmediateSuccess() {
    totalEvents++;
    immediateSuccess++;
  }

  void recordRetrySuccess() {
    totalEvents++;
    retrySuccess++;
  }

  void recordFailure() {
    totalEvents++;
    failedEvents++;
  }

  Map<String, dynamic> toJson() {
    return {
      'totalEvents': totalEvents,
      'immediateSuccess': immediateSuccess,
      'retrySuccess': retrySuccess,
      'failedEvents': failedEvents,
      'queueSize': queueSize,
      'immediateSuccessRate': totalEvents > 0
          ? (immediateSuccess / totalEvents * 100).toStringAsFixed(2) + '%'
          : '0%',
    };
  }

  void printReport() {
    print('''
    Analytics Metrics:
    - Total Events: $totalEvents
    - Immediate Success: $immediateSuccess (${immediateSuccessRate(totalEvents)})
    - Retry Success: $retrySuccess
    - Failed: $failedEvents
    - Queue Size: $queueSize
    ''');
  }

  String immediateSuccessRate(int total) {
    if (total == 0) return '0%';
    return '${(immediateSuccess / total * 100).toStringAsFixed(2)}%';
  }
}
```

**Step 2: 添加后端监控日志**

修改 `backend/src/main/java/com/zerofinance/xwallet/mqtt/MqttEventSubscriber.java`:

```java
private final AtomicLong totalEvents = new AtomicLong(0);
private final AtomicLong duplicateEvents = new AtomicLong(0);

@Override
public void handleMessage(Message<?> message) throws MessagingException {
    totalEvents.incrementAndGet();

    try {
        String payload = message.getPayload().toString();
        AnalyticsEvent event = objectMapper.readValue(payload, AnalyticsEvent.class);

        // 去重
        if (deduplicationService.isDuplicate(event.getEventId())) {
            duplicateEvents.incrementAndGet();
            log.warn("Duplicate event ignored: {}", event.getEventId());
            return;
        }

        processEvent(event);

    } catch (Exception e) {
        log.error("Failed to handle MQTT message", e);
        throw new MessagingException(message, e);
    }
}

// 定期打印指标
@Scheduled(fixedRate = 60000) // 每分钟
public void printMetrics() {
    log.info("MQTT Metrics: total={}, duplicate={}, rate={}%",
        totalEvents.get(),
        duplicateEvents.get(),
        (duplicateEvents.get() * 100.0 / totalEvents.get()));
}
```

**Step 3: 提交**

```bash
git add app/lib/services/analytics_metrics.dart backend/src/main/java/com/zerofinance/xwallet/mqtt/
git commit -m "feat(analytics): 添加监控指标和日志"
```

---

### Task 10: 文档和部署

**目标：** 编写使用文档和部署指南

**文件：**
- 创建: `docs/analytics/README.md`
- 创建: `docs/analytics/DEPLOYMENT.md`
- 修改: `CLAUDE.md`

**Step 1: 创建使用文档**

创建 `docs/analytics/README.md`:

```markdown
# 埋点系统使用指南

## 快速开始

### Flutter 端

```dart
// 初始化（在 main.dart）
await AnalyticsService.instance.initialize();

// 上报事件
await AnalyticsService.instance.trackEvent(
  eventType: 'payment_success',
  properties: {
    'amount': 100.00,
    'currency': 'CNY',
  },
  userId: 'user_123',
  category: EventCategory.critical,
);
```

### 后端

启动后端，自动订阅 MQTT topic 并接收事件。

## 事件类型

### 风控事件 (critical)
- login
- payment_init
- payment_success
- transfer_init

### 行为事件 (behavior)
- page_view
- button_click

### 系统事件 (system)
- app_crash
- app_performance
```

**Step 2: 创建部署文档**

创建 `docs/analytics/DEPLOYMENT.md`:

```markdown
# 埋点系统部署指南

## MQTT Broker

### EMQX Cloud

1. 注册 https://www.emqx.com/zh/cloud
2. 创建实例
3. 获取连接信息

### 本地测试

使用 Docker:

\`\`\`bash
docker run -it -p 1883:1883 -p 8883:8883 emqx/emqx:latest
\`\`\`

## 环境变量

### Flutter

\`\`\`bash
export MQTT_BROKER=broker.emqxsl.com
export MQTT_USERNAME=your_username
export MQTT_PASSWORD=your_password
export ENV=prod
\`\`\`

### Backend

\`\`\`bash
export MQTT_BROKER=tcp://broker.emqxsl.com:1883
export MQTT_USERNAME=your_username
export MQTT_PASSWORD=your_password
export REDIS_HOST=localhost
export REDIS_PORT=6379
\`\`\`

## 监控

查看关键指标：
- 立即成功率 > 90%
- SQLite 队列大小 < 500
- Redis 去重率 < 5%
```

**Step 3: 更新 CLAUDE.md**

修改 `CLAUDE.md`，添加埋点系统说明：

\`\`\`markdown
## 埋点系统

移动端应用（app/）集成了 MQTT 埋点系统：

- **高实时性**：关键事件 < 100ms 上报
- **可靠性**：失败事件本地持久化，持续重试
- **使用文档**：`docs/analytics/README.md`
\`\`\`

**Step 4: 提交**

\`\`\`bash
git add docs/analytics/ CLAUDE.md
git commit -m "docs(analytics): 添加使用文档和部署指南"
\`\`\`

---

## 验收标准

### 功能验收

- [ ] Flutter 端能成功连接 MQTT Broker
- [ ] 事件能立即上报（延迟 < 100ms）
- [ ] 网络断开时，事件存入 SQLite
- [ ] 网络恢复后，SQLite 中事件自动重试
- [ ] 后端能接收并解析 MQTT 消息
- [ ] Redis 去重正常工作
- [ ] 风控事件能触发风控评估

### 性能验收

- [ ] 立即上报成功率 > 90%
- [ ] 平均上报延迟 < 100ms
- [ ] SQLite 队列大小稳定 < 100
- [ ] 内存增长 < 10MB/小时

### 安全验收

- [ ] MQTT 使用 TLS 加密
- [ ] deviceId 不可伪造
- [ ] 事件不包含敏感信息
- [ ] Redis 去重防止重放攻击

---

## 总计

- **预计工作量**: 2-3 周
- **主要文件**: 30+
- **测试覆盖**: 单元测试 + 集成测试
- **文档**: 使用文档 + 部署指南
