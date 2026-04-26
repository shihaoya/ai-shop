# 店铺管理平台 - 开发指南

## 概述

店铺管理平台原型采用**模块化CSS架构**，将公共样式与页面特定样式分离，确保设计一致性和维护效率。

## 目录结构

```
shop/
├── shop-common.css     # 公共样式表（所有页面共享）
├── layout.html         # 布局模板
├── dashboard.html       # 仪表盘
├── categories.html      # 商品分类
├── products.html        # 商品管理
├── orders.html          # 订单管理
├── users.html           # 用户管理
├── messages.html        # 消息中心
├── myshop.html          # 我的店铺
├── profile.html         # 个人中心
└── index.html           # 首页/登录页
```

## CSS 架构

### shop-common.css

包含所有页面共享的样式定义：

| 类别 | 类名/变量 | 说明 |
|------|----------|------|
| **CSS变量** | `--theme-primary`, `--theme-rgb-primary` | 主题色系统 |
| **玻璃效果** | `.glass`, `.glass-strong`, `.glass-content` | 毛玻璃容器 |
| **极光背景** | `.bg-mesh`, `.bg-mesh-secondary` | 动态背景 |
| **布局容器** | `.sidebar-float`, `.topbar-float`, `.content-area` | 页面布局 |
| **导航菜单** | `.nav-item`, `.nav-item-icon` | 侧边导航 |
| **字体系统** | `.font-display-lg`, `.font-headline-md` 等 | 排版类 |
| **按钮** | `.btn`, `.btn-primary`, `.btn-secondary` | 按钮组件 |
| **徽章** | `.badge`, `.badge-primary` | 状态徽章 |
| **表格** | `.table-container`, `.table` | 数据表格 |
| **表单** | `.input`, `.input-error` | 输入框 |
| **动画** | `.animate-fade-in`, `.animate-slide-up` | 过渡动画 |
| **滚动条** | `.custom-scrollbar` | 自定义滚动条 |
| **主题切换** | `.theme-btn`, `.theme-panel` | 主题面板 |

### 主题系统

```css
/* 默认主题：科技蓝 */
--theme-primary: #004ac6;
--theme-secondary: #2563eb;

/* 暗色模式 */
[data-mode="dark"] {
  --color-surface: #121218;
  --color-on-surface: #f0f0fb;
}
```

### 预定义主题色

| 主题 | 属性值 | 描述 |
|------|--------|------|
| 科技蓝 | `data-theme=""` 或不设置 | 默认蓝色主题 |
| 清新绿 | `data-theme="fresh-green"` | 绿色主题 |
| 浪漫紫 | `data-theme="romantic-purple"` | 紫色主题 |
| 活力粉 | `data-theme="vibrant-pink"` | 粉色主题 |
| 纯净白 | `data-theme="pure-white"` | 深色主题（店铺风格） |
| 经典黑 | `data-theme="classic-black"` | 纯黑主题 |

## 页面开发规范

### 1. 正确的资源引用顺序

```html
<head>
  <!-- 1. Google Fonts -->
  <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@400;500;600;700;800&family=Inter:wght@400;500;600&display=swap" rel="stylesheet"/>

  <!-- 2. Material Symbols -->
  <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet"/>

  <!-- 3. TailwindCSS CDN -->
  <script src="https://cdn.tailwindcss.com"></script>

  <!-- 4. Vue3 CDN -->
  <script src="https://unpkg.com/vue@3.4.0/dist/vue.global.js"></script>

  <!-- 5. 公共样式表（必须） -->
  <link rel="stylesheet" href="shop-common.css"/>

  <!-- 6. 页面特定样式（仅包含公共CSS未覆盖的样式） -->
  <style>
    /* 页面特定样式 */
  </style>
</head>
```

### 2. 页面特定样式规则

**仅在以下情况使用内联样式：**
- 页面独有的动画效果（如 `bubble` 动画）
- 页面独有的布局调整
- 覆盖公共样式的例外情况

**禁止在内联样式中重复定义：**
- CSS变量（已在 `shop-common.css` 中定义）
- 玻璃效果类（`.glass`, `.glass-strong`）
- 字体类（`.font-display-lg` 等）
- 按钮、徽章、导航等通用组件样式

### 3. 示例：新增页面

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="utf-8"/>
  <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
  <title>新页面 - 店铺管理平台</title>

  <!-- Google Fonts -->
  <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@400;500;600;700;800&family=Inter:wght@400;500;600&display=swap" rel="stylesheet"/>
  <!-- Material Symbols -->
  <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet"/>
  <!-- TailwindCSS CDN -->
  <script src="https://cdn.tailwindcss.com"></script>
  <!-- Vue3 CDN -->
  <script src="https://unpkg.com/vue@3.4.0/dist/vue.global.js"></script>
  <!-- 公共样式表 -->
  <link rel="stylesheet" href="shop-common.css"/>

  <!-- 页面特定样式（仅必要情况） -->
  <style>
    /* 页面独有的特殊样式 */
  </style>

  <script>
    tailwind.config = {
      darkMode: "class",
      theme: {
        extend: {
          colors: {
            primary: "#004ac6",
            secondary: "#712ae2",
          }
        }
      }
    };
  </script>
</head>
<body>
  <!-- 使用公共样式类 -->
  <div class="sidebar-float">
    <nav class="nav-item active">
      <span class="material-symbols-outlined nav-item-icon">home</span>
      首页
    </nav>
  </div>

  <div class="content-area">
    <!-- 使用玻璃效果 -->
    <div class="glass rounded-2xl p-6">
      内容
    </div>
  </div>
</body>
</html>
```

## 主题切换实现

```javascript
// 设置主题色
document.documentElement.setAttribute('data-theme', 'fresh-green');

// 切换暗色模式
document.documentElement.setAttribute('data-mode', 'dark');

// 移除主题/暗色模式
document.documentElement.removeAttribute('data-theme');
document.documentElement.removeAttribute('data-mode');
```

## 设计令牌 (Design Tokens)

### 颜色系统

```css
/* 主色 */
--color-primary: #004ac6;
--color-secondary: #712ae2;
--color-on-primary: #ffffff;

/* 表面色 */
--color-on-surface: #191b23;
--color-on-surface-variant: #434655;
--color-outline: #737686;

/* 语义色 */
--color-success: #10B981;
--color-warning: #F59E0B;
--color-danger: #EF4444;

/* 玻璃效果 */
--color-glass-bg: rgba(255, 255, 255, 0.6);
--color-glass-border: rgba(255, 255, 255, 0.3);
```

### 字体系统

```css
--font-display: 'Manrope', system-ui, sans-serif;  /* 大标题 */
--font-headline: 'Manrope', system-ui, sans-serif; /* 副标题 */
--font-body: 'Inter', system-ui, sans-serif;        /* 正文 */
--font-label: 'Inter', system-ui, sans-serif;       /* 标签 */
```

## 常见问题

### Q: 为什么背景样式在本地文件和公共CSS中不一致？

**A:** 某些页面（如 `layout.html`）使用了自定义的 `bubble` 动画背景，而 `shop-common.css` 使用 `bg-mesh` 极光背景。如果需要统一，请选择其中一种背景方案。

### Q: 如何添加新的主题色？

**A:** 在 `shop-common.css` 中添加新的 `[data-theme="xxx"]` 选择器：

```css
[data-theme="ocean-blue"] {
  --theme-primary: #0ea5e9;
  --theme-secondary: #0284c7;
  --theme-rgb-primary: 14, 165, 233;
}
```

### Q: 页面特定样式应该放在哪里？

**A:** 保持在各自的 HTML 文件的 `<style>` 标签中。如果多个页面需要相同的特定样式，考虑将其添加到 `shop-common.css` 中。

## 维护指南

### CSS 值修改流程

1. **公共样式** → 修改 `shop-common.css`
2. **单页样式** → 修改对应 HTML 文件的 `<style>` 标签
3. **主题变量** → 修改 `shop-common.css` 中的 `:root` 和 `[data-theme]` 选择器

### 更新公共CSS后验证

1. 检查所有页面是否正确引用 `shop-common.css`
2. 在浏览器中测试每个主题色
3. 验证暗色模式切换
4. 检查页面特定样式是否有冲突

---

最后更新：2024年
