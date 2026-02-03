# App 配置说明

## 快速开始

### 1. 首次设置

```bash
cd app
cp assets/config/config.example.json assets/config/config.json
```

### 2. 修改配置

根据你的开发环境编辑 `assets/config/config.json`：

```json
{
  "apiBaseUrl": "http://10.0.2.2:8080/api",
  "mqttBroker": "10.0.2.2",
  "mqttPort": 1883,
  "mqttUseSSL": false,
  "mqttUsername": "",
  "mqttPassword": "",
  "environment": "dev"
}
```

### 3. 运行应用

```bash
flutter run
```

## 环境配置示例

### Android 模拟器

```json
{
  "apiBaseUrl": "http://10.0.2.2:8080/api",
  "mqttBroker": "10.0.2.2",
  "mqttPort": 1883
}
```

### iOS 模拟器

```json
{
  "apiBaseUrl": "http://localhost:8080/api",
  "mqttBroker": "localhost",
  "mqttPort": 1883
}
```

### 真机调试

确保手机和电脑在同一局域网，将 IP 改为你的电脑 IP：

```json
{
  "apiBaseUrl": "http://192.168.31.47:8080/api",
  "mqttBroker": "192.168.31.47",
  "mqttPort": 1883
}
```

### 远程服务器

```json
{
  "apiBaseUrl": "https://api.example.com/api",
  "mqttBroker": "mqtt.example.com",
  "mqttPort": 8883,
  "mqttUseSSL": true,
  "mqttUsername": "app_user",
  "mqttPassword": "secure_password",
  "environment": "production"
}
```

## 配置字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| `apiBaseUrl` | string | 后端 API 地址 |
| `mqttBroker` | string | MQTT 服务器地址 |
| `mqttPort` | number | MQTT 端口（通常 1883 或 8883） |
| `mqttUseSSL` | boolean | 是否使用 SSL/TLS |
| `mqttUsername` | string | MQTT 用户名（可选） |
| `mqttPassword` | string | MQTT 密码（可选） |
| `environment` | string | 环境标识（dev/staging/prod） |

## 注意事项

1. **不要提交 config.json 到 Git**
   - `config.json` 已加入 `.gitignore`
   - 只提交 `config.example.json` 模板

2. **配置文件修改后需要重启应用**
   - 修改配置后，重新运行 `flutter run`

3. **默认值**
   - 如果配置文件加载失败，会使用默认值（Android 模拟器配置）
   - 应用会在控制台打印配置加载状态

## 故障排查

### 问题：MQTT 连接失败

**检查清单：**
- 确认 MQTT Broker 地址正确
- 确认手机和电脑在同一网络（真机调试）
- 检查防火墙设置
- 确认 MQTT 服务正在运行

### 问题：API 请求失败

**检查清单：**
- 确认 `apiBaseUrl` 正确
- 确认后端服务正在运行
- 检查网络连接
- 查看控制台日志

## 团队协作

每个开发者维护自己的 `config.json`：

```bash
# 新成员首次设置
git clone <repo>
cd app
cp assets/config/config.example.json assets/config/config.json
# 根据自己的网络环境修改 config.json
flutter run
```

`config.json` 不会被提交到 Git，每个开发者的配置互不影响。
