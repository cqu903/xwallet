# AGENTS

本文档为在 xWallet 代码库中工作的 AI 智能体提供指导规范。

## 项目概述

xWallet 是一个多平台钱包应用 monorepo，使用 pnpm workspaces + Turborepo 编排：

- **app/**: Flutter 移动应用（Android/iOS），面向顾客，邮箱登录
- **front-web/**: Next.js 16 Web 管理后台，面向系统员工，工号登录
- **backend/**: Spring Boot 3.3.0 后端 API 服务器（Java 17 + MyBatis）

```
xwallet/
├── app/                  # Flutter 移动应用
├── front-web/            # Next.js Web 管理后台
├── backend/              # Spring Boot 后端
├── package.json          # 根 package.json
├── pnpm-workspace.yaml   # pnpm workspace 配置
└── turbo.json            # Turborepo 配置
```

## 构建、lint 和测试命令

### 根目录命令

```bash
pnpm install              # 安装所有依赖
pnpm dev                  # 启动 front-web 开发服务器
pnpm build                # 构建所有 packages
pnpm test                 # 在所有 packages 中运行测试
pnpm lint                 # 在所有 packages 中运行 lint
pnpm format               # 使用 Prettier 格式化代码
pnpm clean                # 清理构建产物和 node_modules
```

### Front-Web 命令

```bash
cd front-web
pnpm dev                  # 启动 Next.js 开发服务器 (http://localhost:3000)
pnpm build                # 构建生产版本
pnpm lint                 # 运行 ESLint
pnpm test                 # 运行 Jest 测试
pnpm test --watch         # 以 watch 模式运行测试
pnpm test --coverage      # 运行测试并生成覆盖率报告
pnpm test --testNamePattern="login"  # 运行单个测试
```

### Backend 命令

```bash
cd backend
mvn clean install           # 构建项目
mvn spring-boot:run -Dspring-boot.run.profiles=dev  # 开发环境运行
mvn clean package -DskipTests && java -jar target/xwallet-backend-1.0.0.jar  # 打包运行
mvn test                    # 运行测试
```

### Flutter App 命令

```bash
cd app
flutter pub get             # 安装依赖
flutter run                 # 运行应用
flutter run -d android      # 在 Android 设备/模拟器运行
flutter run -d ios          # 在 iOS 设备/模拟器运行
flutter test                # 运行测试
flutter analyze             # 代码分析
```

## 代码风格规范

### TypeScript

- 所有代码使用 **TypeScript strict 模式**
- 对象类型优先使用 **interfaces**，联合类型/原始类型使用 **types**
- 使用 `unknown` 代替 `any`，除非绝对必要

### 命名规范

| 类型  | 规范                                   | 示例                                 |
| ----- | -------------------------------------- | ------------------------------------ |
| 组件  | PascalCase                             | `Button`, `DashboardLayout`          |
| Hooks | use 前缀的 camelCase                   | `useAuthStore`, `useApi`             |
| 变量  | camelCase                              | `userPermissions`, `isAuthenticated` |
| 常量  | SCREAMING_SNAKE_CASE                   | `API_BASE_URL`                       |
| 文件  | utils 用 kebab-case，组件用 PascalCase | `auth-store.ts`, `Button.tsx`        |

### 导入顺序

1. 内置/Node.js 导入
2. 第三方库导入
3. `@/` 开头的绝对路径导入（项目内部导入）
4. 相对导入 (`./`, `../`)

### 组件规范

- 使用 **函数式组件** 配合 TypeScript 接口
- 需要 ref 转发的组件使用 `React.forwardRef`
- 使用 **class-variance-authority (CVA)** 实现组件变体
- 使用 **Radix UI** 原语实现可访问的交互组件
- 目录结构遵循：`components/ui/` 存放基础组件，`components/[feature]/` 存放功能组件

### 状态管理

- 全局客户端状态使用 **Zustand**（配合 persist 中间件实现 localStorage 持久化）
- 局部组件状态使用 React **useState/useReducer**
- 服务端状态和数据获取使用 **SWR**

### 错误处理

- 异步操作使用 **try-catch** 并指定具体错误类型
- 401 未授权时清除认证状态并跳转登录页
- 生产环境切勿向客户端暴露内部错误详情

### API 层

- 使用带类型的 fetch 封装器，自动注入 JWT
- 按资源拆分 API 函数：`api/users.ts`、`api/auth.ts`、`api/menu.ts`

### 样式

- 使用 **Tailwind CSS v4** 配合 `@tailwindcss/postcss`
- 使用 `@/lib/utils` 中的 `cn()` 工具函数合并类名
- 通过 `cn()` 工具使用 `clsx` 和 `tailwind-merge`

### Prettier 配置

```json
{
  "semi": true,
  "singleQuote": true,
  "tabWidth": 2,
  "trailingComma": "es5",
  "printWidth": 100,
  "arrowParens": "always",
  "endOfLine": "lf"
}
```

运行 `pnpm format` 自动格式化代码。

### ESLint

配置位于 `front-web/eslint.config.mjs`，使用 `eslint-config-next` + TypeScript。

### 目录结构

```
front-web/src/
├── app/[locale]/           # Next.js App Router 页面
│   ├── (dashboard)/        # Dashboard 路由组
│   ├── login/
│   └── layout.tsx
├── components/
│   ├── ui/                 # 基础 UI 组件
│   └── [feature]/          # 功能组件
├── lib/
│   ├── api/                # API 客户端和端点
│   ├── stores/             # Zustand store
│   ├── hooks/              # 自定义 React Hooks
│   ├── i18n/               # 国际化
│   └── utils.ts            # 工具函数
└── middleware.ts           # Next.js 中间件

backend/src/main/java/com/zerofinance/xwallet/
├── config/                 # 配置类
│   ├── WebConfig.java      # CORS 配置
│   ├── AuthInterceptor.java # JWT 认证拦截器
│   └── CacheConfig.java    # Caffeine 缓存配置
├── controller/             # REST API 控制器
├── service/                # 业务逻辑层
├── repository/             # MyBatis Mapper 接口
├── model/
│   ├── entity/             # 数据库实体
│   └── dto/                # 数据传输对象
├── util/                   # 工具类
└── annotation/             # 权限注解
```

## 环境变量

- Backend: 使用 `backend/.env` 文件（spring-dotenv 自动加载）
- Front-web: 使用 `NEXT_PUBLIC_` 前缀，API URL 存储在 `NEXT_PUBLIC_API_URL`
- 切勿将 `.env.local` 或 `.env` 提交到版本控制

## 环境要求

- Node.js >= 18.0.0
- pnpm >= 8.0.0（使用 pnpm 9.15.0）
- JDK 17（后端）
- Flutter 3.10+（移动端）
- MySQL 8.x（数据库）

## 测试账号

| 类型     | 账号                 | 密码        | 说明         |
| -------- | -------------------- | ----------- | ------------ |
| 系统用户 | ADMIN001             | admin123    | Web 管理后台 |
| 顾客     | customer@example.com | customer123 | 移动端 App   |
