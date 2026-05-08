# 积分商城系统 (Points Mall) - Agent Instructions

## 项目结构

```
ai-shop/
├── SDD.md              # 解决方案设计文档（唯一的需求文档）
└── code/
    ├── frontend/       # Vue 3 + TypeScript + Vite + pnpm
    └── backend/        # Spring Boot + Maven (ai-shop/ 子目录内)
```

## 技术栈

| 端 | 技术 | 说明 |
|----|------|------|
| 前端 | Vue 3 + Vite + TypeScript | Composition API，单组件 `.vue` 文件 |
| UI | Aceternity UI | 组件库 |
| 后端 | Spring Boot | 标准 Maven 项目，入口在 `ai-shop/src/main/` |
| 数据库 | MySQL | 逻辑删除，不使用物理外键 |
| ID | 雪花ID | 传给前端前转 String（避免精度丢失）|

## 关键设计决策（来自 SDD.md）

- 枚举类型：用 `tinyint` + 备注替代枚举
- 唯一键：逻辑删除后无法保证唯一性，部分字段不使用唯一键
- 删除方式：逻辑删除
- 订单状态流转：已下单 → 已确认 → 已发货 → 已完成
- 店铺/普通用户注册：需要上级审批（pending 状态）

## 开发命令

### 前端
```bash
cd code/frontend
pnpm install
pnpm dev        # 开发服务器
pnpm build      # 构建（先 typecheck 再 build）
pnpm preview    # 预览 build 结果
```

### 后端
```bash
cd code/backend/ai-shop
./mvnw spring-boot:run   # 运行
./mvnw clean package     # 构建
```

## 重要约束

1. **雪花ID精度**：后端返回 Long，前端需转为 String 处理
2. **逻辑删除**：查询时注意 `deleted` 字段条件
3. **邀请码机制**：管理员/店铺用户可生成邀请码，普通用户注册需审核
4. **店铺营业状态**：歇业时普通用户可浏览但无法下单

## 待完成项

- 项目初期，无现有测试、无 CI 配置
- 前后端联调方式尚未确定（可能需要 proxy 配置）

## 回复规范

- **整体使用中文回复**