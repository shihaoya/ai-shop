# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

积分商城系统 (Points Mall) - 支持多店铺运营的积分兑换平台。店铺用户可申请店铺并由管理员审批通过后开展业务，向普通用户发放积分；普通用户使用积分兑换商品。

## 技术栈

| 端 | 技术 |
|---|------|
| 后端 | Spring Boot 3.2.12 (Java 21), MyBatis-Plus 3.5.16, MySQL 8.0, Redis, JWT |
| 前端 | Vue 3.5 + TypeScript, Vite 8.x, Ant Design Vue 4.x, Pinia, UnoCSS |

## 项目结构

```
ai-shop/
├── code/
│   ├── backend/ai-shop/          # Spring Boot 后端
│   │   ├── src/main/java/com/sh/aishop/
│   │   │   ├── controller/       # REST控制器（auth/admin/operator/user/file）
│   │   │   ├── service/          # 业务逻辑（含impl/）
│   │   │   ├── mapper/           # MyBatis Mapper
│   │   │   ├── entity/           # 实体类（含enums/）
│   │   │   ├── dto/              # 数据传输对象
│   │   │   ├── exception/        # 异常类
│   │   │   ├── config/            # 配置类（Redis/Swagger/MVC）
│   │   │   ├── interceptor/      # JWT拦截器
│   │   │   ├── util/              # 工具类（JWT/雪花ID/密码）
│   │   │   └── common/            # Result, ResultCode, GlobalExceptionHandler
│   │   └── sql/init.sql          # 数据库初始化脚本
│   └── frontend/                 # Vue 3 前端
│       └── src/
│           ├── api/              # 按角色分：auth/admin/operator/user/upload
│           ├── views/            # 页面组件
│           ├── stores/           # Pinia状态（user/app/theme/shop）
│           ├── components/       # 组件（common/business/layout/upload）
│           ├── layouts/          # 布局组件
│           ├── types/            # TypeScript类型定义
│           ├── router/           # 路由配置
│           └── composables/      # 组合式函数
├── docker-compose.yml           # Docker部署配置
├── SDD.md                      # 解决方案设计文档（唯一的需求文档）
├── AGENTS.md                   # Agent指令
└── CLAUDE.md                   # 本文件
```

## 常用命令

### 后端
```bash
cd code/backend/ai-shop
./mvnw spring-boot:run          # 开发服务器
./mvnw clean package             # 构建
./mvnw test                      # 测试
./mvnw test -Dtest=AuthServiceTest  # 运行单个测试类
```

### 前端
```bash
cd code/frontend
pnpm dev                         # 开发服务器 (端口18781)
pnpm build                       # 构建生产版本
pnpm type-check                  # TypeScript类型检查
pnpm lint                        # ESLint检查
```

### Docker部署
```bash
docker compose up -d             # 启动所有服务
docker compose logs -f          # 查看日志
docker compose down -v          # 停止并清理数据
```

## 关键约束

### 雪花ID精度丢失（最高优先级）
- JS `Number.MAX_SAFE_INTEGER = 2^53-1`，雪花ID是64位
- **后端所有ID必须是String**：`@JsonSerialize(using = ToStringSerializer.class)` 在 `BaseEntity.id`
- **前端禁止转Number**：禁止 `Number(id)`、`parseInt(id)`、`+id`、`id as number`
- 所有ID用 `string | null` 类型
- 后端其他ID字段（如shopId、userId等）也要确保序列化为String

### 逻辑删除
- 所有表有 `deleted` 字段，MyBatis-Plus 自动过滤 `deleted = 0`
- 配置在 `MyMetaObjectHandler.java`

### 审计字段
- 所有表包含 `created_at`, `updated_at`, `created_by`, `updated_by`
- 自动填充通过 `MyMetaObjectHandler` 实现，从请求属性 `userId` 获取当前用户

### 表结构同步
- 修改实体/表结构后，必须同步更新 `code/backend/sql/init.sql`，确保脚本与实体类一致

## 用户角色

| 角色 | 值 | 说明 |
|------|---|------|
| 管理员 | 1 | 系统唯一，管理所有店铺审批、用户 |
| 店铺用户 | 2 | 运营自己店铺，发行积分 |
| 普通用户 | 3 | 属于某店铺，兑换商品 |

## 用户状态

| 状态 | 值 | 说明 |
|------|---|------|
| 待审核 | 1 | 注册后待审批 |
| 正常 | 2 | 已审批通过 |
| 已冻结 | 3 | 被禁用 |

## 店铺状态

| 状态 | 值 | 说明 |
|------|---|------|
| 待审核 | 1 | 等待管理员审批 |
| 已通过 | 2 | 审批通过 |
| 已拒绝 | 3 | 审批拒绝 |
| 已禁用 | 4 | 被禁用 |

## 订单状态流转

```
已下单(1) → 已确认(2) → 已发货(3) → 已完成(4)
     ↓            ↓
   关闭(5)      关闭(5)
```

## API接口前缀

| 前缀 | 说明 |
|------|------|
| `/api/auth` | 认证（登录/注册/登出/修改密码/用户信息） |
| `/api/admin` | 管理员（店铺/用户/邀请码） |
| `/api/operator` | 店铺用户（店铺/商品/订单/用户/积分/消息） |
| `/api/user` | 普通用户（商品/订单/地址/积分/消息） |
| `/api/file` | 文件上传/访问/删除 |

## 数据库规范

- 不使用物理外键，通过逻辑关联
- 不使用唯一键（逻辑删除后无法保证唯一性）
- 用 `tinyint` + 备注替代枚举
- ID使用雪花算法
- 订单表为 `orders`（非 `order`）

## 编码规范

- **禁止硬编码**：所有状态、类型使用枚举类（`entity/enums/`）
- 枚举：`RoleEnum`, `OrderStatus`, `ShopStatus`, `UserStatus`, `ProductStatus`, `ProductType`, `PointsType`, `MessageType`, `InviteCodeStatus`, `FileStorageType`
- Service层通过 `Result.fail(ResultCode.XXX, "消息")` 返回错误

## 错误码（ResultCode）

| 区间 | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 通用失败 |
| 401 | 未认证 |
| 403 | 无权限 |
| 500 | 服务器错误 |
| 1000-1099 | 认证模块 |
| 1100-1199 | 验证异常 |
| 2000-2999 | 业务错误 |

## 测试框架

- **测试框架**: JUnit 5 + Mockito
- **测试配置**: `src/test/resources/application-test.yml` (H2内存数据库)
- **测试目录**: `src/test/java/com/sh/aishop/service/`
- **现有测试**: `AuthServiceTest`, `AdminServiceTest`, `OperatorServiceTest`, `UserServiceTest`
- **注意事项**: 部分现有测试有未解决的mock问题（pointsMapper等），运行前注意