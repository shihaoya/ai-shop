# 积分商城系统 - 解决方案设计文档 (SDD)

## 1. 项目概述

### 1.1 项目名称
积分商城系统 (Points Mall)

### 1.2 核心定位
一个支持多店铺运营的积分兑换平台。店铺用户可申请店铺并由管理员审批通过后开展业务，向普通用户发放积分；普通用户使用积分兑换商品。

### 1.3 技术规范

| 规范 | 说明 |
|------|------|
| 后端 | Spring Boot + MyBatis-Plus + MySQL |
| 数据库 | MySQL |
| ID生成 | 雪花ID，传递给前端时转为String（避免精度丢失） |
| 删除方式 | 逻辑删除 |
| 外键 | 不使用物理外键 |
| 唯一键 | 部分字段不使用唯一键（逻辑删除后无法保证唯一性） |
| 枚举类型 | 不使用，使用tinyint并添加备注说明含义 |

> **前端技术栈**：详见 `docs/开发文档.md`

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
- 验证：邀请码有效性
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
- 审核通过邀请码注册的店铺用户（approve操作）

### 4.3 邀请码管理
- 生成自己的邀请码
- 重新生成邀请码（旧的立刻失效）
- 未失效的邀请码可一直用来注册

---

## 5. 店铺用户端功能

### 5.1 我的店铺
- 申请店铺（填写店铺名称、简介）
- 查看自己店铺状态（pending/approved/rejected/disabled）
- 歇业/营业切换
  - **关闭后普通用户可以访问本店商品，仅无法下单**
  - 其他功能（订单管理、用户管理等）正常使用

### 5.2 商品分类管理
- 创建/编辑/删除商品分类
- 设置分类排序

### 5.3 商品管理
- 上架/下架商品
- 商品列表支持**卡片/表格**切换展示
- 商品字段：
  - 主图、详情图（多张）
  - 分类（从已有的分类中选择）
  - 发货方式：虚拟 / 实体
  - 名称
  - 描述
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
  - 完成订单

### 5.5 用户管理
- 查看自己店铺下的普通用户
- 管理用户积分（发放/扣除）
- 查看用户积分流水记录
- **审核邀请码注册的用户**（approve操作，审核通过后才能登录）

### 5.6 个人中心
- 维护个人信息（昵称、密码）

### 5.7 邀请码管理
- 生成自己的邀请码
- 重新生成邀请码（旧的立刻失效）
- 未失效的邀请码可一直用来注册

### 5.8 我的消息
- 接收订单通知（新订单、订单状态变更）

---

## 6. 普通用户端功能

### 6.1 商品列表
- 商品列表支持**卡片/表格**切换展示
- 兑换商品（下单扣积分）
- 只能看到营业中店铺的商品

### 6.2 我的订单
- 查看订单列表及状态
- 关闭订单（退回积分）
- 完成订单

### 6.3 个人中心
- 维护个人信息（昵称、密码）
- 查看当前积分余额
- 查看积分流水记录

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
| id | BIGINT | 主键（雪花ID，转String给前端） |
| username | VARCHAR(50) | 用户名 |
| nickname | VARCHAR(50) | 昵称 |
| password | VARCHAR(255) | 密码（BCrypt加密） |
| role | TINYINT | 角色：1=管理员，2=店铺用户，3=普通用户 |
| parent_id | BIGINT | 上级用户ID，管理员时为0 |
| status | TINYINT | 状态：1=待审核，2=正常，3=已冻结 |
| deleted | TINYINT | 删除标记：0=未删除，1=已删除 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### 7.2 店铺表 (shop)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键（雪花ID，转String给前端） |
| operator_id | BIGINT | 店铺用户ID |
| name | VARCHAR(100) | 店铺名称 |
| description | TEXT | 店铺简介 |
| status | TINYINT | 状态：1=待审核，2=已通过，3=已拒绝，4=已禁用 |
| is_active | TINYINT | 营业状态：0=歇业，1=营业 |
| deleted | TINYINT | 删除标记：0=未删除，1=已删除 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### 7.3 商品分类表 (category)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键（雪花ID，转String给前端） |
| shop_id | BIGINT | 店铺ID |
| name | VARCHAR(50) | 分类名称 |
| sort | INT | 排序 |
| deleted | TINYINT | 删除标记：0=未删除，1=已删除 |
| created_at | DATETIME | 创建时间 |

### 7.4 商品表 (product)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键（雪花ID，转String给前端） |
| shop_id | BIGINT | 店铺ID |
| category_id | BIGINT | 分类ID |
| name | VARCHAR(100) | 商品名称 |
| type | TINYINT | 发货方式：1=虚拟，2=实体 |
| price | INT | 积分价格 |
| stock | INT | 库存（-1=无限，0=不可下单） |
| limit_per_user | INT | 单人限购，0=不限 |
| main_image | VARCHAR(255) | 主图 |
| detail_images | TEXT | 详情图（JSON数组） |
| description | TEXT | 商品描述 |
| delivery_info | TEXT | 发货说明 |
| status | TINYINT | 状态：1=上架，2=下架 |
| deleted | TINYINT | 删除标记：0=未删除，1=已删除 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### 7.5 订单表 (order)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键（雪花ID，转String给前端） |
| order_no | VARCHAR(32) | 订单号（唯一） |
| user_id | BIGINT | 普通用户ID |
| shop_id | BIGINT | 店铺ID |
| product_id | BIGINT | 商品ID |
| points | INT | 消耗积分 |
| quantity | INT | 兑换数量 |
| status | TINYINT | 状态：1=已下单，2=已确认，3=已发货，4=已完成，5=已关闭 |
| deleted | TINYINT | 删除标记：0=未删除，1=已删除 |
| created_at | DATETIME | 下单时间 |
| updated_at | DATETIME | 更新时间 |
| completed_at | DATETIME | 完成时间 |
| closed_at | DATETIME | 关闭时间 |
| close_reason | VARCHAR(255) | 关闭原因 |

### 7.5.1 订单收货地址（订单创建时快照，不关联address表）

| 字段 | 类型 | 说明 |
|------|------|------|
| receiver_name | VARCHAR(50) | 收货人姓名 |
| receiver_phone | VARCHAR(20) | 手机号 |
| receiver_province | VARCHAR(50) | 省 |
| receiver_city | VARCHAR(50) | 市 |
| receiver_district | VARCHAR(50) | 区 |
| receiver_detail | VARCHAR(255) | 详细地址 |
| express_company | VARCHAR(50) | 快递公司 |
| express_no | VARCHAR(100) | 快递单号 |
| delivery_content | TEXT | 发货内容（虚拟商品） |

**重要：订单不存储address_id，下单时直接存储收货地址快照，修改地址簿不影响历史订单**

### 7.6 积分表 (points)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键（雪花ID，转String给前端） |
| user_id | BIGINT | 用户ID |
| amount | INT | 积分变动 |
| balance | INT | 变动后余额 |
| type | TINYINT | 类型：1=发放，2=扣除，3=兑换，4=退款 |
| remark | VARCHAR(255) | 备注 |
| operator_id | BIGINT | 操作人ID |
| deleted | TINYINT | 删除标记：0=未删除，1=已删除 |
| created_at | DATETIME | 创建时间 |

### 7.7 收货地址表 (address)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键（雪花ID，转String给前端） |
| user_id | BIGINT | 用户ID |
| name | VARCHAR(50) | 收货人姓名 |
| phone | VARCHAR(20) | 手机号 |
| province | VARCHAR(50) | 省 |
| city | VARCHAR(50) | 市 |
| district | VARCHAR(50) | 区 |
| detail | VARCHAR(255) | 详细地址 |
| is_default | TINYINT | 是否默认：0=否，1=是 |
| deleted | TINYINT | 删除标记：0=未删除，1=已删除 |
| created_at | DATETIME | 创建时间 |

### 7.8 邀请码表 (invite_code)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键（雪花ID，转String给前端） |
| code | VARCHAR(20) | 邀请码 |
| role | TINYINT | 角色：2=店铺用户，3=普通用户 |
| creator_id | BIGINT | 创建者ID |
| status | TINYINT | 状态：1=有效，2=已作废 |
| deleted | TINYINT | 删除标记：0=未删除，1=已删除 |
| created_at | DATETIME | 创建时间 |

### 7.9 消息表 (message)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键（雪花ID，转String给前端） |
| user_id | BIGINT | 接收用户ID |
| title | VARCHAR(100) | 消息标题 |
| content | TEXT | 消息内容 |
| type | TINYINT | 类型：1=积分通知，2=订单通知 |
| related_id | BIGINT | 关联ID |
| is_read | TINYINT | 是否已读：0=未读，1=已读 |
| deleted | TINYINT | 删除标记：0=未删除，1=已删除 |
| created_at | DATETIME | 创建时间 |

---

## 8. 订单状态流转

```
已下单 (1)
    ├── 普通用户关闭 → 退回积分 → closed(5)
    └── 店铺用户关闭 → 退回积分 → closed(5)
           │
           ▼ 店铺用户确认
已确认 (2)
    └── 只有店铺用户可以关闭 → 退回积分 → closed(5)
           │
           ▼ 店铺用户发货
已发货 (3)
    ├── 虚拟商品：delivery_content 填写富文本
    └── 实体商品：express_company + express_no
           │
           ▼ 用户或店铺用户点击完成
已完成 (4)
```

**状态枚举**：1=已下单，2=已确认，3=已发货，4=已完成，5=已关闭

---

## 9. API 接口设计

### 9.1 认证接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/auth/login | POST | 登录（用户名+密码） |
| /api/auth/register | POST | 注册（用户名+昵称+密码+确认密码+邀请码） |
| /api/auth/password | PUT | 修改密码 |
| /api/auth/logout | POST | 退出登录 |
| /api/auth/info | GET | 获取当前用户信息（含积分） |

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
| /api/operator/orders/{id}/complete | PUT | 完成订单 |
| /api/operator/users | GET | 普通用户列表 |
| /api/operator/users/{id}/points | POST | 发放/扣除积分 |
| /api/operator/users/{id}/points/log | GET | 积分流水 |
| /api/operator/users/{id}/approve | PUT | 审核通过普通用户 |
| /api/operator/invite-code | GET | 获取邀请码 |
| /api/operator/invite-code | POST | 生成/重新生成邀请码 |
| /api/operator/users/create | POST | 创建账号 |
| /api/operator/users/import | POST | Excel导入 |
| /api/operator/users/{id}/reset-password | PUT | 重置密码 |

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
| /api/user/points | GET | 获取当前积分余额 |
| /api/user/points/log | GET | 获取积分流水 |
| /api/user/addresses | GET/POST | 地址列表/新增 |
| /api/user/addresses/{id} | PUT/DELETE | 编辑/删除 |
| /api/user/addresses/{id}/default | PUT | 设置默认 |
| /api/user/messages | GET | 消息列表 |
| /api/user/messages/{id}/read | PUT | 标记已读 |

---

## 10. 技术架构

### 10.1 后端技术栈

- **框架**：Spring Boot 3.x + MyBatis-Plus + MySQL 8.0
- **认证**：JWT
- **ID生成**：雪花算法（传前端时转String）

### 10.2 数据库规范

- 不使用物理外键，逻辑关联
- 不使用唯一键（逻辑删除场景下无法保证唯一）
- 使用数字替代枚举，字段备注说明含义
- 所有表必须有deleted字段
- ID使用雪花算法，转String传给前端防精度丢失

---

**文档版本**：v3.1
**创建日期**：2026-05-07
**最后更新**：2026-05-08
**状态**：✅ 店铺用户邀请码管理功能已补充，等待用户确认