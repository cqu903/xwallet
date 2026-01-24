# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

多平台钱包应用 (xWallet)，由三个主要组件组成：

- **app/** - Android 和 iOS 移动应用 (Flutter)，面向顾客，邮箱登录
- **apps/front-web/** - 基于 Web 浏览器的管理后台 (Next.js + React)，面向系统员工，工号登录
- **backend/** - Spring Boot API 服务器 (Spring Boot 3.3.0 + MyBatis)

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

### Web 管理后台 (apps/front-web/)

使用 Next.js 14 + React 18 + TypeScript。

```bash
cd apps/front-web/
npm install                  # 安装依赖
npm run dev                  # 启动开发服务器 (http://localhost:3000)
npm run build                # 构建生产版本
npm run start                # 启动生产服务器
npm run lint                 # 运行 ESLint
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

- **移动端**: Flutter 3.10+ (Dart SDK ^3.10.4)
- **Web 管理后台**: Next.js 14 + React 18 + TypeScript + Tailwind CSS v4 + shadcn/ui + Zustand + SWR
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

### Web 管理后台架构 (apps/front-web/)

```
apps/front-web/src/
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
- Next.js 14 App Router (Server Components + Client Components)
- shadcn/ui 组件库 (基于 Radix UI)
- Zustand 状态管理 (auth, menu, layout)
- SWR 数据获取和缓存
- next-intl 国际化 (中文/英文)
- Tailwind CSS v4 样式
- 动态菜单从后端加载，根据用户角色显示不同菜单
- JWT 认证 + 中间件路由保护

### 移动端架构 (app/)

Flutter 移动应用，面向顾客：
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

1. **app/ 是独立的 Flutter 应用**，与 Web 管理后台是独立的代码库
2. **Backend 开发顺序**: entity → repository/mapper → service → controller
3. **MySQL 在 Docker 中运行**，执行 SQL 需要连接容器
4. **RBAC 权限**: 新增 API 需要配置对应菜单权限和角色关联
5. **动态菜单**: Web 管理后台菜单从 `/api/menu/list` 获取，根据用户角色动态加载
6. **认证拦截**: 所有需要认证的 API 都会被 `AuthInterceptor` 拦截
7. **权限注解**: 使用 `@RequireRole("ADMIN")` 或 `@RequirePermission("user:create")` 控制访问
8. **Web 项目结构**: `apps/front-web/` 使用 Monorepo 结构，共享 packages 中的类型和工具
