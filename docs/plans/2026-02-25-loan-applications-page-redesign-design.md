# 贷款申请单据页面美化设计

**日期**: 2026-02-25
**设计师**: Claude
**状态**: 已批准

## 问题陈述

当前的贷款申请单据页面 (`front-web/src/app/[locale]/(dashboard)/loan/applications/page.tsx`) 与整体项目设计风格不一致。

**现状分析**：

| 方面 | 整体风格 (Dashboard) | 申请单据页面 |
|------|---------------------|-------------|
| 背景 | 渐变横幅 + 网格装饰 | 纯色 Card |
| 卡片 | 悬停动画 + 渐变效果 | 无悬停效果 |
| 表格 | - | 基础 shadcn Table |
| 动画 | `animate-fade-in` | 无入场动画 |
| 细节 | 圆角 `rounded-xl`、阴影、边框 | 样式较简单 |

## 设计目标

1. **风格统一**: 与 Dashboard 页面的精致商务风格保持一致
2. **视觉增强**: 添加渐变、阴影、动画等视觉效果
3. **体验优化**: 折叠式筛选、整行点击表格、加载状态美化

## 设计方向

**选定方案**: 渐进增强
- 在现有结构基础上逐层添加视觉效果
- 改动最小，风险低
- 保持现有功能逻辑

## 详细设计

### 1. 整体布局与背景

**目标**: 为页面增加视觉层次，与 Dashboard 风格一致

```tsx
<div className="space-y-6 animate-fade-in">
  {/* 装饰性背景 - 仅在顶部区域 */}
  <div className="relative overflow-hidden rounded-2xl border border-primary/20">
    {/* 渐变背景层 */}
    <div className="absolute inset-0 bg-gradient-to-br from-primary/10 to-primary/5" />
    {/* 网格装饰 */}
    <div className="absolute inset-0 bg-grid opacity-10" />

    {/* 内容 */}
    <Card className="relative glass">
      {/* 现有内容 */}
    </Card>
  </div>
</div>
```

**关键变化**:
- 添加 `animate-fade-in` 入场动画
- 顶部区域添加渐变背景 + 网格装饰
- 主卡片使用 `glass` 玻璃态效果

---

### 2. 筛选区域（折叠式）

**目标**: 减少视觉干扰，默认只显示最常用的筛选条件

**常用筛选（默认显示）**:
- 申请编号
- 客户ID
- 申请状态
- 风控决策

**高级筛选（默认折叠）**:
- 合同号
- 合同状态
- 开始日期
- 结束日期

```tsx
const [isAdvancedFilterOpen, setIsAdvancedFilterOpen] = useState(false);

<Collapsible open={isAdvancedFilterOpen} onOpenChange={setIsAdvancedFilterOpen}>
  <CollapsibleContent className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
    {/* 高级筛选项 */}
  </CollapsibleContent>
</Collapsible>

<Button
  variant="ghost"
  size="sm"
  onClick={() => setIsAdvancedFilterOpen(!isAdvancedFilterOpen)}
  className="gap-2"
>
  {isAdvancedFilterOpen ? (
    <> <ChevronUp className="w-4 h-4" /> 收起筛选 </>
  ) : (
    <> <ChevronDown className="w-4 h-4" /> 高级筛选 </>
  )}
</Button>
```

---

### 3. 表格增强

**目标**: 增加可读性和交互反馈

**表格样式**:
- 斑马纹：偶数行 `bg-muted/20`
- 悬停整行高亮：`hover:bg-primary/5`
- 整行可点击打开详情
- 表头使用半透明背景

```tsx
<div className="rounded-xl border border-border/50 overflow-hidden bg-card/50 backdrop-blur-sm">
  <Table>
    <TableHeader className="bg-muted/30">
      {/* ... */}
    </TableHeader>
    <TableBody>
      {applicationsData?.list?.map((item, index) => (
        <TableRow
          key={item.applicationId}
          className={cn(
            "transition-colors hover:bg-primary/5 cursor-pointer",
            index % 2 === 0 ? "bg-transparent" : "bg-muted/20"
          )}
          onClick={() => handleOpenDetail(item)}
        >
          {/* ... */}
        </TableRow>
      ))}
    </TableBody>
  </Table>
</div>
```

**状态徽章增强**:

| 状态 | 颜色方案 | 特殊效果 |
|------|---------|----------|
| 已提交 | 蓝色 | - |
| 待签署 | 琥珀色 | `animate-pulse` |
| 已放款 | 绿色渐变 | `from-green-500/20 to-emerald-500/20` |
| 已拒绝 | 红色 | - |
| 已过期 | 灰色 | - |

---

### 4. 详情侧边栏美化

**目标**: 增加视觉层次，让信息更易浏览

**头部设计**:
- 渐变背景 `from-primary/10 to-primary/5`
- 图标 + 标题布局
- 申请编号副标题

**内容分区**:
- 每个分区使用卡片样式 `rounded-xl border border-border/50 bg-card/50`
- 标题前添加圆点装饰
- 横向时间轴显示流程状态

**时间轴设计**:
```tsx
const timelineItems = [
  { label: '提交', completed: !!detail.createdAt },
  { label: '审批', completed: !!detail.approvedAt },
  { label: '签署', completed: !!detail.signedAt },
  { label: '放款', completed: !!detail.disbursedAt },
];

// 渲染为横向进度条
<div className="flex items-center gap-2 overflow-x-auto pb-2">
  {timelineItems.map((item, index) => (
    <div key={index} className="flex items-center gap-2 shrink-0">
      <div className={cn(
        "h-8 w-8 rounded-full flex items-center justify-center text-xs",
        item.completed ? "bg-primary text-primary-foreground" : "bg-muted text-muted-foreground"
      )}>
        {item.completed && <Check className="h-4 w-4" />}
      </div>
      {index < timelineItems.length - 1 && (
        <div className={cn("w-8 h-0.5", item.completed ? "bg-primary" : "bg-border")} />
      )}
    </div>
  ))}
</div>
```

---

### 5. 分页与操作按钮

**搜索按钮**:
- 渐变背景 `from-primary to-primary/90`
- `btn-shine` 光泽效果
- 阴影 `shadow-md`

**其他按钮**:
- 悬停效果 `hover:bg-primary/10 hover:border-primary/30`
- 过渡动画 `transition-all duration-200`

**分页区域**:
- 半透明背景卡片 `bg-muted/30`
- 数字高亮显示

**加载状态**:
- 旋转图标 `animate-spin`
- 脉冲背景 `animate-pulse`

**空状态**:
- 图标 + 提示文字
- 友好引导文案

---

## 技术实现要点

### 新增依赖

无需新增依赖，使用现有的 shadcn/ui 组件：
- `Collapsible` - 筛选区域折叠
- `Badge` - 状态徽章
- `Button` - 操作按钮
- `Card` - 卡片容器

### CSS 类复用

使用 `globals.css` 中已定义的类：
- `.glass` - 玻璃态效果
- `.bg-grid` - 网格背景
- `.animate-fade-in` - 入场动画
- `.btn-shine` - 按钮光泽效果

### 图标

使用 lucide-react 图标库：
- `ChevronUp`, `ChevronDown` - 折叠按钮
- `Check` - 时间轴完成状态
- `Loader2` - 加载动画
- `RefreshCw` - 刷新按钮

---

## 预期效果

| 改进点 | 效果 |
|--------|------|
| 视觉一致性 | 与 Dashboard 风格统一 |
| 信息密度 | 折叠式筛选减少视觉干扰 |
| 交互反馈 | 悬停、点击、加载状态都有反馈 |
| 可读性 | 斑马纹表格、分区卡片、时间轴 |

---

## 后续步骤

1. 创建实现计划 (`writing-plans` 技能)
2. 按计划实现代码变更
3. 测试所有交互状态
4. 验证亮/暗主题兼容性
