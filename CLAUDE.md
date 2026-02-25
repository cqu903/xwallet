# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

多平台钱包应用 (xWallet)，由以下独立组件组成：

- **app/** - Android 和 iOS 移动应用 (Flutter)，面向顾客，邮箱登录
- **front-web/** - 基于 Web 浏览器的管理后台 (Next.js 16 + React 19)，面向系统员工，工号登录
- **backend/** - Spring Boot API 服务器 (Spring Boot 3.3.0 + MyBatis)

```
xwallet/
├── app/                  # Flutter 移动应用（独立项目）
├── front-web/            # Next.js Web 管理后台（独立项目）
└── backend/              # Spring Boot 后端（独立项目）
```

**注意**: 三个项目完全独立，各自管理自己的依赖。

## 开发命令

### Flutter 移动端项目 (app/)

使用 Dart SDK ^3.10.4。

```bash
cd app/
flutter pub get              # 安装依赖
flutter run                  # 运行应用
flutter run -d android       # 在 Android 设备/模拟器运行
flutter run -d ios           # 在 iOS 设备/模拟器运行
flutter test                 # 运行测试
flutter analyze              # 代码分析
flutter clean                # 清理构建缓存
```

### Web 管理后台 (front-web/)

使用 Next.js 16 + React 19 + TypeScript。

```bash
cd front-web/

# 安装依赖
pnpm install

# 启动开发服务器 (http://localhost:3000)
pnpm dev

# 构建生产版本
pnpm build

# 启动生产服务器
pnpm start

# 运行 ESLint
pnpm lint

# 运行测试
pnpm test
```

### Backend (Spring Boot)

```bash
cd backend/

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

**环境变量配置**:
- Backend 使用 **spring-dotenv** 从 `backend/.env` 文件自动加载环境变量
- 首次启动需要创建 `backend/.env` 文件并配置数据库、邮件、JWT 等参数
- 详见 [QUICKSTART.md](QUICKSTART.md) 环境变量配置部分

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

- **移动端**: Flutter 3.10+ (Dart SDK ^3.10.4)
  - 状态管理: Provider
  - HTTP: http package
  - 本地存储: shared_preferences, sqflite
  - MQTT 埋点: mqtt_client, device_info_plus, connectivity_plus

- **Web 管理后台**: Next.js 16.1.4 + React 19.2.3 + TypeScript + Tailwind CSS v4 + shadcn/ui + Zustand + SWR
  - 国际化: next-intl (中文/英文)
  - 主题: next-themes (亮/暗模式)
  - 表单: React Hook Form + Zod
  - 路由: App Router (Server Components + Client Components)

- **后端**: Spring Boot 3.3.0 + MyBatis 3.0.3 + MySQL 8.x (Java 17)
  - 认证: JWT (jjwt 0.12.5) + BCrypt 密码加密
  - 权限: 自定义 RBAC (基于角色的访问控制)
  - 缓存: Caffeine (本地缓存)
  - 邮件: Spring Mail (腾讯企业邮箱)
  - 环境变量: spring-dotenv (自动加载 .env)
  - 开发工具: Spring Boot DevTools (热加载)
  - API 文档: SpringDoc OpenAPI (Swagger UI)
  - 消息队列: Spring Integration MQTT (埋点数据收集)


### 后端架构

标准三层架构 + RBAC 权限系统：

```
backend/src/main/java/com/zerofinance/xwallet/
├── config/              # 配置类
│   ├── WebConfig.java           # CORS 配置
│   ├── AuthInterceptor.java     # JWT 认证拦截器
│   ├── PermissionInterceptor.java  # 权限拦截器
│   ├── CacheConfig.java         # Caffeine 缓存配置
│   └── OpenApiConfig.java       # Swagger/OpenAPI 文档配置
├── controller/          # REST API 控制器
├── service/             # 业务逻辑层
├── repository/          # MyBatis Mapper 接口
├── model/
│   ├── entity/         # 数据库实体
│   └── dto/            # 数据传输对象
├── mqtt/               # MQTT 埋点数据处理
│   ├── MqttConfig.java
│   ├── MqttEventSubscriber.java
│   └── MqttJsonConverter.java
├── util/               # 工具类 (JwtUtil, ResponseResult, UserContext)
└── annotation/         # 权限注解 (@RequireRole, @RequirePermission)
```

**核心流程**:
1. 认证拦截器 (`AuthInterceptor`) 验证 JWT Token，将用户信息存入 `UserContext` (ThreadLocal)
2. 权限拦截器 (`PermissionInterceptor`) 检查 `@RequireRole` 和 `@RequirePermission` 注解
3. 请求完成后清理 ThreadLocal 避免内存泄漏

### Web 管理后台架构 (front-web/)

```
front-web/src/
├── app/               # Next.js App Router 页面
│   ├── [locale]/              # 国际化路由
│   │   ├── (dashboard)/       # 受保护的管理页面
│   │   │   ├── dashboard/     # 仪表板
│   │   │   ├── users/         # 用户管理
│   │   │   └── roles/         # 角色管理
│   │   └── login/             # 登录页
│   └── globals.css            # 全局样式
├── components/
│   ├── layout/                # 布局组件
│   │   ├── DashboardLayout    # 主布局
│   │   ├── Header             # 顶部栏
│   │   └── Sidebar            # 侧边栏
│   └── ui/                    # shadcn/ui 组件
├── lib/
│   ├── api/                   # API 客户端
│   ├── stores/                # Zustand 状态管理
│   ├── hooks/                 # React Hooks
│   ├── i18n/                  # 国际化配置
│   └── utils.ts               # 工具函数
└── middleware.ts              # Next.js 中间件 (认证)
```

**核心特性**:
- Next.js 16 App Router (Server Components + Client Components)
- shadcn/ui 组件库 (基于 Radix UI)
- Zustand 状态管理 (auth, menu, layout)
- SWR 数据获取和缓存
- next-intl 国际化 (中文/英文)
- Tailwind CSS v4 样式（使用 @theme 语法）
- next-themes 主题切换（亮/暗模式）
- 动态菜单从后端 `/api/menu/list` 加载，根据用户角色显示不同菜单
- JWT 认证 + 中间件路由保护

**权限控制三层架构**:
1. **Middleware 层** (`middleware.ts`) - 路由级 JWT 验证和权限检查
2. **服务端组件层** (`ProtectedRoute`) - 菜单权限控制
3. **客户端组件层** (`usePermission` Hook) - 操作级权限检查

**JWT 存储策略**:
- Access Token: HttpOnly Cookie（防 XSS）
- 用户信息: LocalStorage（非敏感数据）
- 有效期: 30 分钟

### 移动端架构 (app/)

Flutter 移动应用，面向顾客：
- 邮箱登录 (而非工号)
- 绿色主题 (区别于管理系统的蓝色)
- 支持用户注册功能
- **MQTT 埋点系统**: 使用 `mqtt_client` 上报用户行为数据，配合后端 Redis + Spring Integration MQTT 进行数据分析

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
- **接口文档**: Swagger UI `/api/swagger-ui.html`，OpenAPI JSON `/api/v3/api-docs`，便于对接与调试
- **用户类型**:
  - 系统用户 (`userType=SYSTEM`): 管理后台使用，`account` 为工号 (如 `ADMIN001`)
  - 顾客 (`userType=CUSTOMER`): 移动端使用，`account` 为邮箱

## 后端注解和拦截器

### 权限注解
- `@RequireRole("ADMIN")` - 角色级别权限控制
- `@RequirePermission("user:create")` - 操作级别权限控制

### 拦截器执行顺序
1. `AuthInterceptor` - JWT Token 验证，将用户信息存入 `UserContext` (ThreadLocal)
2. `PermissionInterceptor` - 检查权限注解，验证用户权限
3. 请求完成后自动清理 ThreadLocal 避免内存泄漏

### 开发工具
- **SpringDoc OpenAPI** - 提供 Swagger UI (`/api/swagger-ui.html`) 和 OpenAPI 文档
- **Spring Boot DevTools** - 开发时热加载，检测到文件修改自动重启

## 开发注意事项

1. **项目独立性**: app、front-web、backend 三个项目完全独立，各自管理依赖
2. **Backend 开发顺序**: entity → repository/mapper → service → controller
3. **MySQL 在 Docker 中运行**，执行 SQL 需要连接容器
4. **RBAC 权限**: 新增 API 需要配置对应菜单权限和角色关联
5. **动态菜单**: Web 管理后台菜单从 `/api/menu/list` 获取，根据用户角色动态加载
6. **环境变量**:
   - Backend: 必须创建 `backend/.env` 文件（spring-dotenv 自动加载）
   - Front-web: 默认使用硬编码 API 地址，`.env.local` 为可选配置
7. **包管理**:
   - app: 使用 Flutter pub
   - front-web: 使用 pnpm
   - backend: 使用 Maven

## 测试

### Backend 测试覆盖率要求

项目使用 JaCoCo 强制测试覆盖率门禁：
- Controller 层: 最低 95% 覆盖率
- Service 层: 最低 95% 覆盖率

```bash
cd backend/

# 运行所有测试
mvn test

# 运行测试并生成覆盖率报告
mvn verify

# 查看覆盖率报告
# 报告位置: target/site/jacoco/index.html
```

### Front-Web 测试

```bash
cd front-web/

# 运行单元测试
pnpm test

# 运行 E2E 测试 (Playwright)
pnpm test:e2e
```

### App 测试

```bash
cd app/

# 运行所有测试
flutter test

# 运行集成测试
flutter test integration_test
```

## 主题色彩

- **Web 管理后台**: 蓝色主题 (`--primary: 221 83% 53%`)
- **移动端 App**: 绿色主题

## 设计文档

项目包含详细的设计文档在 `docs/plans/` 目录：
- `2025-01-21-user-management-design.md` - 用户管理设计
- `2026-01-22-mqtt-analytics-design.md` - MQTT 埋点系统设计
- `2026-01-22-mqtt-analytics-implementation.md` - MQTT 埋点系统实现计划
- `2026-01-24-front-web-refactor-design.md` - Front-Web 重构设计（Next.js 架构详细说明）
- `2026-02-07-loan-transaction-*-checklist.md` - 贷款交易功能检查清单
- `2026-02-07-loan-transaction-front-design.md` - 贷款交易前端设计
- `2026-02-14-loan-application-flow-design.md` - 贷款申请全流程设计
- `2026-02-14-loan-application-flow-implementation-plan.md` - 贷款申请实现计划
- `2026-02-16-loan-application-admin-design.md` - 贷款管理后台设计
- `2026-02-16-loan-application-admin-implementation-plan.md` - 贷款管理后台实现计划
