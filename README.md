# xWallet

xWallet 是一个面向贷款钱包业务的多项目同仓仓库，包含顾客移动端、管理后台、后端 API，以及配套的产品与实施文档。

这个仓库不是根目录统一 workspace。开发、构建、测试都应进入对应子项目目录执行。

## 项目内容

xWallet 覆盖了贷款业务的核心链路：

- 顾客注册、登录
- 贷款申请、审批后签约
- 首次放款、再次提款、还款
- 交易记录与账户额度查询
- 还款清分与额度恢复
- 管理后台的用户、角色、权限、菜单管理
- 顾客管理、贷款申请管理、贷款交易管理
- 贷后催收任务与跟进记录
- Flutter App 埋点采集与后台事件查询

## 仓库结构

```text
xwallet/
├── front-web/   # Next.js 管理后台
├── backend/     # Spring Boot + MyBatis API
├── app/         # Flutter 移动端
├── docs/        # 产品、设计、实施、测试文档
└── README.md
```

## 子项目说明

### `front-web/`

管理后台，面向系统管理员和运营角色。

当前代码配置显示：

- Next.js 16
- React 19
- TypeScript
- Tailwind CSS 4
- Zustand
- SWR
- next-intl

主要页面包括：

- 仪表盘
- 用户管理
- 顾客管理
- 贷款申请管理
- 贷款交易管理
- 催收任务管理
- 角色 / 权限 / 菜单管理
- MQTT 事件查询

### `backend/`

统一业务后端，负责鉴权、权限、贷款域、交易域、贷后与埋点能力。

当前代码配置显示：

- Java 21
- Spring Boot 3.3.0
- MyBatis 3.0.3
- MySQL 8
- Redis
- MQTT
- SpringDoc / Swagger UI

主要接口领域包括：

- 认证与注册
- 用户、角色、权限、菜单
- 顾客管理
- 贷款申请
- 贷款交易与还款
- 还款计划与支付记录
- 催收任务与催收记录
- 埋点事件查询

服务默认运行在：

- `http://localhost:8080/api`
- Swagger UI: `http://localhost:8080/api/swagger-ui.html`

### `app/`

Flutter 顾客端应用。

当前代码配置显示：

- Flutter SDK `^3.10.4`
- Provider
- HTTP
- shared_preferences
- MQTT Client
- sqflite

主要页面/流程包括：

- 登录 / 注册
- 首页
- 钱包账户页
- 交易记录页
- 贷款申请向导
- 合同列表
- 还款页
- 个人中心

## 核心业务规则

当前仓库文档显示，贷款交易是本项目的主业务域：

- 合同签署成功后触发首放
- 首放后可用额度归零
- 还款默认先还利息，再还本金
- 只有归还本金才会恢复可用额度
- 再次提款不得超过可用额度
- 历史交易流水不可物理修改，纠错通过冲正完成

详细说明见：

- [docs/product/loan-transaction-product-spec.md](docs/product/loan-transaction-product-spec.md)
- [docs/features/repayment-feature.md](docs/features/repayment-feature.md)
- [docs/ANALYTICS_SPEC.md](docs/ANALYTICS_SPEC.md)

## 开发前提

- Node.js >= 18
- npm
- Java 21
- Flutter SDK `^3.10.4`
- MySQL 8

## 快速开始

### 1. 管理后台

```bash
cd front-web
npm install
npm run dev
```

默认访问：

- `http://localhost:3000`

可选环境变量：

```env
NEXT_PUBLIC_API_URL=http://localhost:8080/api
NEXT_PUBLIC_APP_NAME=xWallet
```

### 2. 后端 API

```bash
cd backend
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

后端通过 `backend/.env` 配置数据库、邮件、JWT、MQTT 等环境变量。

### 3. Flutter App

```bash
cd app
flutter pub get
flutter run
```

## 常用命令

### `front-web/`

```bash
cd front-web
npm run dev
npm run build
npm start
npm run lint
npm test
npm run test:e2e
```

### `backend/`

```bash
cd backend
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=dev
mvn clean package -DskipTests
mvn test
```

### `app/`

```bash
cd app
flutter pub get
flutter run
flutter analyze
flutter test
```

## 测试账号

### 管理后台

- 工号：`ADMIN001`
- 密码：`admin123`
- 角色：`ADMIN`

### 顾客端

- 顾客邮箱：见 `backend/database/init_all.sql` 中 `customer` 表初始化数据
- 密码：`customer123`

仅用于开发和测试环境。

## 重要约定

- 不要在仓库根目录盲目执行 `npm build`、`npm test`、`mvn test` 等命令。
- 所有命令都应在对应子项目目录执行。
- 修改时应保持各子项目原有架构边界稳定。
- 涉及鉴权、权限、401 处理、交易流水与额度规则时，不应破坏既有安全与业务约束。

## 相关文档

- [QUICKSTART.md](QUICKSTART.md)
- [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md)
- [docs/](docs/)
- [front-web/README.md](front-web/README.md)
- [backend/README.md](backend/README.md)
- [app/README.md](app/README.md)

## 说明

仓库内部分历史 README 与当前代码配置可能存在版本差异。实际开发请优先以对应子项目的配置文件为准，例如：

- `front-web/package.json`
- `backend/pom.xml`
- `app/pubspec.yaml`
