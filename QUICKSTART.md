# xWallet 登录功能 - 快速启动指南

## 前置条件

1. **Docker Desktop** - MySQL 运行在 Docker 容器中
2. **JDK 17** - Java 开发环境
3. **Node.js 18+** - 前端开发环境（推荐使用 pnpm，npm 也可以）
4. **Flutter 3.10+** - 移动端开发环境（可选）
5. **浏览器** - Chrome（用于 Web 管理系统）

## 第一步：启动 Docker 和 MySQL

### 1.1 启动 Docker Desktop

**Windows:**
```bash
# 启动 Docker Desktop（如果未运行）
start "" "C:/Program Files/Docker/Docker/Docker Desktop.exe"

# 等待 Docker 启动完成后验证
docker info
```

**Linux/Mac:**
```bash
# 确保 Docker 服务运行
sudo systemctl start docker  # Linux
# 或打开 Docker Desktop 应用 (Mac)
```

### 1.2 启动 MySQL 容器

```bash
# 查看容器状态
docker ps -a --filter "name=mysql"

# 如果容器存在但未运行，启动它
docker start xwallet-mysql

# 如果容器不存在，创建并启动（首次运行）
cd backend
docker-compose up -d
```

### 1.3 初始化数据库

**方式一：通过 Docker exec（推荐）**

```bash
# 执行初始化脚本
docker exec -i xwallet-mysql mysql -u root -p123321qQ < backend/database/init_all.sql

# 验证数据表
docker exec -it xwallet-mysql mysql -u root -p123321qQ -e "USE xwallet; SHOW TABLES;"

# 查看测试用户
docker exec -it xwallet-mysql mysql -u root -p123321qQ -e "SELECT id, username, email, employee_no FROM xwallet.sys_user;"
docker exec -it xwallet-mysql mysql -u root -p123321qQ -e "SELECT id, email, nickname FROM xwallet.customer;"
```

**方式二：直接登录 MySQL**

```bash
# 进入 MySQL 容器
docker exec -it xwallet-mysql mysql -u root -p123321qQ

# 在 MySQL 命令行中执行
source /path/to/xwallet/backend/database/init_all.sql
# 或手动执行脚本内容
```

## 第二步：配置环境变量

项目使用 **spring-dotenv** 从 `.env` 文件自动加载环境变量。

### 环境变量配置文件

**文件位置：** `backend/.env` ⚠️ **唯一需要**

**创建配置文件：**

```bash
cd backend

# Windows
notepad .env
# 或
vim .env

# Linux/Mac
vim .env
```

**示例配置：**

```bash
# 数据库配置
DB_URL=jdbc:mysql://localhost:3306/xwallet?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&characterEncoding=utf8
DB_USERNAME=root
DB_PASSWORD=123321qQ

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
- 应该看到 401 错误或 `{"code":401,"errmsg":"未登录或登录已过期"}`
- 查看启动日志，确认没有数据库连接错误
- 看到日志 `Started XWalletBackendApplication in X.XXX seconds` 表示启动成功

**API 文档 (Swagger UI)：**

- 在线文档与调试: http://localhost:8080/api/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api/v3/api-docs
- 在 Swagger UI 中先调用「认证 > 用户登录」获取 token，再点击右上角「Authorize」填入 token，即可调试需鉴权的接口。

## 第四步：启动前端 Web 管理系统

```bash
cd front-web

# 安装依赖（首次运行）
# 推荐：使用 pnpm
pnpm install

# 或者：使用 npm
npm install

# 启动开发服务器
# 使用 pnpm
pnpm dev

# 或者使用 npm
npm run dev
```

Web 管理系统将在浏览器中打开: http://localhost:3000

**测试登录：**

- 工号: `ADMIN001`
- 密码: `admin123`

**注意：** Next.js 16.1.4 使用 Turbopack，首次启动可能需要几秒钟编译。

## 第五步：启动移动端 App（可选）

```bash
cd app

# 安装依赖（首次运行）
flutter pub get

# 在 Android 设备/模拟器运行
flutter run -d android

# 或在 iOS 设备/模拟器运行（需要 Mac）
flutter run -d ios
```

**测试登录：**

- 邮箱: `customer@example.com`
- 密码: `customer123`

## 快速验证脚本

**Windows PowerShell:**

```powershell
# 验证所有服务状态
Write-Host "=== 检查 Docker ===" -ForegroundColor Green
docker info --format '{{.ServerVersion}}' 2>$null
if ($LASTEXITCODE -ne 0) { Write-Host "Docker 未运行，请启动 Docker Desktop" -ForegroundColor Red }

Write-Host "`n=== 检查 MySQL 容器 ===" -ForegroundColor Green
docker ps --filter "name=mysql" --format "table {{.Names}}\t{{.Status}}"

Write-Host "`n=== 检查后端 API ===" -ForegroundColor Green
$response = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" -UseBasicParsing -ErrorAction SilentlyContinue
if ($response) { Write-Host "后端运行中: $($response.StatusCode)" -ForegroundColor Green }
else { Write-Host "后端未启动" -ForegroundColor Red }

Write-Host "`n=== 检查前端 ===" -ForegroundColor Green
$response = Invoke-WebRequest -Uri "http://localhost:3000" -UseBasicParsing -ErrorAction SilentlyContinue
if ($response) { Write-Host "前端运行中: $($response.StatusCode)" -ForegroundColor Green }
else { Write-Host "前端未启动" -ForegroundColor Red }
```

**Linux/Mac Bash:**

```bash
# 验证所有服务状态
echo "=== 检查 Docker ==="
docker info --format '{{.ServerVersion}}' 2>/dev/null || echo "Docker 未运行"

echo -e "\n=== 检查 MySQL 容器 ==="
docker ps --filter "name=mysql" --format "table {{.Names}}\t{{.Status}}"

echo -e "\n=== 检查后端 API ==="
curl -s http://localhost:8080/api/auth/login && echo -e "\n后端运行中" || echo "后端未启动"

echo -e "\n=== 检查前端 ==="
curl -s http://localhost:3000 > /dev/null && echo "前端运行中" || echo "前端未启动"
```

## 使用 Postman/cURL 测试 API

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

### 3. 验证 Token

```bash
# 替换 YOUR_TOKEN 为上一步获取的 token
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
# 替换 YOUR_TOKEN 为你的 token
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

### Q1: Docker Desktop 未启动

**症状：**

```
error during connect: This error may indicate that the docker daemon is not running
```

**解决方案：**

1. Windows: 打开 Docker Desktop 应用程序
2. 等待 Docker 图标显示 "Docker Desktop is running"
3. 验证: `docker info`

### Q2: MySQL 容器未运行

**症状：**

```
docker ps 返回空列表或没有 xwallet-mysql
```

**解决方案：**

```bash
# 检查容器是否存在
docker ps -a --filter "name=mysql"

# 如果容器存在但停止了，启动它
docker start xwallet-mysql

# 如果容器不存在，使用 docker-compose 创建
cd backend
docker-compose up -d
```

### Q3: 后端启动失败 - 找不到环境变量

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
   vim .env  # Linux/Mac
   notepad .env  # Windows
   ```
3. 确认 backend/.env 文件包含所有必需的环境变量配置（参考文档中的示例配置）

### Q4: 后端启动失败 - 数据库连接错误

**症状：**

```
java.sql.SQLException: Access denied for user 'root'@'localhost'
或
Communications link failure
```

**解决方案：**

1. 检查 MySQL 容器是否运行: `docker ps | grep mysql`
2. 检查 backend/.env 文件中的数据库配置是否正确
3. 确认数据库已创建:
   ```bash
   docker exec -it xwallet-mysql mysql -u root -p123321qQ -e "SHOW DATABASES;"
   ```
4. 测试数据库连接:
   ```bash
   docker exec -it xwallet-mysql mysql -u root -p123321qQ
   ```

### Q5: 修改了 .env 文件但后端没有读取新配置

**解决方案：**

1. 重启后端服务：

   ```bash
   # 停止旧进程（Ctrl+C 或）
   # Windows
   taskkill /F /IM java.exe

   # Linux/Mac
   pkill -f "spring-boot:run"

   # 重新启动
   cd backend
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

2. 确认修改的是 backend/.env 文件（不是项目根目录的 .env）

### Q6: 前端无法连接后端

**解决方案：**

1. 确认后端已启动: `curl http://localhost:8080/api/auth/login`
2. 检查浏览器控制台是否有 CORS 错误
3. 确认后端运行在 8080 端口

### Q7: pnpm 命令找不到

**症状：**

```
'pnpm' is not recognized as an internal or external command
```

**解决方案：**

```bash
# 方式一：使用 npm 代替
npm install
npm run dev

# 方式二：安装 pnpm
npm install -g pnpm
```

### Q8: Flutter 依赖安装失败

**解决方案：**

```bash
# 清理并重新获取依赖
flutter clean
flutter pub get

# 如果还是失败，升级 Flutter
flutter upgrade
```

### Q9: Token 验证失败

**原因：**

- Token 已过期（30 分钟有效期）
- Token 格式错误
- Token 在黑名单中

**解决方案：**

- 重新登录获取新 Token
- 检查 Token 格式：`Bearer {token}`

### Q10: 密码错误

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
│   │   └── init_all.sql
│   ├── docker-compose.yml  # MySQL Docker 配置
│   ├── .env           # 环境变量配置（需创建）
│   ├── src/main/
│   │   ├── java/     # Java 源代码
│   │   └── resources/ # 配置文件和 Mapper XML
│   └── pom.xml       # Maven 配置
│
├── front-web/        # Web 管理系统 (Next.js + React)
│   ├── src/
│   │   ├── app/      # Next.js App Router 页面
│   │   ├── components/ # React 组件
│   │   └── lib/      # 工具库、API、状态管理
│   └── package.json  # 依赖配置
│
├── app/              # 移动端 App (Flutter)
│   └── lib/
│       ├── models/   # 数据模型
│       ├── services/ # API 服务
│       ├── providers/ # 状态管理
│       ├── screens/  # UI 页面
│       └── main.dart # 应用入口
│
├── CLAUDE.md         # Claude Code 项目指南
├── LOGIN_README.md   # 详细功能说明
└── QUICKSTART.md     # 本文件
```

## 服务端口汇总

| 服务 | 端口 | 地址 |
|------|------|------|
| MySQL (Docker) | 3306 | localhost:3306 |
| Backend API | 8080 | http://localhost:8080/api |
| Swagger UI | 8080 | http://localhost:8080/api/swagger-ui.html |
| Front-Web | 3000 | http://localhost:3000 |

## 下一步

登录功能完成后，你可以继续开发：

1. **钱包功能**: 创建钱包、查看余额、交易记录
2. **用户管理**: 用户注册、信息修改、密码重置
3. **交易功能**: 转账、收款、交易历史
4. **安全功能**: 双因素认证、生物识别、交易密码
5. **管理功能**: 后台管理、数据统计、报表生成

## 技术支持

如有问题，请查看：

- `CLAUDE.md` - Claude Code 项目指南
- `LOGIN_README.md` - 详细功能说明
- `IMPLEMENTATION_SUMMARY.md` - 实现总结

祝开发顺利！🚀
