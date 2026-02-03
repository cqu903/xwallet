# 📱 Flutter App MQTT 埋点测试指南（修复版）

## ✅ 已修复的问题

### 1. 网络配置问题
- ✅ **Android 模拟器**：自动使用 `10.0.2.2` 访问宿主机服务
- ✅ **iOS 模拟器**：自动使用 `localhost`
- ✅ 创建了 `PlatformConfig` 工具类自动检测平台

### 2. MQTT 配置
- ✅ 修复端口配置（使用 1883 非 TLS 端口）
- ✅ 添加连接状态日志
- ✅ 连接失败时自动降级到 SQLite 存储

## 🚀 快速开始

### 1. 启动服务（确保运行中）

```bash
# 检查后端
curl http://localhost:8080/api/test/password

# 检查 MQTT Broker
docker ps | grep emqx

# 检查 Redis
docker ps | grep redis
```

### 2. 运行 Flutter App

```bash
cd /Users/royyuan/Downloads/codes/xwallet/app

# Android 模拟器
flutter run -d android

# iOS 模拟器
flutter run -d ios
```

### 3. 首次启动查看日志

**成功的日志应该是：**
```
✅ MQTT connected to 10.0.2.2:1883  (Android)
或
✅ MQTT connected to localhost:1883       (iOS)
```

**如果连接失败：**
```
⚠️  MQTT connection failed: ...
📦 Events will be saved to SQLite for retry
```
→ 这是正常的，事件会保存到本地 SQLite，等 MQTT 连接后自动重试

## 🧪 测试功能

### 测试账号
- **邮箱**: `customer@example.com`
- **密码**: `customer123`

### 触发事件的操作

| 操作 | 事件类型 | 说明 |
|-----|---------|------|
| 登录 | `login` | 关键事件，登录成功/失败 |
| 进入主页 | `page_view` | 页面浏览 |
| 点击"申请贷款" | `button_click` | 按钮点击 |
| 点击活动卡片 | `activity_click` | 活动点击 |

## 🔍 实时监控

### 查看后端日志
```bash
tail -f /tmp/backend.log | grep -i "received event"
```

### 查询数据库最新事件
```bash
docker exec mysql-dev mysql -u root -p123321qQ -e "
SELECT id, event_type, user_id, is_critical,
       DATE_FORMAT(created_at, '%H:%i:%s') as time
FROM xwallet.analytics_event
ORDER BY id DESC LIMIT 5;
" 2>&1 | grep -v Warning
```

### 查看完整事件详情
```bash
docker exec mysql-dev mysql-dev mysql -u root -p123321qQ -e "
SELECT event_type, JSON_PRETTY(payload) as payload
FROM xwallet.analytics_event
ORDER BY id DESC LIMIT 1\G
" 2>&1 | grep -v Warning
```

## 📊 前端管理页面

访问：`http://localhost:3000/system/mqtt-events`

**筛选示例：**
1. 环境：选择 `dev`
2. 事件类型：输入 `login`
3. 点击"搜索"

**查看详情：**
- 点击任意行的"详情"按钮
- 查看完整的 JSON Payload

## ⚠️ 常见问题

### Q1: App 无法连接后端 API
**症状**: `Connection refused` 错误

**解决方案**:
```bash
# 检查后端是否运行
curl http://localhost:8080/api/test/password

# 如果没运行，启动后端
cd /Users/royyuan/Downloads/codes/xwallet/backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Q2: MQTT 连接失败
**症状**: `⚠️ MQTT connection failed`

**这是正常的！**
- 事件会自动保存到 SQLite
- MQTT 重连后自动重试
- 不影响 App 正常使用

**手动检查 MQTT:**
```bash
# 检查 EMQX 是否运行
docker ps | grep emqx

# 检查端口
nc -zv localhost 1883
```

### Q3: 数据库没有事件
**检查步骤:**
1. 确认后端日志有 `Received event` 消息
2. 确认 Redis 正在运行（去重需要）
3. 查看后端日志是否有 SQL 错误

```bash
# 检查 Redis
docker ps | grep redis

# 重启 Redis（如果需要）
docker restart xwallet-redis
```

### Q4: iOS 模拟器网络问题
如果 iOS 模拟器无法连接，尝试：

```bash
# 重启 iOS 模拟器
flutter run -d ios

# 或在 iOS 模拟器中：
Settings → Developer → Network → Disable "Network Link Conditioner"
```

## 🎯 测试清单

运行 App 后，检查以下内容：

- [ ] App 成功启动，无崩溃
- [ ] 控制台显示 `✅ MQTT connected` 或 `⚠️ MQTT connection failed`
- [ ] 使用 `customer@example.com` / `customer123` 登录
- [ ] 登录成功后能进入主页
- [ ] 后端日志显示 `Received event: login`
- [ ] 数据库新增 `login` 事件记录
- [ ] 主页加载时触发 `page_view` 事件
- [ ] 点击按钮时触发相应事件
- [ ] 前端管理页面显示所有事件

## 📝 事件数据结构示例

**登录事件（成功）:**
```json
{
  "eventId": "uuid-v4",
  "eventType": "login",
  "userId": "123",
  "properties": {
    "loginMethod": "email",
    "success": true
  },
  "context": {
    "appVersion": "1.0.0",
    "os": "Android",
    "osVersion": "13",
    "deviceModel": "Pixel 5",
    "networkType": "wifi"
  }
}
```

**页面浏览事件:**
```json
{
  "eventId": "uuid-v4",
  "eventType": "page_view",
  "properties": {
    "pageName": "HomeScreen"
  }
}
```

## 🔧 开发环境配置总结

| 平台 | API 地址 | MQTT Broker | 说明 |
|------|---------|-------------|------|
| **Android 模拟器** | `http://10.0.2.2:8080/api` | `10.0.2.2:1883` | 自动检测 |
| **iOS 模拟器** | `http://localhost:8080/api` | `localhost:1883` | 自动检测 |
| **真机** | 需要使用局域网 IP | 需要使用局域网 IP | 同一 WiFi 下 |

## 🎉 完成！

现在 App 会：
1. 自动检测平台并使用正确的网络配置
2. MQTT 连接成功时实时上报事件
3. MQTT 连接失败时降级到 SQLite 存储
4. 定时重试失败的事件
