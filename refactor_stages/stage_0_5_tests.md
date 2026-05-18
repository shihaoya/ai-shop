# 阶段0.5：单元测试准备（与阶段0并行）

## 目标
编写新的单元测试，基于重组后的新架构，用于验证每个阶段完成后的代码。

## 测试文件结构

```
src/test/java/com/sh/aishop/
├── auth/
│   └── service/
│       └── AuthServiceTest.java      # 认证服务测试
├── shop/
│   └── service/
│       └── ShopServiceTest.java      # 店铺服务测试
├── product/
│   └── service/
│       ├── ProductServiceTest.java   # 商品服务测试
│       └── CategoryServiceTest.java   # 分类服务测试
├── order/
│   └── service/
│       └── OrderServiceTest.java     # 订单服务测试
├── user/
│   └── service/
│       ├── UserServiceTest.java      # 用户服务测试
│       ├── PointsServiceTest.java    # 积分服务测试
│       └── AddressServiceTest.java   # 地址服务测试
├── message/
│   └── service/
│       └── MessageServiceTest.java   # 消息服务测试
└── file/
    └── service/
        └── FileServiceTest.java      # 文件服务测试
```

## 测试框架
- JUnit 5 + Mockito
- 测试配置：`application-test.yml` (H2内存数据库)
- 使用 `@ExtendWith(MockitoExtension.class)`

## 测试策略
1. 每个测试类对应一个新架构的Service
2. Mock该Service依赖的Mapper
3. 测试Service的核心业务方法
4. 测试完成后与旧测试对比，确保功能一致

## 注意事项
- 这些测试在**阶段0不执行**，仅准备好文件
- 阶段1完成后，entity/enums移到common包，这些测试的import需要更新
- 阶段2完成后，Service拆分，这些测试需要对应修改
- 每次阶段完成后运行测试，确保该阶段正确