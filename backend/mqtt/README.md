# EMQX MQTT 服务器使用指南

## 快速启动

### 1. 启动 EMQX 服务器

```bash
cd backend
docker-compose up -d
```

### 2. 验证服务状态

```bash
# 查看容器状态
docker-compose ps

# 查看日志
docker-compose logs -f emqx
```

### 3. 访问 Dashboard

打开浏览器访问：http://localhost:18083

- 用户名：`admin`
- 密码：`public`

## 端口说明

| 端口 | 用途 | 说明 |
|------|------|------|
| 1883 | MQTT TCP | 标准 MQTT 连接端口 |
| 8083 | MQTT WebSocket | WebSocket 连接端口 |
| 8084 | MQTT SSL/TLS | 加密连接端口 |
| 18083 | Dashboard | Web 管理界面 |

## 配置说明

### 开发环境配置

在 `application-dev.yml` 中已配置：

```yaml
mqtt:
  broker: tcp://localhost:1883
  client-id: xwallet-backend-dev-${random.value}
  username: ${MQTT_USERNAME:}  # 可选，开发环境允许匿名连接
  password: ${MQTT_PASSWORD:}  # 可选
```

### 生产环境配置

生产环境建议使用 `application-mqtt.yml` 配置，连接到 EMQX Cloud 或自建 EMQX 集群：

```yaml
mqtt:
  broker: tcp://${MQTT_BROKER_HOST:broker.emqxsl.com}:1883
  client-id: xwallet-backend-${random.value}
  username: ${MQTT_USERNAME:}
  password: ${MQTT_PASSWORD:}
```

## 测试连接

### 使用 MQTT 客户端测试

可以使用 MQTTX 或其他 MQTT 客户端工具测试连接：

1. 下载 MQTTX：https://mqttx.app/
2. 创建新连接：
   - Broker: `localhost`
   - Port: `1883`
   - Client ID: 任意
   - Username/Password: 留空（开发环境允许匿名）

### 测试发布消息

在 MQTTX 中订阅主题：`app/dev/+` 或 `app/prod/+`

然后发布一条测试消息到 `app/dev/test`：

```json
{
  "eventId": "test-001",
  "eventType": "test",
  "userId": "test-user",
  "deviceId": "test-device",
  "timestamp": 1234567890
}
```

## 常用命令

```bash
# 启动服务
docker-compose up -d

# 停止服务
docker-compose down

# 重启服务
docker-compose restart

# 查看日志
docker-compose logs -f emqx

# 进入容器
docker exec -it xwallet-emqx sh

# 清理数据（注意：会删除所有数据）
docker-compose down -v
```

## 安全建议

### 生产环境配置

1. **禁用匿名连接**：修改环境变量 `EMQX_ALLOW_ANONYMOUS=false`
2. **修改默认密码**：在 Dashboard 中修改 admin 密码
3. **配置 ACL**：设置主题访问控制列表
4. **启用 TLS**：使用 8084 端口进行加密连接
5. **使用认证插件**：配置数据库或 JWT 认证

### 启用认证示例

在 `docker-compose.yml` 中添加：

```yaml
environment:
  - EMQX_ALLOW_ANONYMOUS=false
  - EMQX_AUTHENTICATION__1__MECHANISM=password_based
  - EMQX_AUTHENTICATION__1__BACKEND=built_in_database
```

然后在 Dashboard 中创建用户和 ACL 规则。

## 故障排查

### 连接失败

1. 检查容器是否运行：`docker-compose ps`
2. 检查端口是否被占用：`lsof -i :1883`
3. 查看日志：`docker-compose logs emqx`

### 消息收不到

1. 检查主题订阅是否正确
2. 检查 QoS 级别是否匹配
3. 在 Dashboard 的 "监控" -> "消息" 中查看消息流

## 数据持久化

数据存储在 `mqtt/emqx/data` 和 `mqtt/emqx/log` 目录中，即使容器重启数据也会保留。

## 更多信息

- EMQX 官方文档：https://www.emqx.io/docs
- EMQX GitHub：https://github.com/emqx/emqx
