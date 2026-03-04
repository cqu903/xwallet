# AGENTS

本文件面向在 `xwallet` 仓库内执行任务的智能体（编码、重构、测试、排障）。
在开始修改前，请先阅读并遵守以下约定。

## 1) 仓库结构与工作边界
- 该仓库是多项目同仓，不是根目录统一 workspace。
- 子项目：
  - `front-web/`：Next.js + TypeScript 管理后台
  - `backend/`：Spring Boot + MyBatis API
  - `app/`：Flutter 移动端
- 不要假设可以在仓库根目录执行统一 `pnpm build/test/lint`。
- 一切命令都应在对应子项目目录执行。

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
单测与定向测试：
```bash
cd front-web
pnpm test -- src/components/__tests__/LoginForm.test.tsx
pnpm test -- src/__tests__/lib/api/users.test.ts
pnpm test -- --testNamePattern="login"
pnpm test -- --watch
pnpm test -- --coverage
```
说明：
- Jest 配置在 `front-web/jest.config.js`。
- 全局覆盖率阈值为 80%（branches/functions/lines/statements）。
- E2E 使用 Playwright（`pnpm test:e2e`）。
### backend（`backend/pom.xml`）
```bash
cd backend
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=dev
mvn clean package -DskipTests
java -jar target/xwallet-backend-1.0.0.jar --spring.profiles.active=dev
mvn test
```
单测与定向测试：
```bash
cd backend
mvn test -Dtest=UserServiceTest
mvn test -Dtest=MenuE2ETest
mvn test -Dtest=UserServiceTest#testCreateUser
mvn test -Dtest=AuthControllerTest
```
说明：
- JaCoCo 在 `verify` 阶段执行报告与门禁检查。
- `pom.xml` 中对 `controller` / `service` 包设置了高覆盖率要求。
- 若环境中 `mvn` 不可直接用，使用机器已配置的 Maven 可执行路径。
### app（`app/pubspec.yaml`）
```bash
cd app
flutter pub get
flutter run
flutter run -d android
flutter run -d ios
flutter analyze
flutter test
```
单测与定向测试：
```bash
cd app
flutter test test/models/analytics_event_test.dart
flutter test test/providers/loan_application_provider_test.dart
flutter test -n "login"
```

## 3) 代码风格与架构约定
### front-web（TypeScript / React）
- TypeScript 采用严格模式（`front-web/tsconfig.json` 中 `strict: true`）。
- 路径别名为 `@/*` -> `src/*`，优先使用别名而非过深相对路径。
- ESLint 基于 `eslint-config-next`（core-web-vitals + typescript）。
- 导入顺序建议：第三方 -> `@/` 内部模块 -> 相对路径。
- 修改时保持“就近一致”，不要顺手重排不相关代码风格。
- 基础 UI 组件遵循现有模式：`forwardRef` + `cva` + `cn`。
- 客户端状态管理使用 Zustand，远程数据获取/缓存使用 SWR。
- API 调用统一走 `src/lib/api/client.ts`，避免散落直接 `fetch`。
- 鉴权约定：自动带 JWT；遇到 401 需清理登录态并跳转登录页。
- 表单校验尽量复用集中校验逻辑（如 `src/lib/utils/validation`）。
### backend（Java / Spring Boot）
- 保持三层职责清晰：`controller -> service -> repository`。
- DTO 与实体位置固定：`model/dto`、`model/entity`。
- 统一响应包装：`ResponseResult<T>`。
- Controller 层按现有风格记录日志、捕获异常并返回安全错误信息。
- 业务规则与事务写操作应落在 service 层（`@Transactional`）。
- 权限体系基于注解与拦截器（`@RequirePermission` 等）。
- 遵循现有包结构：`com.zerofinance.xwallet`。
- 禁止将内部堆栈、SQL 细节等敏感信息直接返回前端。
### app（Flutter / Dart）
- Lint 规则来源于 `flutter_lints`（`app/analysis_options.yaml`）。
- 状态管理模式为 Provider（`ChangeNotifierProvider`）。
- 网络与埋点逻辑放在 `services/`，模型放在 `models/`。
- `*.g.dart` 属于生成文件，应修改源文件后重新生成，不直接手改。
- UI 与主题风格保持现有一致性，避免无必要视觉偏移。

## 4) 类型、命名、格式化规则（跨项目）
- 命名约定：类型/类/组件用 PascalCase；变量/函数/方法用 camelCase；常量用 SCREAMING_SNAKE_CASE。
- TypeScript：
  - 对象结构优先 `interface`，联合/映射/工具类型优先 `type`。
  - 避免新增 `any`；确需使用时限制范围并说明原因。
- Java：
  - 方法命名使用动词短语，语义明确。
  - 参数校验尽量前置，异常语义与业务场景匹配。
- Dart：
  - 优先不可变数据；复杂 Widget 树拆分为小组件或私有构建函数。
- 格式化：
  - 优先遵循目标文件当前风格。
  - 非任务要求下，不做大范围“纯格式化”改动。

## 5) 错误处理与安全底线
- 前端/移动端：对用户展示可理解错误，不暴露内部实现细节。
- 后端：日志记录完整上下文，对外返回脱敏后的错误消息。
- 严禁提交敏感信息：`.env`、`.env.local`、密钥、令牌、真实凭据。
- 涉及鉴权/权限逻辑时，必须保留现有安全行为（如 401 清理会话）。

## 6) 环境变量与运行前提
- front-web：可用 `NEXT_PUBLIC_API_URL` 覆盖默认后端地址。
- backend：通过 `backend/.env` + `spring-dotenv` 自动加载。
- 当前仓库现实依赖：
  - Node.js >= 18
  - pnpm
  - Java 21（以 `backend/pom.xml` 为准）
  - Flutter SDK（`sdk: ^3.10.4`）
  - MySQL 8

## 7) Cursor / Copilot 规则检查
已检查路径：
- `.cursor/rules/`
- `.cursorrules`
- `.github/copilot-instructions.md`

当前仓库未发现上述规则文件。
如后续新增，请在本节补充摘要并将其纳入执行约束。

## 8) 给智能体的执行清单
- 先确认目标子项目，再执行命令，避免在根目录盲跑脚本。
- 采用小步改动策略，避免无关文件噪音。
- 完成改动后，至少执行与改动最相关的 lint/test。
- 跨端联调优先保证 backend 接口行为正确，再推进前端/移动端。
- 除非任务明确要求重构，否则保持现有架构与边界稳定。
