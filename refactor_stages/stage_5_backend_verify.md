# 阶段5：后端验证

## 目标
验证重组后的后端代码能正常编译和启动。

## 前提条件
- 阶段1-4已完成

## 验证步骤

### Step 5.1：编译检查
```bash
cd code/backend/ai-shop
./mvnw clean compile
```

预期：无编译错误

### Step 5.2：启动测试
```bash
cd code/backend/ai-shop
./mvnw spring-boot:run
```

预期：
- 启动成功，无异常
- 各模块ComponentScan正常
- 数据库连接正常

### Step 5.3：API接口抽查

使用curl或Postman测试关键接口：

**Auth接口**：
```bash
curl -X POST http://localhost:18780/api/auth/login -H "Content-Type: application/json" -d '{"username":"admin","password":"admin123"}'
```

**Admin接口**（需先登录获取token）：
```bash
curl http://localhost:18780/api/admin/shops -H "Authorization: Bearer {token}"
```

### Step 5.4：单元测试（可选）

如果需要验证单元测试：
```bash
cd code/backend/ai-shop
./mvnw test
```

## 完成标准
- 编译通过无错误
- 启动成功
- 核心API接口能正常调用
- 无运行时异常

## 回滚方案
如果验证失败，检查错误信息，定位到具体阶段回滚：
```bash
# 回滚到最后一次可工作的状态
git checkout -- .
```

## 注意事项
- 如果启动失败，检查@ComponentScan是否覆盖新包路径
- 如果Mapper找不到，检查@MapperScan是否覆盖新包路径
- 如果entity找不到，检查import是否正确