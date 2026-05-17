# 积分商城系统 - API 重构方案

## 一、现状分析

### 1.1 当前问题

当前后端接口按**角色**分组，存在以下问题：

| 问题 | 说明 |
|------|------|
| 功能重叠 | 运营和管理员都有"用户管理"相关接口（`/operator/users/*` 和 `/admin/users/*`） |
| 扩展性差 | 新功能继续堆砌到现有 Controller，导致单个文件过长 |
| RESTful 不规范 | 路径设计不一致，如 `GET /operator/shop` vs `GET /user/products` |
| 权限耦合 | 接口路径包含角色信息，前端需要根据不同角色调用不同模块 |
| 代码重复 | 认证、分页等逻辑散落各处 |

### 1.2 当前接口结构

```
/api/auth/*      - 认证（登录/注册/用户信息/密码）
/api/admin/*     - 管理员（店铺审核/用户管理/邀请码）
/api/operator/*  - 运营（店铺/商品/订单/用户/积分/分类）
/api/user/*      - 用户（商品浏览/订单/积分/地址）
/api/file/*      - 文件上传
```

### 1.3 前端 API 模块对应

| 前端文件 | 调用后端前缀 | 主要功能 |
|---------|------------|---------|
| `api/auth.ts` | `/auth/*` | 登录/注册/修改密码/用户信息 |
| `api/admin.ts` | `/admin/*` | 店铺审核/用户管理/邀请码 |
| `api/operator.ts` | `/operator/*` | 店铺/商品/订单/用户/积分/分类 |
| `api/user.ts` | `/user/*` | 商品浏览/订单/积分/地址 |
| `api/upload.ts` | `/file/*` | 文件上传/删除/访问 |

---

## 二、重构方案

### 2.1 目标：按业务域（Resource）分组

将接口按**资源类型**重组，权限通过 RBAC 控制，不再体现在路径中。

### 2.2 重组后的 API 结构

```
# 认证（保持不变）
/api/auth/*                     # 认证相关
  ├── POST /login               # 登录
  ├── POST /register            # 注册
  ├── GET  /info                # 当前用户信息
  ├── PUT  /password            # 修改密码
  ├── PUT  /info                # 修改个人信息
  └── POST /logout              # 登出

# 店铺资源
/api/shops/*
  ├── GET  /                    # 店铺列表（管理员）
  ├── GET  /{id}               # 店铺详情
  ├── POST /                    # 申请店铺（运营人员）
  ├── PUT  /{id}/status         # 修改营业状态（运营人员）
  └── PUT  /{id}/audit          # 审核店铺（管理员）

# 商品资源
/api/products/*
  ├── GET  /                    # 商品列表（支持 keyword, categoryId 筛选）
  ├── GET  /{id}               # 商品详情
  ├── POST /                    # 创建商品（运营人员）
  ├── PUT  /{id}               # 修改商品（运营人员）
  ├── PUT  /{id}/status         # 上架/下架（运营人员）
  └── DELETE /{id}             # 删除商品（运营人员）

# 订单资源
/api/orders/*
  ├── GET  /                    # 订单列表（根据角色返回不同范围）
  ├── GET  /{id}               # 订单详情
  ├── POST /                    # 创建订单（用户）
  ├── PUT  /{id}/close          # 取消订单（用户/运营）
  ├── PUT  /{id}/confirm        # 确认订单（运营人员）
  ├── PUT  /{id}/ship           # 发货（运营人员）
  └── PUT  /{id}/complete       # 完成订单（用户）

# 用户资源
/api/users/*
  ├── GET  /                    # 用户列表（管理员/运营可见不同范围）
  ├── GET  /{id}               # 用户详情
  ├── PUT  /{id}/status         # 启用/禁用用户（管理员）
  ├── PUT  /{id}/approve        # 审批通过用户（管理员/运营）
  ├── PUT  /{id}/reject         # 拒绝用户（管理员/运营）
  ├── PUT  /{id}/password/reset  # 重置密码（管理员/运营）
  ├── PUT  /{id}/points         # 调整积分（运营）
  └── GET  /{id}/points/log     # 积分变动记录

# 分类资源
/api/categories/*
  ├── GET  /                    # 分类列表
  ├── POST /                    # 创建分类（运营人员）
  ├── PUT  /{id}               # 修改分类（运营人员）
  └── DELETE /{id}             # 删除分类（运营人员）

# 积分资源
/api/points/*
  ├── GET  /                    # 我的积分（当前用户）
  └── GET  /log                 # 积分变动记录（当前用户）

# 地址资源
/api/addresses/*
  ├── GET  /                    # 地址列表（当前用户）
  ├── POST /                    # 添加地址
  ├── PUT  /{id}               # 修改地址
  ├── DELETE /{id}             # 删除地址
  └── PUT  /{id}/default        # 设置默认地址

# 邀请码资源
/api/invites/*
  ├── GET  /                    # 邀请码列表（管理员）
  ├── POST /                    # 生成邀请码
  └── PUT  /{id}/status         # 禁用/启用邀请码（管理员）

# 消息资源
/api/messages/*
  ├── GET  /                    # 消息列表（运营人员）
  └── PUT  /{id}/read           # 标记已读

# 文件资源（保持不变）
/api/file/*
  ├── POST /upload              # 上传文件
  ├── POST /upload/product      # 上传商品图片
  ├── GET  /{fileId}           # 获取文件信息
  ├── GET  /list                # 根据业务查询文件列表
  └── DELETE /{fileId}         # 删除文件
```

---

## 三、前端调整清单

### 3.1 API 模块重组

| 原模块 | 原路径前缀 | 目标模块 | 新路径前缀 |
|-------|----------|---------|----------|
| `auth.ts` | `/auth` | `auth.ts` | `/auth`（不变） |
| `admin.ts` | `/admin` | `shops.ts` | `/shops` |
| | | `users.ts` | `/users` |
| | | `invites.ts` | `/invites` |
| `operator.ts` | `/operator` | `shops.ts` | `/shops` |
| | | `products.ts` | `/products` |
| | | `orders.ts` | `/orders` |
| | | `categories.ts` | `/categories` |
| | | `messages.ts` | `/messages` |
| `user.ts` | `/user` | `products.ts` | `/products` |
| | | `orders.ts` | `/orders` |
| | | `points.ts` | `/points` |
| | | `addresses.ts` | `/addresses` |
| `upload.ts` | `/file` | `file.ts` | `/file`（不变） |

### 3.2 具体改动

#### 3.2.1 新建 API 模块（按资源）

```
src/api/
├── auth.ts        # 保持不变
├── shops.ts       # 新增：店铺相关 API（含审核、营业状态）
├── products.ts    # 新增：商品相关 API（含用户浏览、运营管理）
├── orders.ts      # 新增：订单相关 API（含用户下单、运营处理）
├── users.ts       # 新增：用户相关 API（含管理员/运营操作用户）
├── categories.ts  # 新增：分类相关 API
├── points.ts      # 新增：积分相关 API
├── addresses.ts   # 新增：地址相关 API
├── invites.ts     # 新增：邀请码相关 API
├── messages.ts    # 新增：消息相关 API
└── file.ts        # 保持不变
```

#### 3.2.2 路径变更对照表

| 前端操作 | 原路径 | 新路径 | 说明 |
|---------|-------|-------|------|
| 获取商品列表（用户） | `GET /user/products` | `GET /products` | 合并到 products 模块 |
| 获取商品列表（运营） | `GET /operator/products` | `GET /products` | 通过权限控制返回不同数据 |
| 店铺详情 | `GET /operator/shop` | `GET /shops/{id}` | 运营查自己店铺用 `/shops/me` |
| 申请店铺 | `POST /operator/shop` | `POST /shops` | - |
| 修改营业状态 | `PUT /operator/shop/status` | `PUT /shops/{id}/status` | - |
| 管理员审核店铺 | `PUT /admin/shops/{id}/audit` | `PUT /shops/{id}/audit` | - |
| 管理员店铺列表 | `GET /admin/shops` | `GET /shops` | 通过 role 筛选 |
| 用户订单列表 | `GET /user/orders` | `GET /orders` | 通过当前用户 token 识别 |
| 运营订单列表 | `GET /operator/orders` | `GET /orders` | 通过权限控制返回不同范围 |
| 创建订单 | `POST /user/orders` | `POST /orders` | - |
| 管理员用户列表 | `GET /admin/users` | `GET /users` | 通过 role 筛选 |
| 运营用户列表 | `GET /operator/users` | `GET /users` | 仅能看到自己店铺用户 |
| 调整积分 | `POST /operator/users/{id}/points` | `PUT /users/{id}/points` | - |
| 用户积分 | `GET /user/points` | `GET /points` | - |
| 积分记录 | `GET /user/points/log` | `GET /points/log` | - |
| 用户地址 | `GET /user/addresses` | `GET /addresses` | - |
| 分类列表 | `GET /operator/categories` | `GET /categories` | - |
| 创建分类 | `POST /operator/categories` | `POST /categories` | - |

---

## 四、后端调整清单

### 4.1 Controller 重组

| 原 Controller | 职责 | 目标 Controller |
|--------------|------|----------------|
| `AdminController` | 管理员功能 | 拆分到 `ShopController`(审核)、`UserController`(用户管理)、`InviteController`(邀请码) |
| `OperatorController` | 运营功能 | 拆分到 `ShopController`(店铺)、`ProductController`(商品)、`OrderController`(订单)、`CategoryController`(分类)、`MessageController`(消息) |
| `UserController` | 用户功能 | 合并到 `ProductController`(商品浏览)、`OrderController`(订单)、`PointsController`(积分)、`AddressController`(地址) |
| `AuthController` | 认证 | 保持不变 |
| `FileController` | 文件 | 保持不变 |

### 4.2 目录结构目标

```
com.sh.aishop.controller/
├── AuthController.java           # 认证（不变）
├── FileController.java           # 文件（不变）
├── ShopController.java           # 新增：店铺（含审核、营业状态）
├── ProductController.java        # 新增：商品（含用户浏览、运营管理）
├── OrderController.java          # 新增：订单（含用户下单、运营处理）
├── UserController.java           # 新增：用户（含管理员/运营操作用户）
├── CategoryController.java       # 新增：分类
├── PointsController.java         # 新增：积分
├── AddressController.java        # 新增：地址
├── InviteController.java         # 新增：邀请码
└── MessageController.java        # 新增：消息
```

### 4.3 权限控制方案（Spring Security RBAC）

```java
// 通过注解控制权限，替代当前在 Controller 中手动获取 userId 判断角色
@PreAuthorize("hasRole('ADMIN')")           // 仅管理员
@PreAuthorize("hasRole('OPERATOR')")       // 仅运营
@PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")  // 管理员或运营
@PreAuthorize("isAuthenticated()")         // 任何登录用户
```

---

## 五、实施计划

### 阶段一：后端重构（第 1-2 周）

1. 创建新的 Controller 结构
2. 迁移 AuthController、FileController（保持路径不变）
3. 实现 Spring Security RBAC 权限配置
4. 逐个迁移业务 Controller 到新结构
5. 保留旧 Controller 作为兼容（通过 301 重定向或内部转发）

### 阶段二：前端适配（第 2-3 周）

1. 创建新的 API 模块（按资源拆分）
2. 更新 API 调用路径
3. 配合后端兼容期进行联调
4. 逐步迁移功能模块

### 阶段三：清理（第 3-4 周）

1. 废弃旧 Controller
2. 删除旧 API 文件
3. 完善 API 文档

---

## 六、风险与应对

| 风险 | 影响 | 应对措施 |
|------|------|---------|
| 接口变更影响前端联调 | 大 | 预留 2 周兼容期，旧接口通过内部转发到新接口 |
| 权限控制逻辑复杂化 | 中 | 通过 Spring Security 注解统一处理，减少手写判断 |
| 订单/商品等业务逻辑分散 | 中 | 通过 Service 层聚合，同一资源的业务逻辑放在同一 Service |

---

## 七、建议

1. **优先实施后端权限层**：先完成 Spring Security RBAC 配置，确保权限控制到位
2. **接口文档同步更新**：使用 Swagger/OpenAPI 自动生成文档
3. **前后端并行开发**：后端新接口和前端新 API 模块可以同时进行
4. **充分测试**：特别是权限相关的测试用例要覆盖各种角色组合