# xWallet 登录功能 - 快速启动指南

## 前置条件

1. **MySQL 8.x** - 数据库服务
2. **JDK 17** - Java开发环境
3. **Flutter 3.10+** - 前端和移动端开发环境
4. **浏览器** - Chrome（用于Web管理系统）

## 第一步：初始化数据库

```bash
# 登录MySQL
mysql -u root -p

# 执行初始化脚本
source /home/roy/codes/claudes/xwallet/backend/database/init.sql

# 验证数据表
USE xwallet;
SHOW TABLES;

# 查看测试用户
SELECT * FROM sys_user;
SELECT * FROM customer;
```

## 第二步：启动后端服务

```bash
cd /home/roy/codes/claudes/xwallet/backend

# 方式1：使用Maven直接运行
mvn spring-boot:run

# 方式2：先打包再运行
mvn clean package -DskipTests
java -jar target/xwallet-backend-1.0.0.jar
```

**验证后端是否启动成功：**
- 访问: http://localhost:8080/api
- 应该看到404错误（这是正常的，说明服务已启动）

## 第三步：启动前端Web管理系统

```bash
cd /home/roy/codes/claudes/xwallet/front

# 安装依赖（首次运行）
flutter pub get

# 启动Web版本
flutter run -d chrome
```

**测试登录：**
- 工号: `ADMIN001`
- 密码: `admin123`

## 第四步：启动移动端App

```bash
cd /home/roy/codes/claudes/xwallet/app

# 安装依赖（首次运行）
flutter pub get

# 在Android设备/模拟器运行
flutter run -d android

# 或在iOS设备/模拟器运行（需要Mac）
flutter run -d ios
```

**测试登录：**
- 邮箱: `customer@example.com`
- 密码: `customer123`

## 使用Postman/cURL测试API

### 1. 系统用户登录

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "userType": "SYSTEM",
    "account": "ADMIN001",
    "password": "admin123"
  }'
```

**预期响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userInfo": {
      "userId": 1,
      "username": "系统管理员",
      "userType": "SYSTEM",
      "role": "ADMIN"
    }
  }
}
```

### 2. 顾客登录

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "userType": "CUSTOMER",
    "account": "customer@example.com",
    "password": "customer123"
  }'
```

**预期响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userInfo": {
      "userId": 1,
      "username": "测试顾客",
      "userType": "CUSTOMER",
      "role": null
    }
  }
}
```

### 3. 验证Token

```bash
# 替换YOUR_TOKEN为上一步获取的token
curl -X GET http://localhost:8080/api/auth/validate \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**预期响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

### 4. 登出

```bash
# 替换YOUR_TOKEN为你的token
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**预期响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

## 常见问题

### Q1: 后端启动失败 - 连接数据库错误
**解决方案：**
1. 检查MySQL是否运行: `sudo systemctl status mysql`
2. 检查数据库配置: `/home/roy/codes/claudes/xwallet/backend/src/main/resources/application-dev.yml`
3. 确认数据库已创建: `SHOW DATABASES;`

### Q2: 前端无法连接后端
**解决方案：**
1. 确认后端已启动: `curl http://localhost:8080/api`
2. 检查API地址配置: `/home/roy/codes/claudes/xwallet/front/lib/services/api_service.dart`
3. 确认baseUrl为: `http://localhost:8080/api`

### Q3: Flutter依赖安装失败
**解决方案：**
```bash
# 清理并重新获取依赖
flutter clean
flutter pub get

# 如果还是失败，升级Flutter
flutter upgrade
```

### Q4: Token验证失败
**原因：**
- Token已过期（30分钟有效期）
- Token格式错误
- Token在黑名单中

**解决方案：**
- 重新登录获取新Token
- 检查Token格式：`Bearer {token}`

### Q5: 密码错误
**注意：**
- 测试账号的密码已经在数据库中预先加密
- 密码区分大小写
- 系统用户: admin123
- 顾客: customer123

## 项目结构速览

```
xwallet/
├── backend/          # 后端服务 (Spring Boot)
│   ├── database/     # 数据库初始化脚本
│   ├── src/main/
│   │   ├── java/     # Java源代码
│   │   └── resources/# 配置文件和Mapper XML
│   └── pom.xml       # Maven配置
│
├── front/            # Web管理系统 (Flutter Web)
│   └── lib/
│       ├── models/   # 数据模型
│       ├── services/ # API服务
│       ├── providers/# 状态管理
│       ├── screens/  # UI页面
│       └── main.dart # 应用入口
│
├── app/              # 移动端App (Flutter)
│   └── lib/
│       ├── models/   # 数据模型（复用front）
│       ├── services/ # API服务（复用front）
│       ├── providers/# 状态管理
│       ├── screens/  # UI页面
│       └── main.dart # 应用入口
│
├── LOGIN_README.md           # 详细功能说明
├── IMPLEMENTATION_SUMMARY.md # 实现总结
└── QUICKSTART.md             # 本文件
```

## 下一步

登录功能完成后，你可以继续开发：

1. **钱包功能**: 创建钱包、查看余额、交易记录
2. **用户管理**: 用户注册、信息修改、密码重置
3. **交易功能**: 转账、收款、交易历史
4. **安全功能**: 双因素认证、生物识别、交易密码
5. **管理功能**: 后台管理、数据统计、报表生成

## 技术支持

如有问题，请查看：
- `/home/roy/codes/claudes/xwallet/LOGIN_README.md` - 详细功能说明
- `/home/roy/codes/claudes/xwallet/IMPLEMENTATION_SUMMARY.md` - 实现总结

祝开发顺利！🚀
