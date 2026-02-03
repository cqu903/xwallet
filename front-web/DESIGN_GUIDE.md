# xWallet Front-Web 设计风格指南

## 设计理念

**xWallet** 采用专业金融管理后台设计风格，强调：
- **信任感**: 紫色主题传递尊贵、专业、可信赖的品牌形象
- **安全性**: 通过视觉层次和精致的细节体现系统的安全性
- **高效性**: 清晰的信息架构和流畅的交互提升工作效率
- **用户价值**: 突出关键数据指标，让用户快速获取价值信息

---

## 配色方案

### 主色（Primary）
- **紫色**: `#6B21A8` → OKLCH `oklch(42% 0.22 295)`
- 用于：主按钮、链接、品牌元素、强调内容
- 亮色模式: `oklch(42% 0.22 295)`
- 暗色模式: `oklch(58% 0.24 295)`（更亮）

### 辅助色（Accent）
- **淡紫色**: `oklch(94% 0.02 295)` (亮色) / `oklch(24% 0.03 295)` (暗色)
- 用于：次要背景、悬停状态
- **金色渐变**: `.gradient-gold` → 保留用于特殊强调

### 背景色
- **背景**: `oklch(98.5% 0.004 280)` (亮色) / `oklch(11% 0.02 280)` (暗色)
- **卡片**: `oklch(100% 0 0)` (亮色) / `oklch(16% 0.022 280)` (暗色)
- **次要背景**: `oklch(95% 0.006 280)` (亮色) / `oklch(20% 0.022 280)` (暗色)

### 功能色
- **成功**: 绿色系 `oklch(65% 0.15 145)`
- **警告**: 金色系 `oklch(70% 0.15 45)`
- **错误**: 红色系 `oklch(52% 0.22 25)`
- **信息**: 蓝色系 `oklch(60% 0.18 250)`

---

## 字体系统

### 字体家族
```css
/* 使用 next/font 加载 DM Sans */
/* layout.tsx 中通过 DM_Sans 组件加载，设置 --font-dm-sans 变量 */

/* 标题和正文统一使用 DM Sans */
font-family: var(--font-dm-sans), ui-sans-serif, system-ui, sans-serif;
class: .font-display  /* 与正文相同，保持专业后台一致性 */
```

### 字体层级
- **大标题**: `font-display text-3xl font-bold` - 页面主标题
- **标题**: `font-display text-2xl font-bold` - 区块标题
- **副标题**: `text-lg font-semibold` - 次要标题
- **正文**: `text-sm` / `text-base` - 常规内容
- **辅助文字**: `text-xs text-muted-foreground` - 元信息

---

## 视觉效果

### 渐变效果
```css
/* 主色渐变 - 紫色系 */
.gradient-bg {
  background: linear-gradient(135deg,
    oklch(42% 0.22 295) 0%,
    oklch(38% 0.20 290) 50%,
    oklch(34% 0.18 285) 100%
  );
}

/* 金色渐变 - 特殊强调 */
.gradient-gold {
  background: linear-gradient(135deg,
    oklch(70% 0.15 45) 0%,
    oklch(75% 0.12 55) 50%,
    oklch(80% 0.10 65) 100%
  );
}

/* 文字渐变 - 紫色 */
.text-gradient {
  background: linear-gradient(135deg,
    oklch(42% 0.22 295) 0%,
    oklch(38% 0.20 290) 50%,
    oklch(50% 0.18 295) 100%
  );
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
```

### 玻璃态效果
```css
.glass {
  background: oklch(100% 0 0 / 0.85);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid oklch(91% 0.008 280 / 0.6);
  box-shadow: 0 4px 24px -4px oklch(0% 0 0 / 0.08);
}

/* 暗色模式 */
.dark .glass {
  background: oklch(16% 0.022 280 / 0.85);
  border-color: oklch(26% 0.025 280 / 0.5);
  box-shadow: 0 4px 24px -4px oklch(0% 0 0 / 0.3);
}
```

### 背景图案
```css
/* 网格背景 */
.bg-grid {
  background-size: 50px 50px;
  background-image:
    linear-gradient(to right, oklch(90% 0.01 280 / 0.3) 1px, transparent 1px),
    linear-gradient(to bottom, oklch(90% 0.01 280 / 0.3) 1px, transparent 1px);
}

/* 装饰性背景图案 - 紫色 */
.bg-pattern {
  background-image:
    radial-gradient(circle at 20% 50%, oklch(42% 0.22 295 / 0.04) 0%, transparent 50%),
    radial-gradient(circle at 80% 80%, oklch(42% 0.22 295 / 0.03) 0%, transparent 50%),
    radial-gradient(circle at 40% 20%, oklch(42% 0.22 295 / 0.02) 0%, transparent 50%);
}
```

---

## 动画系统

### 入场动画
```css
/* 从下淡入 */
.animate-fade-in-up {
  animation: fadeInUp 0.6s ease-out forwards;
}

/* 淡入 */
.animate-fade-in {
  animation: fadeIn 0.5s ease-out forwards;
}

/* 缩放淡入 */
.animate-scale-in {
  animation: scaleIn 0.4s ease-out forwards;
}
```

### 延迟类
```css
.delay-100 { animation-delay: 0.1s; }
.delay-200 { animation-delay: 0.2s; }
.delay-300 { animation-delay: 0.3s; }
.delay-400 { animation-delay: 0.4s; }
.delay-500 { animation-delay: 0.5s; }
```

### 交互动画
```css
/* 卡片悬停 - 使用 GPU 加速避免闪烁 */
.card-hover {
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  transform: translateZ(0);
  backface-visibility: hidden;
  will-change: transform, box-shadow;
}
.card-hover:hover {
  transform: translateY(-2px) translateZ(0);
  box-shadow: 0 8px 20px -6px oklch(42% 0.22 295 / 0.12);
}

/* 暗色模式 */
.dark .card-hover:hover {
  box-shadow: 0 8px 20px -6px oklch(58% 0.24 295 / 0.2);
}

/* 脉冲发光 */
.pulse-glow,
.animate-pulse-glow {
  animation: pulse-glow 2s ease-in-out infinite;
}
@keyframes pulse-glow {
  0%, 100% {
    box-shadow: 0 0 20px oklch(42% 0.22 295 / 0.25);
  }
  50% {
    box-shadow: 0 0 36px oklch(42% 0.22 295 / 0.4);
  }
}
```

---

## 组件样式规范

### 卡片（Card）
```tsx
/* 普通卡片 - 专业后台风格 */
<Card className="border border-border shadow-sm">
  <CardHeader>...</CardHeader>
  <CardContent>...</CardContent>
</Card>

/* 可交互卡片 */
<Card className="card-hover border border-border shadow-sm cursor-pointer">
  ...
</Card>
```

### 按钮（Button）
```tsx
/* 主按钮 - 简洁专业 */
<Button className="h-11 font-medium shadow-sm hover:shadow transition-shadow">
  按钮文字
</Button>

/* 次要按钮 */
<Button variant="outline" className="h-11">
  次要操作
</Button>
```

### 输入框（Input）
```tsx
<Input className="h-11" />
```

### 徽章（Badge）
```tsx
<span className="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-primary/10 text-primary">
  文字
</span>
```

---

## 图标使用

### Logo 图标
```tsx
<div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary">
  <svg className="h-4 w-4 text-primary-foreground" fill="none" viewBox="0 0 24 24" stroke="currentColor">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
      d="M13 10V3L4 14h7v7l9-11h-7z" />
  </svg>
</div>
```

### 常用图标
- 使用 `lucide-react` 图标库
- 尺寸: `h-4 w-4` / `h-5 w-5` / `h-6 w-6`
- 图标背景: `bg-primary/10 p-2 rounded-lg` 或 `bg-primary p-2 rounded-lg text-white`

---

## 布局模式

### 页面结构
```tsx
<div className="space-y-8 animate-fade-in">
  {/* 欢迎横幅 - 紫色渐变 */}
  <div className="relative overflow-hidden rounded-xl border border-border bg-card shadow-sm">
    <div className="absolute inset-0 gradient-bg opacity-95" />
    <div className="relative p-6">...</div>
  </div>

  {/* 统计卡片网格 */}
  <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
    <Card className="relative card-hover overflow-hidden border border-border shadow-sm isolate">
      <div className="absolute inset-0 bg-gradient-to-br from-primary/15 to-primary/25 opacity-50 -z-10" />
      ...
    </Card>
  </div>

  {/* 主要内容区 */}
  <Card className="border border-border shadow-sm">...</Card>

  {/* 快捷操作 */}
  <div className="grid gap-6 md:grid-cols-3">
    <Card className="card-hover border border-border shadow-sm">...</Card>
  </div>
</div>
```

### 统计卡片（重要）
> **注意**：使用绝对定位背景时，必须添加 `relative isolate` 到父容器，并为背景层添加 `-z-10`，否则会导致悬停时背景闪烁。

```tsx
<Card className="relative card-hover overflow-hidden border border-border shadow-sm isolate">
  {/* 背景层 - 必须添加 -z-10 */}
  <div className="absolute inset-0 bg-gradient-to-br from-primary/15 to-primary/25 opacity-50 -z-10" />
  <CardHeader className="relative">
    <div className="flex items-center justify-between">
      <CardTitle className="text-sm font-medium text-muted-foreground">标题</CardTitle>
      <div className="rounded-lg bg-primary p-2 text-white shadow-md">
        {icon}
      </div>
    </div>
  </CardHeader>
  <CardContent className="relative">
    <div className="text-3xl font-bold text-gradient">1,234</div>
    <div className="flex items-center space-x-2">
      <span className="text-sm font-medium text-green-500">+20.1%</span>
      <span className="text-xs text-muted-foreground">较上月</span>
    </div>
  </CardContent>
</Card>
```

### 间距规范
- 页面块间距: `space-y-8`
- 卡片间距: `gap-6`
- 表单项间距: `space-y-5`
- 元素间距: `space-x-2` / `space-x-3` / `space-x-4`
- 圆角: `--radius: 0.5rem`（较紧凑，适合后台）

---

## 响应式设计

### 断点
- `sm`: 640px
- `md`: 768px
- `lg`: 1024px
- `xl`: 1280px

### 常用响应式类
```tsx
/* 网格 */
<div className="grid md:grid-cols-2 lg:grid-cols-4">

/* 显示/隐藏 */
<div className="hidden lg:block">     {/* 桌面端显示 */}
<div className="lg:hidden">            {/* 移动端显示 */}

/* 字体 */
<h1 className="text-2xl lg:text-3xl">

/* 间距 */
<div className="p-4 lg:p-6">
```

---

## 登录页设计

### 布局结构
```tsx
<div className="relative min-h-screen overflow-hidden bg-background bg-grid bg-pattern">
  {/* 低调装饰光晕 */}
  <div className="absolute inset-0 overflow-hidden pointer-events-none">
    <div className="absolute -top-40 -right-40 h-80 w-80 animate-pulse-glow rounded-full bg-primary/8 blur-3xl" />
  </div>

  <div className="relative flex min-h-screen items-center justify-center p-4">
    <div className="w-full max-w-5xl grid-cols-2 gap-16 lg:grid">
      {/* 左侧：品牌简介 */}
      <div className="hidden lg:flex flex-col justify-center space-y-8">
        {/* Logo + 标题 + 简短描述 */}
      </div>

      {/* 右侧：登录表单 */}
      <div className="flex items-center justify-center">
        <div className="w-full max-w-[400px]">
          <div className="rounded-xl border border-border bg-card p-8 shadow-sm space-y-6">
            {/* 表单内容 */}
          </div>
        </div>
      </div>
    </div>
  </div>
</div>
```

---

## 暗色模式

### 实现方式
- 使用 `next-themes` 的 **class 策略**（`attribute="class"`）
- `globals.css` 中同时定义 `@media (prefers-color-scheme: dark)` 和 `.dark` 选择器
- 确保所有组件使用语义化颜色类（如 `bg-background`, `text-foreground`, `border-border`）

### 切换组件
```tsx
// Header.tsx 中的主题切换按钮
<Button
  variant="ghost"
  size="icon"
  onClick={() => setTheme(theme === 'dark' ? 'light' : 'dark')}
>
  <Sun className="h-5 w-5 rotate-0 scale-100 transition-all dark:-rotate-90 dark:scale-0" />
  <Moon className="absolute h-5 w-5 rotate-90 scale-0 transition-all dark:rotate-0 dark:scale-100" />
</Button>
```

---

## 侧栏与顶栏

### 侧栏（Sidebar）
- 高度：`h-14`（Logo 区）
- 宽度：展开 `w-64`，收起 `w-16`
- Logo：紫色背景图标 + 品牌名

```tsx
<div className="flex h-14 items-center justify-between border-b border-border px-3">
  <div className="flex items-center gap-2">
    <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-primary">
      <svg className="h-4 w-4 text-primary-foreground">...</svg>
    </div>
    <span className="font-display text-base font-semibold tracking-tight text-foreground">xWallet</span>
  </div>
</div>
```

### 顶栏（Header）
- 高度：`h-14`
- 样式：`border-b border-border bg-card shadow-sm`

---

## 可访问性

### 焦点状态
所有可交互元素默认具备可见的焦点环（通过 `--color-ring`）。

### 对比度
- 正文文本与背景对比度 ≥ 4.5:1
- 大文本与背景对比度 ≥ 3:1

### 语义化 HTML
- 使用正确的 HTML5 语义标签
- 为图标添加 `aria-label`（如必要）
- 确保键盘导航可用

---

## 性能优化

### 动画性能
- 优先使用 `transform` 和 `opacity` 进行动画
- 避免动画 `width`, `height`, `top`, `left` 等属性
- 使用 `will-change` 提示浏览器优化（如 `card-hover`）
- 使用 `translateZ(0)` 和 `backface-visibility: hidden` 启用 GPU 加速

### 字体加载
- 使用 `next/font` 加载字体（DM Sans）
- 自动优化字体加载，避免 FOIT/FOUT

### 图片优化
- 使用 Next.js `<Image>` 组件
- 响应式图片尺寸

---

## 设计原则清单

在设计新页面时，遵循以下原则：

✅ **色彩**: 使用紫色主题色 `oklch(42% 0.22 295)`
✅ **字体**: 统一使用 DM Sans（通过 next/font 加载）
✅ **渐变**: 适当使用 `.gradient-bg` 和 `.text-gradient`
✅ **卡片**: 使用 `border border-border shadow-sm`，避免 `border-none shadow-lg`
✅ **动画**: 入场用 `.animate-fade-in-up`，悬停用 `.card-hover`（含 GPU 加速）
✅ **定位**: 使用绝对定位背景时添加 `relative isolate` + `-z-10`
✅ **圆角**: 统一使用 `rounded-lg` / `rounded-xl`（`--radius: 0.5rem`）
✅ **间距**: 遵循 4/8/12/16/24 倍数系统
✅ **图标**: lucide-react + `bg-primary/10` 或 `bg-primary text-white` 背景
✅ **暗色**: 支持 `.dark` class 和 `@media (prefers-color-scheme: dark)`
✅ **响应式**: 移动优先，渐进增强

---

## 版本历史

- **v1.1** (2026-01-31): 更新为专业后台风格
  - 主色调整为 `oklch(42% 0.22 295)`
  - 字体改为 DM Sans（next/font 加载）
  - 圆角从 0.75rem 改为 0.5rem
  - card-hover 添加 GPU 加速，避免闪烁
  - 统计卡片添加 `relative isolate -z-10` 规范
  - 暗色模式支持 `.dark` class（next-themes）
  - 简化按钮样式，移除 btn-shine

- **v1.0** (2025-01-31): 初始设计规范
  - 紫色主题 (#7424F5)
  - Playfair Display + Inter 字体
  - 玻璃态 + 渐变效果
  - 动画系统
