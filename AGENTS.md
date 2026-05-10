# 积分商城系统 (Points Mall) - Agent Instructions

## 项目结构

```
ai-shop/
├── SDD.md              # 解决方案设计文档（唯一的需求文档）
├── docs/
│   └── 开发调整记录.md   # 开发过程中的技术变更记录
├── docs/开发文档.md      # 前后端开发文档（技术栈、命令、约束等）
└── code/
    ├── frontend/       # React + TypeScript + Vite + pnpm
    └── backend/        # Spring Boot + Maven (ai-shop/ 子目录内)
```

## 重要约束

1. **雪花ID精度**：后端返回 Long，前端需转为 String 处理
2. **逻辑删除**：查询时注意 `deleted` 字段条件
3. **邀请码机制**：管理员/店铺用户可生成邀请码，普通用户注册需审核
4. **店铺营业状态**：歇业时普通用户可浏览但无法下单

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

## 技术栈说明

**详细技术栈信息请参考 `docs/开发文档.md`，其他文档中不再重复描述技术栈。**

### 快速参考

| 端 | 技术栈 | 参考文档 |
|---|--------|----------|
| 前端 | React 18 + TypeScript + Vite + pnpm + Tailwind CSS + shadcn/ui | `docs/开发文档.md` |
| 后端 | Spring Boot + Maven + MyBatis-Plus + MySQL | `docs/开发文档.md` |