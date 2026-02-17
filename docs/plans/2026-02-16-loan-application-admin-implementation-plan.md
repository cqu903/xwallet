# 贷款申请单据管理实施计划（Front-web + Backend Admin API）

**日期**: 2026-02-16  
**来源设计**: `docs/plans/2026-02-16-loan-application-admin-design.md`  
**目标**: 在管理后台交付“贷款申请单据只读台账”，支持筛选、分页、抽屉详情与合同明文查看

## 1. 实施范围

- `front-web`
  - 新增贷款申请台账页面：`/loan/applications`
  - 新增 admin API 封装（列表 + 详情）
  - 新增页面与 API 单测
- `backend`
  - 新增 admin 查询接口：
    - `GET /admin/loan/applications`
    - `GET /admin/loan/applications/{applicationId}`
  - 新增读取权限点：`loan:application:read`

## 2. 前置依赖

1. 后端提供可联调的 admin 接口（含 Swagger）。
2. 菜单侧配置可见路径 `/loan/applications`（与权限同步）。
3. 返回字段与设计文档第 6 节保持一致（尤其状态枚举与合同字段）。

## 3. 里程碑与任务拆分

### M1：Backend Admin 查询接口（1-2 天）

1. DTO 与 Controller
- 新增 `LoanApplicationAdminQueryRequest`
- 新增 `LoanApplicationAdminItemResponse`
- 新增 `LoanApplicationAdminDetailResponse`
- 新增 `LoanApplicationAdminController`

2. Service 与 Repository
- `LoanApplicationService` 增加 admin 查询方法
- `LoanApplicationMapper` 增加分页与总数查询
- 联表 `loan_contract_document` 返回合同状态与合同号

3. 权限与异常处理
- 接口加 `@RequirePermission("loan:application:read")`
- 对齐当前后台 `ResponseResult` 错误返回风格

完成标准：
- Swagger 可见接口与参数
- Postman 可分页查询并拉取详情
- 无权限时返回 403

### M2：Front API 封装与类型（0.5 天）

1. 新增文件
- `front-web/src/lib/api/loan-applications-admin.ts`

2. 方法与类型
- `fetchAdminLoanApplications(params)`
- `fetchAdminLoanApplicationDetail(applicationId)`
- 统一 `unwrap` 逻辑，复用 `client.ts`

3. 单测
- `front-web/src/__tests__/lib/api/loan-applications-admin.test.ts`
- 覆盖参数拼接、成功/失败解包、详情查询

完成标准：
- API 封装可被页面直接调用
- API 单测通过

### M3：Front 台账页面（1-1.5 天）

1. 新增页面
- `front-web/src/app/[locale]/(dashboard)/loan/applications/page.tsx`

2. 列表能力
- 筛选区：申请编号、客户 ID、申请状态、风险决策、合同号、合同状态、时间区间
- 列表列：申请编号、客户、状态、风险、核准金额、合同状态、创建时间、更新时间、操作
- 分页：`page + PAGE_SIZE`

3. 数据流
- 列表使用 `useSWR(['admin-loan-applications', queryParams], ...)`
- 保持“输入态/提交态”双状态，避免输入即请求

完成标准：
- 具备加载态、空态、错误态、分页能力
- 与现有 `loan/transactions` 页面交互风格一致

### M4：详情抽屉与状态映射（0.5-1 天）

1. 抽屉详情
- 点击“查看详情”打开右侧抽屉
- 按需加载详情数据（延迟请求）
- 分区展示：基本信息、风险财务、时间轴、合同信息

2. 状态映射
- 申请状态：`SUBMITTED/REJECTED/APPROVED_PENDING_SIGN/SIGNED/DISBURSED/EXPIRED`
- 合同状态：`DRAFT/SIGNED`
- 未知状态原值回显

完成标准：
- 详情失败仅影响抽屉，不阻断列表
- 合同正文可完整查看

### M5：菜单、回归与验收（0.5 天）

1. 菜单接入
- 确保后端菜单返回中存在 `/loan/applications` 项
- 前端 Sidebar 自动展示并可访问

2. 回归测试
- 回归 `loan/transactions` 页面基本可用性
- 验证 401 自动登出与跳转逻辑未回归

3. 验收记录
- 记录样例查询参数、详情展示截图、错误场景处理结果

完成标准：
- 功能可由运营账号完整走通
- 回归无阻塞问题

## 4. 风险与缓解

1. 后端分页字段/命名不一致
- 缓解：先冻结接口契约，前后端联调前做字段对齐清单。

2. 状态枚举新增导致前端显示异常
- 缓解：所有状态显示加默认兜底（原值回显 + 中性样式）。

3. 合同内容较长影响抽屉可读性
- 缓解：合同区使用可滚动容器与等宽排版，避免撑破布局。

4. 权限未配置导致菜单可见但接口 403
- 缓解：联调时同时校验菜单权限与接口权限，提供最小权限角色样例。

## 5. 验收标准（Definition of Done）

1. 管理后台可通过 `/loan/applications` 访问台账页面。
2. 筛选、分页、列表展示符合接口返回。
3. 抽屉详情展示完整申请与合同信息（含 `hkid`、`contractContent`）。
4. 列表/详情在加载、空数据、错误场景均有明确反馈。
5. 新增测试通过，且不影响既有贷款交易管理能力。

## 6. 执行与验证命令

后端（联调前）：

```bash
cd backend
mvn test -Dtest=LoanApplication*Test
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

前端（开发与回归）：

```bash
cd front-web
pnpm test -- src/__tests__/lib/api/loan-applications-admin.test.ts
pnpm test -- src/app/[locale]/(dashboard)/loan/applications/__tests__/page.test.tsx
pnpm lint
pnpm build
```

## 7. 交付清单

- 设计文档：`docs/plans/2026-02-16-loan-application-admin-design.md`
- 实施计划：`docs/plans/2026-02-16-loan-application-admin-implementation-plan.md`
- `front-web` 页面/API/测试代码
- `backend` admin 查询接口代码与权限配置
