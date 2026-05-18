# 阶段2：后端Service拆分迁移

## 目标
拆分并重组Service层，解决大型Service难维护的问题。

## 前提条件
- 阶段1已完成（实体/枚举已迁移到common包）

## Service拆分方案

### 原Service分析

| 原Service | 行数 | 职责 |
|-----------|------|------|
| `OperatorService` | ~990行 | 店铺、商品、订单、用户积分、消息 |
| `UserService` | ~610行 | 用户、地址、积分、订单 |
| `AuthService` | ~300行 | 登录、注册、密码、用户信息 |
| `AdminService` | ~400行 | 店铺审批、用户管理、邀请码 |
| `FileService` | ~200行 | 文件上传、删除、查看 |

### 拆分后结构

| 新Service | 来源 | 职责 |
|-----------|------|------|
| `auth/AuthService` | 原AuthService | 登录/注册/密码/用户信息 |
| `shop/ShopService` | OperatorService中店铺部分 | 店铺管理、营业状态 |
| `product/ProductService` | OperatorService中商品部分 | 商品/分类管理 |
| `order/OrderService` | OperatorService+UserService订单部分 | 订单状态流转 |
| `user/UserService` | 原AdminService+UserService用户部分 | 用户管理、冻结/审核 |
| `user/PointsService` | OperatorService+UserService积分部分 | 积分发放/扣除/流水 |
| `user/AddressService` | UserService中地址部分 | 地址簿管理 |
| `message/MessageService` | OperatorService中消息部分 | 消息发送/读取 |
| `file/FileService` | 原FileService | 文件上传/删除/查看 |
| `admin/AdminService` | 原AdminService（保持） | 管理员专属功能 |

## 执行步骤

### Step 2.1：创建各模块的Service

各模块目录已创建，现在需要在各模块下创建Service类。

### Step 2.2：拆分OperatorService

OperatorService的主要方法对应模块：

**ShopService 方法**（来自OperatorService）：
- `getMyShop()` - 获取我的店铺
- `applyShop()` - 申请店铺
- `updateShopStatus()` - 切换营业/歇业

**ProductService 方法**（来自OperatorService）：
- `getCategories()` - 获取分类
- `createCategory()` - 创建分类
- `updateCategory()` - 更新分类
- `deleteCategory()` - 删除分类
- `getProducts()` - 商品列表
- `createProduct()` - 创建商品
- `getProduct()` - 商品详情
- `updateProduct()` - 更新商品
- `deleteProduct()` - 删除商品
- `updateProductStatus()` - 上架/下架

**OrderService 方法**（来自OperatorService）：
- `getOrders()` - 订单列表（店铺端）
- `confirmOrder()` - 确认订单
- `shipOrder()` - 发货
- `closeOrder()` - 关闭订单
- `completeOrder()` - 完成订单

**PointsService 方法**（来自OperatorService）：
- `getUsers()` - 普通用户列表
- `getPoints()` - 获取用户积分
- `adjustPoints()` - 调整积分
- `getPointsLog()` - 积分流水

**MessageService 方法**（来自OperatorService）：
- `getMessages()` - 消息列表
- `getUnreadCount()` - 未读数

### Step 2.3：拆分UserService

**UserService 方法**（来自UserService）：
- `getUserInfo()` - 用户信息
- `updateUserInfo()` - 更新用户信息

**AddressService 方法**（来自UserService）：
- `getAddresses()` - 地址列表
- `createAddress()` - 创建地址
- `getAddress()` - 地址详情
- `updateAddress()` - 更新地址
- `deleteAddress()` - 删除地址
- `setDefaultAddress()` - 设置默认

### Step 2.4：移动/重命名Service文件

```bash
cd code/backend/ai-shop/src/main/java/com/sh/aishop

# 移动AuthService到auth模块
mv service/AuthService.java auth/service/AuthService.java

# 移动AdminService到admin模块
mv service/AdminService.java admin/service/AdminService.java

# 移动FileService到file模块
mv service/FileService.java file/service/FileService.java

# 创建新的ShopService
# 创建新的ProductService
# 创建新的OrderService
# 创建新的PointsService
# 创建新的MessageService
# 创建新的UserService（新）
# 创建新的AddressService
```

### Step 2.5：更新所有import

```bash
# Service层import更新
# auth.service.AuthService → auth.service.AuthService
# mapper.* → 对应模块.mapper.*
```

### Step 2.6：验证编译

```bash
cd code/backend/ai-shop
./mvnw compile
```

## 完成标准
- 原OperatorService拆分为4个新Service（Shop/Product/Order/PointsService）
- 原UserService拆分为2个新Service（User/AddressService）
- AuthService/AdminService/FileService已移动到对应模块
- 所有import正确
- 编译通过

## 回滚方案
```bash
git checkout -- service/ auth/ shop/ product/ order/ user/ message/ admin/
```

## 注意事项
- OperatorService是最大最复杂的，需要仔细拆分
- 拆分时注意依赖的Mapper和Service不要循环引用
- 有些方法如getOrders在Operator和User两边都有，需确保都移到OrderService