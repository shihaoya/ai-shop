# 阶段0：环境准备

## 目标
删除旧单元测试，创建模块目录结构，为后续重组做准备。

## 前提条件
- 无

## 执行步骤

### Step 0.1：删除旧单元测试文件

删除以下测试文件（这些测试基于旧的包结构，重组后会失效）：
- `src/test/java/com/sh/aishop/AiShopApplicationTests.java`
- `src/test/java/com/sh/aishop/service/AdminServiceTest.java`
- `src/test/java/com/sh/aishop/service/AuthServiceTest.java`
- `src/test/java/com/sh/aishop/service/OperatorServiceTest.java`
- `src/test/java/com/sh/aishop/service/UserServiceTest.java`

```bash
cd code/backend/ai-shop/src/test/java/com/sh/aishop
rm -f AiShopApplicationTests.java
rm -f service/AdminServiceTest.java
rm -f service/AuthServiceTest.java
rm -f service/OperatorServiceTest.java
rm -f service/UserServiceTest.java
```

### Step 0.2：创建后端模块目录

在 `src/main/java/com/sh/aishop/` 下创建7个功能模块和common包：

```bash
cd code/backend/ai-shop/src/main/java/com/sh/aishop

# 7个功能模块
mkdir -p auth/controller auth/service auth/mapper auth/dto
mkdir -p shop/controller shop/service shop/mapper shop/dto
mkdir -p product/controller product/service product/mapper product/dto
mkdir -p order/controller order/service order/mapper order/dto
mkdir -p user/controller user/service user/mapper user/dto
mkdir -p message/controller message/service message/mapper message/dto
mkdir -p file/controller file/service

# common包（存放公共代码）
mkdir -p common/entity common/enums common/dto
```

### Step 0.3：验证目录结构

```bash
ls -la code/backend/ai-shop/src/main/java/com/sh/aishop/
```

预期输出应包含：auth/, shop/, product/, order/, user/, message/, file/, common/, config/, controller/, dto/, entity/, exception/, interceptor/, mapper/, service/, util/

## 完成标准
- 旧测试文件已删除
- 7个模块目录已创建
- common包目录已创建

## 回滚方案
如需回滚，从git checkout恢复删除的文件和目录。