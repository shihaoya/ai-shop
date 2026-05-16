# 积分商城系统 (Points Mall)

一个支持多店铺运营的积分兑换平台。店铺用户可申请店铺并由管理员审批通过后开展业务，向普通用户发放积分；普通用户使用积分兑换商品。

---

## 技术栈

### 后端
- **框架**: Spring Boot 3.2.12 (Java 21)
- **ORM**: MyBatis-Plus 3.5.16
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **认证**: JWT (jjwt 0.12.5)
- **构建**: Maven

### 前端
- **框架**: Vue 3.5 + TypeScript
- **构建**: Vite 8.x
- **UI**: Ant Design Vue 4.x
- **状态管理**: Pinia 3.x
- **路由**: Vue Router 4.x
- **样式**: UnoCSS + Sass

---

## 用户角色

| 角色 | 说明 |
|------|------|
| 管理员 | 系统唯一管理员，管理所有店铺审批、管理所有用户 |
| 店铺用户 | 运营角色，管理自己店铺的商品、订单、用户、积分 |
| 普通用户 | 属于某店铺，只能看到和兑换本店铺商品，查看订单和积分 |

---

## 功能模块

### 管理员端
- 店铺管理（审核/拒绝店铺申请）
- 用户管理（冻结/解冻/审批用户）
- 邀请码管理

### 店铺用户端
- 店铺管理（申请店铺/歇业切换）
- 商品分类管理
- 商品管理（上架/下架商品）
- 订单管理（确认/发货/关闭订单）
- 用户管理（积分发放/扣除）
- 个人中心

### 普通用户端
- 商品列表（卡片/表格切换展示）
- 订单管理
- 个人中心（积分余额/积分流水）
- 地址簿管理

---

## 项目结构

```
ai-shop/
├── SDD.md                    # 解决方案设计文档
├── docs/                     # 开发文档
├── code/
│   ├── backend/              # Spring Boot 后端
│   │   ├── ai-shop/          # 主项目
│   │   │   └── src/main/java/com/sh/aishop/
│   │   │       ├── config/   # 配置类
│   │   │       ├── controller/  # REST 控制器
│   │   │       ├── entity/   # 实体类
│   │   │       ├── mapper/   # MyBatis Mapper
│   │   │       ├── service/  # 业务逻辑
│   │   │       └── common/   # 通用类
│   │   └── sql/
│   │       └── init.sql      # 数据库初始化脚本
│   └── frontend/             # Vue 3 前端
│       └── src/
│           ├── api/          # API 模块
│           ├── views/        # 页面组件
│           ├── stores/       # Pinia 状态管理
│           └── router/       # 路由配置
```

---

## 快速开始

### 后端

```bash
cd code/backend/ai-shop

# 运行开发服务器
./mvnw spring-boot:run

# 构建项目
./mvnw clean package
```

### 前端

```bash
cd code/frontend

# 安装依赖
pnpm install

# 运行开发服务器
pnpm dev

# 构建生产版本
pnpm build
```

---

## 技术规范

| 规范 | 说明 |
|------|------|
| ID生成 | 雪花算法，转String传给前端避免精度丢失 |
| 删除方式 | 逻辑删除 |
| 外键 | 不使用物理外键 |
| 枚举类型 | 使用tinyint + 备注说明 |
| 审计字段 | 所有表包含 created_at, updated_at, created_by |

---

## API 文档

启动后端服务后访问：
- Swagger UI: http://localhost:18780/swagger-ui.html
- Knife4j: http://localhost:18780/doc.html

---

## License

MIT