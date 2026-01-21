# xWallet 登录功能实现说明

## 功能概述

xWallet登录功能已完整实现，支持以下功能：
- 系统用户用工号登录（Web管理系统）
- 顾客用邮箱登录（移动端App）
- JWT Token认证（30分钟有效期）
- Token黑名单机制（支持登出）
- BCrypt密码加密
- 完整的前后端分离架构

## 技术栈

### 后端 (Backend)
- Spring Boot 3.3.0
- MyBatis 3.0.3
- MySQL 8.x
- JWT (jjwt 0.12.5)
- Spring Security Crypto (BCrypt)

### 前端 (Front - Web管理系统)
- Flutter Web
- Provider状态管理
- HTTP请求
- SharedPreferences本地存储

### 移动端 (App - 顾客端)
- Flutter (Android/iOS)
- Provider状态管理
- HTTP请求
- SharedPreferences本地存储

## 项目结构

### 后端
```
backend/
├── src/main/java/com/zerofinance/xwallet/
│   ├── config/
│   │   └── WebConfig.java                    # CORS配置
│   ├── controller/
│   │   └── AuthController.java               # 认证控制器
│   ├── service/
│   │   ├── AuthService.java                  # 认证服务接口
│   │   └── impl/AuthServiceImpl.java         # 认证服务实现
│   ├── repository/
│   │   ├── SysUserMapper.java                # 系统用户Mapper
│   │   ├── CustomerMapper.java               # 顾客Mapper
│   │   └── TokenBlacklistMapper.java         # Token黑名单Mapper
│   ├── model/
│   │   ├── entity/
│   │   │   ├── SysUser.java                  # 系统用户实体
│   │   │   ├── Customer.java                 # 顾客实体
│   │   │   └── TokenBlacklist.java           # Token黑名单实体
│   │   └── dto/
│   │       ├── LoginRequest.java             # 登录请求DTO
│   │       └── LoginResponse.java            # 登录响应DTO
│   └── util/
│       ├── JwtUtil.java                      # JWT工具类
│       └── ResponseResult.java               # 响应结果工具类
└── src/main/resources/
    ├── mapper/
    │   ├── SysUserMapper.xml                 # 系统用户Mapper XML
    │   ├── CustomerMapper.xml                # 顾客Mapper XML
    │   └── TokenBlacklistMapper.xml          # Token黑名单Mapper XML
    └── application-dev.yml                   # JWT配置
```

### 前端 (Front)
```
front/
└── lib/
    ├── models/
    │   ├── login_request.dart                # 登录请求模型
    │   └── login_response.dart               # 登录响应模型
    ├── services/
    │   └── api_service.dart                  # API服务
    ├── providers/
    │   └── auth_provider.dart                # 认证Provider
    ├── screens/
    │   ├── login_screen.dart                 # 登录页面
    │   └── home_screen.dart                  # 主页
    └── main.dart                             # 应用入口（集成Provider）
```

### 移动端 (App)
```
app/
└── lib/
    ├── models/
    │   ├── login_request.dart                # 登录请求模型（复用Front）
    │   └── login_response.dart               # 登录响应模型（复用Front）
    ├── services/
    │   └── api_service.dart                  # API服务（复用Front）
    ├── providers/
    │   └── auth_provider.dart                # 认证Provider（CUSTOMER类型）
    ├── screens/
    │   ├── login_screen.dart                 # 登录页面（邮箱登录）
    │   └── home_screen.dart                  # 主页
    └── main.dart                             # 应用入口（集成Provider）
```

## 快速开始

### 1. 数据库初始化

```bash
# 登录MySQL
mysql -u root -p

# 执行初始化脚本
source /home/roy/codes/claudes/xwallet/backend/database/init.sql
```

### 2. 后端启动

```bash
cd /home/roy/codes/claudes/xwallet/backend

# 安装依赖
mvn clean install

# 启动应用
mvn spring-boot:run
```

后端将在 `http://localhost:8080/api` 启动

### 3. 前端启动 (Web管理系统)

```bash
cd /home/roy/codes/claudes/xwallet/front

# 安装依赖
flutter pub get

# 运行Web版本
flutter run -d chrome
```

Web管理系统将在浏览器中打开

### 4. 移动端启动 (App)

```bash
cd /home/roy/codes/claudes/xwallet/app

# 安装依赖
flutter pub get

# 在Android设备/模拟器运行
flutter run -d android

# 或在iOS设备/模拟器运行
flutter run -d ios
```

## 测试账号

### 系统用户登录（Web管理系统）
- 工号: `ADMIN001`
- 密码: `admin123`

### 顾客登录（移动端App）
- 邮箱: `customer@example.com`
- 密码: `customer123`

## API接口

### 1. 用户登录
```
POST /api/auth/login
Content-Type: application/json

请求体:
{
  "userType": "SYSTEM" | "CUSTOMER",
  "account": "工号或邮箱",
  "password": "密码"
}

响应:
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "JWT Token字符串",
    "userInfo": {
      "userId": 1,
      "username": "用户名",
      "userType": "SYSTEM" | "CUSTOMER",
      "role": "ADMIN" | "OPERATOR" | null
    }
  }
}
```

### 2. 用户登出
```
POST /api/auth/logout
Headers:
  Authorization: Bearer {token}

响应:
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 3. 验证Token
```
GET /api/auth/validate
Headers:
  Authorization: Bearer {token}

响应:
{
  "code": 200,
  "message": "success",
  "data": true | false
}
```

## 核心功能说明

### 1. JWT认证流程

1. **登录**:
   - 用户提交工号/邮箱和密码
   - 后端验证用户身份
   - 生成JWT Token（30分钟有效期）
   - 返回Token和用户信息
   - 前端保存Token到本地存储

2. **请求认证**:
   - 每次请求在Header中携带 `Authorization: Bearer {token}`
   - 后端验证Token有效性
   - 检查Token是否在黑名单中
   - 检查Token是否过期

3. **登出**:
   - 前端调用登出接口
   - 后端将Token加入黑名单表
   - 前端清除本地存储的Token

### 2. 密码加密

- 使用BCrypt算法加密密码
- 加密强度: 10轮（默认）
- 每次加密结果不同（自动加盐）
- 数据库中存储加密后的密码

### 3. Token黑名单机制

- 登出时将Token加入黑名单表
- 记录Token过期时间
- 自动清理过期的黑名单Token
- 验证Token时检查黑名单

## 配置说明

### 后端配置 (application-dev.yml)

```yaml
jwt:
  secret: xWalletSecretKeyForJWT2024MustBeAtLeast32BytesLong  # JWT密钥（至少32字节）
  expiration: 1800000  # Token过期时间（毫秒）= 30分钟
```

### 前端配置 (api_service.dart)

```dart
static const String baseUrl = 'http://localhost:8080/api';
```

### 移动端配置 (api_service.dart)

```dart
static const String baseUrl = 'http://localhost:8080/api';
```

## 安全特性

1. **密码加密**: 使用BCrypt加密，不可逆
2. **Token过期**: 30分钟自动过期
3. **Token黑名单**: 登出后Token立即失效
4. **CORS配置**: 允许跨域请求
5. **输入验证**: 前后端双重验证

## 注意事项

1. **生产环境部署前需要修改**:
   - JWT密钥（使用更强的随机字符串）
   - 数据库密码
   - API地址（改为实际服务器地址）

2. **数据库密码说明**:
   - 测试账号的密码已在初始化脚本中预先加密
   - 新用户注册时需要使用BCrypt加密密码
   - BCrypt加密后的密码每次都不同（自动加盐）

3. **Token刷新**:
   - 当前实现不支持Token刷新
   - Token过期后需要重新登录
   - 后续可添加Refresh Token机制

## 扩展建议

1. **Token刷新**: 实现Refresh Token机制
2. **记住密码**: 添加"记住我"功能
3. **多设备登录**: 支持同一账号多设备登录
4. **登录历史**: 记录登录历史和设备信息
5. **邮箱验证**: 新用户注册时验证邮箱
6. **密码重置**: 支持忘记密码功能
7. **二维码登录**: 支持扫码登录

## 故障排查

### 后端启动失败
- 检查MySQL是否运行
- 检查数据库配置是否正确
- 检查端口8080是否被占用

### 前端无法连接后端
- 检查后端是否启动
- 检查API地址配置是否正确
- 检查CORS配置是否正确

### 登录失败
- 检查用户账号和密码是否正确
- 检查用户状态是否启用
- 查看后端日志获取详细错误信息

### Token验证失败
- 检查Token是否过期
- 检查Token是否在黑名单中
- 检查Token格式是否正确

## 开发者

- xWallet Team
- 完成日期: 2026-01-20

## 许可证

Copyright © 2026 ZeroFinance. All rights reserved.
