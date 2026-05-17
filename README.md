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
├── .env.example              # 环境变量配置示例
├── docker-compose.yml        # Docker 编排（MySQL + Redis + 后端 + 前端）
├── SDD.md                    # 解决方案设计文档
├── docs/                     # 开发文档
├── code/
│   ├── backend/              # Spring Boot 后端
│   │   ├── ai-shop/          # 主项目
│   │   │   ├── Dockerfile   # 后端多阶段构建
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
│       ├── Dockerfile        # 前端多阶段构建
│       ├── nginx.conf        # Nginx SPA + API 代理配置
│       └── src/
│           ├── api/          # API 模块
│           ├── views/        # 页面组件
│           ├── stores/       # Pinia 状态管理
│           └── router/       # 路由配置
```

---

## 快速开始

### 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |

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

## Docker 部署

项目支持通过 Docker Compose 一键部署，包含 MySQL、Redis、后端、前端四个服务。

### 前置条件

- [Docker](https://docs.docker.com/engine/install/) ≥ 24.0
- [Docker Compose](https://docs.docker.com/compose/install/) ≥ 2.0

### 快速部署

```bash
# 1. 从项目根目录启动
docker compose up -d

# 2. 查看启动日志
docker compose logs -f

# 3. 等待所有服务就绪后，访问
#    http://localhost
```

首次启动会自动完成以下操作：
1. 构建后端镜像（Maven 编译 → JRE 运行）
2. 构建前端镜像（Node 编译 → Nginx 运行）
3. 启动 MySQL 并自动执行 `init.sql` 初始化数据库
4. 启动 Redis
5. 启动后端服务（等待 MySQL 和 Redis 就绪后启动）
6. 启动前端 Nginx 服务

### 自定义配置

复制环境变量模板并修改：

```bash
cp .env.example .env
```

可配置项：

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `FRONTEND_PORT` | `80` | 前端访问端口（宿主机） |
| `BACKEND_PORT` | `18780` | 后端 API 端口（宿主机） |
| `MYSQL_PORT` | `3307` | MySQL 外部访问端口 |
| `MYSQL_PASSWORD` | `root123` | MySQL root 密码 |
| `JWT_SECRET` | `...` | JWT 签名密钥（生产环境务必修改） |

修改端口后重新部署：

```bash
docker compose down
# 编辑 .env 文件修改端口
docker compose up -d
```

### 端口配置说明

| 配置文件 | 配置项 | 说明 |
|---------|--------|------|
| `code/backend/ai-shop/src/main/resources/application.yml` | `server.port` | 后端监听端口（默认 `18780`），支持环境变量 `SERVER_PORT` 覆盖 |
| `code/frontend/nginx.conf` | `listen` | Nginx 容器内部监听端口（默认 `80`），外部映射由 `FRONTEND_PORT` 控制 |
| `code/frontend/vite.config.ts` | `server.port` | 前端开发服务器端口（默认 `18781`，仅本地开发有效） |

> **注意**：如果修改了后端 `server.port`，需要同步更新 `code/frontend/nginx.conf` 中的 `proxy_pass http://backend:<端口>` 和目标端口映射。

### 管理命令

```bash
# 启动所有服务
docker compose up -d

# 停止所有服务
docker compose down

# 查看实时日志
docker compose logs -f

# 查看特定服务日志
docker compose logs -f backend

# 重启单个服务（如修改配置后）
docker compose restart backend

# 重新构建镜像（代码变更后）
docker compose build

# 完全重建（不使用缓存）
docker compose build --no-cache
```

### 数据持久化

数据存储在 Docker 命名卷中，`docker compose down` **不会**丢失数据：

| 卷名 | 挂载点 | 存储内容 |
|------|--------|---------|
| `mysql-data` | `/var/lib/mysql` | 数据库文件 |
| `redis-data` | `/data` | Redis 缓存 |
| `upload-data` | `/uploads` | 用户上传文件 |

如需清理数据：

```bash
docker compose down -v
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