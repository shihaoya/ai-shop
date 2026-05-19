# 积分商城系统 (Points Mall) - Agent Instructions

## 项目结构

```
ai-shop/
├── SDD.md              # 解决方案设计文档（唯一的需求文档）
├── docs/
│   ├── 前端开发文档.md   # 前端开发技术栈、命令、组件使用
│   └── 后端开发文档.md   # 后端开发技术栈、API、数据库规范
└── code/
    ├── frontend/       # React + TypeScript + Vite + pnpm
    └── backend/        # Spring Boot + Maven (ai-shop/ 子目录内)
```

## 重要约束

1. **雪花ID精度丢失风险（最高优先级）**：
   - JS `Number.MAX_SAFE_INTEGER = 2^53-1 = 9007199254740991`，而雪花ID是64位
   - 后端所有ID（id、fileId、shopId、userId等）**必须是 String** 传给前端，禁止 Long 在 JSON 中序列化
   - 后端：`@JsonSerialize(using = ToStringSerializer.class)` 已加在 `BaseEntity.id`，所有继承实体自动生效
   - 后端其他 ID 字段（如 shopId、userId、mainImage 等）如有外键关联，**也要确保是 String**
   - 前端**禁止**把 id 转成 `Number`：禁止 `Number(id)`、`parseInt(id)`、`+id`、`id as number`
   - 前端 API 返回的 id 全部当 string 处理，业务类型用 `string | null`，不要用 `number`
   - 违反此约束 → JS 精度丢失 → 数据错乱，排查困难，**必须牢记**
2. **逻辑删除**：查询时注意 `deleted` 字段条件
3. **邀请码机制**：管理员/店铺用户可生成邀请码，普通用户注册需审核
4. **店铺营业状态**：歇业时普通用户可浏览但无法下单
5. **表结构同步**：修改实体/表结构后，必须同步更新 `code/backend/sql/init.sql`，确保脚本与实体类一致

## 回复规范

- **整体使用中文回复**

## 已知限制

### 工具可用性

- **`glob` 不可用**：探索目录请用 `bash` PowerShell 命令（`Get-ChildItem`、`Test-Path`）或 `read` 工具。
- **直接读取图片不可用**：直接传入图片会报错 "this model does not support image input"。如需图片识别，使用 `MiniMax_understand_image` MCP 工具。

### 已验证可用的工具

- 文件操作：`read`、`edit`、`write`
- 目录操作：`bash`（PowerShell：`Get-ChildItem`、`Test-Path`、`echo`）
- 图片识别：`MiniMax_understand_image`（MCP）
- 网页搜索：`MiniMax_web_search`（MCP）
- LSP：`lsp_diagnostics`、`lsp_goto_definition` 等

## 开发文档索引

| 端 | 文档 | 说明 |
|---|------|------|
| 前端 | `docs/前端开发文档.md` | 技术栈、命令、组件、路由、API约定、开发规范、AI自检清单 |
| 后端 | `docs/后端开发文档.md` | 技术栈、命令、数据库规范、API接口、分层开发、AI自检清单 |

## AI 自检命令

### 前端自检

```bash
cd code/frontend
pnpm type-check   # TypeScript 类型检查
pnpm lint         # ESLint 检查
```

### 后端自检

```bash
cd code/backend/ai-shop
./mvnw test      # 单元测试
```