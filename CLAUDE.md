# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

多平台钱包应用 (xWallet)，由三个主要组件组成：

- **app/** - Android 和 iOS 移动应用 (Flutter)，面向顾客，邮箱登录
- **front/** - 基于 Web 浏览器的管理后台 (Flutter Web)，面向系统员工，工号登录
- **backend/** - Spring Boot API 服务器 (Spring Boot 3.3.0 + MyBatis)

## 开发命令

### Flutter 项目 (app/ 和 front/)

`app/` 和 `front/` 都使用 Dart SDK ^3.10.4。

```bash
cd app/  # 或 front/
flutter pub get              # 安装依赖
flutter run                  # 运行应用
flutter run -d chrome        # 运行 Web 版本 (front/ 主要使用此方式)
flutter run -d android       # 在 Android 设备/模拟器运行
flutter run -d ios           # 在 iOS 设备/模拟器运行
flutter test                 # 运行测试
flutter analyze              # 代码分析
flutter clean                # 清理构建缓存
```

### Backend (Spring Boot)

```bash
cd backend/

# Maven 在 ~/.bashrc 中配置，使用完整路径
# 构建项目
mvn clean install

# 运行应用 (开发环境，使用 dev profile)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 打包后运行
mvn clean package -DskipTests
java -jar target/xwallet-backend-1.0.0.jar --spring.profiles.active=dev

# 运行测试
mvn test
```

### 数据库 (MySQL in Docker)

```bash
# MySQL 运行在 Docker 容器中
# 执行 SQL 脚本需要先连接到 MySQL 容器

docker exec -i <mysql-container-name> mysql -u root -p123321qQ < backend/database/init_all.sql

# 或进入容器
docker exec -it <mysql-container-name> mysql -u root -p123321qQ
```

## 架构设计

### 技术栈

- **移动端和 Web 端**: Flutter 3.10+ (Dart SDK ^3.10.4)
- **后端**: Spring Boot 3.3.0 + MyBatis 3.0.3 + MySQL 8.x (Java 17)
- **认证**: JWT (jjwt 0.12.5) + BCrypt 密码加密
- **权限**: 自定义 RBAC (基于角色的访问控制)
- **缓存**: Caffeine (本地缓存)
- **邮件**: Spring Mail (腾讯企业邮箱)

### 后端架构

标准三层架构 + RBAC 权限系统：

```
backend/src/main/java/com/zerofinance/xwallet/
├── config/              # 配置类
│   ├── WebConfig.java           # CORS 配置
│   ├── AuthInterceptor.java     # JWT 认证拦截器
│   ├── PermissionInterceptor.java  # 权限拦截器
│   └── CacheConfig.java         # Caffeine 缓存配置
├── controller/          # REST API 控制器
├── service/             # 业务逻辑层
├── repository/          # MyBatis Mapper 接口
├── model/
│   ├── entity/         # 数据库实体
│   └── dto/            # 数据传输对象
├── util/               # 工具类 (JwtUtil, ResponseResult, UserContext)
└── annotation/         # 权限注解 (@RequireRole, @RequirePermission)
```

**核心流程**:
1. 认证拦截器 (`AuthInterceptor`) 验证 JWT Token，将用户信息存入 `UserContext` (ThreadLocal)
2. 权限拦截器 (`PermissionInterceptor`) 检查 `@RequireRole` 和 `@RequirePermission` 注解
3. 请求完成后清理 ThreadLocal 避免内存泄漏

### 前端架构 (front/)

```
front/lib/
├── layouts/           # 布局组件
│   └── main_layout.dart       # 主布局 (侧边栏 + 内容区)
├── providers/         # 状态管理 (Provider)
│   ├── auth_provider.dart     # 认证状态
│   ├── menu_provider.dart     # 菜单状态 (动态菜单)
│   └── layout_provider.dart   # 布局状态 (侧边栏宽度等)
├── routes/            # 路由 (go_router)
│   └── app_router.dart        # 路由配置 + 登录重定向
├── screens/           # 页面
├── widgets/           # 可复用组件
│   ├── sidebar.dart            # 侧边栏
│   └── sidebar_item.dart       # 侧边栏项
├── services/          # API 服务
└── models/            # 数据模型
```

**核心特性**:
- `go_router` 用于声明式路由，支持登录状态重定向
- `ShellRoute` 实现固定侧边栏布局
- 动态菜单从后端加载，根据用户角色显示不同菜单

### 移动端架构 (app/)

与 front/ 类似，但面向顾客：
- 邮箱登录 (而非工号)
- 绿色主题 (区别于管理系统的蓝色)
- 支持用户注册功能

## 数据库表结构

核心表 (`backend/database/init_all.sql`):
- `sys_user` (系统用户) - 包含 employee_no, username, email, password, status
- `customer` (顾客) - 包含 email, password, nickname, status
- `token_blacklist` (Token黑名单)
- `verification_code` (验证码)
- `sys_menu` (菜单权限) - RBAC核心表
- `sys_role` (角色)
- `sys_role_menu` (角色菜单关联)
- `sys_user_role` (用户角色关联)
- `sys_operation_log` (操作日志)

## 测试账号

### 系统用户 (Web 管理系统)
- 工号: `ADMIN001` / 密码: `admin123`

### 顾客 (移动端 App)
- 邮箱: `customer@example.com` / 密码: `customer123`

## API 基础信息

- 后端地址: `http://localhost:8080/api`
- 认证方式: JWT Bearer Token
- Token 有效期: 30 分钟

## 开发注意事项

1. **app/ 和 front/ 是独立的 Flutter 应用**，不是 monorepo，不共享代码
2. **Backend 开发顺序**: entity → repository/mapper → service → controller
3. **MySQL 在 Docker 中运行**，执行 SQL 需要连接容器
4. **RBAC 权限**: 新增 API 需要配置对应菜单权限和角色关联
5. **动态菜单**: 前端菜单从 `/api/menu/list` 获取，根据用户角色动态加载
6. **认证拦截**: 所有需要认证的 API 都会被 `AuthInterceptor` 拦截
7. **权限注解**: 使用 `@RequireRole("ADMIN")` 或 `@RequirePermission("user:create")` 控制访问
