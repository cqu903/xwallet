# Front-Web 管理后台重构设计文档

**日期**: 2026-01-24
**状态**: 设计阶段
**目标**: 使用 React.js 技术栈重构 Flutter Web 管理后台

## 1. 概述

### 1.1 项目背景

现有的 `front/` 项目使用 Flutter Web 构建，存在以下问题：
- Flutter 在 Web 管理后台场景下性能不佳
- 生态相对较弱，UI 组件选择有限
- 包体积较大，首屏加载慢

### 1.2 重构目标

- 使用现代 React 技术栈提升开发体验和性能
- 保持功能完全一致，实现无缝迁移
- 建立完善的测试和代码规范体系
- 支持国际化和主题切换

## 2. 技术栈

### 2.1 核心技术

| 技术 | 版本 | 用途 |
|------|------|------|
| Next.js | 14+ | React 框架，使用 App Router |
| React | 18+ | UI 库 |
| TypeScript | 5+ | 类型安全 |
| Tailwind CSS | 3+ | 样式框架 |
| shadcn/ui | latest | UI 组件库 |

### 2.2 状态和数据管理

| 技术 | 用途 |
|------|------|
| Zustand | 客户端状态管理（认证、菜单、布局） |
| SWR | 服务端状态管理（API 数据获取和缓存） |
| React Hook Form | 表单状态管理 |
| Zod | 表单验证 |

### 2.3 开发工具

| 技术 | 用途 |
|------|------|
| Turborepo | Monorepo 构建工具 |
| pnpm | 包管理器 |
| ESLint + Prettier | 代码规范 |
| Husky + lint-staged | Git Hooks |
| Jest + Testing Library | 单元测试 |
| Playwright | E2E 测试 |
| MSW | API Mock |

### 2.4 其他库

| 技术 | 用途 |
|------|------|
| next-intl | 国际化（中文/英文） |
| next-themes | 主题切换（亮/暗模式） |
| Axios | HTTP 客户端（备用，主要使用 Fetch） |
| clsx / cn | 类名合并工具 |

## 3. 项目架构

### 3.1 Monorepo 结构

```
xwallet/
├── apps/
│   ├── backend/          # Spring Boot 后端（现有）
│   ├── front/            # Flutter Web（保留，不维护）
│   └── front-web/        # Next.js 前端（新建）
├── packages/
│   ├── shared-types/     # 共享 TypeScript 类型定义
│   ├── shared-utils/     # 共享工具函数
│   └── eslint-config/    # 共享 ESLint 配置
├── turbo.json            # Turborepo 配置
└── pnpm-workspace.yaml   # pnpm workspace 配置
```

### 3.2 架构层次

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│  (Next.js App Router + Server/Client)  │
├─────────────────────────────────────────┤
│         State Management Layer          │
│         (Zustand Stores)                │
├─────────────────────────────────────────┤
│           Service Layer                 │
│    (SWR Hooks + API Clients)            │
├─────────────────────────────────────────┤
│            Data Layer                   │
│     (TypeScript Types from shared-types)│
└─────────────────────────────────────────┘
```

## 4. 目录结构

### 4.1 front-web 详细结构

```
apps/front-web/
├── src/
│   ├── app/                          # Next.js App Router
│   │   ├── [locale]/                 # 国际化路由
│   │   │   ├── (auth)/              # 认证路由组
│   │   │   │   └── login/
│   │   │   │       └── page.tsx     # 登录页面
│   │   │   ├── (dashboard)/         # 主应用路由组
│   │   │   │   ├── layout.tsx       # 侧边栏布局
│   │   │   │   ├── dashboard/
│   │   │   │   │   └── page.tsx     # 仪表盘
│   │   │   │   ├── users/
│   │   │   │   │   ├── page.tsx     # 用户列表
│   │   │   │   │   └── [id]/
│   │   │   │   │       └── page.tsx # 用户详情
│   │   │   │   └── system/
│   │   │   │       └── roles/
│   │   │   │           └── page.tsx # 角色管理
│   │   │   └── layout.tsx           # 根布局（Provider）
│   │   ├── api/                     # API 路由（可选）
│   │   └── globals.css              # 全局样式
│   ├── components/                   # React 组件
│   │   ├── ui/                      # shadcn/ui 组件
│   │   ├── layout/                  # 布局组件
│   │   │   ├── Sidebar.tsx
│   │   │   ├── Header.tsx
│   │   │   └── SidebarItem.tsx
│   │   ├── auth/                    # 认证相关
│   │   │   └── ProtectedRoute.tsx
│   │   └── features/                # 功能组件
│   │       ├── users/
│   │       │   ├── UserTable.tsx
│   │       │   └── UserForm.tsx
│   │       └── roles/
│   │           └── RoleTable.tsx
│   ├── lib/                         # 工具库
│   │   ├── api/                     # API 客户端
│   │   │   ├── client.ts            # Fetch 封装
│   │   │   ├── auth.ts              # 认证 API
│   │   │   └── users.ts             # 用户 API
│   │   ├── stores/                  # Zustand stores
│   │   │   ├── auth-store.ts
│   │   │   ├── menu-store.ts
│   │   │   └── layout-store.ts
│   │   ├── hooks/                   # 自定义 Hooks
│   │   │   ├── use-auth.ts
│   │   │   └── use-permission.ts
│   │   ├── i18n/                    # 国际化配置
│   │   │   ├── request.ts
│   │   │   └── config.ts
│   │   └── utils.ts                 # 工具函数
│   ├── middleware.ts                # Next.js 中间件（权限控制）
│   └── types/                       # 本地类型（从 shared-types 导入）
├── public/                          # 静态资源
├── tests/                           # 测试文件
│   ├── unit/
│   └── e2e/
├── .env.local                       # 环境变量
├── next.config.js                   # Next.js 配置
├── tailwind.config.ts               # Tailwind 配置
├── package.json
└── tsconfig.json
```

## 5. 认证和权限系统

### 5.1 认证流程

```
Login Form (Client Component)
  ↓
POST /api/auth/login (lib/api/auth.ts)
  ↓
Response: { token, user, permissions }
  ↓
Zustand auth-store 更新状态
  ↓
Cookie 存储 HttpOnly JWT (安全)
  ↓
LocalStorage 存储用户信息 (非敏感)
  ↓
重定向到 /dashboard
```

### 5.2 JWT 存储策略

- **Access Token**: HttpOnly Cookie（防 XSS）
- **用户信息**: LocalStorage（非敏感数据：姓名、头像）
- **有效期**: 30 分钟
- **刷新策略**: Token 过期前 5 分钟自动刷新

### 5.3 三层权限控制

1. **Middleware 层**（`middleware.ts`）
   - 拦截所有路由请求
   - 验证 JWT 有效性
   - 检查路由级权限
   - 未授权重定向到 `/login`

2. **服务端组件层**（`ProtectedRoute.tsx`）
   - 检查菜单权限
   - 根据用户角色动态渲染菜单

3. **客户端组件层**（`usePermission` Hook）
   - 细粒度权限检查（按钮级、操作级）
   - 示例：`canDeleteUser = usePermission('user:delete')`

### 5.4 权限数据流

```
Backend: /api/auth/login
  ↓ Response: { token, user: { roles, permissions } }
  ↓
Frontend: auth-store (Zustand)
  ↓
├→ middleware.ts (路由守卫)
├→ ProtectedRoute (组件守卫)
└→ usePermission Hook (操作守卫)
```

## 6. 组件架构和数据流

### 6.1 布局组件

```typescript
<DashboardLayout>
  ├─ <Sidebar>
  │   └─ <SidebarItem>
  ├─ <Header>
  └─ {children}
```

### 6.2 功能组件示例（用户管理）

```typescript
<UsersPage>
  ├─ <PageHeader>
  ├─ <UsersTable>
  │   ├─ <DataTable>
  │   ├─ <UserActions>
  │   └─ <Pagination>
  └─ <UserDialog>
      └─ <UserForm>
```

### 6.3 数据获取策略

**服务端组件**（首屏渲染）：
```typescript
async function UsersPage() {
  const users = await fetchUsers();
  return <UsersTable initialData={users} />;
}
```

**客户端组件**（交互更新）：
```typescript
function UsersTable({ initialData }) {
  const { data } = useSWR('/api/users', fetchUsers, {
    fallbackData: initialData
  });
  return <DataTable data={data} />;
}
```

### 6.4 状态管理

| 状态类型 | 管理方案 | 示例 |
|---------|---------|------|
| 全局状态 | Zustand | 用户信息、菜单数据 |
| 服务端状态 | SWR | 用户列表、角色列表 |
| 表单状态 | React Hook Form | 登录表单、用户表单 |
| UI 状态 | useState | Modal 开关、Loading 状态 |

### 6.5 错误处理

- **API 错误**: SWR 统一处理，ErrorBoundary
- **表单错误**: React Hook Form 字段级错误
- **网络错误**: Toast 通知 + 重试按钮

## 7. 国际化和主题

### 7.1 国际化实现

**技术**: next-intl

**目录结构**:
```
lib/i18n/
├── request.ts
├── config.ts
└── locales/
    ├── zh-CN.json
    └── en-US.json
```

**语言切换**: URL 路径前缀
- `/zh-CN/dashboard` - 中文
- `/en/dashboard` - 英文

**使用方式**:
```typescript
const t = useTranslations('auth');
return <Label>{t('employeeNo')}</Label>;
```

### 7.2 主题系统

**技术**: next-themes + Tailwind CSS

**主题变量**:
```css
:root {
  --primary: 221 83% 53%;  /* 蓝色主色调 */
  --background: 0 0% 100%;
}

.dark {
  --primary: 217 91% 60%;
  --background: 222 47% 11%;
}
```

**切换方式**: 顶部栏主题切换按钮

### 7.3 响应式设计

| 设备 | 侧边栏状态 |
|------|-----------|
| 移动端 | 默认收起，点击展开 |
| 平板 | 半展开 |
| 桌面 | 完全展开 |

## 8. 测试策略

### 8.1 单元测试

**技术**: Jest + React Testing Library + MSW

**覆盖范围**:
- 组件渲染测试
- 用户交互测试
- Hooks 测试
- Store 测试

**示例**:
```typescript
test('renders user list', () => {
  render(
    <SWRConfig value={{ fallback: { '/api/users': mockUsers } }}>
      <UserTable />
    </SWRConfig>
  );
  expect(screen.getByText('admin')).toBeInTheDocument();
});
```

### 8.2 E2E 测试

**技术**: Playwright + Chrome DevTools MCP

**测试场景**:
- 登录流程完整性
- 菜单动态加载
- 权限控制正确性
- 国际化切换
- 主题切换

**示例**:
```typescript
test('should login successfully', async ({ page }) => {
  await page.goto('/login');
  await page.fill('[name="employeeNo"]', 'ADMIN001');
  await page.fill('[name="password"]', 'admin123');
  await page.click('button[type="submit"]');
  await expect(page).toHaveURL('/dashboard');
});
```

### 8.3 测试覆盖率目标

- 单元测试覆盖率: ≥ 80%
- 关键流程 E2E 覆盖: 100%

## 9. 代码规范

### 9.1 ESLint 配置

```javascript
{
  "extends": [
    "next/core-web-vitals",
    "plugin:@typescript-eslint/recommended",
    "prettier"
  ],
  "rules": {
    "@typescript-eslint/no-unused-vars": "error",
    "@typescript-eslint/no-explicit-any": "warn",
    "react-hooks/exhaustive-deps": "error",
    "no-console": ["warn", { "allow": ["warn", "error"] }]
  }
}
```

### 9.2 Prettier 配置

```javascript
{
  "semi": true,
  "singleQuote": true,
  "tabWidth": 2,
  "trailingComma": "es5",
  "printWidth": 100
}
```

### 9.3 Git Hooks

**工具**: Husky + lint-staged

**配置**:
```json
{
  "lint-staged": {
    "*.{ts,tsx}": ["eslint --fix", "prettier --write"],
    "*.{json,css}": ["prettier --write"]
  }
}
```

### 9.4 提交规范

使用 Conventional Commits:
- `feat`: 新功能
- `fix`: 修复 Bug
- `refactor`: 重构
- `test`: 测试
- `docs`: 文档

## 10. 环境配置

### 10.1 环境变量

```env
# .env.local
NEXT_PUBLIC_API_URL=http://localhost:8080/api
NEXT_PUBLIC_APP_NAME=xWallet
```

### 10.2 依赖安装

```bash
# 项目根目录
pnpm install

# 单独安装 front-web 依赖
pnpm --filter front-web install
```

### 10.3 开发命令

```bash
# 启动开发服务器
pnpm --filter front-web dev

# 构建生产版本
pnpm --filter front-web build

# 运行测试
pnpm --filter front-web test

# 运行 E2E 测试
pnpm --filter front-web test:e2e

# 代码检查
pnpm --filter front-web lint

# 代码格式化
pnpm --filter front-web format
```

## 11. 实施计划

### 11.1 第一阶段：基础架构搭建
- [ ] 初始化 Next.js 项目
- [ ] 配置 Turborepo 和 pnpm workspace
- [ ] 配置 TypeScript、ESLint、Prettier
- [ ] 配置 Tailwind CSS 和 shadcn/ui
- [ ] 配置 next-intl 国际化
- [ ] 配置 next-themes 主题系统

### 11.2 第二阶段：认证和权限
- [ ] 实现 JWT 认证流程
- [ ] 创建 Zustand stores（auth、menu、layout）
- [ ] 实现 middleware 权限控制
- [ ] 实现 ProtectedRoute 组件
- [ ] 实现登录页面

### 11.3 第三阶段：核心功能迁移
- [ ] 仪表盘页面
- [ ] 用户管理（列表、新增、编辑、删除）
- [ ] 角色管理（列表、新增、编辑、删除、权限配置）
- [ ] 动态菜单系统

### 11.4 第四阶段：测试和优化
- [ ] 编写单元测试
- [ ] 编写 E2E 测试
- [ ] 使用 Chrome DevTools MCP 验证
- [ ] 性能优化
- [ ] 代码审查和重构

### 11.5 第五阶段：部署和文档
- [ ] 配置 CI/CD
- [ ] 编写部署文档
- [ ] 编写组件文档
- [ ] 生产环境验证

## 12. 风险和挑战

### 12.1 技术风险

| 风险 | 影响 | 缓解措施 |
|------|------|---------|
| Next.js 学习曲线 | 中 | 团队培训，参考官方文档 |
| 类型同步问题 | 中 | 使用 shared-types 包 |
| 性能问题 | 低 | 使用服务端组件优化首屏 |

### 12.2 业务风险

| 风险 | 影响 | 缓解措施 |
|------|------|---------|
| 功能遗漏 | 高 | 详细功能对比测试 |
| 数据不一致 | 高 | 使用相同 API 接口 |
| 用户体验下降 | 中 | 保持 UI 一致性 |

## 13. 成功标准

- [ ] 所有现有功能完整迁移
- [ ] 单元测试覆盖率 ≥ 80%
- [ ] 关键流程 E2E 测试通过
- [ ] 首屏加载时间 < 2 秒
- [ ] 代码规范检查 100% 通过
- [ ] 生产环境稳定运行 1 个月

## 14. 附录

### 14.1 参考文档

- [Next.js Documentation](https://nextjs.org/docs)
- [shadcn/ui Documentation](https://ui.shadcn.com)
- [Zustand Documentation](https://docs.pmnd.rs/zustand)
- [SWR Documentation](https://swr.vercel.app)
- [next-intl Documentation](https://next-intl-docs.vercel.app)

### 14.2 相关设计文档

- [用户管理设计](./2025-01-21-user-management-design.md)
- [MQTT 埋点系统设计](./2026-01-22-mqtt-analytics-design.md)

---

**文档版本**: 1.0
**最后更新**: 2026-01-24
**作者**: Claude Code
**审核状态**: 待审核
