# 阶段1：后端实体/枚举迁移

## 目标
将所有 entity 和 enums 移动到 `common/` 包下，作为全局共享代码。

## 前提条件
- 阶段0已完成

## 涉及文件

### 实体文件（11个）
| 原路径 | 新路径 |
|--------|--------|
| `entity/BaseEntity.java` | `common/entity/BaseEntity.java` |
| `entity/User.java` | `common/entity/User.java` |
| `entity/Shop.java` | `common/entity/Shop.java` |
| `entity/Product.java` | `common/entity/Product.java` |
| `entity/Category.java` | `common/entity/Category.java` |
| `entity/Order.java` | `common/entity/Order.java` |
| `entity/Points.java` | `common/entity/Points.java` |
| `entity/Address.java` | `common/entity/Address.java` |
| `entity/InviteCode.java` | `common/entity/InviteCode.java` |
| `entity/Message.java` | `common/entity/Message.java` |
| `entity/FileRecord.java` | `common/entity/FileRecord.java` |

### 枚举文件（9个）
| 原路径 | 新路径 |
|--------|--------|
| `entity/enums/RoleEnum.java` | `common/enums/RoleEnum.java` |
| `entity/enums/UserStatus.java` | `common/enums/UserStatus.java` |
| `entity/enums/ShopStatus.java` | `common/enums/ShopStatus.java` |
| `entity/enums/ProductStatus.java` | `common/enums/ProductStatus.java` |
| `entity/enums/ProductType.java` | `common/enums/ProductType.java` |
| `entity/enums/OrderStatus.java` | `common/enums/OrderStatus.java` |
| `entity/enums/PointsType.java` | `common/enums/PointsType.java` |
| `entity/enums/InviteCodeStatus.java` | `common/enums/InviteCodeStatus.java` |
| `entity/enums/MessageType.java` | `common/enums/MessageType.java` |
| `entity/enums/FileStorageType.java` | `common/enums/FileStorageType.java` |

## 执行步骤

### Step 1.1：移动实体文件

```bash
cd code/backend/ai-shop/src/main/java/com/sh/aishop

# 移动实体文件
mv entity/BaseEntity.java common/entity/
mv entity/User.java common/entity/
mv entity/Shop.java common/entity/
mv entity/Product.java common/entity/
mv entity/Category.java common/entity/
mv entity/Order.java common/entity/
mv entity/Points.java common/entity/
mv entity/Address.java common/entity/
mv entity/InviteCode.java common/entity/
mv entity/Message.java common/entity/
mv entity/FileRecord.java common/entity/

# 移动枚举文件
mv entity/enums/RoleEnum.java common/enums/
mv entity/enums/UserStatus.java common/enums/
mv entity/enums/ShopStatus.java common/enums/
mv entity/enums/ProductStatus.java common/enums/
mv entity/enums/ProductType.java common/enums/
mv entity/enums/OrderStatus.java common/enums/
mv entity/enums/PointsType.java common/enums/
mv entity/enums/InviteCodeStatus.java common/enums/
mv entity/enums/MessageType.java common/enums/
mv entity/enums/FileStorageType.java common/enums/

# 删除旧目录
rmdir entity/enums 2>/dev/null || true
rmdir entity 2>/dev/null || true
```

### Step 1.2：更新所有import语句

需要更新的文件类型：
- 所有Controller（`controller/*.java`）
- 所有Service（`service/*.java`）
- 所有Mapper（`mapper/*.java`）
- 所有DTO（`dto/*.java`）

**import替换规则**：
```bash
# 使用sed批量替换（Linux/macOS）
# 实体：com.sh.aishop.entity. → com.sh.aishop.common.entity.
# 枚举：com.sh.aishop.entity.enums. → com.sh.aishop.common.enums.

find . -name "*.java" -exec sed -i 's/com\.sh\.aishop\.entity\./com.sh.aishop.common.entity./g' {} \;
find . -name "*.java" -exec sed -i 's/com\.sh\.aishop\.entity\.enums\./com.sh.aishop.common.enums./g' {} \;
```

### Step 1.3：验证编译

```bash
cd code/backend/ai-shop
./mvnw compile
```

## 完成标准
- 所有实体文件已移动到 `common/entity/`
- 所有枚举文件已移动到 `common/enums/`
- 所有import语句已更新
- 编译通过

## 回滚方案
```bash
cd code/backend/ai-shop/src/main/java/com/sh/aishop
# 从git恢复
git checkout -- entity/ mapper/ service/ controller/ dto/
```

## 注意事项
- 先移动文件，再批量更新import
- 备份git状态以便回滚
- Windows用户需要用PowerShell或git bash执行sed命令