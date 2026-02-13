# AGENTS

本文件面向在 xWallet 仓库内执行任务的智能体（编码、重构、测试、排障）。

## 1) 仓库现实结构（先读）

当前仓库是多项目同仓，不是标准 pnpm workspace 根工程：

- `front-web/`：Next.js + TypeScript 管理后台
- `backend/`：Spring Boot + MyBatis API
- `app/`：Flutter 客户端

注意：仓库根目录当前没有 `package.json`、`pnpm-workspace.yaml`、`turbo.json`。
不要假设可在根目录直接执行统一 `pnpm build/test/lint`。

## 2) 构建 / Lint / 测试命令（含单测）

### front-web（`front-web/package.json`）

```bash
cd front-web
pnpm install
pnpm dev
pnpm build
pnpm start
pnpm lint
pnpm test
pnpm test:e2e
```

单测/定向测试：

```bash
cd front-web
pnpm test -- --testNamePattern="login"
pnpm test -- src/components/__tests__/LoginForm.test.tsx
pnpm test -- --watch
pnpm test -- --coverage
```

说明：Jest 覆盖率阈值为全局 80%（见 `front-web/jest.config.js`）。

### backend（`backend/pom.xml` + `backend/README.md`）

```bash
cd backend
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=dev
mvn clean package -DskipTests
java -jar target/xwallet-backend-1.0.0.jar --spring.profiles.active=dev
mvn test
```

单测/定向测试：

```bash
cd backend
mvn test -Dtest=UserServiceTest
mvn test -Dtest=MenuE2ETest
mvn test -Dtest=UserServiceTest#testCreateUser
```

说明：测试阶段挂载了 JaCoCo 报告（见 `backend/pom.xml`）。

### app（`app/pubspec.yaml`）

```bash
cd app
flutter pub get
flutter run
flutter run -d android
flutter run -d ios
flutter test
flutter analyze
```

单测/定向测试：

```bash
cd app
flutter test test/models/analytics_event_test.dart
flutter test -n "login"
```

## 3) 代码风格与工程约定

### front-web（TS/React）

- TypeScript `strict: true`（`front-web/tsconfig.json`）。
- 路径别名使用 `@/*`（`front-web/tsconfig.json`）。
- ESLint 使用 `eslint-config-next`（`front-web/eslint.config.mjs`）。
- UI 基础组件模式：`React.forwardRef` + `cva` + `cn`（例：`front-web/src/components/ui/button.tsx`）。
- 状态管理：Zustand；服务端数据：SWR（例：`front-web/src/lib/stores/auth-store.ts`, `front-web/src/app/[locale]/(dashboard)/users/page.tsx`）。
- API 调用统一走封装客户端，自动带 JWT，401 触发登出与重定向（`front-web/src/lib/api/client.ts`）。
- 导入分组遵循：第三方 -> `@/` 内部 -> 相对路径；并保持“文件内已有风格一致”。
- 现状存在单双引号混用（UI 文件多双引号、业务文件多单引号），修改时优先保持目标文件原风格。

### backend（Java/Spring）

- 分层结构稳定：`controller -> service -> repository`。
- DTO/实体位于 `model/dto`, `model/entity`；响应统一 `ResponseResult<T>`。
- 控制器普遍使用 `try/catch + log + ResponseResult.error(...)`（例：`backend/src/main/java/com/zerofinance/xwallet/controller/UserController.java`）。
- 业务规则在 service 层实现，关键写操作配合 `@Transactional`（例：`UserServiceImpl`）。
- 权限控制通过注解与拦截器（`@RequirePermission`, `AuthInterceptor`, `PermissionInterceptor`）。

### app（Flutter/Dart）

- 使用 `flutter_lints`（`app/analysis_options.yaml`）。
- 状态管理为 Provider（`app/lib/main.dart` 与 providers 目录）。
- 网络与埋点逻辑在 `services/`，模型在 `models/`。
- UI 常量与主题颜色已有既定视觉风格，新增页面应保持一致。

## 4) 命名与类型建议（跨项目）

- 组件类名：PascalCase；函数/变量：camelCase；常量：SCREAMING_SNAKE_CASE。
- TS 中对象结构优先 `interface`，复杂联合可用 `type`。
- 不要引入无必要的 `any`；已有历史 `as any` 仅限兼容场景，新增代码避免复制。

## 5) 错误处理与安全底线

- 前端：统一抛出可读错误信息，401 必须清理鉴权态。
- 后端：不要把内部堆栈直接透传给客户端。
- 不提交任何 `.env`、`.env.local`、密钥或真实凭据。

## 6) 环境变量与运行前提

- backend：使用 `backend/.env`（spring-dotenv 自动加载）。
- front-web：可用 `NEXT_PUBLIC_API_URL` 覆盖默认 API 地址。
- 运行要求：Node >= 18，pnpm，JDK 17，Flutter 3.10+，MySQL 8。

## 7) Cursor / Copilot 规则

已检查以下位置：

- `.cursor/rules/`
- `.cursorrules`
- `.github/copilot-instructions.md`

当前仓库未发现上述规则文件。若后续新增，请在本节同步摘要关键约束。

## 8) 给智能体的执行原则

- 先确认目标子项目，再执行命令；不要在仓库根目录盲跑脚本。
- 优先小步修改并就地验证（至少跑相关 lint/test）。
- 涉及跨端联调时，先保证 backend API 可用，再推进 front-web/app。
- 对现有风格不统一区域，遵循“就近一致、最小扰动”策略。

## 9) Superpowers 技能使用策略（按需加载）

- 会话启动时不自动 `bootstrap`，默认不加载任何 Superpowers 技能。
- 仅在识别到明确场景时提出技能建议，且加载前必须获得用户确认（除非用户已明确要求）。
- 用户拒绝加载后，必须继续完成当前任务，不得以“最佳实践”为由强制使用技能。
- 技能仅在对应阶段生效；存在冲突时以后加载技能为主，不同时由多个冲突技能主导。
- 建议加载技能时，必须附带一条可直接复制执行的命令，并按当前 shell 输出代码块格式。

详细规范与示例见：`HOW-TO-USE-SUPERPOWERS-SKILL.md`。
