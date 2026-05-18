# 项目结构调整计划跟踪

## 项目信息
- 项目：积分商城系统 (Points Mall)
- 开始日期：2026-05-18
- 目标：将后端按业务域拆分为7个功能模块

## 目标架构

```
com.sh.aishop
├── auth/               # 认证（登录/注册/密码/邀请码）
├── shop/               # 店铺（店铺管理/营业状态）
├── product/            # 商品（商品/分类管理）
├── order/              # 订单（订单状态流转）
├── user/               # 用户（用户管理/积分/地址）
├── message/            # 消息
├── file/               # 文件上传
└── common/             # 公共（Result/ResultCode/异常/拦截器/config/util/entity/enums/dto）
```

---

## 阶段进度

| 阶段 | 名称 | 状态 | 开始时间 | 完成时间 | 备注 |
|------|------|------|----------|----------|------|
| 阶段0 | 环境准备 | 未开始 | - | - | 删除旧测试、创建目录结构 |
| 阶段0.5 | 单元测试准备 | 未开始 | - | - | 编写新架构单元测试文件 |
| 阶段1 | 后端实体/枚举迁移 | 未开始 | - | - | entity/enums移到common包 |
| 阶段2 | 后端Service拆分迁移 | 未开始 | - | - | OperatorService/UserService拆分 |
| 阶段3 | 后端Controller迁移 | 未开始 | - | - | 按模块重组Controller |
| 阶段4 | 后端Mapper/DTO迁移 | 未开始 | - | - | 按模块重组 |
| 阶段5 | 后端验证 | 未开始 | - | - | 启动测试、Docker验证 |
| 阶段6 | 前端API重组 | 未开始 | - | - | 按模块重组api/ |
| 阶段7 | 前端Views重组 | 未开始 | - | - | 按模块重组views/ |
| 阶段8 | 最终验证 | 未开始 | - | - | 完整功能测试 |

---

## 当前阶段：阶段0 - 环境准备

### 任务清单
- [ ] 删除旧单元测试文件
- [ ] 创建7个功能模块目录
- [ ] 创建common包目录结构

### 执行命令
```bash
# 删除旧测试（阶段0）
rm -f code/backend/ai-shop/src/test/java/com/sh/aishop/AiShopApplicationTests.java
rm -f code/backend/ai-shop/src/test/java/com/sh/aishop/service/AdminServiceTest.java
rm -f code/backend/ai-shop/src/test/java/com/sh/aishop/service/AuthServiceTest.java
rm -f code/backend/ai-shop/src/test/java/com/sh/aishop/service/OperatorServiceTest.java
rm -f code/backend/ai-shop/src/test/java/com/sh/aishop/service/UserServiceTest.java

# 创建后端模块目录（阶段0）
cd code/backend/ai-shop/src/main/java/com/sh/aishop
mkdir -p auth/controller auth/service auth/mapper auth/dto
mkdir -p shop/controller shop/service shop/mapper shop/dto
mkdir -p product/controller product/service product/mapper product/dto
mkdir -p order/controller order/service order/mapper order/dto
mkdir -p user/controller user/service user/mapper user/dto
mkdir -p message/controller message/service message/mapper message/dto
mkdir -p file/controller file/service
mkdir -p common/entity common/enums common/dto
```

---

## 阶段详细文档索引

| 阶段 | 文档路径 | 说明 |
|------|----------|------|
| 阶段0 | `refactor_stages/stage_0_setup.md` | 删除旧测试、创建目录结构 |
| 阶段0.5 | `refactor_stages/stage_0_5_tests.md` | 单元测试准备 |
| 阶段1 | `refactor_stages/stage_1_entity_enums.md` | entity/enums移到common包 |
| 阶段2 | `refactor_stages/stage_2_service_split.md` | Service拆分迁移 |
| 阶段3 | `refactor_stages/stage_3_controller_move.md` | Controller迁移 |
| 阶段4 | `refactor_stages/stage_4_mapper_dto_move.md` | Mapper/DTO迁移 |
| 阶段5 | `refactor_stages/stage_5_backend_verify.md` | 后端验证 |
| 阶段6 | `refactor_stages/stage_6_frontend_api.md` | 前端API重组 |
| 阶段7 | `refactor_stages/stage_7_frontend_views.md` | 前端Views重组 |
| 阶段8 | `refactor_stages/stage_8_final_verify.md` | 最终验证 |

---

## 下一步行动

执行 **阶段0** - 环境准备：
1. 删除旧单元测试
2. 创建模块目录结构

完成后继续阶段1。