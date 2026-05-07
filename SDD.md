# 积分商城系统 - 解决方案设计文档 (SDD)

## 1. 项目概述

### 1.1 项目名称
积分商城系统 (Points Mall)

### 1.2 核心定位
一个支持多店铺运营的积分兑换平台。店铺用户可申请店铺并由管理员审批通过后开展业务，向普通用户发放积分；普通用户使用积分兑换商品。

---

## 2. 用户角色

| 角色 | 说明 |
|------|------|
| 管理员 | 系统唯一管理员，管理所有店铺审批、管理所有用户 |
| 店铺用户 | 运营角色，管理自己店铺的商品、订单、用户、积分 |
| 普通用户 | 属于某店铺，只能看到和兑换本店铺商品，查看订单和积分 |

### 2.1 用户关系

```
管理员
  └── 店铺用户（通过邀请码注册，需管理员审核）
          └── 普通用户（通过邀请码注册，需店铺用户审核）
```

---

## 3. 认证模块

### 3.1 登录
- 字段：用户名、密码
- 成功后返回JWT Token

### 3.2 注册
- 字段：用户名、昵称、密码、确认密码、邀请码
- 验证码：邀请码有效性
- 注册后状态为pending，需审核后才能登录

### 3.3 全局头部
- 右上角显示：个人昵称（用户名）
- 鼠标悬浮展示下拉框：
  - 退出登录
  - 修改密码

---

## 4. 管理员端功能

### 4.1 店铺管理
- 查看所有店铺列表（名称、状态、所属店铺用户、申请时间）
- 审核店铺申请（通过/拒绝）
- 查看店铺申请历史（被拒绝后店铺用户可重新申请）

### 4.2 用户管理
- 查看所有用户（店铺用户+普通用户）
- 冻结/解冻用户（冻结后用户无法登录）
- 审核通过邀请码注册的店铺用户

### 4.3 邀请码管理
- 生成自己的邀请码
- 重新生成邀请码（旧的立刻失效）
- 未失效的邀请码可一直用来注册

---

## 5. 店铺用户端功能

### 5.1 我的店铺
- 申请店铺（填写店铺名称、简介）
- 查看自己店铺状态（pending/approved/rejected/disabled）
- 歇业/营业切换（关闭店铺后普通用户无法访问）

### 5.2 商品分类管理
- 创建/编辑/删除商品分类
- 设置分类排序

### 5.3 商品管理
- 上架/下架商品
- 商品字段：
  - 主图、详情图（多张）
  - 分类（从已有的分类中选择）
  - 发货方式：虚拟 / 实体
  - 名称、描述
  - 积分价格
  - 库存（-1=无限，0=不可下单）
  - 单人限购（0=不限）
  - 发货说明

### 5.4 订单管理
- 查看自己店铺下的所有订单
- 订单状态：已下单 → 已确认 → 已发货 → 已完成
- 操作：
  - 确认订单
  - 发货（虚拟填写发货内容富文本，实体填写快递公司+快递单号）
  - 关闭订单（退回积分）

### 5.5 用户管理
- 查看自己店铺下的普通用户
- 管理用户积分（发放/扣除）
- 查看用户积分流水记录

### 5.6 个人中心
- 维护个人信息（昵称、密码）

### 5.7 我的消息
- 接收订单通知（新订单、订单状态变更）

---

## 6. 普通用户端功能

### 6.1 商品列表
- 切换卡片/表格形式查看商品
- 兑换商品（下单扣积分）

### 6.2 我的订单
- 查看订单列表及状态
- 关闭订单（退回积分）
- 完成订单

### 6.3 个人中心
- 维护个人信息（昵称、密码）

### 6.4 地址簿管理
- 新增/编辑/删除收货地址
- 设置默认地址
- 兑换实体商品时：
  - 默认地址自动填充到表单
  - 可从地址簿选择其他地址

### 6.5 我的消息
- 积分变动通知
- 订单状态变更通知

---

## 7. 数据模型

### 7.1 用户表 (user)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| username | VARCHAR(50) | 用户名（唯一） |
| nickname | VARCHAR(50) | 昵称 |
| password | VARCHAR(255) | 密码（BCrypt加密） |
| role | ENUM | admin / operator / user |
| parent_id | BIGINT | 上级用户ID |
| status | ENUM | pending / active / frozen |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### 7.2 店铺表 (shop)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| operator_id | BIGINT | 店铺用户ID |
| name | VARCHAR(100) | 店铺名称 |
| description | TEXT | 店铺简介 |
| status | ENUM | pending / approved / rejected / disabled |
| is_active | TINYINT | 营业状态（0=歇业，1=营业） |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### 7.3 商品分类表 (category)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| shop_id | BIGINT | 店铺ID |
| name | VARCHAR(50) | 分类名称 |
| sort | INT | 排序 |
| created_at | DATETIME | 创建时间 |

### 7.4 商品表 (product)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| shop_id | BIGINT | 店铺ID |
| category_id | BIGINT | 分类ID |
| name | VARCHAR(100) | 商品名称 |
| type | ENUM | virtual / physical |
| price | INT | 积分价格 |
| stock | INT | 库存（-1=无限，0=不可下单） |
| limit_per_user | INT | 单人限购 |
| main_image | VARCHAR(255) | 主图 |
| detail_images | TEXT | 详情图（JSON数组） |
| description | TEXT | 商品描述 |
| delivery_info | TEXT | 发货说明 |
| status | ENUM | active / inactive |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### 7.5 订单表 (order)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| order_no | VARCHAR(32) | 订单号（唯一） |
| user_id | BIGINT | 普通用户ID |
| shop_id | BIGINT | 店铺ID |
| product_id | BIGINT | 商品ID |
| points | INT | 消耗积分 |
| quantity | INT | 兑换数量 |
| status | ENUM | pending / confirmed / shipped / completed / closed |
| address_id | BIGINT | 收货地址ID |
| express_company | VARCHAR(50) | 快递公司 |
| express_no | VARCHAR(100) | 快递单号 |
| delivery_content | TEXT | 发货内容（虚拟商品） |
| created_at | DATETIME | 下单时间 |
| updated_at | DATETIME | 更新时间 |
| completed_at | DATETIME | 完成时间 |
| closed_at | DATETIME | 关闭时间 |
| close_reason | VARCHAR(255) | 关闭原因 |

### 7.6 积分表 (points)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 用户ID |
| amount | INT | 积分变动 |
| balance | INT | 变动后余额 |
| type | ENUM | grant / deduct / exchange / refund |
| remark | VARCHAR(255) | 备注 |
| operator_id | BIGINT | 操作人ID |
| created_at | DATETIME | 创建时间 |

### 7.7 收货地址表 (address)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 用户ID |
| name | VARCHAR(50) | 收货人姓名 |
| phone | VARCHAR(20) | 手机号 |
| province | VARCHAR(50) | 省 |
| city | VARCHAR(50) | 市 |
| district | VARCHAR(50) | 区 |
| detail | VARCHAR(255) | 详细地址 |
| is_default | TINYINT | 是否默认 |
| created_at | DATETIME | 创建时间 |

### 7.8 邀请码表 (invite_code)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| code | VARCHAR(20) | 邀请码（唯一） |
| role | ENUM | operator / user |
| creator_id | BIGINT | 创建者ID |
| status | ENUM | active / inactive |
| created_at | DATETIME | 创建时间 |

### 7.9 消息表 (message)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 接收用户ID |
| title | VARCHAR(100) | 消息标题 |
| content | TEXT | 消息内容 |
| type | ENUM | points / order |
| related_id | BIGINT | 关联ID |
| is_read | TINYINT | 是否已读 |
| created_at | DATETIME | 创建时间 |

---

## 8. 订单状态流转

```
已下单 (pending)
    ├── 普通用户关闭 → 退回积分 → closed
    └── 店铺用户关闭 → 退回积分 → closed
           │
           ▼ 店铺用户确认
已确认 (confirmed)
    └── 只有店铺用户可以关闭 → 退回积分 → closed
           │
           ▼ 店铺用户发货
已发货 (shipped)
    ├── 虚拟商品：delivery_content 填写富文本
    └── 实体商品：express_company + express_no
           │
           ▼ 用户或店铺用户点击完成
已完成 (completed)
```

---

## 9. API 接口设计

### 9.1 认证接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/auth/login | POST | 登录（用户名+密码） |
| /api/auth/register | POST | 注册（用户名+昵称+密码+确认密码+邀请码） |
| /api/auth/password | PUT | 修改密码 |
| /api/auth/logout | POST | 退出登录 |

### 9.2 管理员接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/admin/shops | GET | 所有店铺列表 |
| /api/admin/shops/{id}/audit | PUT | 审核店铺 |
| /api/admin/users | GET | 所有用户列表 |
| /api/admin/users/{id}/status | PUT | 冻结/解冻用户 |
| /api/admin/users/{id}/approve | PUT | 审核通过店铺用户 |
| /api/admin/invite-code | GET | 获取邀请码 |
| /api/admin/invite-code | POST | 生成/重新生成邀请码 |

### 9.3 店铺用户接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/operator/shop | GET | 获取我的店铺 |
| /api/operator/shop | POST | 申请店铺 |
| /api/operator/shop/status | PUT | 切换营业/歇业 |
| /api/operator/categories | GET/POST | 获取/创建分类 |
| /api/operator/categories/{id} | PUT/DELETE | 编辑/删除分类 |
| /api/operator/products | GET/POST | 商品列表/创建 |
| /api/operator/products/{id} | GET/PUT/DELETE | 商品详情/编辑/删除 |
| /api/operator/orders | GET | 订单列表 |
| /api/operator/orders/{id}/confirm | PUT | 确认订单 |
| /api/operator/orders/{id}/ship | PUT | 发货 |
| /api/operator/orders/{id}/close | PUT | 关闭订单 |
| /api/operator/users | GET | 普通用户列表 |
| /api/operator/users/{id}/points | POST | 发放/扣除积分 |
| /api/operator/users/{id}/points/log | GET | 积分流水 |
| /api/operator/invite-code | GET | 获取邀请码 |
| /api/operator/invite-code | POST | 生成/重新生成邀请码 |
| /api/operator/users/create | POST | 创建账号 |
| /api/operator/users/import | POST | Excel导入 |
| /api/operator/users/{id}/reset-password | PUT | 重置密码 |
| /api/operator/messages | GET | 消息列表 |

### 9.4 普通用户接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/user/products | GET | 商品列表 |
| /api/user/products/{id} | GET | 商品详情 |
| /api/user/orders | POST | 下单 |
| /api/user/orders | GET | 订单列表 |
| /api/user/orders/{id} | GET | 订单详情 |
| /api/user/orders/{id}/close | PUT | 关闭订单 |
| /api/user/orders/{id}/complete | PUT | 完成订单 |
| /api/user/addresses | GET/POST | 地址列表/新增 |
| /api/user/addresses/{id} | PUT/DELETE | 编辑/删除 |
| /api/user/addresses/{id}/default | PUT | 设置默认 |
| /api/user/messages | GET | 消息列表 |
| /api/user/messages/{id}/read | PUT | 标记已读 |

---

## 10. 技术架构

### 10.1 技术栈

- **后端**：Spring Boot 3.x + MyBatis-Plus + MySQL 8.0
- **前端**：Vue 3 + Element Plus + Vite
- **认证**：JWT

### 10.2 项目结构

```
points-mall/
├── points-mall-api/        # 通用实体、枚举、工具类
├── points-mall-admin/       # 管理员后端
├── points-mall-operator/    # 店铺用户后端
├── points-mall-user/        # 普通用户后端
├── points-mall-web/         # 前端（多角色统一入口）
└── sql/                     # SQL脚本
```

---

**文档版本**：v2.0
**创建日期**：2026-05-07
**状态**：✅ 待用户确认后启动开发