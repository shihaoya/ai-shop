# 阶段3：后端Controller迁移

## 目标
将Controller按业务模块重组，与Service模块对应。

## 前提条件
- 阶段2已完成（Service已拆分并移动）

## Controller重组方案

### 原Controller分析

| 原Controller | 所属角色 | 职责范围 |
|--------------|----------|----------|
| `AuthController` | 所有用户 | 登录/注册/密码/用户信息 → auth模块 |
| `AdminController` | 管理员 | 店铺审批/用户管理/邀请码 → admin模块 |
| `OperatorController` | 店铺用户 | 店铺/商品/订单/用户/积分/消息 → 拆分到shop/product/order/user/message |
| `UserController` | 普通用户 | 商品/订单/地址/积分/消息 → 拆分到product/order/user/message |
| `FileController` | 所有用户 | 文件上传/删除/查看 → file模块 |

### 重组后Controller结构

| 新Controller | 来源 | 路由前缀 |
|--------------|------|----------|
| `auth/AuthController` | 原AuthController | `/api/auth` |
| `admin/AdminController` | 原AdminController | `/api/admin` |
| `shop/ShopController` | OperatorController店铺部分 | `/api/operator` |
| `product/ProductController` | OperatorController商品部分 | `/api/operator` |
| `order/OrderController` | Operator+UserController订单部分 | `/api/operator` + `/api/user` |
| `user/UserController` | OperatorController用户部分 | `/api/operator` |
| `user/PointsController` | OperatorController积分部分 | `/api/operator` |
| `user/AddressController` | UserController地址部分 | `/api/user` |
| `message/MessageController` | Operator+UserController消息部分 | `/api/operator` + `/api/user` |
| `file/FileController` | 原FileController | `/api/file` |

## 执行步骤

### Step 3.1：移动AuthController
```bash
mv controller/AuthController.java auth/controller/AuthController.java
```

### Step 3.2：移动AdminController
```bash
mv controller/AdminController.java admin/controller/AdminController.java
```

### Step 3.3：移动FileController
```bash
mv controller/FileController.java file/controller/FileController.java
```

### Step 3.4：拆分OperatorController

OperatorController包含多个职责，需要拆分为：
- `shop/ShopController` - 店铺相关
- `product/ProductController` - 商品/分类相关
- `order/OrderController` - 订单相关
- `user/PointsController` - 积分相关
- `message/MessageController` - 消息相关

拆分方式：创建4个新Controller文件，把OperatorController的方法拆分到各新文件

### Step 3.5：拆分UserController

UserController包含多个职责，需要拆分为：
- `product/ProductController` - 商品浏览（如果需要）
- `order/OrderController` - 订单相关
- `user/AddressController` - 地址相关
- `user/UserController` - 用户信息
- `user/PointsController` - 积分相关
- `message/MessageController` - 消息相关

拆分方式：创建新Controller文件，把UserController的方法拆分

### Step 3.6：更新import和@Service注入

拆分后需要更新：
- Controller中import的Service（路径变化）
- Controller中@Autowired注入的Service

### Step 3.7：验证编译

```bash
cd code/backend/ai-shop
./mvnw compile
```

## 完成标准
- 5个Controller拆分为10+个Controller
- 所有import和@Service注入正确
- 编译通过
- API路由不变（仍通过@RequestMapping的path参数区分）

## 回滚方案
```bash
git checkout -- controller/ auth/ shop/ product/ order/ user/ message/ file/ admin/
```

## 注意事项
- OperatorController(16071行)是最大的，需要仔细拆分
- 拆分时保持API路由不变，只改变代码文件位置
- @RequestMapping的path需要按原Controller的path设置