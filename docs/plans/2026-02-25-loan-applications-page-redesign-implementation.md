# 贷款申请单据页面美化实现计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 美化贷款申请单据页面，使其与整体项目的精致商务风格一致。

**Architecture:** 渐进增强方案 - 在现有 React 组件结构上添加视觉效果，不改变核心逻辑。使用 Tailwind CSS v4 类名和现有的 shadcn/ui 组件。

**Tech Stack:** Next.js 16, React 19, TypeScript, Tailwind CSS v4, shadcn/ui, SWR

---

## Task 1: 添加 Collapsible 组件依赖

**Files:**
- Create: `front-web/src/components/ui/collapsible.tsx`
- Reference: `front-web/src/components/ui/` (检查现有 UI 组件模式)

**Step 1: 检查是否已有 Collapsible 组件**

Run: `ls front-web/src/components/ui/collapsible.tsx`
Expected: 文件不存在

**Step 2: 使用 shadcn-ui CLI 添加 Collapsible 组件**

Run: `cd front-web && npx shadcn@latest add collapsible`
Expected: 组件安装成功，显示 `Success! Component added`

**Step 3: 验证组件文件存在**

Run: `cat front-web/src/components/ui/collapsible.tsx`
Expected: 显示 Collapsible 组件代码

**Step 4: 提交**

```bash
git add front-web/src/components/ui/collapsible.tsx
git commit -m "feat(ui): add Collapsible component for filter section"
```

---

## Task 2: 添加整体布局背景装饰

**Files:**
- Modify: `front-web/src/app/[locale]/(dashboard)/loan/applications/page.tsx:213-429`

**Step 1: 读取当前页面文件**

Run: `head -220 front-web/src/app/[locale]/(dashboard)/loan/applications/page.tsx`
Expected: 显示组件当前 return 语句

**Step 2: 修改外层容器，添加装饰背景**

在 `return (` 后面的 `<div className="space-y-6">` 添加装饰层：

```tsx
return (
  <div className="space-y-6 animate-fade-in">
    {/* 装饰性背景 */}
    <div className="relative overflow-hidden rounded-2xl border border-primary/20">
      <div className="absolute inset-0 bg-gradient-to-br from-primary/10 to-primary/5" />
      <div className="absolute inset-0 bg-grid opacity-10" />
      <Card className="relative glass">
        <CardHeader>
```

同时在文件末尾 `</div>` 前添加闭合标签。

**Step 3: 运行开发服务器验证**

Run: `cd front-web && pnpm dev`
Expected: 服务器启动成功，访问页面显示带装饰背景的卡片

**Step 4: 提交**

```bash
git add front-web/src/app/[locale]/(dashboard)/loan/applications/page.tsx
git commit -m "refactor(page): add decorative background with gradient and grid pattern"
```

---

## Task 3: 实现折叠式筛选区域

**Files:**
- Modify: `front-web/src/app/[locale]/(dashboard)/loan/applications/page.tsx:1-50, 220-330`

**Step 1: 添加 Collapsible 导入和状态**

在文件顶部导入区域添加：

```tsx
import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from '@/components/ui/collapsible';
import { ChevronDown, ChevronUp } from 'lucide-react';
```

在 `const [selectedApplication, setSelectedApplication]` 后添加状态：

```tsx
const [isAdvancedFilterOpen, setIsAdvancedFilterOpen] = useState(false);
```

**Step 2: 重组筛选区域为常用 + 高级两部分**

将现有的 8 个筛选项分为两组，前 4 个保持原样，后 4 个包裹在 Collapsible 中。

在 `startTime` 和 `endTime` 的 div 外包裹 Collapsible：

```tsx
{/* 高级筛选 */}
<Collapsible open={isAdvancedFilterOpen} onOpenChange={setIsAdvancedFilterOpen}>
  <CollapsibleContent className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6 space-y-0">
    <div className="space-y-2">
      <Label htmlFor="contractNo">合同号</Label>
      <Input id="contractNo" ... />
    </div>
    <div className="space-y-2">
      <Label htmlFor="contractStatus">合同状态</Label>
      <Select>...</Select>
    </div>
    <div className="space-y-2">
      <Label htmlFor="startTime">开始日期</Label>
      <Input id="startTime" type="date" ... />
    </div>
    <div className="space-y-2">
      <Label htmlFor="endTime">结束日期</Label>
      <Input id="endTime" type="date" ... />
    </div>
  </CollapsibleContent>
</Collapsible>
```

**Step 3: 添加折叠触发按钮**

在搜索按钮区域前添加：

```tsx
<div className="flex gap-2 mb-4">
  <Button onClick={handleSearch} className="gap-2">
    <Search className="w-4 h-4" />
    搜索
  </Button>
  <Button onClick={handleReset} variant="outline">
    重置
  </Button>
  <Button
    variant="ghost"
    size="sm"
    onClick={() => setIsAdvancedFilterOpen(!isAdvancedFilterOpen)}
    className="gap-2 ml-auto"
  >
    {isAdvancedFilterOpen ? (
      <>
        <ChevronUp className="w-4 h-4" />
        收起筛选
      </>
    ) : (
      <>
        <ChevronDown className="w-4 h-4" />
        高级筛选
      </>
    )}
  </Button>
  <Button className="gap-2" variant="outline" onClick={() => mutate()}>
    <RefreshCw className="w-4 h-4" />
    刷新
  </Button>
</div>
```

**Step 4: 验证折叠功能**

访问页面，点击"高级筛选"按钮，验证展开/收起功能正常。

**Step 5: 提交**

```bash
git add front-web/src/app/[locale]/(dashboard)/loan/applications/page.tsx
git commit -m "feat(page): add collapsible advanced filter section"
```

---

## Task 4: 增强表格视觉效果

**Files:**
- Modify: `front-web/src/app/[locale]/(dashboard)/loan/applications/page.tsx:345-400`

**Step 1: 修改表格容器样式**

将 `<div className="rounded-md border">` 改为：

```tsx
<div className="rounded-xl border border-border/50 overflow-hidden bg-card/50 backdrop-blur-sm">
```

**Step 2: 修改表头样式**

将 `<TableHeader>` 添加背景类：

```tsx
<TableHeader className="bg-muted/30">
```

**Step 3: 修改表格行样式**

在 `applicationsData?.list?.map` 内的 `<TableRow>` 添加样式和点击事件：

```tsx
<TableRow
  key={item.applicationId}
  className={cn(
    "transition-colors hover:bg-primary/5 cursor-pointer",
    index % 2 === 0 ? "bg-transparent" : "bg-muted/20"
  )}
  onClick={() => handleOpenDetail(item)}
>
```

注意：需要在 map 中添加 `index` 参数。

**Step 4: 移除原有的"查看详情"按钮**

删除整列 `<TableCell className="text-right">` 中的按钮内容，因为现在整行可点击。

同时删除 `<TableHead>` 中的"操作"列。

**Step 5: 验证表格交互**

访问页面，验证：
- 表格有斑马纹效果
- 鼠标悬停行高亮
- 点击行打开详情

**Step 6: 提交**

```bash
git add front-web/src/app/[locale]/(dashboard)/loan/applications/page.tsx
git commit -m "feat(table): add zebra striping, hover effect, and row click"
```

---

## Task 5: 增强状态徽章样式

**Files:**
- Modify: `front-web/src/app/[locale]/(dashboard)/loan/applications/page.tsx:42-78, 123-132`

**Step 1: 更新状态映射，添加 className**

修改 `APPLICATION_STATUS_MAP`、`CONTRACT_STATUS_MAP`、`RISK_DECISION_MAP` 的类型定义：

```tsx
const APPLICATION_STATUS_MAP: Record<string, {
  label: string;
  variant: 'default' | 'secondary' | 'destructive' | 'outline';
  className?: string;
}> = {
  SUBMITTED: {
    label: '已提交',
    variant: 'outline',
    className: 'bg-blue-500/10 text-blue-600 dark:text-blue-400 border-blue-500/20'
  },
  REJECTED: {
    label: '已拒绝',
    variant: 'destructive',
  },
  APPROVED_PENDING_SIGN: {
    label: '待签署',
    variant: 'outline',
    className: 'bg-amber-500/10 text-amber-600 dark:text-amber-400 border-amber-500/20 animate-pulse'
  },
  SIGNED: {
    label: '已签署',
    variant: 'secondary',
  },
  DISBURSED: {
    label: '已放款',
    variant: 'outline',
    className: 'bg-gradient-to-r from-green-500/20 to-emerald-500/20 text-green-700 dark:text-green-400 border-green-500/30'
  },
  EXPIRED: {
    label: '已过期',
    variant: 'secondary',
    className: 'bg-gray-500/10 text-gray-500 border-gray-500/20'
  },
};
```

**Step 2: 更新渲染函数支持 className**

修改 `renderEnumBadge` 函数：

```tsx
function renderEnumBadge(
  value: unknown,
  map: Record<string, {
    label: string;
    variant: 'default' | 'secondary' | 'destructive' | 'outline';
    className?: string;
  }>
) {
  const raw = textOrDash(value);
  if (raw === '-') return raw;
  const mapped = map[raw];
  if (!mapped) return <Badge variant="outline">{raw}</Badge>;
  return (
    <Badge
      variant={mapped.variant}
      className={cn("font-medium", mapped.className)}
    >
      {mapped.label}
    </Badge>
  );
}
```

**Step 3: 验证状态徽章显示**

访问页面，检查不同状态的徽章颜色和动画效果。

**Step 4: 提交**

```bash
git add front-web/src/app/[locale]/(dashboard)/loan/applications/page.tsx
git commit -m "feat(badge): enhance status badges with custom colors and animations"
```

---

## Task 6: 美化详情侧边栏头部

**Files:**
- Modify: `front-web/src/app/[locale]/(dashboard)/loan/applications/page.tsx:430-440`

**Step 1: 修改 DialogContent 样式**

将现有的 `<DialogContent>` className 更新为：

```tsx
<DialogContent
  className="left-auto right-0 top-0 h-full max-h-none w-full max-w-2xl
              translate-x-0 translate-y-0 overflow-y-auto rounded-none sm:rounded-none
              bg-gradient-to-b from-background to-muted/20"
>
```

**Step 2: 美化 DialogHeader**

将现有的 `<DialogHeader>` 替换为：

```tsx
<DialogHeader className="border-b border-border/50 pb-4 bg-gradient-to-r from-primary/10
                              to-primary/5 -mx-6 px-6 pt-6">
  <div className="flex items-center gap-3">
    <div className="h-10 w-10 rounded-lg bg-primary/20 flex items-center justify-center
                    transition-transform hover:scale-105">
      <FileText className="h-5 w-5 text-primary" />
    </div>
    <div>
      <DialogTitle className="text-lg">申请详情</DialogTitle>
      <DialogDescription className="text-xs text-muted-foreground">
        {selectedApplication ? `申请编号：${selectedApplication.applicationNo}` : '查看申请全量字段与合同信息'}
      </DialogDescription>
    </div>
  </div>
</DialogHeader>
```

**Step 3: 验证头部样式**

打开详情弹窗，验证渐变背景和图标显示。

**Step 4: 提交**

```bash
git add front-web/src/app/[locale]/(dashboard)/loan/applications/page.tsx
git commit -m "refactor(dialog): beautify detail sidebar header with gradient background"
```

---

## Task 7: 美化详情内容分区

**Files:**
- Modify: `front-web/src/app/[locale]/(dashboard)/loan/applications/page.tsx:450-518`

**Step 1: 修改每个 section 的样式**

将所有 `<section className="space-y-3">` 更新为：

```tsx
<section className="rounded-xl border border-border/50 bg-card/50 p-4 shadow-sm
                   transition-all duration-200 hover:shadow-md hover:border-primary/20">
```

**Step 2: 更新 section 标题样式**

将所有 `<h3 className="text-sm font-semibold">` 更新为：

```tsx
<h3 className="text-sm font-semibold text-primary mb-3 flex items-center gap-2">
  <div className="h-1.5 w-1.5 rounded-full bg-primary" />
  {/* 原有标题文字 */}
</h3>
```

包括：基本信息、风险与财务、时间轴、合同信息。

**Step 3: 验证分区样式**

打开详情弹窗，验证每个分区的卡片样式。

**Step 4: 提交**

```bash
git add front-web/src/app/[locale]/(dashboard)/loan/applications/page.tsx
git commit -m "refactor(dialog): style detail sections as cards with decorative dots"
```

---

## Task 8: 添加横向时间轴组件

**Files:**
- Modify: `front-web/src/app/[locale]/(dashboard)/loan/applications/page.tsx:1-40, 486-497`

**Step 1: 添加 Check 图标导入**

在文件顶部添加：

```tsx
import { Check } from 'lucide-react';
```

**Step 2: 在详情数据获取后创建时间轴数据**

在 `const detail: LoanApplicationAdminDetail | undefined = detailData;` 后添加：

```tsx
const timelineItems = useMemo(() => {
  if (!detail) return [];
  return [
    { label: '提交', completed: !!detail.createdAt },
    { label: '审批', completed: !!detail.approvedAt },
    { label: '签署', completed: !!detail.signedAt },
    { label: '放款', completed: !!detail.disbursedAt },
  ];
}, [detail]);
```

**Step 3: 替换时间轴 section 内容**

将"时间轴" section 的内容替换为横向时间轴：

```tsx
<section className="rounded-xl border border-border/50 bg-card/50 p-4 shadow-sm">
  <h3 className="text-sm font-semibold text-primary mb-3 flex items-center gap-2">
    <div className="h-1.5 w-1.5 rounded-full bg-primary" />
    时间轴
  </h3>
  <div className="flex items-center gap-2 overflow-x-auto pb-2">
    {timelineItems.map((item, index) => (
      <React.Fragment key={index}>
        <div className="flex items-center gap-2 shrink-0">
          <div className={cn(
            "h-8 w-8 rounded-full flex items-center justify-center text-xs font-medium transition-all duration-300",
            item.completed
              ? "bg-primary text-primary-foreground"
              : "bg-muted text-muted-foreground"
          )}>
            {item.completed ? <Check className="h-4 w-4" /> : (index + 1)}
          </div>
          <span className={cn(
            "text-xs font-medium whitespace-nowrap",
            item.completed ? "text-foreground" : "text-muted-foreground"
          )}>
            {item.label}
          </span>
        </div>
        {index < timelineItems.length - 1 && (
          <div className={cn(
            "w-8 h-0.5 transition-all duration-300",
            item.completed ? "bg-primary" : "bg-border"
          )} />
        )}
      </React.Fragment>
    ))}
  </div>
</section>
```

**Step 4: 验证时间轴显示**

打开一个已放款的申请详情，验证时间轴显示正确的进度。

**Step 5: 提交**

```bash
git add front-web/src/app/[locale]/(dashboard)/loan/applications/page.tsx
git commit -m "feat(dialog): add horizontal timeline for application progress"
```

---

## Task 9: 美化操作按钮

**Files:**
- Modify: `front-web/src/app/[locale]/(dashboard)/loan/applications/page.tsx:332-343`

**Step 1: 添加 RefreshCw 图标**

更新导入：
```tsx
import { Search, RefreshCw } from 'lucide-react';
```

**Step 2: 更新按钮样式**

将搜索按钮区域更新为：

```tsx
<div className="flex gap-2 mb-4">
  <Button
    onClick={handleSearch}
    className="gap-2 bg-gradient-to-r from-primary to-primary/90 hover:from-primary/90
               hover:to-primary shadow-md btn-shine"
  >
    <Search className="w-4 h-4" />
    搜索
  </Button>
  <Button
    onClick={handleReset}
    variant="outline"
    className="hover:bg-primary/10 hover:border-primary/30 transition-all duration-200"
  >
    重置
  </Button>
  <Button
    className="ml-auto gap-2 hover:bg-primary/10 transition-all duration-200"
    variant="outline"
    onClick={() => mutate()}
  >
    <RefreshCw className="w-4 h-4" />
    刷新
  </Button>
</div>
```

**Step 3: 验证按钮样式和交互**

访问页面，验证按钮悬停效果和光泽动画。

**Step 4: 提交**

```bash
git add front-web/src/app/[locale]/(dashboard)/loan/applications/page.tsx
git commit -m "feat(buttons): enhance action buttons with gradient and hover effects"
```

---

## Task 10: 美化分页区域

**Files:**
- Modify: `front-web/src/app/[locale]/(dashboard)/loan/applications/page.tsx:402-426`

**Step 1: 更新分页区域样式**

将分页区域包裹在卡片中，并高亮数字：

```tsx
{applicationsData && applicationsData.total > 0 && (
  <div className="flex items-center justify-between mt-4 p-3 rounded-lg
                  bg-muted/30 border border-border/50">
    <div className="text-sm text-muted-foreground">
      共 <span className="font-semibold text-foreground">
        {applicationsData.total}
      </span> 条记录，
      第 <span className="font-semibold text-foreground">
        {page}
      </span> / {Math.ceil(applicationsData.total / applicationsData.size)} 页
    </div>
    <div className="flex gap-2">
      <Button
        variant="outline"
        size="sm"
        onClick={() => setPage((p) => Math.max(1, p - 1))}
        disabled={page === 1}
        className="disabled:opacity-50 transition-all duration-200 hover:bg-primary/10
                   hover:border-primary/30 disabled:hover:bg-transparent"
      >
        上一页
      </Button>
      <Button
        variant="outline"
        size="sm"
        onClick={() => setPage((p) => p + 1)}
        disabled={page * applicationsData.size >= applicationsData.total}
        className="disabled:opacity-50 transition-all duration-200 hover:bg-primary/10
                   hover:border-primary/30 disabled:hover:bg-transparent"
      >
        下一页
      </Button>
    </div>
  </div>
)}
```

**Step 2: 验证分页样式**

访问有多页数据的页面，验证分页样式显示正确。

**Step 3: 提交**

```bash
git add front-web/src/app/[locale]/(dashboard)/loan/applications/page.tsx
git commit -m "feat(pagination): beautify pagination area with card style"
```

---

## Task 11: 美化加载和空状态

**Files:**
- Modify: `front-web/src/app/[locale]/(dashboard)/loan/applications/page.tsx:1-5, 361-378`

**Step 1: 添加 Loader2 图标导入**

```tsx
import { FileText, Search, RefreshCw, Loader2 } from 'lucide-react';
```

**Step 2: 更新加载状态**

将 `{isLoading ? (...)` 的内容替换为：

```tsx
{isLoading ? (
  <TableRow>
    <TableCell colSpan={8} className="text-center py-12">
      <div className="flex flex-col items-center gap-3">
        <div className="h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center animate-pulse">
          <Loader2 className="h-5 w-5 text-primary animate-spin" />
        </div>
        <p className="text-muted-foreground">加载中...</p>
      </div>
    </TableCell>
  </TableRow>
) : error ? (
```

**Step 3: 更新空状态**

将空状态内容替换为：

```tsx
) : applicationsData?.list?.length === 0 ? (
  <TableRow>
    <TableCell colSpan={8} className="text-center py-12">
      <div className="flex flex-col items-center gap-3">
        <div className="h-12 w-12 rounded-full bg-muted flex items-center justify-center">
          <FileText className="h-6 w-6 text-muted-foreground" />
        </div>
        <p className="text-muted-foreground">暂无申请记录</p>
        <p className="text-xs text-muted-foreground/60">请调整筛选条件后重试</p>
      </div>
    </TableCell>
  </TableRow>
```

**Step 4: 验证加载和空状态**

访问页面，筛选无数据条件验证空状态，刷新验证加载状态。

**Step 5: 提交**

```bash
git add front-web/src/app/[locale]/(dashboard)/loan/applications/page.tsx
git commit -m "feat(table): beautify loading and empty states with icons"
```

---

## Task 12: 测试亮色/暗主题兼容性

**Files:**
- Modify: `front-web/src/app/[locale]/(dashboard)/loan/applications/page.tsx` (验证)

**Step 1: 启动开发服务器**

Run: `cd front-web && pnpm dev`

**Step 2: 测试亮色主题**

访问: `http://localhost:3000/zh/loan/applications`

验证项：
- [ ] 装饰背景渐变显示正确
- [ ] 表格斑马纹清晰
- [ ] 状态徽章颜色正确
- [ ] 详情侧边栏渐变显示正确
- [] 时间轴进度显示正确

**Step 3: 测试暗色主题**

切换到暗色主题（Header 主题切换按钮）

验证项：
- [ ] 装饰背景在暗模式下不刺眼
- [ ] 状态徽章在暗模式下可读
- [ ] 详情侧边栏渐变在暗模式下正确

**Step 4: 修复发现的样式问题**

如有问题，使用 `dark:` 前缀添加暗模式样式。

**Step 5: 提交（如有修复）**

```bash
git add front-web/src/app/[locale]/(dashboard)/loan/applications/page.tsx
git commit -m "fix(page): add dark mode compatibility fixes"
```

---

## Task 13: 运行测试确保功能正常

**Files:**
- Test: `front-web/src/app/[locale]/(dashboard)/loan/applications/__tests__/page.test.tsx`

**Step 1: 运行现有测试**

Run: `cd front-web && pnpm test src/app/[locale]/(dashboard)/loan/applications/__tests__/page.test.tsx`

Expected: 所有测试通过

**Step 2: 如有失败，修复测试**

根据错误信息更新测试用例以匹配新的 DOM 结构。

**Step 3: 提交（如有修复）**

```bash
git add front-web/src/app/[locale]/(dashboard)/loan/applications/__tests__/page.test.tsx
git commit -m "test(page): update tests for redesigned UI"
```

---

## Task 14: 最终验证和文档

**Files:**
- Modify: `docs/plans/2026-02-25-loan-applications-page-redesign-design.md` (标记完成)

**Step 1: 完整页面功能验证**

- [ ] 筛选展开/收起正常
- [ ] 搜索功能正常
- [ ] 重置功能正常
- [ ] 表格排序（如有）
- [ ] 分页切换正常
- [ ] 详情打开正常
- [ ] 详情时间轴显示正确
- [ ] 刷新功能正常

**Step 2: 浏览器兼容性检查**

在 Chrome、Firefox、Safari 中测试（如有可能）。

**Step 3: 更新设计文档状态**

在设计文档顶部添加：`**状态**: 已实现`

**Step 4: 最终提交**

```bash
git add docs/plans/2026-02-25-loan-applications-page-redesign-design.md
git commit -m "docs: mark loan applications page redesign as completed"
```

---

## 完成标准

- [x] 所有 14 个任务完成
- [x] 所有测试通过
- [x] 亮色/暗主题兼容
- [x] 无控制台错误或警告
- [x] 视觉效果与设计文档一致

---

## 相关文件

- 设计文档: `docs/plans/2026-02-25-loan-applications-page-redesign-design.md`
- 实现文件: `front-web/src/app/[locale]/(dashboard)/loan/applications/page.tsx`
- 测试文件: `front-web/src/app/[locale]/(dashboard)/loan/applications/__tests__/page.test.tsx`
- 全局样式: `front-web/src/app/[locale]/globals.css`
