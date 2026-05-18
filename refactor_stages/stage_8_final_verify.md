# 阶段8：最终验证

## 目标
完整验证整个重组后的系统能正常工作。

## 前提条件
- 阶段1-7已完成

## 验证步骤

### Step 8.1：后端完整编译
```bash
cd code/backend/ai-shop
./mvnw clean compile
./mvnw test -Dtest=*Test  # 如果有单元测试
```

### Step 8.2：后端启动
```bash
cd code/backend/ai-shop
./mvnw spring-boot:run
```
确认启动成功，控制台无报错。

### Step 8.3：前端启动
```bash
cd code/frontend
pnpm dev
```
确认开发服务器启动成功，页面能访问。

### Step 8.4：Docker部署验证
```bash
cd c:/Users/hao/Desktop/ai/ai-shop
docker compose up -d
docker compose logs -f
```

### Step 8.5：功能测试

#### 认证流程
1. 注册新用户
2. 登录
3. 修改密码
4. 登出

#### 管理员流程
1. 登录管理员账号
2. 查看店铺列表
3. 审批店铺申请
4. 查看/冻结用户

#### 店铺用户流程
1. 登录店铺账号
2. 申请店铺
3. 管理商品分类
4. 上架商品
5. 处理订单
6. 发放积分

#### 普通用户流程
1. 登录普通用户账号
2. 浏览商品
3. 兑换商品（下单）
4. 查看订单
5. 管理地址簿

### Step 8.6：更新计划文档

确认各阶段完成状态：
- 更新 `REFACTOR_PLAN.md` 中的阶段状态为"已完成"
- 记录完成时间

## 完成标准
- 后端编译、启动正常
- 前端编译、启动正常
- Docker部署正常
- 所有核心功能测试通过

## 回滚方案
如果最终验证失败且无法快速修复，回滚到重组前状态：
```bash
git checkout HEAD -- .
git clean -fd
```

## 注意事项
- 重组涉及文件多，建议每阶段完成都commit一次
- 出现问题时先检查import路径是否正确
- 确认MyBatis-Plus的@MapperScan和Spring的@ComponentScan覆盖新包路径