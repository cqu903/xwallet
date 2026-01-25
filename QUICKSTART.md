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
source /home/roy/codes/claudes/xwallet/backend/database/init_all.sql

# 验证数据表
USE xwallet;
SHOW TABLES;

# 查看测试用户
SELECT * FROM sys_user;
SELECT * FROM customer;
```

## 第二步：配置环境变量

项目使用 **spring-dotenv** 从 `.env` 文件自动加载环境变量。

### 环境变量配置文件

**文件位置：** `backend/.env` ⚠️ **唯一需要**

**创建配置文件：**
```bash
cd backend
vim .env
```

**示例配置：**
```bash
# 数据库配置
DB_URL=jdbc:mysql://localhost:3306/xwallet?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&characterEncoding=utf8
DB_USERNAME=root
DB_PASSWORD=your_password_here

# 邮件配置
MAIL_HOST=smtp.exmail.qq.com
MAIL_PORT=465
MAIL_USERNAME=your_email@example.com
MAIL_PASSWORD=your_email_password

# JWT配置
JWT_SECRET=your_jwt_secret_key_here

# MQTT配置（可选）
MQTT_BROKER_HOST=broker.emqxsl.com
MQTT_USERNAME=your_mqtt_username
MQTT_PASSWORD=your_mqtt_password
```

**重要说明：**
- spring-dotenv 会从 backend/ 目录加载 .env 文件
- 修改配置后需要重启后端才能生效
- 如果启动时遇到数据库连接错误，首先检查 backend/.env 文件是否存在且配置正确

## 第三步：启动后端服务

```bash
# 进入 backend 目录
cd backend

# 启动开发环境服务（使用 dev profile）
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 或者先打包再运行（生产环境）
mvn clean package -DskipTests
java -jar target/xwallet-backend-1.0.0.jar --spring.profiles.active=dev
```

**验证后端是否启动成功：**
- 访问: http://localhost:8080/api/auth/login
- 应该看到 401 错误或 {"code":401,"errmsg":"未登录或登录已过期"}
- 查看启动日志，确认没有数据库连接错误

**API 文档 (Swagger UI)：**
- 在线文档与调试: http://localhost:8080/api/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api/v3/api-docs
- 在 Swagger UI 中先调用「认证 > 用户登录」获取 token，再点击右上角「Authorize」填入 token，即可调试需鉴权的接口。

## 第四步：启动前端Web管理系统

```bash
cd front-web

# 安装依赖（首次运行）
npm install

# 启动开发服务器
npm run dev
```

Web 管理系统将在浏览器中打开: http://localhost:3000

**测试登录：**
- 工号: `ADMIN001`
- 密码: `admin123`

## 第五步：启动移动端App

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

### Q1: 后端启动失败 - 找不到环境变量
**症状：**
```
Could not resolve placeholder 'DB_URL' in value "${DB_URL}"
或
Could not resolve placeholder 'MAIL_HOST' in value "${MAIL_HOST}"
```

**原因：**
- backend/.env 文件不存在或配置不完整

**解决方案：**
1. 确认 backend/.env 文件存在：
   ```bash
   ls backend/.env
   ```
2. 如果不存在，创建配置文件：
   ```bash
   cd backend
   vim .env
   ```
3. 确认 backend/.env 文件包含所有必需的环境变量配置（参考文档中的示例配置）

### Q2: 后端启动失败 - 数据库连接错误
**症状：**
```
java.sql.SQLException: Access denied for user 'root'@'localhost'
或
Communications link failure
```

**解决方案：**
1. 检查 MySQL 是否运行: `docker ps | grep mysql` 或 `sudo systemctl status mysql`
2. 检查 backend/.env 文件中的数据库配置是否正确
3. 确认数据库已创建: `SHOW DATABASES;`
4. 测试数据库连接: `mysql -u root -p -h localhost`

### Q3: 修改了 .env 文件但后端没有读取新配置
**解决方案：**
1. 重启后端服务：
   ```bash
   # 停止旧进程
   pkill -f "spring-boot:run"

   # 重新启动
   cd backend
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```
2. 确认修改的是 backend/.env 文件（不是项目根目录的 .env）

### Q4: 前端无法连接后端
**解决方案：**
1. 确认后端已启动: `curl http://localhost:8080/api/auth/login`
2. 检查API地址配置: `front/lib/services/api_service.dart`
3. 确认baseUrl为: `http://localhost:8080/api`

### Q5: Flutter依赖安装失败
**解决方案：**
```bash
# 清理并重新获取依赖
flutter clean
flutter pub get

# 如果还是失败，升级Flutter
flutter upgrade
```

### Q6: Token验证失败
**原因：**
- Token已过期（30分钟有效期）
- Token格式错误
- Token在黑名单中

**解决方案：**
- 重新登录获取新Token
- 检查Token格式：`Bearer {token}`

### Q7: 密码错误
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
├── front-web/        # Web管理系统 (Next.js + React)
│   ├── src/
│   │   ├── app/      # Next.js App Router 页面
│   │   ├── components/# React 组件
│   │   └── lib/      # 工具库、API、状态管理
│   └── package.json  # npm 配置
│
├── app/              # 移动端App (Flutter)
│   └── lib/
│       ├── models/   # 数据模型
│       ├── services/ # API服务
│       ├── providers/# 状态管理
│       ├── screens/  # UI页面
│       └── main.dart # 应用入口
│
├── packages/         # 共享包
│   ├── shared-types/ # 共享类型定义
│   └── shared-utils/ # 共享工具函数
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
