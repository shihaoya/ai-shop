# 多店铺积分商城系统 - UI 设计规范

> **版本**：v1.0
> **生成时间**：2026-04-26
> **设计风格**：拟态玻璃（Glassmorphism）
> **技术栈**：reka-ui + unocss (tailwindcss 预设)

---

## 一、设计原则

### 1.1 核心设计理念

- **玻璃拟态（Glassmorphism）**：磨砂玻璃质感的 UI 元素，半透明背景配合模糊效果
- **悬浮布局**：侧边栏和顶部栏均为悬浮气泡，不贴边，四周保留间距
- **圆角美学**：使用大圆角（24px-40px）营造柔和、现代的视觉感受
- **层次光影**：通过透明度和阴影叠加营造虚实层次感

### 1.2 布局规范

```
┌─────────────────────────────────────────────────────────────┐
│                      页面背景（渐变模糊）                        │
│  ┌─────────┐  ┌─────────────────────────────────────────┐  │
│  │         │  │  顶部栏（悬浮气泡，h-20, rounded-3xl）     │  │
│  │ 侧边栏  │  ├─────────────────────────────────────────┤  │
│  │ w-64    │  │                                         │  │
│  │ rounded-│  │              内容区域                     │  │
│  │ 3xl     │  │         bg-white/40, backdrop-blur       │  │
│  │ m-4     │  │                                         │  │
│  │         │  │                                         │  │
│  └─────────┘  └─────────────────────────────────────────┘  │
│                                          ┌──────┐           │
│                                          │主题  │           │
│                                          │切换  │           │
└──────────────────────────────────────────└──────┘──────────┘
```

### 1.3 悬浮间距规范

| 元素 | 边距 | 圆角 | 宽度/高度 |
|------|------|------|----------|
| 侧边栏 | `m-4` (16px) | `rounded-3xl` (24px) | `w-64` |
| 顶部栏 | `m-4` (16px) | `rounded-3xl` (24px) | `h-20` (80px) |
| 内容区 | `m-4 ml-0` | `rounded-3xl` (24px) | `flex-1` |
| 主题按钮 | `bottom-8 right-8` | `rounded-full` | `w-14 h-14` |

---

## 二、颜色系统

### 2.1 基础色板（Light Mode）

```css
/* Surface 层级 */
--color-surface-dim: #d9d9e5;
--color-surface: #faf8ff;
--color-surface-bright: #faf8ff;
--color-surface-container-lowest: #ffffff;
--color-surface-container-low: #f3f3fe;
--color-surface-container: #ededf9;
--color-surface-container-high: #e7e7f3;
--color-surface-container-highest: #e1e2ed;
--color-surface-variant: #e1e2ed;

/* On Surface */
--color-on-surface: #191b23;
--color-on-surface-variant: #434655;
--color-inverse-surface: #2e3039;
--color-inverse-on-surface: #f0f0fb;

/* Outline */
--color-outline: #737686;
--color-outline-variant: #c3c6d7;

/* Tint */
--color-surface-tint: #0053db;

/* Background */
--color-background: #faf8ff;
--color-on-background: #191b23;
```

### 2.2 主色系（Primary/Secondary）

```css
/* Primary */
--color-primary: #004ac6;
--color-on-primary: #ffffff;
--color-primary-container: #2563eb;
--color-on-primary-container: #eeefff;
--color-inverse-primary: #b4c5ff;
--color-primary-fixed: #dbe1ff;
--color-primary-fixed-dim: #b4c5ff;
--color-on-primary-fixed: #00174b;
--color-on-primary-fixed-variant: #003ea8;

/* Secondary */
--color-secondary: #712ae2;
--color-on-secondary: #ffffff;
--color-secondary-container: #8a4cfc;
--color-on-secondary-container: #fffbff;
--color-secondary-fixed: #eaddff;
--color-secondary-fixed-dim: #d2bbff;
--color-on-secondary-fixed: #25005a;
--color-on-secondary-fixed-variant: #5a00c6;

/* Tertiary */
--color-tertiary: #943700;
--color-on-tertiary: #ffffff;
--color-tertiary-container: #bc4800;
--color-on-tertiary-container: #ffede6;
--color-tertiary-fixed: #ffdbcd;
--color-tertiary-fixed-dim: #ffb596;
--color-on-tertiary-fixed: #360f00;
--color-on-tertiary-fixed-variant: #7d2d00;
```

### 2.3 语义色

```css
/* 状态色 */
--color-success: #10B981;
--color-warning: #F59E0B;
--color-danger: #EF4444;
--color-error: #ba1a1a;
--color-on-error: #ffffff;
--color-error-container: #ffdad6;
--color-on-error-container: #93000a;

/* 玻璃效果 */
--color-glass-bg: rgba(255, 255, 255, 0.6);
--color-glass-border: rgba(255, 255, 255, 0.3);
```

### 2.4 六种主题色板

#### 科技蓝（Tech Blue）- 默认主题

```css
--theme-primary: #004ac6;
--theme-secondary: #2563eb;
--theme-rgb-primary: 0, 74, 198;
--theme-gradient: linear-gradient(135deg, #004ac6 0%, #2563eb 100%);
```

#### 清新绿（Fresh Green）

```css
--theme-primary: #10B981;
--theme-secondary: #059669;
--theme-rgb-primary: 16, 185, 129;
--theme-gradient: linear-gradient(135deg, #10B981 0%, #059669 100%);
```

#### 浪漫紫（Romantic Purple）

```css
--theme-primary: #8B5CF6;
--theme-secondary: #7C3AED;
--theme-rgb-primary: 139, 92, 246;
--theme-gradient: linear-gradient(135deg, #8B5CF6 0%, #7C3AED 100%);
```

#### 活力粉（Vibrant Pink）

```css
--theme-primary: #EC4899;
--theme-secondary: #DB2777;
--theme-rgb-primary: 236, 72, 153;
--theme-gradient: linear-gradient(135deg, #EC4899 0%, #DB2777 100%);
```

#### 纯净白（Pure White）- 深色主题

```css
--theme-primary: #1F2937;
--theme-secondary: #374151;
--theme-rgb-primary: 31, 41, 55;
--theme-gradient: linear-gradient(135deg, #1F2937 0%, #374151 100%);
```

#### 经典黑（Classic Black）- 深色主题

```css
--theme-primary: #FFFFFF;
--theme-secondary: #E5E7EB;
--theme-rgb-primary: 255, 255, 255;
--theme-gradient: linear-gradient(135deg, #FFFFFF 0%, #E5E7EB 100%);
```

### 2.5 暗色模式适配（Dark Mode）

```css
/* 暗色模式 Surface */
--color-surface-dim-dark: #1a1a24;
--color-surface-dark: #121218;
--color-surface-bright-dark: #1e1e2a;
--color-surface-container-lowest-dark: #0f0f14;
--color-surface-container-low-dark: #16161e;
--color-surface-container-dark: #1c1c26;
--color-surface-container-high-dark: #262630;
--color-surface-container-highest-dark: #2e2e3a;

/* 暗色模式玻璃效果 */
--color-glass-bg-dark: rgba(0, 0, 0, 0.4);
--color-glass-border-dark: rgba(255, 255, 255, 0.1);
```

---

## 三、字体系统

### 3.1 字体家族

```css
/* 标题字体 - Manrope */
--font-display: 'Manrope', system-ui, sans-serif;
--font-headline: 'Manrope', system-ui, sans-serif;

/* 正文字体 - Inter */
--font-body: 'Inter', system-ui, sans-serif;
--font-label: 'Inter', system-ui, sans-serif;
```

### 3.2 字体量表

| 名称 | 字体 | 字号 | 字重 | 行高 | 字间距 | 用途 |
|------|------|------|------|------|--------|------|
| display-lg | manrope | 48px | 800 | 1.2 | -0.02em | 大标题/登录页标题 |
| headline-md | manrope | 24px | 700 | 1.4 | - | 页面主标题 |
| headline-sm | manrope | 20px | 600 | 1.4 | - | 卡片标题/区块标题 |
| body-lg | inter | 16px | 400 | 1.6 | - | 正文/正文输入 |
| body-md | inter | 14px | 400 | 1.6 | - | 次要正文/表格内容 |
| label-md | inter | 12px | 600 | 1.2 | - | 标签/辅助文字 |
| points-display | manrope | 32px | 800 | 1 | -0.01em | 积分数字展示 |

### 3.3 UnoCSS 字体配置

```js
// uno.config.ts
export default defineConfig({
  theme: {
    fontFamily: {
      'display-lg': ['Manrope', 'system-ui', 'sans-serif'],
      'headline-md': ['Manrope', 'system-ui', 'sans-serif'],
      'headline-sm': ['Manrope', 'system-ui', 'sans-serif'],
      'body-lg': ['Inter', 'system-ui', 'sans-serif'],
      'body-md': ['Inter', 'system-ui', 'sans-serif'],
      'label-md': ['Inter', 'system-ui', 'sans-serif'],
      'points-display': ['Manrope', 'system-ui', 'sans-serif'],
    },
    fontSize: {
      'display-lg': ['48px', { lineHeight: '1.2', letterSpacing: '-0.02em', fontWeight: '800' }],
      'headline-md': ['24px', { lineHeight: '1.4', fontWeight: '700' }],
      'headline-sm': ['20px', { lineHeight: '1.4', fontWeight: '600' }],
      'body-lg': ['16px', { lineHeight: '1.6', fontWeight: '400' }],
      'body-md': ['14px', { lineHeight: '1.6', fontWeight: '400' }],
      'label-md': ['12px', { lineHeight: '1.2', fontWeight: '600' }],
      'points-display': ['32px', { lineHeight: '1', letterSpacing: '-0.01em', fontWeight: '800' }],
    }
  }
})
```

### 3.4 Tailwind CSS Classes 映射

```html
<!-- 字体类 -->
<p class="font-display-lg text-display-lg">大标题</p>
<p class="font-headline-md text-headline-md">页面标题</p>
<p class="font-headline-sm text-headline-sm">区块标题</p>
<p class="font-body-lg text-body-lg">正文</p>
<p class="font-body-md text-body-md">次要正文</p>
<p class="font-label-md text-label-md">标签文字</p>
<p class="font-points-display text-points-display">积分展示</p>
```

---

## 四、圆角系统

### 4.1 圆角量表

| 名称 | 值 | Tailwind Class | 用途 |
|------|-----|----------------|------|
| sm | 0.25rem (4px) | `rounded-sm` | 小按钮/小标签 |
| DEFAULT | 0.5rem (8px) | `rounded` | 输入框/小卡片 |
| md | 0.75rem (12px) | `rounded-md` | 中等按钮 |
| lg | 1rem (16px) | `rounded-lg` | 卡片/面板 |
| xl | 1.5rem (24px) | `rounded-xl` | 主卡片/统计卡 |
| 2xl | 2rem (32px) | `rounded-2xl` | 抽屉/大面板 |
| 3xl | 2.5rem (40px) | `rounded-3xl` | 侧边栏/顶部栏 |
| full | 9999px | `rounded-full` | 圆形按钮/头像 |

### 4.2 UnoCSS 圆角配置

```js
// uno.config.ts
export default defineConfig({
  theme: {
    borderRadius: {
      'sm': '0.25rem',
      'DEFAULT': '0.5rem',
      'md': '0.75rem',
      'lg': '1rem',
      'xl': '1.5rem',
      '2xl': '2rem',
      '3xl': '2.5rem',
      'full': '9999px',
    }
  }
})
```

### 4.3 组件圆角规范

| 组件类型 | 圆角 | 示例 |
|----------|------|------|
| 侧边栏/顶部栏 | 3xl (40px) | `rounded-3xl` |
| 主内容区 | 3xl (40px) | `rounded-3xl` |
| 统计卡片 | xl (24px) | `rounded-xl` |
| 按钮 | lg/md (16px/12px) | `rounded-lg` |
| 输入框 | lg (16px) | `rounded-lg` |
| 小标签/徽章 | full (9999px) | `rounded-full` |
| 头像 | full (9999px) | `rounded-full` |
| 抽屉面板 | 3xl (40px) | `rounded-3xl` |
| 弹窗 | 2xl (32px) | `rounded-2xl` |

---

## 五、间距系统

### 5.1 间距量表

| 名称 | 值 | Tailwind Class | 用途 |
|------|-----|----------------|------|
| xs | 4px | `space-xs` / `p-xs` | 紧凑间距 |
| sm | 8px | `space-sm` / `p-sm` | 小间距 |
| md | 16px | `space-md` / `p-md` | 标准间距 |
| lg | 24px | `space-lg` / `p-lg` | 大间距 |
| xl | 40px | `space-xl` / `p-xl` | 特大间距 |
| 2xl | 64px | `space-2xl` / `p-2xl` | 页面级间距 |
| unit | 4px | - | 基础单位 |
| gutter | 20px | `gutter` | 栅格间隙 |
| container-padding | 32px | `container-padding` | 容器内边距 |

### 5.2 UnoCSS 间距配置

```js
// uno.config.ts
export default defineConfig({
  theme: {
    spacing: {
      'xs': '4px',
      'sm': '8px',
      'md': '16px',
      'lg': '24px',
      'xl': '40px',
      '2xl': '64px',
      'unit': '4px',
      'gutter': '20px',
      'container-padding': '32px',
    }
  }
})
```

### 5.3 布局间距规范

```css
/* 页面级间距 */
page-padding: 20px;        /* 内容区与视口边缘 */
section-gap: 24px;         /* 主要区块间距 */
card-gap: 16px;            /* 卡片间距 */
element-gap: 8px;          /* 元素间距 */

/* 容器内边距 */
container-padding: 32px;   /* 大容器内边距 */
card-padding: 24px;         /* 卡片内边距 */
input-padding: 16px;       /* 输入框内边距 */

/* 悬浮元素间距 */
float-gap: 16px;          /* 悬浮元素与内容间距 */
sidebar-width: 256px;      /* 侧边栏宽度 */
topbar-height: 80px;       /* 顶部栏高度 */
```

---

## 六、阴影系统

### 6.1 阴影量表

| 名称 | CSS 值 | 用途 |
|------|--------|------|
| sm | `0 2px 8px rgba(0,0,0,0.04)` | 浅层卡片 |
| DEFAULT | `0 4px 16px rgba(0,0,0,0.06)` | 标准卡片 |
| md | `0 8px 32px rgba(0,0,0,0.08)` | 悬浮卡片 |
| lg | `0 16px 48px rgba(0,0,0,0.12)` | 弹窗/抽屉 |
| xl | `0 32px 64px rgba(0,0,0,0.16)` | 重点弹窗 |
| glass | `0 8px 32px rgba(0,0,0,0.1)` | 玻璃元素 |
| glass-strong | `0 16px 48px rgba(31,38,135,0.15)` | 强玻璃效果 |

### 6.2 玻璃效果阴影

```css
/* 标准玻璃效果 */
.glass-effect {
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

/* 强玻璃效果 */
.glass-effect-strong {
  box-shadow: 0 16px 48px rgba(31, 38, 135, 0.15);
}

/* 暗色模式玻璃效果 */
.glass-effect-dark {
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}
```

### 6.3 UnoCSS 阴影配置

```js
// uno.config.ts
export default defineConfig({
  theme: {
    boxShadow: {
      'sm': '0 2px 8px rgba(0,0,0,0.04)',
      'DEFAULT': '0 4px 16px rgba(0,0,0,0.06)',
      'md': '0 8px 32px rgba(0,0,0,0.08)',
      'lg': '0 16px 48px rgba(0,0,0,0.12)',
      'xl': '0 32px 64px rgba(0,0,0,0.16)',
      'glass': '0 8px 32px rgba(0,0,0,0.1)',
      'glass-strong': '0 16px 48px rgba(31,38,135,0.15)',
      '2xl': '0 32px 64px rgba(0,0,0,0.16)',
    }
  }
})
```

### 6.4 组件阴影规范

| 组件 | 阴影 | 示例 |
|------|------|------|
| 侧边栏 | `shadow-2xl` | `shadow-[0_32px_64px_-12px_rgba(0,0,0,0.15)]` |
| 顶部栏 | `shadow-sm` | `shadow-[0_8px_32px_0_rgba(31,38,135,0.07)]` |
| 统计卡片 | `shadow-md` | `shadow-[0_8px_32px_0_rgba(0,0,0,0.04)]` |
| 按钮 | 使用 `shadow-lg` 配合主题色 | `shadow-lg shadow-primary/20` |
| 抽屉 | `shadow-[0_32px_64px_-12px_rgba(0,0,0,0.15)]` | - |
| 弹窗 | `shadow-xl` | `shadow-[0_32px_64px_-12px_rgba(0,0,0,0.2)]` |

---

## 七、玻璃效果系统

### 7.1 玻璃效果配方

```css
/* 标准玻璃效果 */
.glass {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

/* 强玻璃效果 */
.glass-strong {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.4);
}

/* 内容区玻璃效果 */
.glass-content {
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

/* 暗色模式玻璃效果 */
.glass-dark {
  background: rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}
```

### 7.2 UnoCSS 玻璃效果变体

```js
// uno.config.ts
export default defineConfig({
  theme: {
    backgroundColor: {
      'glass': 'rgba(255, 255, 255, 0.6)',
      'glass-strong': 'rgba(255, 255, 255, 0.7)',
      'glass-content': 'rgba(255, 255, 255, 0.4)',
      'glass-dark': 'rgba(0, 0, 0, 0.3)',
    },
    borderColor: {
      'glass-border': 'rgba(255, 255, 255, 0.3)',
      'glass-border-strong': 'rgba(255, 255, 255, 0.4)',
      'glass-border-dark': 'rgba(255, 255, 255, 0.1)',
    }
  }
})
```

### 7.3 背景装饰

```css
/* 渐变背景网格 */
.background-mesh {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
}

.background-mesh::before {
  content: '';
  position: absolute;
  top: -20%;
  left: -10%;
  width: 50vw;
  height: 50vw;
  border-radius: 50%;
  background: var(--color-primary) / 10%;
  blur: 100px;
  mix-blend-mode: multiply;
  opacity: 0.7;
}

.background-mesh::after {
  content: '';
  position: absolute;
  top: 30%;
  right: -20%;
  width: 60vw;
  height: 60vw;
  border-radius: 50%;
  background: var(--color-secondary) / 10%;
  blur: 120px;
  mix-blend-mode: multiply;
  opacity: 0.6;
}
```

---

## 八、组件规范

### 8.1 按钮组件

#### 8.1.1 按钮变体

| 变体 | 样式 | 用途 |
|------|------|------|
| primary | 实心主题色 + 白色文字 + 阴影 | 主要操作 |
| secondary | 主题色描边 + 主题色文字 | 次要操作 |
| ghost | 透明背景 + 主题色文字 | 辅助操作 |
| danger | 红色实心 + 白色文字 | 危险操作 |

#### 8.1.2 按钮尺寸

| 尺寸 | 高度 | 圆角 | 字体 |
|------|------|------|------|
| sm | 32px | `rounded-lg` | `text-sm` |
| md | 40px | `rounded-lg` | `text-base` |
| lg | 48px | `rounded-xl` | `text-lg` |

#### 8.1.3 按钮样式代码

```html
<!-- Primary 按钮 -->
<button class="
  h-12 px-6 rounded-xl
  bg-primary text-white
  font-semibold
  shadow-lg shadow-primary/20
  hover:brightness-110
  active:scale-98
  transition-all duration-200
  flex items-center justify-center gap-2
">
  <span class="material-symbols-outlined">check_circle</span>
  <span>确认操作</span>
</button>

<!-- Secondary 按钮 -->
<button class="
  h-12 px-6 rounded-xl
  bg-white border-2 border-primary
  text-primary font-semibold
  hover:bg-primary/5
  active:scale-98
  transition-all duration-200
  flex items-center justify-center gap-2
">
  <span>取消</span>
</button>

<!-- Ghost 按钮 -->
<button class="
  h-10 px-4 rounded-lg
  text-primary font-medium
  hover:bg-primary/10
  active:scale-98
  transition-all duration-200
  flex items-center gap-2
">
  <span class="material-symbols-outlined">settings</span>
  <span>设置</span>
</button>

<!-- Icon 按钮 -->
<button class="
  w-10 h-10 rounded-full
  bg-white/60 backdrop-blur
  border border-white/50
  text-slate-600
  hover:bg-white/80 hover:scale-105
  active:scale-95
  transition-all duration-200
  flex items-center justify-center
">
  <span class="material-symbols-outlined">dark_mode</span>
</button>
```

### 8.2 输入框组件

#### 8.2.1 输入框样式

```html
<!-- 标准输入框 -->
<div class="relative">
  <span class="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-slate-400">
    search
  </span>
  <input
    class="
      w-full h-12 pl-12 pr-4 rounded-2xl
      bg-white/40 border border-white/50
      text-slate-700 placeholder-slate-500
      focus:outline-none focus:ring-2 focus:ring-primary/20
      focus:bg-white/60 focus:border-white/70
      transition-all duration-200
    "
    placeholder="搜索..."
    type="text"
  />
</div>

<!-- 带标签输入框 -->
<div class="flex flex-col gap-2">
  <label class="text-sm font-semibold text-slate-700">店铺名称</label>
  <input
    class="
      w-full h-12 px-4 rounded-xl
      bg-white/60 border border-white/50
      text-slate-800 placeholder-slate-400
      focus:outline-none focus:ring-2 focus:ring-primary/20
      transition-all duration-200
    "
    placeholder="请输入店铺名称"
    type="text"
  />
</div>
```

#### 8.2.2 输入框状态

| 状态 | 样式变化 |
|------|----------|
| default | `bg-white/40 border-white/50` |
| focus | `bg-white/60 border-white/70 ring-2 ring-primary/20` |
| error | `bg-red-50 border-red-200 ring-2 ring-red/20` |
| disabled | `bg-slate-100 border-slate-200 text-slate-400` |

### 8.3 卡片组件

#### 8.3.1 统计卡片

```html
<div class="
  bg-white/60 backdrop-blur-xl
  border border-white/60 rounded-xl
  p-6 shadow-md
  relative overflow-hidden
  group hover:-translate-y-1
  transition-all duration-300
">
  <!-- 装饰角 -->
  <div class="
    absolute top-0 right-0
    w-32 h-32
    bg-primary/5 rounded-bl-full
    -mr-10 -mt-10
    transition-transform group-hover:scale-110
  "></div>

  <div class="flex justify-between items-start relative z-10">
    <div>
      <p class="font-label-md text-label-md text-on-surface-variant mb-2">
        店铺总数
      </p>
      <h3 class="font-points-display text-points-display text-on-surface">
        128
      </h3>
    </div>
    <div class="
      w-12 h-12 rounded-full
      bg-primary-container/10
      flex items-center justify-center
      text-primary-container
    ">
      <span class="material-symbols-outlined text-2xl" style="font-variation-settings: 'FILL' 1;">
        storefront
      </span>
    </div>
  </div>

  <div class="mt-4 flex items-center gap-2 text-sm">
    <span class="
      flex items-center text-success bg-success/10
      px-2 py-0.5 rounded-full font-medium
    ">
      <span class="material-symbols-outlined text-[16px] mr-1">trending_up</span>
      +12%
    </span>
    <span class="text-outline text-xs">较上月</span>
  </div>
</div>
```

#### 8.3.2 内容卡片

```html
<div class="
  bg-white/60 backdrop-blur-xl
  border border-white/60 rounded-xl
  p-6 shadow-md
">
  <h3 class="font-headline-sm text-headline-sm text-on-surface mb-4">
    卡片标题
  </h3>
  <div class="text-body-md text-on-surface-variant">
    卡片内容...
  </div>
</div>
```

### 8.4 弹窗/抽屉组件

#### 8.4.1 抽屉样式

```html
<!-- 抽屉容器 -->
<div class="
  fixed top-4 right-4 bottom-4 w-[520px]
  bg-white/80 backdrop-blur-3xl
  border border-white/60
  shadow-[0_32px_64px_-12px_rgba(0,0,0,0.15)]
  z-[60] flex flex-col
  rounded-[2.5rem] overflow-hidden
">
  <!-- 抽屉头部 -->
  <div class="
    flex items-center justify-between
    px-8 py-6 border-b border-slate-100
  ">
    <h2 class="text-2xl font-bold text-slate-800">抽屉标题</h2>
    <button class="
      p-2.5 rounded-full
      bg-slate-50 text-slate-400
      hover:bg-slate-100 transition-all
    ">
      <span class="material-symbols-outlined">close</span>
    </button>
  </div>

  <!-- 抽屉内容 -->
  <div class="flex-1 overflow-y-auto p-8">
    <!-- 内容 -->
  </div>

  <!-- 抽屉底部 -->
  <div class="
    px-8 py-6 border-t border-slate-100
    bg-white/40 backdrop-blur-md
    flex items-center gap-4
  ">
    <button class="flex-1 py-4 rounded-2xl bg-white border border-slate-200 text-slate-600 font-bold hover:bg-red-50 hover:text-red-500 hover:border-red-100 transition-all">
      取消
    </button>
    <button class="flex-1 py-4 rounded-2xl bg-primary text-white shadow-xl shadow-primary/20 font-bold hover:brightness-110 transition-all">
      确认
    </button>
  </div>
</div>
```

#### 8.4.2 弹窗样式

```html
<!-- 弹窗遮罩 -->
<div class="
  fixed inset-0
  bg-slate-900/10 backdrop-blur-sm
  z-50 rounded-[2rem]
  pointer-events-auto
"></div>

<!-- 弹窗主体 -->
<div class="
  fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2
  w-[480px] max-h-[80vh]
  bg-white/80 backdrop-blur-2xl
  border border-white/60
  shadow-[0_32px_64px_-12px_rgba(0,0,0,0.2)]
  z-[60] flex flex-col
  rounded-2xl overflow-hidden
">
  <!-- 内容 -->
</div>
```

### 8.5 侧边栏组件

```html
<aside class="
  m-4 w-64
  bg-white/70 backdrop-blur-xl
  border border-white/40
  rounded-3xl shadow-xl
  flex flex-col z-40
">
  <!-- Logo 区域 -->
  <div class="p-8 flex flex-col items-center border-b border-gray-100">
    <div class="w-20 h-20 rounded-2xl bg-blue-50 overflow-hidden mb-4 border-2 border-white shadow-sm">
      <img alt="Logo" class="w-full h-full object-cover" src="..."/>
    </div>
    <h2 class="text-lg font-bold text-slate-800">系统名称</h2>
    <span class="text-[10px] uppercase tracking-widest text-blue-600 font-bold mt-1 bg-blue-50 px-2 py-0.5 rounded-full">
      Role Label
    </span>
  </div>

  <!-- 导航菜单 -->
  <nav class="flex-1 px-4 py-6 flex flex-col gap-2 overflow-y-auto">
    <!-- 激活状态 -->
    <a class="
      bg-primary text-white shadow-lg shadow-primary/20
      flex items-center gap-3 px-4 py-3 rounded-2xl
      transition-all
    " href="#">
      <span class="material-symbols-outlined fill-icon">grid_view</span>
      <span class="font-semibold">菜单项</span>
    </a>

    <!-- 默认状态 -->
    <a class="
      text-slate-500 hover:bg-white hover:text-primary
      hover:shadow-sm flex items-center gap-3 px-4 py-3 rounded-2xl
      transition-all
    " href="#">
      <span class="material-symbols-outlined">storefront</span>
      <span class="font-semibold">菜单项</span>
    </a>
  </nav>

  <!-- 底部操作 -->
  <div class="p-4 mt-auto">
    <button class="
      w-full flex items-center justify-center gap-2 py-3
      rounded-2xl text-slate-400
      hover:text-red-500 hover:bg-red-50
      transition-all
    ">
      <span class="material-symbols-outlined">logout</span>
      <span class="font-semibold">退出登录</span>
    </button>
  </div>
</aside>
```

### 8.6 顶部栏组件

```html
<header class="
  h-20
  bg-white/70 backdrop-blur-xl
  border border-white/40
  rounded-3xl shadow-sm
  px-8 flex items-center justify-between
">
  <!-- 左侧 -->
  <div class="flex items-center gap-4">
    <span class="text-xl font-extrabold bg-clip-text text-transparent bg-gradient-to-r from-primary to-blue-400 uppercase tracking-tight">
      ShopAdmin
    </span>
  </div>

  <!-- 右侧 -->
  <div class="flex items-center gap-4">
    <!-- 搜索框 -->
    <div class="relative">
      <span class="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-slate-400">
        search
      </span>
      <input
        class="
          bg-slate-100/50 border-none rounded-2xl
          py-2.5 pl-12 pr-6 text-sm
          focus:ring-2 focus:ring-primary/20 w-64
        "
        placeholder="搜索..."
        type="text"
      />
    </div>

    <!-- 图标按钮组 -->
    <div class="flex items-center gap-2 bg-white/50 p-1 rounded-2xl">
      <button class="p-2 rounded-xl hover:bg-white transition-all text-slate-500">
        <span class="material-symbols-outlined">notifications</span>
      </button>
      <button class="p-2 rounded-xl hover:bg-white transition-all text-slate-500 relative">
        <span class="material-symbols-outlined">account_circle</span>
        <span class="absolute top-1 right-1 w-2 h-2 bg-danger rounded-full"></span>
      </button>
    </div>
  </div>
</header>
```

### 8.7 表格组件

```html
<div class="
  bg-white/60 backdrop-blur-xl
  border border-white/60 rounded-xl
  overflow-hidden
">
  <table class="w-full">
    <thead class="bg-slate-50/50">
      <tr>
        <th class="px-6 py-4 text-left text-sm font-semibold text-slate-600">列标题</th>
        <th class="px-6 py-4 text-left text-sm font-semibold text-slate-600">列标题</th>
      </tr>
    </thead>
    <tbody class="divide-y divide-white/40">
      <tr class="hover:bg-white/50 transition-colors">
        <td class="px-6 py-4 text-sm text-slate-700">内容</td>
        <td class="px-6 py-4 text-sm text-slate-700">内容</td>
      </tr>
    </tbody>
  </table>
</div>
```

### 8.8 标签/徽章组件

```html
<!-- 状态标签 -->
<span class="
  px-3 py-1 rounded-full
  bg-amber-50 text-amber-600
  border border-amber-100
  text-xs font-bold
">
  待审核
</span>

<!-- 主题色标签 -->
<span class="
  px-3 py-1 rounded-full
  bg-primary/10 text-primary
  text-xs font-semibold
">
  进行中
</span>

<!-- 成功标签 -->
<span class="
  px-3 py-1 rounded-full
  bg-success/10 text-success
  text-xs font-medium
  flex items-center gap-1
">
  <span class="material-symbols-outlined text-[14px]">check_circle</span>
  已完成
</span>
```

---

## 九、主题切换规范

### 9.1 主题切换按钮

```html
<!-- 固定在右下角的悬浮按钮 -->
<button class="
  fixed bottom-8 right-8
  w-14 h-14 rounded-full
  bg-white/40 backdrop-blur-xl
  border border-white/50
  shadow-[0_8px_32px_0_rgba(31,38,135,0.15)]
  flex items-center justify-center
  text-primary-container
  hover:bg-white/60 hover:scale-105
  active:scale-95
  transition-all z-50
  group
">
  <span class="material-symbols-outlined text-2xl group-hover:rotate-45 transition-transform duration-500">
    dark_mode
  </span>
</button>
```

### 9.2 主题选择面板

```html
<div class="
  fixed bottom-24 right-8
  bg-white/80 backdrop-blur-2xl
  border border-white/50
  rounded-2xl p-6
  shadow-[0_16px_48px_rgba(0,0,0,0.15)]
  z-50
">
  <!-- 颜色主题选择 -->
  <div class="flex gap-3 mb-6">
    <button class="w-10 h-10 rounded-full bg-blue-500 border-2 border-white shadow-md hover:scale-110 transition-all" title="科技蓝"></button>
    <button class="w-10 h-10 rounded-full bg-green-500 border-2 border-white shadow-md hover:scale-110 transition-all" title="清新绿"></button>
    <button class="w-10 h-10 rounded-full bg-purple-500 border-2 border-white shadow-md hover:scale-110 transition-all" title="浪漫紫"></button>
    <button class="w-10 h-10 rounded-full bg-pink-500 border-2 border-white shadow-md hover:scale-110 transition-all" title="活力粉"></button>
    <button class="w-10 h-10 rounded-full bg-gray-600 border-2 border-white shadow-md hover:scale-110 transition-all" title="纯净白"></button>
    <button class="w-10 h-10 rounded-full bg-white border-2 border-gray-200 shadow-md hover:scale-110 transition-all" title="经典黑"></button>
  </div>

  <!-- 明暗模式切换 -->
  <div class="flex items-center justify-between">
    <span class="text-sm font-medium text-slate-600">暗色模式</span>
    <button class="
      w-12 h-6 rounded-full
      bg-slate-200 relative
      transition-all duration-200
    ">
      <span class="
        absolute top-1 left-1
        w-4 h-4 rounded-full
        bg-white shadow
        transition-all duration-200
      "></span>
    </button>
  </div>
</div>
```

### 9.3 主题色 CSS 变量应用

```css
/* 应用主题色的方式 */
.card {
  background: linear-gradient(
    135deg,
    rgba(var(--theme-rgb-primary), 0.1) 0%,
    rgba(var(--theme-rgb-primary), 0.05) 100%
  );
}

/* 使用主题色变量 */
.button-primary {
  background-color: var(--theme-primary);
  box-shadow: 0 8px 32px rgba(var(--theme-rgb-primary), 0.3);
}
```

---

## 十、Material Symbols 图标规范

### 10.1 图标字体引用

```html
<!-- Google Material Symbols -->
<link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet"/>

<style>
  .material-symbols-outlined {
    font-variation-settings:
      'FILL' 0,
      'wght' 400,
      'GRAD' 0,
      'opsz' 24;
  }
  .fill-icon {
    font-variation-settings:
      'FILL' 1,
      'wght' 400,
      'GRAD' 0,
      'opsz' 24;
  }
</style>
```

### 10.2 常用图标类

```html
<!-- 填充图标（激活状态） -->
<span class="material-symbols-outlined fill-icon">home</span>

<!-- 描边图标（默认状态） -->
<span class="material-symbols-outlined">home</span>
```

### 10.3 常用图标映射

| 功能 | 图标名称 | 用途 |
|------|----------|------|
| 仪表盘 | `dashboard` | 仪表盘入口 |
| 店铺 | `storefront` | 店铺管理 |
| 用户 | `group` | 用户管理 |
| 个人中心 | `person` | 个人中心 |
| 设置 | `settings` | 系统设置 |
| 退出 | `logout` | 退出登录 |
| 搜索 | `search` | 搜索框 |
| 消息 | `notifications` | 通知消息 |
| 主题 | `palette` | 主题切换 |
| 账户 | `account_circle` | 用户账户 |
| 订单 | `receipt_long` | 订单管理 |
| 积分 | `stars` | 积分相关 |
| 趋势 | `trending_up` / `trending_down` | 数据趋势 |
| 添加 | `add` | 新增操作 |
| 编辑 | `edit` | 编辑操作 |
| 删除 | `delete` | 删除操作 |
| 关闭 | `close` | 关闭弹窗 |
| 确认 | `check_circle` | 确认操作 |
| 取消 | `cancel` | 取消操作 |
| 提示 | `info` | 信息提示 |

---

## 十一、reka-ui + UnoCSS 集成规范

### 11.1 项目配置

```typescript
// uno.config.ts
import { defineConfig } from 'unocss'

export default defineConfig({
  presets: [
    presetWind(),
    presetIcons({
      scale: 1.2,
      cjs: true,
    }),
  ],
  theme: {
    colors: {
      // 完整颜色系统
      surface: '#faf8ff',
      // ... 所有颜色变量
    },
    borderRadius: {
      'sm': '0.25rem',
      'DEFAULT': '0.5rem',
      'md': '0.75rem',
      'lg': '1rem',
      'xl': '1.5rem',
      '2xl': '2rem',
      '3xl': '2.5rem',
      'full': '9999px',
    },
    spacing: {
      'xs': '4px',
      'sm': '8px',
      'md': '16px',
      'lg': '24px',
      'xl': '40px',
    },
    boxShadow: {
      'glass': '0 8px 32px rgba(0,0,0,0.1)',
    },
    fontFamily: {
      'display': ['Manrope', 'system-ui', 'sans-serif'],
      'body': ['Inter', 'system-ui', 'sans-serif'],
    },
  },
  shortcuts: {
    // 常用快捷类
    'glass-card': 'bg-white/60 backdrop-blur-xl border border-white/60 rounded-xl shadow-md',
    'glass-btn': 'bg-white/40 backdrop-blur border border-white/50 rounded-lg hover:bg-white/60 transition-all',
    'input-base': 'bg-white/40 border border-white/50 rounded-xl px-4 py-3 focus:outline-none focus:ring-2 focus:ring-primary/20',
  },
})
```

### 11.2 reka-ui 组件使用示例

```tsx
import { Button, Card, Input } from 'reka-ui'
import type { VariantProps } from 'class-variance-authority'

// Button 组件使用
<Button
  variant="default"
  size="md"
  class="bg-primary text-white shadow-lg shadow-primary/20"
>
  确认操作
</Button>

// Card 组件使用
<Card class="glass-card p-6">
  <CardTitle class="font-headline-sm text-headline-sm">卡片标题</CardTitle>
  <CardContent>卡片内容</CardContent>
</Card>
```

### 11.3 样式应用示例

```html
<!-- 完整页面布局示例 -->
<div class="min-h-screen bg-surface relative">
  <!-- 背景装饰 -->
  <div class="fixed inset-0 z-0 pointer-events-none overflow-hidden">
    <div class="absolute -top-[20%] -left-[10%] w-[50vw] h-[50vw] rounded-full bg-primary/10 blur-[100px] mix-blend-multiply opacity-70"></div>
    <div class="absolute top-[30%] -right-[20%] w-[60vw] h-[60vw] rounded-full bg-secondary/10 blur-[120px] mix-blend-multiply opacity-60"></div>
  </div>

  <!-- 侧边栏 -->
  <nav class="glass-card m-4 w-64 h-[calc(100vh-32px)] rounded-3xl flex flex-col">
    <div class="p-6 border-b border-white/40">
      <div class="w-12 h-12 rounded-xl bg-gradient-to-br from-primary to-secondary flex items-center justify-center text-white">
        <span class="material-symbols-outlined fill-icon">generating_tokens</span>
      </div>
    </div>
    <div class="flex-1 p-4 space-y-2">
      <a class="flex items-center gap-3 px-4 py-3 rounded-xl bg-primary/10 text-primary" href="#">
        <span class="material-symbols-outlined fill-icon">dashboard</span>
        <span>Dashboard</span>
      </a>
    </div>
  </nav>

  <!-- 主内容区 -->
  <main class="absolute top-4 left-72 right-4 bottom-4 glass-card rounded-3xl p-8">
    <h1 class="font-display-lg text-display-lg text-on-surface">欢迎回来</h1>
  </main>

  <!-- 主题切换按钮 -->
  <button class="fixed bottom-8 right-8 w-14 h-14 rounded-full glass-btn flex items-center justify-center z-50">
    <span class="material-symbols-outlined">dark_mode</span>
  </button>
</div>
```

---

## 十二、响应式断点

### 12.1 断点定义

| 断点 | 宽度 | Tailwind | 用途 |
|------|------|----------|------|
| sm | 640px | `sm:` | 手机横屏 |
| md | 768px | `md:` | 平板 |
| lg | 1024px | `lg:` | 小屏电脑 |
| xl | 1280px | `xl:` | 标准电脑 |
| 2xl | 1536px | `2xl:` | 大屏显示器 |

### 12.2 响应式布局规范

```html
<!-- 栅格响应式 -->
<div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-lg">
  <!-- 卡片内容 -->
</div>

<!-- 隐藏/显示响应式 -->
<button class="md:hidden text-slate-500 hover:bg-white/20 p-2 rounded-lg">
  <span class="material-symbols-outlined">menu</span>
</button>
```

---

## 十三、设计检查清单

### 13.1 新页面开发检查

- [ ] 使用玻璃效果背景 (`bg-white/60 backdrop-blur-xl`)
- [ ] 侧边栏和顶部栏悬浮且不贴边 (`m-4 rounded-3xl`)
- [ ] 颜色使用 CSS 变量或 Tailwind 主题色
- [ ] 字体使用定义的字体类 (`font-headline-md`, `text-body-lg` 等)
- [ ] 圆角使用定义的系统值 (`rounded-xl`, `rounded-2xl` 等)
- [ ] 阴影使用定义的系统值 (`shadow-md`, `shadow-lg` 等)
- [ ] 间距使用定义的系统值 (`gap-lg`, `p-xl` 等)
- [ ] 图标使用 Material Symbols Outlined
- [ ] 按钮有 hover/active 状态
- [ ] 输入框有 focus 状态
- [ ] 主题切换功能可用

### 13.2 设计一致性检查

- [ ] 所有玻璃卡片使用相同的模糊值 (12px-16px)
- [ ] 所有玻璃边框使用相同的透明度 (0.3)
- [ ] 所有悬浮元素使用相同的圆角 (3xl = 40px)
- [ ] 所有按钮使用相同的过渡时长 (200ms-300ms)
- [ ] 所有图标使用相同的大小系统 (16px, 20px, 24px, 32px)
- [ ] 字体权重遵循定义的系统 (600 for headings, 400 for body)