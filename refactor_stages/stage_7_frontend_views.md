# 阶段7：前端Views重组

## 目标
将前端Views按业务模块重组，与后端模块对应。

## 前提条件
- 阶段6已完成（前端API重组）

## 前端Views重组方案

### 原Views结构
```
views/
├── auth/          # 登录注册
├── admin/         # 管理员页面
├── operator/      # 店铺用户页面
├── user/          # 普通用户页面
└── (其他组件)
```

### 重组后结构
```
views/
├── auth/          # 登录注册（保持）
│   ├── Login.vue
│   └── Register.vue
├── shop/          # 店铺管理
│   └── ShopManage.vue
├── product/       # 商品管理
│   ├── ProductManage.vue
│   └── CategoryManage.vue
├── order/         # 订单管理
│   ├── OrderManage.vue (运营商)
│   └── OrderList.vue (用户)
├── user/          # 用户/积分/地址
│   ├── UserPoints.vue
│   ├── AddressList.vue
│   ├── Profile.vue
│   └── PointsInfo.vue
├── message/       # 消息
│   ├── MessageManage.vue (运营商)
│   └── MessageList.vue (用户)
└── admin/         # 管理员页面（保持）
    ├── ShopReview.vue
    └── UserManage.vue
```

## 执行步骤

### Step 7.1：创建Views模块目录
```bash
cd code/frontend/src
mkdir -p views/shop views/product views/order views/user views/message
```

### Step 7.2：移动店铺页面
从 `views/operator/ShopManage.vue` 移动到 `views/shop/`

### Step 7.3：移动商品/分类页面
从 `views/operator/ProductManage.vue` → `views/product/`
从 `views/operator/CategoryManage.vue` → `views/product/`

### Step 7.4：移动订单页面
从 `views/operator/OrderManage.vue` → `views/order/OrderManage.vue`
从 `views/user/OrderList.vue` → `views/order/OrderList.vue`

### Step 7.5：移动用户/积分/地址页面
从 `views/operator/UserPoints.vue` → `views/user/`
从 `views/user/AddressList.vue` → `views/user/`
从 `views/user/Profile.vue` → `views/user/`
从 `views/user/PointsInfo.vue` → `views/user/`

### Step 7.6：移动消息页面
从 `views/operator/MessageManage.vue` → `views/message/MessageManage.vue`
从 `views/user/MessageList.vue` → `views/message/MessageList.vue`

### Step 7.7：更新路由
更新 `router/index.ts` 中的组件引用路径

### Step 7.8：更新API引用
更新各Views中import的API路径

### Step 7.9：验证
```bash
cd code/frontend
pnpm dev
```

## 完成标准
- Views已重组到各模块目录
- 路由正确
- 页面能正常显示

## 回滚方案
```bash
git checkout -- src/views/
```