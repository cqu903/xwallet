# 📱 iOS 真机 MQTT 埋点测试指南

## ✅ 配置完成

### 已自动配置的内容
1. **API 地址**: `http://192.168.31.47:8080/api` (你的 Mac 局域网 IP)
2. **MQTT Broker**: `192.168.31.47:1883`
3. **HTTP 允许**: iOS 已配置允许 HTTP 连接

## 🚀 运行测试

### 1. 重新编译并运行 App

```bash
cd /Users/royyuan/Downloads/codes/xwallet/app

# 确保设备连接
flutter devices

# 运行到 iOS 真机
flutter run
```

### 2. 首次启动需要信任开发者证书

**iOS 设备上：**
1. 打开 **设置** → **通用** → **VPN 与设备管理**
2. 找到你的开发者证书
3. 点击 **信任**

## 🔍 验证连接

### 检查 1：API 连接
**在 App 中登录时：**
- 邮箱：`1`
- 密码：`customer123`

**如果登录成功** ✅ 说明 API 连接正常

**如果登录失败** ❌ 检查：
1. 确保设备和 Mac 在**同一个 WiFi**
2. 在设备上打开 Safari 访问：`http://192.168.31.47:8080/api/test/password`
   - 如果能看到返回结果，说明后端可访问
   - 如果无法访问，检查防火墙设置

### 检查 2：MQTT 连接
**查看 Xcode 控制台日志：**
```
✅ MQTT connected to 192.168.31.47:1883
```

**如果显示连接失败：**
```
⚠️  MQTT connection failed
📦 Events will be saved to SQLite for retry
```
→ 这是正常的，事件会存到 SQLite，等后端可用时自动重试

## 📊 查看埋点事件

### 方式 1：查询数据库
```bash
docker exec mysql-dev mysql -u root -p123321qQ -e "
SELECT
  id,
  event_type,
  user_id,
  is_critical,
  DATE_FORMAT(created_at, '%H:%i:%s') as time
FROM xwallet.analytics_event
ORDER BY id DESC
LIMIT 10;
" 2>&1 | grep -v Warning
```

### 方式 2：后端日志
```bash
tail -f /tmp/backend.log | grep "Received event"
```

### 方式 3：前端管理页面
访问：`http://localhost:3000/system/mqtt-events`

## 🧪 测试步骤

1. **启动 App**（已连接 WiFi 和信任证书）

2. **登录**：
   - 邮箱：`customer@example.com`
   - 密码：`customer123`

3. **观察事件上报**：
   - 登录 → 触发 `login` 事件（关键事件）
   - 进入主页 → 触发 `page_view` 事件
   - 点击"申请贷款" → 触发 `button_click` 事件

4. **验证后端接收**：
   ```bash
   # 查看最新事件
   docker exec mysql-dev mysql -u root -p123321qQ -e "
   SELECT event_type, is_critical FROM xwallet.analytics_event
   ORDER BY id DESC LIMIT 5;
   " 2>&1 | grep -v Warning
   ```

## ⚠️ 故障排查

### 问题 1：无法连接 API

**症状**: `Connection refused` 或 `Network error`

**解决方案**:

1. **确认 WiFi 连接**
   ```bash
   # 在 Mac 上查看当前 WiFi IP
   ifconfig | grep "inet " | grep -v 127.0.0.1
   ```

2. **测试后端可访问性**
   - 在 iOS 设备浏览器访问：`http://192.168.31.47:8080/api/test/password`
   - 如果无法访问，检查 Mac 防火墙设置

3. **关闭 Mac 防火墙**（临时测试）
   - 系统设置 → 网络 → 防火墙
   - 关闭防火墙或允许端口 8080

4. **更新 IP 地址**
   - 如果你的 IP 改变了，更新 `lib/utils/platform_config.dart`:
     ```dart
     static const String localNetworkIP = '你的新IP';
     ```

### 问题 2：MQTT 连接失败

**症状**: `⚠️ MQTT connection failed`

**这是正常的！**
- 事件会保存到 SQLite
- 后端恢复后自动重试
- 不影响 App 功能

**如果想测试 MQTT 实时上报**：
1. 确保 EMQX 正在运行：
   ```bash
   docker ps | grep emqx
   ```

2. 测试端口可访问：
   ```bash
   nc -l -p 1883  # 在 Mac 上运行
   # 然后在设备上测试连接
   ```

### 问题 3：证书信任问题

**症状**: iOS 无法安装 App

**解决方案**：
1. Xcode → Settings → Accounts
2. 选择你的 Apple ID
3. 在 **Signing & Capabilities** 中：
   - Team 选择你的个人团队
   - 确保 Bundle Identifier 唯一

### 问题 4：代码已修改但 App 没变化

**解决方案**：
```bash
# 清理并重新编译
flutter clean
flutter pub get
flutter run
```

## 📝 配置总结

| 配置项 | 值 | 说明 |
|-------|---|------|
| API 地址 | `http://192.168.31.47:8080/api` | 后端 API |
| MQTT Broker | `192.168.31.47` | EMQX 地址 |
| MQTT 端口 | `1883` | 非 TLS 端口 |
| 环境 | `dev` | 开发环境 |

## 🎯 测试清单

- [ ] iOS 设备和 Mac 在同一 WiFi
- [ ] 后端服务正在运行
- [ ] EMQX 正在运行
- [ ] Redis 正在运行
- [ ] App 已信任开发者证书
- [ ] 登录成功
- [ ] Xcode 控制台显示 `✅ MQTT connected`
- [ ] 后端日志显示 `Received event`
- [ ] 数据库有新事件记录
- [ ] 前端管理页面可以查看事件

## 🎉 成功标志

当你看到以下内容，说明配置成功：

1. **App 控制台**：
   ```
   ✅ MQTT connected to 192.168.31.47:1883
   ```

2. **后端日志**：
   ```
   Received event: login from device: xxx
   ```

3. **数据库**：
   ```
   | event_type | user_id |
   |------------|---------|
   | login      | 123     |
   ```

## 🔄 IP 地址变更处理

如果你的 Mac IP 地址改变了（例如换 WiFi），更新配置：

```dart
// lib/utils/platform_config.dart
static const String localNetworkIP = '新的IP地址';
```

然后重新运行 App：
```bash
flutter run
```

## 📱 真机测试的优势

✅ **更真实**：接近生产环境
✅ **完整功能**：可以使用相机、GPS 等
✅ **性能测试**：真实的设备性能
✅ **网络测试**：真实的网络环境

## 🎊 开始测试吧！

现在所有配置已就绪，祝测试顺利！
