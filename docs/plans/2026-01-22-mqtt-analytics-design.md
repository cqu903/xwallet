# 埋点系统技术设计文档

**日期**: 2026-01-22
**作者**: Claude Code
**状态**: 设计阶段

## 1. 需求概述

为移动端应用（app/）添加高实时性的用户行为埋点系统，支持：

- **高实时性**：关键事件 < 100ms 上报到后端，支持风控决策
- **弱网稳定**：网络抖动、切换、暂时不可用不影响上报
- **可靠送达**：失败事件本地持久化，持续重试，容量上限时 LRU 淘汰
- **不阻塞用户**：上报失败不影响用户正常操作

**应用场景**：后端风控决策（如支付、登录、转账等关键操作的实时监控）

---

## 2. 技术方案总览

### 2.1 核心技术选型

| 组件 | 技术选型 | 理由 |
|------|---------|------|
| **传输协议** | MQTT QoS 1 | 高实时（< 10ms）、弱网稳定、天然重试 |
| **MQTT Broker** | EMQX Cloud / Mosquitto | 成熟稳定，支持百万级连接 |
| **本地存储** | SQLite | 离线事件持久化，支持恢复 |
| **去重方案** | Redis + eventId 前后端双重去重 | 防止 QoS 1 重复送达 |

### 2.2 架构对比：MQTT vs HTTP

| 维度 | HTTP | MQTT | 选择 |
|------|------|------|------|
| 延迟 | 50-200ms | < 10ms | ✅ MQTT |
| 弱网表现 | 易受影响 | 自动重连 | ✅ MQTT |
| 可靠性 | 需自己实现 | QoS 1 天然支持 | ✅ MQTT |
| 流量/电量 | 较高 | 更优 | ✅ MQTT |
| 实施复杂度 | 低 | 中等 | - |

**结论**：风控场景对实时性要求极高，选择 **MQTT QoS 1**

---

## 3. Topic 设计

### 3.1 Topic 结构

```
app/{environment}/{eventCategory}
```

**固定的 Topic（仅 3-6 个）：**

```
app/prod/critical      # 所有设备的风控事件
app/prod/behavior      # 所有设备的普通行为
app/prod/system        # 系统级事件（崩溃、性能）
app/dev/critical       # 测试环境
```

**参数说明：**
- `environment`: `prod` / `dev` / `test`
- `eventCategory`: `critical` / `behavior` / `system`

**为什么不在 Topic 里放 deviceId？**
- 避免百万级 Topic，减轻 Broker 压力
- deviceId 放在 payload 中区分

---

## 4. 事件格式定义

### 4.1 基础数据结构

```json
{
  // === 通用字段（所有事件必填）===
  "eventId": "uuid-v4",
  "deviceId": "abc-123-def",
  "userId": "12345",
  "eventType": "payment_success",
  "timestamp": 1705210800000,
  "environment": "prod",

  // === 上下文信息（自动采集）===
  "context": {
    "appVersion": "1.0.0",
    "os": "iOS",
    "osVersion": "17.2",
    "deviceModel": "iPhone 15 Pro",
    "networkType": "wifi",
    "carrier": "China Mobile",
    "screenSize": "393x852",
    "timezone": "Asia/Shanghai",
    "language": "zh-CN"
  },

  // === 事件特定属性 ===
  "properties": {
    // 业务自定义字段
  },

  // === 风控专用字段 ===
  "riskContext": {
    "ipAddress": "192.168.1.1",
    "location": {
      "country": "CN",
      "province": "Beijing",
      "city": "Beijing"
    },
    "sessionId": "sess-123",
    "lastEventType": "login",
    "timeSinceLastEvent": 5000
  }
}
```

### 4.2 事件类型示例

**风控关键事件：**

```json
// 登录
{
  "eventType": "login",
  "properties": {
    "loginMethod": "email",
    "success": true,
    "failureReason": null
  }
}

// 支付
{
  "eventType": "payment_init",
  "properties": {
    "amount": 100.00,
    "currency": "CNY",
    "paymentMethod": "alipay"
  }
}
```

**普通行为事件：**

```json
// 页面浏览
{
  "eventType": "page_view",
  "properties": {
    "pageName": "WalletScreen",
    "referrer": "DashboardScreen"
  }
}
```

**系统事件：**

```json
// 崩溃
{
  "eventType": "app_crash",
  "properties": {
    "errorMessage": "NullPointerException",
    "stackTrace": "...",
    "isFatal": true
  }
}
```

---

## 5. 前端架构设计

### 5.1 核心原则

```
事件发生 → 立即上报 MQTT → 成功则结束 → 失败才存 SQLite
```

**SQLite 不是正常流程，而是故障兜底**

### 5.2 数据流转

```
[用户操作]
    ↓
EventCollector.collectEvent()
    ├─ 生成 eventId (UUID v4)
    ├─ 采集 context（设备、网络）
    └─ 构建 payload
    ↓
EventReporter.reportEvent(topic, payload)
    ↓
┌────────────────┐
│ 立即上报 MQTT  │
│ (QoS 1)        │
└───────┬────────┘
        │
   ┌────┴────┐
   │         │
成功│         │失败
   │         ↓
结束   ┌─────────────┐
       │ 存入 SQLite │
       │ status=     │
       │ pending     │
       └──────┬──────┘
              │
       ┌──────┴──────────────────────┐
       │  定时任务 / 网络恢复触发     │
       │  (每 10 秒 或 MQTT 重连)    │
       └──────┬──────────────────────┘
              ↓
       ┌─────────────┐
       │ 批量重试     │
       │ SQLite →    │
       │ MQTT        │
       └──────┬──────┘
              │
          ┌───┴───┐
          │       │
       成功│       │失败（指数退避）
          │       ↓
      删除记录 更新 next_retry_at
```

### 5.3 SQLite 表结构

**表 1: events（事件主表）**

```sql
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
);
```

**表 2: config（配置表）**

```sql
CREATE TABLE config (
    key TEXT PRIMARY KEY,
    value TEXT NOT NULL
);

INSERT INTO config (key, value) VALUES
    ('max_events', '1000'),
    ('batch_size', '100'),
    ('flush_interval', '5000');
```

### 5.4 重试策略

**指数退避算法：**

```dart
int calculateNextRetry(int retryCount) {
  int delaySeconds = pow(2, retryCount).toInt();
  delaySeconds = min(delaySeconds, 60);
  return DateTime.now().millisecondsSinceEpoch + (delaySeconds * 1000);
}
```

| 重试次数 | 等待时间 | 累计时间 |
|---------|---------|---------|
| 1 | 2 秒 | 2 秒 |
| 2 | 4 秒 | 6 秒 |
| 3 | 8 秒 | 14 秒 |
| 4 | 16 秒 | 30 秒 |
| 5 | 32 秒 | 62 秒 |
| 6+ | 60 秒 | 持续重试 |

**最大重试次数：无限 + 容量上限（1000 条，LRU 淘汰）**

---

## 6. 后端架构设计

### 6.1 MQTT 订阅架构

```
MQTT Broker (EMQX)
    ↓
Spring Boot Backend
    ├── 风控服务 (订阅 app/prod/critical)
    ├── 分析服务 (订阅 app/prod/behavior)
    └── 系统监控 (订阅 app/prod/system)
```

### 6.2 去重策略

**混合方案：前端 + 后端双重去重**

#### **前端去重（防止重复入队）**

```dart
class EventQueue {
  final Set<String> _pendingEvents = {};

  void publishEvent(String topic, Map payload) {
    String eventId = payload['eventId'];

    if (_pendingEvents.contains(eventId)) {
      return;
    }

    _pendingEvents.add(eventId);
    mqttClient.publish(topic, payload, qos: 1);
  }

  void onAckReceived(String eventId) {
    _pendingEvents.remove(eventId);
  }
}
```

#### **后端去重（Redis + eventId）**

```java
@Service
public class EventDeduplicationService {

    private final RedisTemplate<String, String> redisTemplate;

    public boolean isDuplicate(String eventId) {
        String key = "event:" + eventId;

        Boolean isNew = redisTemplate.opsForValue()
            .setIfAbsent(key, "1", Duration.ofHours(1));

        return Boolean.FALSE.equals(isNew);
    }

    @Subscribe(topic = "app/prod/+", qos = 1)
    public void handleEvent(String payload) {
        Event event = JsonParser.parse(payload);

        if (isDuplicate(event.getEventId())) {
            log.warn("Duplicate event ignored: {}", event.getEventId());
            return;
        }

        processEvent(event);
    }
}
```

### 6.3 Spring Boot MQTT 集成

```java
@Service
public class MqttEventSubscriber {

    @Subscribe(topic = "app/prod/critical", qos = 1)
    public void handleCriticalEvent(String payload) {
        Event event = JsonParser.parse(payload);

        // 1. 去重
        if (deduplicationService.isDuplicate(event.getEventId())) {
            return;
        }

        // 2. 补充后端信息（IP、地理位置）
        enrichEventContext(event);

        // 3. 风控决策
        riskService.evaluate(event);
    }
}
```

---

## 7. QoS 策略

| 事件类别 | QoS 级别 | 说明 |
|---------|---------|------|
| **critical** | QoS 1 | 至少送达一次，风控事件绝对不能丢 |
| **behavior** | QoS 1 | 至少送达一次，数据分析需要完整性 |
| **system** | QoS 0 | 最多送达一次，崩溃日志允许丢 |

---

## 8. 设备标识

### 8.1 deviceId 获取

```dart
import 'package:device_info_plus/device_info_plus.dart';

Future<String> getDeviceId() async {
  final deviceInfo = DeviceInfoPlugin();

  if (Platform.isIOS) {
    IosDeviceInfo iosInfo = await deviceInfo.iosInfo;
    return iosInfo.identifierForVendor; // UUID
  } else {
    AndroidDeviceInfo androidInfo = await deviceInfo.androidInfo;
    return androidInfo.id; // Android ID
  }
}
```

### 8.2 持久化方案

**iOS UUID 问题：** 卸载重装后 `identifierForVendor` 会变化

**解决方案：**
- 使用 Keychain 存储（卸载后保留）
- 或接受变化（风控通过其他特征识别）

---

## 9. 网络状态感知

```dart
class NetworkAwareReporter {
  bool _isConnected = false;

  void init() {
    // 监听网络变化
    Connectivity().onConnectivityChanged.listen((result) {
      _isConnected = result != ConnectivityResult.none;

      if (_isConnected) {
        _triggerImmediateRetry(); // 立即重试
      }
    });

    // 监听 MQTT 连接状态
    mqttClient.onConnected = () {
      _triggerImmediateRetry();
    };
  }

  void _triggerImmediateRetry() async {
    await retryPendingEvents();
  }
}
```

---

## 10. 性能指标

| 指标 | 目标值 | 说明 |
|------|--------|------|
| **上报延迟** | < 100ms | 前台立即上报 |
| **重试延迟** | 2-60 秒 | 指数退避 |
| **去重延迟** | < 1ms | Redis 内存操作 |
| **风控决策** | < 500ms | 端到端（含网络传输） |
| **本地存储** | 最大 1000 条 | 容量上限，LRU 淘汰 |

---

## 11. 安全考虑

| 风险 | 防护措施 |
|------|----------|
| **MQTT 传输** | TLS 1.3 加密 |
| **设备伪造** | deviceId + 设备指纹双重验证 |
| **重放攻击** | eventId 去重 + timestamp 校验（±5 分钟） |
| **敏感信息** | payload 不包含密码、token 等敏感数据 |
| **DDoS 攻击** | Broker 限流（每设备 100 msg/s） |

---

## 12. 监控和告警

### 12.1 关键指标

```dart
class EventReporterMetrics {
  int totalEvents = 0;       // 总事件数
  int immediateSuccess = 0;  // 立即成功
  int retrySuccess = 0;      // 重试成功
  int failedEvents = 0;      // 失败事件
  int queueSize = 0;         // SQLite 队列大小
  double avgLatency = 0;     // 平均上报延迟
}
```

### 12.2 告警规则

| 指标 | 阈值 | 告警级别 |
|------|------|----------|
| 立即成功率 | < 90% | Warning |
| SQLite 队列大小 | > 500 | Warning |
| SQLite 队列大小 | > 900 | Critical |
| 平均上报延迟 | > 500ms | Warning |

---

## 13. 技术依赖

### 13.1 Flutter 端

```yaml
dependencies:
  mqtt_client: ^10.0.0        # MQTT 客户端
  sqflite: ^2.3.0             # 本地数据库
  device_info_plus: ^9.0.0    # 设备信息
  connectivity_plus: ^5.0.0   # 网络状态
  uuid: ^4.0.0                # UUID 生成
  json_annotation: ^4.8.0     # JSON 序列化
```

### 13.2 后端

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

---

## 14. MQTT Broker 选型

| 方案 | 说明 | 成本 |
|------|------|------|
| **EMQX Cloud** | 推荐，托管服务 | ¥200/月（百万级连接） |
| **Mosquitto** | 自建，轻量级 | 服务器成本 |
| **AWS IoT Core** | 云原生，按量付费 | 按连接数和消息量 |

**推荐**：初期使用 EMQX Cloud，降低运维成本

---

## 15. 实施阶段规划

### 阶段一：基础设施（1-2 天）
- MQTT Broker 部署（EMQX Cloud）
- 后端 MQTT 集成
- Redis 去重服务

### 阶段二：前端开发（3-5 天）
- EventCollector（事件采集）
- EventReporter（上报逻辑）
- SQLite 持久化
- 重试机制

### 阶段三：后端开发（2-3 天）
- MQTT 订阅服务
- 风控规则引擎接入
- 监控指标采集

### 阶段四：测试验证（2-3 天）
- 单元测试
- 弱网测试
- 压力测试

### 阶段五：灰度发布（1 周）
- 10% 用户
- 50% 用户
- 全量

**总计：2-3 周**

---

## 16. 风险和挑战

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| **MQTT Broker 故障** | 所有上报失败 | 多 Broker 集群部署 |
| **设备存储不足** | SQLite 写入失败 | 容量监控 + 降级策略 |
| **deviceId 伪造** | 风控误判 | 设备指纹验证 |
| **消息积压** | 后端处理延迟 | 异步队列 + 水位告警 |

---

## 17. 后续优化方向

1. **端到端加密**：敏感事件 payload 加密
2. **边缘计算**：部分风控规则下沉到 MQTT Broker（EMQX 规则引擎）
3. **数据压缩**：大批量事件启用 gzip 压缩
4. **A/B 测试**：基于埋点数据的动态实验平台
