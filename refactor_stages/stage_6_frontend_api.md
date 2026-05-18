# 阶段6：前端API重组

## 目标
将前端API按业务模块重组，与后端模块对应。

## 前提条件
- 阶段5已完成（后端验证通过）

## 前端API重组方案

### 原API结构
```
api/
├── auth.ts       # 认证（登录/注册/密码）
├── admin.ts      # 管理员
├── operator.ts   # 店铺用户
├── user.ts       # 普通用户
├── upload.ts     # 文件上传
└── request.ts    # axios封装
```

### 重组后结构
```
api/
├── auth/         # 认证模块
│   ├── index.ts  # 登录/注册/密码
│   └── types.ts  # 相关类型
├── shop/         # 店铺模块
│   ├── index.ts  # 店铺管理
│   └── types.ts
├── product/      # 商品模块
│   ├── index.ts  # 商品/分类
│   └── types.ts
├── order/        # 订单模块
│   ├── index.ts  # 订单
│   └── types.ts
├── user/         # 用户模块
│   ├── index.ts  # 用户/积分/地址
│   └── types.ts
├── message/      # 消息模块
│   ├── index.ts
│   └── types.ts
├── file/         # 文件模块
│   ├── index.ts
│   └── types.ts
├── admin/        # 管理员模块（独立）
│   ├── index.ts
│   └── types.ts
└── request.ts    # axios封装（保持不变）
```

## 执行步骤

### Step 6.1：创建API模块目录
```bash
cd code/frontend/src
mkdir -p api/auth api/shop api/product api/order api/user api/message api/file api/admin
```

### Step 6.2：拆分auth.ts
移动 `api/auth.ts` 内容到 `api/auth/index.ts`

### Step 6.3：拆分operator.ts
从 `api/operator.ts` 拆分出：
- 店铺相关 → `api/shop/index.ts`
- 商品/分类相关 → `api/product/index.ts`
- 订单相关 → `api/order/index.ts`
- 用户/积分相关 → `api/user/index.ts`
- 消息相关 → `api/message/index.ts`

### Step 6.4：拆分user.ts
从 `api/user.ts` 拆分出：
- 商品浏览（如有） → `api/product/index.ts`
- 订单相关 → `api/order/index.ts`
- 地址相关 → `api/user/index.ts`
- 积分相关 → `api/user/index.ts`
- 消息相关 → `api/message/index.ts`

### Step 6.5：拆分admin.ts
移动到 `api/admin/index.ts`

### Step 6.6：移动upload.ts
移动到 `api/file/index.ts`

### Step 6.7：更新引用

更新所有使用API的文件：
```bash
# 查找使用了旧API路径的文件
grep -r "from.*api/auth" --include="*.ts" .
grep -r "from.*api/operator" --include="*.ts" .
grep -r "from.*api/user" --include="*.ts" .
```

更新import路径

### Step 6.8：类型检查
```bash
cd code/frontend
pnpm type-check
```

## 完成标准
- API文件已重组到各模块
- 所有import正确
- 类型检查通过

## 回滚方案
```bash
git checkout -- src/api/
```

## 注意事项
- 保持API路径不变（/api/auth, /api/operator等），只改变文件位置
- 抽取公共类型到各模块的types.ts
- 更新stores中使用API的文件