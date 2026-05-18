# 阶段4：后端Mapper和DTO迁移

## 目标
将Mapper和DTO按业务模块重组，与Controller/Service模块对应。

## 前提条件
- 阶段3已完成（Controller已迁移）

## Mapper重组方案

### 原Mapper（10个）

| 原Mapper | 模块归属 |
|----------|----------|
| `UserMapper` | user模块 |
| `ShopMapper` | shop模块 |
| `ProductMapper` | product模块 |
| `CategoryMapper` | product模块 |
| `OrderMapper` | order模块 |
| `PointsMapper` | user模块 |
| `AddressMapper` | user模块 |
| `InviteCodeMapper` | auth模块 |
| `MessageMapper` | message模块 |
| `FileRecordMapper` | file模块 |

### 重组后结构

| 新路径 | 说明 |
|--------|------|
| `auth/mapper/InviteCodeMapper.java` | 邀请码 |
| `shop/mapper/ShopMapper.java` | 店铺 |
| `product/mapper/ProductMapper.java` | 商品 |
| `product/mapper/CategoryMapper.java` | 分类 |
| `order/mapper/OrderMapper.java` | 订单 |
| `user/mapper/UserMapper.java` | 用户 |
| `user/mapper/PointsMapper.java` | 积分 |
| `user/mapper/AddressMapper.java` | 地址 |
| `message/mapper/MessageMapper.java` | 消息 |
| `file/mapper/FileRecordMapper.java` | 文件记录 |

## DTO重组方案

### 原DTO（10个）

| 原DTO | 模块归属 |
|-------|----------|
| `LoginRequest` | auth模块 |
| `RegisterRequest` | auth模块 |
| `PasswordRequest` | auth模块 |
| `PageRequest` | common（通用） |
| `PageResult` | common（通用） |
| `UserDTO` | user模块 |
| `ProductDTO` | product模块 |
| `ShopDTO` | shop模块 |
| `OrderDTO` | order模块 |
| `UserImportDTO` | user模块 |

### 重组后结构

| 新路径 | 说明 |
|--------|------|
| `auth/dto/LoginRequest.java` | 登录 |
| `auth/dto/RegisterRequest.java` | 注册 |
| `auth/dto/PasswordRequest.java` | 改密 |
| `common/dto/PageRequest.java` | 分页（通用） |
| `common/dto/PageResult.java` | 分页结果（通用） |
| `user/dto/UserDTO.java` | 用户信息 |
| `user/dto/UserImportDTO.java` | 用户导入 |
| `shop/dto/ShopDTO.java` | 店铺信息 |
| `product/dto/ProductDTO.java` | 商品信息 |
| `order/dto/OrderDTO.java` | 订单信息 |

## 执行步骤

### Step 4.1：移动Mapper

```bash
cd code/backend/ai-shop/src/main/java/com/sh/aishop

# 移动Mapper到各模块
mv mapper/UserMapper.java user/mapper/
mv mapper/ShopMapper.java shop/mapper/
mv mapper/ProductMapper.java product/mapper/
mv mapper/CategoryMapper.java product/mapper/
mv mapper/OrderMapper.java order/mapper/
mv mapper/PointsMapper.java user/mapper/
mv mapper/AddressMapper.java user/mapper/
mv mapper/InviteCodeMapper.java auth/mapper/
mv mapper/MessageMapper.java message/mapper/
mv mapper/FileRecordMapper.java file/mapper/FileRecordMapper.java

# 删除旧mapper目录
rmdir mapper
```

### Step 4.2：移动DTO

```bash
cd code/backend/ai-shop/src/main/java/com/sh/aishop

# 移动DTO到各模块
mv dto/LoginRequest.java auth/dto/
mv dto/RegisterRequest.java auth/dto/
mv dto/PasswordRequest.java auth/dto/
mv dto/PageRequest.java common/dto/
mv dto/PageResult.java common/dto/
mv dto/UserDTO.java user/dto/
mv dto/ProductDTO.java product/dto/
mv dto/ShopDTO.java shop/dto/
mv dto/OrderDTO.java order/dto/
mv dto/UserImportDTO.java user/dto/

# 删除旧dto目录
rmdir dto
```

### Step 4.3：更新所有import

```bash
# 更新所有Java文件的import
# mapper路径变化
# dto路径变化
```

### Step 4.4：验证编译

```bash
cd code/backend/ai-shop
./mvnw compile
```

## 完成标准
- 所有Mapper已移动到对应模块
- 所有DTO已移动到对应模块
- 所有import正确
- 编译通过

## 回滚方案
```bash
git checkout -- mapper/ dto/ auth/ shop/ product/ order/ user/ message/ file/ admin/
```

## 注意事项
- PageRequest和PageResult放在common/dto因为通用
- Mapper XML文件如果有也需要同步移动
- MyBatis-Plus的BaseMapper接口路径也需要更新