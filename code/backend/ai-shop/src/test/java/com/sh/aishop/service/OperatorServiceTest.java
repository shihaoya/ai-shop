package com.sh.aishop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sh.aishop.common.Result;
import com.sh.aishop.common.ResultCode;
import com.sh.aishop.dto.PageRequest;
import com.sh.aishop.entity.*;
import com.sh.aishop.entity.enums.*;
import com.sh.aishop.mapper.*;
import com.sh.aishop.util.SnowflakeIdUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OperatorService 单元测试")
class OperatorServiceTest {

    @Mock
    private ShopMapper shopMapper;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PointsMapper pointsMapper;

    @Mock
    private InviteCodeMapper inviteCodeMapper;

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private OperatorService operatorService;

    private static final Long OPERATOR_ID = 1001L;
    private static final Long SHOP_ID = 2001L;
    private static final Long USER_ID = 3001L;
    private static final Long PRODUCT_ID = 4001L;
    private static final Long ORDER_ID = 5001L;
    private static final Long CATEGORY_ID = 6001L;

    private Shop createTestShop(Long operatorId, Integer status, Integer isActive) {
        Shop shop = new Shop();
        shop.setId(SHOP_ID);
        shop.setOperatorId(operatorId);
        shop.setName("测试店铺");
        shop.setDescription("测试描述");
        shop.setStatus(status);
        shop.setIsActive(isActive);
        shop.setDeleted(0);
        return shop;
    }

    private User createTestUser(Long id, Long parentId, Integer role, Integer status) {
        User user = new User();
        user.setId(id);
        user.setUsername("user_" + id);
        user.setNickname("用户" + id);
        user.setParentId(parentId);
        user.setRole(role);
        user.setStatus(status);
        user.setDeleted(0);
        return user;
    }

    private Product createTestProduct(Long shopId, Integer status, Integer stock) {
        Product product = new Product();
        product.setId(PRODUCT_ID);
        product.setShopId(shopId);
        product.setName("测试商品");
        product.setType(ProductType.PHYSICAL.getCode());
        product.setPrice(100);
        product.setStock(stock);
        product.setLimitPerUser(5);
        product.setStatus(status);
        product.setDeleted(0);
        return product;
    }

    private Category createTestCategory(Long shopId) {
        Category category = new Category();
        category.setId(CATEGORY_ID);
        category.setShopId(shopId);
        category.setName("测试分类");
        category.setSort(1);
        category.setDeleted(0);
        return category;
    }

    private Order createTestOrder(Long shopId, Long userId, Integer status) {
        Order order = new Order();
        order.setId(ORDER_ID);
        order.setShopId(shopId);
        order.setUserId(userId);
        order.setProductId(PRODUCT_ID);
        order.setPoints(100);
        order.setQuantity(1);
        order.setStatus(status);
        order.setOrderNo("P1234567890001");
        order.setCreatedAt(LocalDateTime.now());
        order.setDeleted(0);
        return order;
    }

    private Points createTestPoints(Long userId, Integer balance) {
        Points points = new Points();
        points.setId(7001L);
        points.setUserId(userId);
        points.setAmount(100);
        points.setBalance(balance);
        points.setType(PointsType.GRANT.getCode());
        points.setCreatedAt(LocalDateTime.now());
        points.setDeleted(0);
        return points;
    }

    private InviteCode createTestInviteCode(Long creatorId, Integer status) {
        InviteCode code = new InviteCode();
        code.setId(8001L);
        code.setCreatorId(creatorId);
        code.setCode("ABC12345");
        code.setRole(RoleEnum.NORMAL_USER.getCode());
        code.setStatus(status);
        code.setDeleted(0);
        return code;
    }

    private PageRequest createPageRequest() {
        PageRequest pageRequest = new PageRequest();
        pageRequest.setPage(1);
        pageRequest.setPageSize(10);
        return pageRequest;
    }

    // ============ 店铺管理测试 ============

    @Nested
    @DisplayName("getMyShop 测试")
    class GetMyShopTests {

        @Test
        @DisplayName("有店铺时返回店铺信息")
        void shouldReturnShopWhenExists() {
            try (MockedStatic<SnowflakeIdUtil> mockedStatic = mockStatic(SnowflakeIdUtil.class)) {
                Shop shop = createTestShop(OPERATOR_ID, ShopStatus.APPROVED.getCode(), 1);

                when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);

                Result<?> result = operatorService.getMyShop(OPERATOR_ID);

                assertEquals(ResultCode.SUCCESS, result.getCode());
                Map<String, Object> data = (Map<String, Object>) result.getData();
                assertEquals(true, data.get("hasShop"));
                assertEquals("测试店铺", data.get("name"));
            }
        }

        @Test
        @DisplayName("无店铺时返回hasShop=false")
        void shouldReturnNoShopWhenNotExists() {
            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            Result<?> result = operatorService.getMyShop(OPERATOR_ID);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            Map<String, Object> data = (Map<String, Object>) result.getData();
            assertEquals(false, data.get("hasShop"));
        }
    }

    @Nested
    @DisplayName("applyShop 测试")
    class ApplyShopTests {

        @Test
        @DisplayName("申请新店铺成功")
        void shouldCreateShopSuccessfully() {
            try (MockedStatic<SnowflakeIdUtil> mockedStatic = mockStatic(SnowflakeIdUtil.class)) {
                mockedStatic.when(SnowflakeIdUtil::nextId).thenReturn(SHOP_ID);

                when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
                when(shopMapper.insert(any(Shop.class))).thenReturn(1);

                Result<?> result = operatorService.applyShop(OPERATOR_ID, "新店铺", "描述");

                assertEquals(ResultCode.SUCCESS, result.getCode());
                verify(shopMapper).insert(any(Shop.class));
            }
        }

        @Test
        @DisplayName("已有店铺不能重复申请")
        void shouldFailWhenShopAlreadyExists() {
            Shop existingShop = createTestShop(OPERATOR_ID, ShopStatus.PENDING.getCode(), 0);
            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingShop);

            Result<?> result = operatorService.applyShop(OPERATOR_ID, "新店铺", "描述");

            assertEquals(ResultCode.FAIL, result.getCode());
            assertTrue(result.getMessage().contains("已有店铺"));
            verify(shopMapper, never()).insert(any(Shop.class));
        }
    }

    @Nested
    @DisplayName("changeShopStatus 测试")
    class ChangeShopStatusTests {

        @Test
        @DisplayName("修改店铺状态成功")
        void shouldChangeShopStatusSuccessfully() {
            Shop shop = createTestShop(OPERATOR_ID, ShopStatus.APPROVED.getCode(), 0);
            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);
            when(shopMapper.updateById(any(Shop.class))).thenReturn(1);

            Result<?> result = operatorService.changeShopStatus(OPERATOR_ID, 1);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            verify(shopMapper).updateById(any(Shop.class));
        }

        @Test
        @DisplayName("店铺不存在时返回失败")
        void shouldFailWhenShopNotFound() {
            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            Result<?> result = operatorService.changeShopStatus(OPERATOR_ID, 1);

            assertEquals(ResultCode.SHOP_NOT_FOUND, result.getCode());
        }
    }

    // ============ 分类管理测试 ============

    @Nested
    @DisplayName("getCategories 测试")
    class GetCategoriesTests {

        @Test
        @DisplayName("获取分类列表成功")
        void shouldReturnCategoriesSuccessfully() {
            Shop shop = createTestShop(OPERATOR_ID, ShopStatus.APPROVED.getCode(), 1);
            List<Category> categories = Arrays.asList(createTestCategory(SHOP_ID));

            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);
            when(categoryMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(categories);

            Result<?> result = operatorService.getCategories(OPERATOR_ID);

            assertEquals(ResultCode.SUCCESS, result.getCode());
        }

        @Test
        @DisplayName("店铺不存在时返回失败")
        void shouldFailWhenShopNotFound() {
            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            Result<?> result = operatorService.getCategories(OPERATOR_ID);

            assertEquals(ResultCode.SHOP_NOT_FOUND, result.getCode());
        }
    }

    @Nested
    @DisplayName("createCategory 测试")
    class CreateCategoryTests {

        @Test
        @DisplayName("创建分类成功")
        void shouldCreateCategorySuccessfully() {
            try (MockedStatic<SnowflakeIdUtil> mockedStatic = mockStatic(SnowflakeIdUtil.class)) {
                mockedStatic.when(SnowflakeIdUtil::nextId).thenReturn(CATEGORY_ID);

                Shop shop = createTestShop(OPERATOR_ID, ShopStatus.APPROVED.getCode(), 1);
                when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);
                when(categoryMapper.insert(any(Category.class))).thenReturn(1);

                Result<?> result = operatorService.createCategory(OPERATOR_ID, "新分类", 1);

                assertEquals(ResultCode.SUCCESS, result.getCode());
                verify(categoryMapper).insert(any(Category.class));
            }
        }

        @Test
        @DisplayName("店铺不存在时返回失败")
        void shouldFailWhenShopNotFound() {
            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            Result<?> result = operatorService.createCategory(OPERATOR_ID, "新分类", 1);

            assertEquals(ResultCode.SHOP_NOT_FOUND, result.getCode());
        }
    }

    @Nested
    @DisplayName("updateCategory 测试")
    class UpdateCategoryTests {

        @Test
        @DisplayName("更新分类成功")
        void shouldUpdateCategorySuccessfully() {
            Shop shop = createTestShop(OPERATOR_ID, ShopStatus.APPROVED.getCode(), 1);
            Category category = createTestCategory(SHOP_ID);

            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);
            when(categoryMapper.selectById(CATEGORY_ID)).thenReturn(category);
            when(categoryMapper.updateById(any(Category.class))).thenReturn(1);

            Result<?> result = operatorService.updateCategory(CATEGORY_ID, OPERATOR_ID, "更新分类", 2);

            assertEquals(ResultCode.SUCCESS, result.getCode());
        }

        @Test
        @DisplayName("分类不存在时返回失败")
        void shouldFailWhenCategoryNotFound() {
            Shop shop = createTestShop(OPERATOR_ID, ShopStatus.APPROVED.getCode(), 1);
            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);
            when(categoryMapper.selectById(CATEGORY_ID)).thenReturn(null);

            Result<?> result = operatorService.updateCategory(CATEGORY_ID, OPERATOR_ID, "更新分类", 2);

            assertEquals(ResultCode.CATEGORY_NOT_FOUND, result.getCode());
        }

        @Test
        @DisplayName("店铺不存在时返回失败")
        void shouldFailWhenShopNotFound() {
            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            Result<?> result = operatorService.updateCategory(CATEGORY_ID, OPERATOR_ID, "更新分类", 2);

            assertEquals(ResultCode.SHOP_NOT_FOUND, result.getCode());
        }
    }

    @Nested
    @DisplayName("deleteCategory 测试")
    class DeleteCategoryTests {

        @Test
        @DisplayName("删除分类成功")
        void shouldDeleteCategorySuccessfully() {
            Shop shop = createTestShop(OPERATOR_ID, ShopStatus.APPROVED.getCode(), 1);
            Category category = createTestCategory(SHOP_ID);

            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);
            when(categoryMapper.selectById(CATEGORY_ID)).thenReturn(category);
            when(categoryMapper.deleteById(CATEGORY_ID)).thenReturn(1);

            Result<?> result = operatorService.deleteCategory(CATEGORY_ID, OPERATOR_ID);

            assertEquals(ResultCode.SUCCESS, result.getCode());
        }

        @Test
        @DisplayName("分类不属于店铺时返回失败")
        void shouldFailWhenCategoryBelongsToOtherShop() {
            Shop shop = createTestShop(OPERATOR_ID, ShopStatus.APPROVED.getCode(), 1);
            Category category = createTestCategory(9999L);

            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);
            when(categoryMapper.selectById(CATEGORY_ID)).thenReturn(category);

            Result<?> result = operatorService.deleteCategory(CATEGORY_ID, OPERATOR_ID);

            assertEquals(ResultCode.CATEGORY_NOT_FOUND, result.getCode());
        }
    }

    // ============ 商品管理测试 ============

    @Nested
    @DisplayName("getProducts 测试")
    class GetProductsTests {

        @Test
        @DisplayName("获取商品列表成功")
        void shouldReturnProductsSuccessfully() {
            Shop shop = createTestShop(OPERATOR_ID, ShopStatus.APPROVED.getCode(), 1);
            List<Product> products = Arrays.asList(createTestProduct(SHOP_ID, ProductStatus.ON_SALE.getCode(), 100));

            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);
            when(productMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(products);

            PageRequest pageRequest = createPageRequest();
            Result<?> result = operatorService.getProducts(OPERATOR_ID, pageRequest);

            assertEquals(ResultCode.SUCCESS, result.getCode());
        }

        @Test
        @DisplayName("店铺不存在时返回失败")
        void shouldFailWhenShopNotFound() {
            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            PageRequest pageRequest = createPageRequest();
            Result<?> result = operatorService.getProducts(OPERATOR_ID, pageRequest);

            assertEquals(ResultCode.SHOP_NOT_FOUND, result.getCode());
        }
    }

    @Nested
    @DisplayName("createProduct 测试")
    class CreateProductTests {

        @Test
        @DisplayName("创建商品成功")
        void shouldCreateProductSuccessfully() {
            try (MockedStatic<SnowflakeIdUtil> mockedStatic = mockStatic(SnowflakeIdUtil.class)) {
                mockedStatic.when(SnowflakeIdUtil::nextId).thenReturn(PRODUCT_ID);

                Shop shop = createTestShop(OPERATOR_ID, ShopStatus.APPROVED.getCode(), 1);
                when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);
                when(productMapper.insert(any(Product.class))).thenReturn(1);

                Map<String, Object> params = new HashMap<>();
                params.put("name", "新商品");
                params.put("price", 100);
                params.put("stock", 50);

                Result<?> result = operatorService.createProduct(OPERATOR_ID, params);

                assertEquals(ResultCode.SUCCESS, result.getCode());
                verify(productMapper).insert(any(Product.class));
            }
        }

        @Test
        @DisplayName("店铺不存在时返回失败")
        void shouldFailWhenShopNotFound() {
            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            Map<String, Object> params = new HashMap<>();
            params.put("name", "新商品");

            Result<?> result = operatorService.createProduct(OPERATOR_ID, params);

            assertEquals(ResultCode.SHOP_NOT_FOUND, result.getCode());
        }
    }

    @Nested
    @DisplayName("getProduct 测试")
    class GetProductTests {

        @Test
        @DisplayName("获取单个商品成功")
        void shouldReturnProductSuccessfully() {
            Shop shop = createTestShop(OPERATOR_ID, ShopStatus.APPROVED.getCode(), 1);
            Product product = createTestProduct(SHOP_ID, ProductStatus.ON_SALE.getCode(), 100);

            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);
            when(productMapper.selectById(PRODUCT_ID)).thenReturn(product);

            Result<?> result = operatorService.getProduct(OPERATOR_ID, PRODUCT_ID);

            assertEquals(ResultCode.SUCCESS, result.getCode());
        }

        @Test
        @DisplayName("商品不属于店铺时返回失败")
        void shouldFailWhenProductBelongsToOtherShop() {
            Shop shop = createTestShop(OPERATOR_ID, ShopStatus.APPROVED.getCode(), 1);
            Product product = createTestProduct(9999L, ProductStatus.ON_SALE.getCode(), 100);

            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);
            when(productMapper.selectById(PRODUCT_ID)).thenReturn(product);

            Result<?> result = operatorService.getProduct(OPERATOR_ID, PRODUCT_ID);

            assertEquals(ResultCode.PRODUCT_NOT_FOUND, result.getCode());
        }
    }

    @Nested
    @DisplayName("updateProduct 测试")
    class UpdateProductTests {

        @Test
        @DisplayName("更新商品成功")
        void shouldUpdateProductSuccessfully() {
            Shop shop = createTestShop(OPERATOR_ID, ShopStatus.APPROVED.getCode(), 1);
            Product product = createTestProduct(SHOP_ID, ProductStatus.ON_SALE.getCode(), 100);

            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);
            when(productMapper.selectById(PRODUCT_ID)).thenReturn(product);
            when(productMapper.updateById(any(Product.class))).thenReturn(1);

            Map<String, Object> params = new HashMap<>();
            params.put("name", "更新商品");

            Result<?> result = operatorService.updateProduct(OPERATOR_ID, PRODUCT_ID, params);

            assertEquals(ResultCode.SUCCESS, result.getCode());
        }
    }

    @Nested
    @DisplayName("deleteProduct 测试")
    class DeleteProductTests {

        @Test
        @DisplayName("删除商品成功")
        void shouldDeleteProductSuccessfully() {
            Shop shop = createTestShop(OPERATOR_ID, ShopStatus.APPROVED.getCode(), 1);
            Product product = createTestProduct(SHOP_ID, ProductStatus.ON_SALE.getCode(), 100);

            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);
            when(productMapper.selectById(PRODUCT_ID)).thenReturn(product);
            when(productMapper.deleteById(PRODUCT_ID)).thenReturn(1);

            Result<?> result = operatorService.deleteProduct(OPERATOR_ID, PRODUCT_ID);

            assertEquals(ResultCode.SUCCESS, result.getCode());
        }
    }

    // ============ 商品上下架（通过updateProduct）测试 ============

    @Nested
    @DisplayName("getOrders 测试")
    class GetOrdersTests {

        @Test
        @DisplayName("获取订单列表成功")
        void shouldReturnOrdersSuccessfully() {
            Shop shop = createTestShop(OPERATOR_ID, ShopStatus.APPROVED.getCode(), 1);
            User user = createTestUser(USER_ID, OPERATOR_ID, RoleEnum.NORMAL_USER.getCode(), UserStatus.NORMAL.getCode());
            List<Order> orders = Arrays.asList(createTestOrder(SHOP_ID, USER_ID, OrderStatus.CREATED.getCode()));

            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);
            when(orderMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(orders);
            when(userMapper.selectById(USER_ID)).thenReturn(user);

            PageRequest pageRequest = createPageRequest();
            Result<?> result = operatorService.getOrders(OPERATOR_ID, pageRequest, null);

            assertEquals(ResultCode.SUCCESS, result.getCode());
        }

        @Test
        @DisplayName("按状态筛选订单")
        void shouldFilterOrdersByStatus() {
            Shop shop = createTestShop(OPERATOR_ID, ShopStatus.APPROVED.getCode(), 1);
            List<Order> orders = Arrays.asList(createTestOrder(SHOP_ID, USER_ID, OrderStatus.CREATED.getCode()));

            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);
            when(orderMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(orders);
            when(userMapper.selectById(USER_ID)).thenReturn(null);

            PageRequest pageRequest = createPageRequest();
            Result<?> result = operatorService.getOrders(OPERATOR_ID, pageRequest, OrderStatus.CREATED.getCode());

            assertEquals(ResultCode.SUCCESS, result.getCode());
        }
    }

    @Nested
    @DisplayName("confirmOrder 测试")
    class ConfirmOrderTests {

        @Test
        @DisplayName("确认订单成功")
        void shouldConfirmOrderSuccessfully() {
            Shop shop = createTestShop(OPERATOR_ID, ShopStatus.APPROVED.getCode(), 1);
            Order order = createTestOrder(SHOP_ID, USER_ID, OrderStatus.CREATED.getCode());

            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);
            when(orderMapper.selectById(ORDER_ID)).thenReturn(order);
            when(orderMapper.updateById(any(Order.class))).thenReturn(1);
            when(messageMapper.insert(any(Message.class))).thenReturn(1);

            Result<?> result = operatorService.confirmOrder(OPERATOR_ID, ORDER_ID);

            assertEquals(ResultCode.SUCCESS, result.getCode());
        }

        @Test
        @DisplayName("订单不存在时返回失败")
        void shouldFailWhenOrderNotFound() {
            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            Result<?> result = operatorService.confirmOrder(OPERATOR_ID, ORDER_ID);

            assertEquals(ResultCode.ORDER_NOT_FOUND, result.getCode());
        }

        @Test
        @DisplayName("订单状态不正确时返回失败")
        void shouldFailWhenOrderStatusIncorrect() {
            Shop shop = createTestShop(OPERATOR_ID, ShopStatus.APPROVED.getCode(), 1);
            Order order = createTestOrder(SHOP_ID, USER_ID, OrderStatus.SHIPPED.getCode());

            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);
            when(orderMapper.selectById(ORDER_ID)).thenReturn(order);

            Result<?> result = operatorService.confirmOrder(OPERATOR_ID, ORDER_ID);

            assertEquals(ResultCode.ORDER_STATUS_ERROR, result.getCode());
        }
    }

    @Nested
    @DisplayName("shipOrder 测试")
    class ShipOrderTests {

        @Test
        @DisplayName("发货实体商品成功")
        void shouldShipPhysicalOrderSuccessfully() {
            Shop shop = createTestShop(OPERATOR_ID, ShopStatus.APPROVED.getCode(), 1);
            Order order = createTestOrder(SHOP_ID, USER_ID, OrderStatus.CONFIRMED.getCode());
            Product product = createTestProduct(SHOP_ID, ProductStatus.ON_SALE.getCode(), 100);
            product.setType(ProductType.PHYSICAL.getCode());

            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);
            when(orderMapper.selectById(ORDER_ID)).thenReturn(order);
            when(productMapper.selectById(PRODUCT_ID)).thenReturn(product);
            when(orderMapper.updateById(any(Order.class))).thenReturn(1);
            when(messageMapper.insert(any(Message.class))).thenReturn(1);

            Map<String, Object> params = new HashMap<>();
            params.put("expressCompany", "顺丰");
            params.put("expressNo", "SF123456789");

            Result<?> result = operatorService.shipOrder(OPERATOR_ID, ORDER_ID, params);

            assertEquals(ResultCode.SUCCESS, result.getCode());
        }

        @Test
        @DisplayName("发货虚拟商品成功")
        void shouldShipVirtualOrderSuccessfully() {
            Shop shop = createTestShop(OPERATOR_ID, ShopStatus.APPROVED.getCode(), 1);
            Order order = createTestOrder(SHOP_ID, USER_ID, OrderStatus.CONFIRMED.getCode());
            Product product = createTestProduct(SHOP_ID, ProductStatus.ON_SALE.getCode(), 100);
            product.setType(ProductType.VIRTUAL.getCode());

            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);
            when(orderMapper.selectById(ORDER_ID)).thenReturn(order);
            when(productMapper.selectById(PRODUCT_ID)).thenReturn(product);
            when(orderMapper.updateById(any(Order.class))).thenReturn(1);
            when(messageMapper.insert(any(Message.class))).thenReturn(1);

            Map<String, Object> params = new HashMap<>();
            params.put("deliveryContent", "兑换码：ABC123");

            Result<?> result = operatorService.shipOrder(OPERATOR_ID, ORDER_ID, params);

            assertEquals(ResultCode.SUCCESS, result.getCode());
        }

        @Test
        @DisplayName("订单状态不正确时返回失败")
        void shouldFailWhenOrderStatusIncorrect() {
            Shop shop = createTestShop(OPERATOR_ID, ShopStatus.APPROVED.getCode(), 1);
            Order order = createTestOrder(SHOP_ID, USER_ID, OrderStatus.CREATED.getCode());

            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);
            when(orderMapper.selectById(ORDER_ID)).thenReturn(order);

            Map<String, Object> params = new HashMap<>();
            params.put("expressCompany", "顺丰");

            Result<?> result = operatorService.shipOrder(OPERATOR_ID, ORDER_ID, params);

            assertEquals(ResultCode.ORDER_STATUS_ERROR, result.getCode());
        }
    }

    @Nested
    @DisplayName("closeOrder 测试")
    class CloseOrderTests {

        @Test
        @DisplayName("关闭订单成功")
        void shouldCloseOrderSuccessfully() {
            Shop shop = createTestShop(OPERATOR_ID, ShopStatus.APPROVED.getCode(), 1);
            Order order = createTestOrder(SHOP_ID, USER_ID, OrderStatus.CREATED.getCode());
            Points points = createTestPoints(USER_ID, 500);

            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);
            when(orderMapper.selectById(ORDER_ID)).thenReturn(order);
            when(pointsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(points);
            when(orderMapper.updateById(any(Order.class))).thenReturn(1);
            when(pointsMapper.insert(any(Points.class))).thenReturn(1);
            when(messageMapper.insert(any(Message.class))).thenReturn(1);

            Result<?> result = operatorService.closeOrder(OPERATOR_ID, ORDER_ID, "缺货");

            assertEquals(ResultCode.SUCCESS, result.getCode());
        }

        @Test
        @DisplayName("订单已结束时返回失败")
        void shouldFailWhenOrderAlreadyClosed() {
            Shop shop = createTestShop(OPERATOR_ID, ShopStatus.APPROVED.getCode(), 1);
            Order order = createTestOrder(SHOP_ID, USER_ID, OrderStatus.CLOSED.getCode());

            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);
            when(orderMapper.selectById(ORDER_ID)).thenReturn(order);

            Result<?> result = operatorService.closeOrder(OPERATOR_ID, ORDER_ID, "缺货");

            assertEquals(ResultCode.ORDER_STATUS_ERROR, result.getCode());
        }
    }

    @Nested
    @DisplayName("completeOrder 测试")
    class CompleteOrderTests {

        @Test
        @DisplayName("完成订单成功")
        void shouldCompleteOrderSuccessfully() {
            Shop shop = createTestShop(OPERATOR_ID, ShopStatus.APPROVED.getCode(), 1);
            Order order = createTestOrder(SHOP_ID, USER_ID, OrderStatus.SHIPPED.getCode());

            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);
            when(orderMapper.selectById(ORDER_ID)).thenReturn(order);
            when(orderMapper.updateById(any(Order.class))).thenReturn(1);
            when(messageMapper.insert(any(Message.class))).thenReturn(1);

            Result<?> result = operatorService.completeOrder(OPERATOR_ID, ORDER_ID);

            assertEquals(ResultCode.SUCCESS, result.getCode());
        }

        @Test
        @DisplayName("订单状态不正确时返回失败")
        void shouldFailWhenOrderStatusIncorrect() {
            Shop shop = createTestShop(OPERATOR_ID, ShopStatus.APPROVED.getCode(), 1);
            Order order = createTestOrder(SHOP_ID, USER_ID, OrderStatus.CREATED.getCode());

            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);
            when(orderMapper.selectById(ORDER_ID)).thenReturn(order);

            Result<?> result = operatorService.completeOrder(OPERATOR_ID, ORDER_ID);

            assertEquals(ResultCode.ORDER_STATUS_ERROR, result.getCode());
        }
    }

    // ============ 用户管理测试 ============

    @Nested
    @DisplayName("getUsers 测试")
    class GetUsersTests {

        @Test
        @DisplayName("获取用户列表成功")
        void shouldReturnUsersSuccessfully() {
            Shop shop = createTestShop(OPERATOR_ID, ShopStatus.APPROVED.getCode(), 1);
            List<User> users = Arrays.asList(createTestUser(USER_ID, OPERATOR_ID, RoleEnum.NORMAL_USER.getCode(), UserStatus.NORMAL.getCode()));
            Points points = createTestPoints(USER_ID, 500);

            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);
            when(userMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(users);
            when(pointsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(points);

            PageRequest pageRequest = createPageRequest();
            Result<?> result = operatorService.getUsers(OPERATOR_ID, pageRequest);

            assertEquals(ResultCode.SUCCESS, result.getCode());
        }
    }

    @Nested
    @DisplayName("adjustPoints 测试")
    class AdjustPointsTests {

        @Test
        @DisplayName("发放积分成功")
        void shouldGrantPointsSuccessfully() {
            try (MockedStatic<SnowflakeIdUtil> mockedStatic = mockStatic(SnowflakeIdUtil.class)) {
                mockedStatic.when(SnowflakeIdUtil::nextId).thenReturn(7001L);

                Shop shop = createTestShop(OPERATOR_ID, ShopStatus.APPROVED.getCode(), 1);
                User user = createTestUser(USER_ID, OPERATOR_ID, RoleEnum.NORMAL_USER.getCode(), UserStatus.NORMAL.getCode());
                Points points = createTestPoints(USER_ID, 500);

                when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);
                when(userMapper.selectById(USER_ID)).thenReturn(user);
                when(pointsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(points);
                when(pointsMapper.insert(any(Points.class))).thenReturn(1);
                when(messageMapper.insert(any(Message.class))).thenReturn(1);

                Result<?> result = operatorService.adjustPoints(OPERATOR_ID, USER_ID, 100, "测试发放");

                assertEquals(ResultCode.SUCCESS, result.getCode());
            }
        }

        @Test
        @DisplayName("扣除积分成功")
        void shouldDeductPointsSuccessfully() {
            try (MockedStatic<SnowflakeIdUtil> mockedStatic = mockStatic(SnowflakeIdUtil.class)) {
                mockedStatic.when(SnowflakeIdUtil::nextId).thenReturn(7001L);

                Shop shop = createTestShop(OPERATOR_ID, ShopStatus.APPROVED.getCode(), 1);
                User user = createTestUser(USER_ID, OPERATOR_ID, RoleEnum.NORMAL_USER.getCode(), UserStatus.NORMAL.getCode());
                Points points = createTestPoints(USER_ID, 500);

                when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);
                when(userMapper.selectById(USER_ID)).thenReturn(user);
                when(pointsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(points);
                when(pointsMapper.insert(any(Points.class))).thenReturn(1);
                when(messageMapper.insert(any(Message.class))).thenReturn(1);

                Result<?> result = operatorService.adjustPoints(OPERATOR_ID, USER_ID, -100, "测试扣除");

                assertEquals(ResultCode.SUCCESS, result.getCode());
            }
        }

        @Test
        @DisplayName("积分不足时返回失败")
        void shouldFailWhenPointsInsufficient() {
            Shop shop = createTestShop(OPERATOR_ID, ShopStatus.APPROVED.getCode(), 1);
            User user = createTestUser(USER_ID, OPERATOR_ID, RoleEnum.NORMAL_USER.getCode(), UserStatus.NORMAL.getCode());
            Points points = createTestPoints(USER_ID, 50);

            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);
            when(userMapper.selectById(USER_ID)).thenReturn(user);
            when(pointsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(points);

            Result<?> result = operatorService.adjustPoints(OPERATOR_ID, USER_ID, -100, "测试扣除");

            assertEquals(ResultCode.FAIL, result.getCode());
        }

        @Test
        @DisplayName("用户不属于操作员时返回失败")
        void shouldFailWhenUserNotBelongsToOperator() {
            Shop shop = createTestShop(OPERATOR_ID, ShopStatus.APPROVED.getCode(), 1);
            User user = createTestUser(USER_ID, 9999L, RoleEnum.NORMAL_USER.getCode(), UserStatus.NORMAL.getCode());

            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);
            when(userMapper.selectById(USER_ID)).thenReturn(user);

            Result<?> result = operatorService.adjustPoints(OPERATOR_ID, USER_ID, 100, "测试");

            assertEquals(ResultCode.USER_NOT_FOUND, result.getCode());
        }
    }

    @Nested
    @DisplayName("getPointsLog 测试")
    class GetPointsLogTests {

        @Test
        @DisplayName("获取积分日志成功")
        void shouldReturnPointsLogSuccessfully() {
            Shop shop = createTestShop(OPERATOR_ID, ShopStatus.APPROVED.getCode(), 1);
            List<Points> pointsList = Arrays.asList(createTestPoints(USER_ID, 500));

            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);
            when(pointsMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(pointsList);

            PageRequest pageRequest = createPageRequest();
            Result<?> result = operatorService.getPointsLog(OPERATOR_ID, USER_ID, pageRequest);

            assertEquals(ResultCode.SUCCESS, result.getCode());
        }
    }

    @Nested
    @DisplayName("approveUser 测试")
    class ApproveUserTests {

        @Test
        @DisplayName("审核用户成功")
        void shouldApproveUserSuccessfully() {
            Shop shop = createTestShop(OPERATOR_ID, ShopStatus.APPROVED.getCode(), 1);
            User user = createTestUser(USER_ID, OPERATOR_ID, RoleEnum.NORMAL_USER.getCode(), UserStatus.PENDING.getCode());

            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);
            when(userMapper.selectById(USER_ID)).thenReturn(user);
            when(userMapper.updateById(any(User.class))).thenReturn(1);
            when(messageMapper.insert(any(Message.class))).thenReturn(1);

            Result<?> result = operatorService.approveUser(OPERATOR_ID, USER_ID);

            assertEquals(ResultCode.SUCCESS, result.getCode());
        }

        @Test
        @DisplayName("用户不是待审核状态时返回失败")
        void shouldFailWhenUserNotPending() {
            Shop shop = createTestShop(OPERATOR_ID, ShopStatus.APPROVED.getCode(), 1);
            User user = createTestUser(USER_ID, OPERATOR_ID, RoleEnum.NORMAL_USER.getCode(), UserStatus.NORMAL.getCode());

            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);
            when(userMapper.selectById(USER_ID)).thenReturn(user);

            Result<?> result = operatorService.approveUser(OPERATOR_ID, USER_ID);

            assertEquals(ResultCode.FAIL, result.getCode());
        }
    }

    // ============ 邀请码测试 ============

    @Nested
    @DisplayName("getInviteCode 测试")
    class GetInviteCodeTests {

        @Test
        @DisplayName("存在邀请码时返回")
        void shouldReturnInviteCodeWhenExists() {
            InviteCode code = createTestInviteCode(OPERATOR_ID, InviteCodeStatus.ACTIVE.getCode());

            when(inviteCodeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(code);

            Result<?> result = operatorService.getInviteCode(OPERATOR_ID);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            assertEquals("ABC12345", result.getData());
        }

        @Test
        @DisplayName("无邀请码时返回null")
        void shouldReturnNullWhenNoCode() {
            when(inviteCodeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            Result<?> result = operatorService.getInviteCode(OPERATOR_ID);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            assertNull(result.getData());
        }
    }

    @Nested
    @DisplayName("createInviteCode 测试")
    class CreateInviteCodeTests {

        @Test
        @DisplayName("创建新邀请码成功（无旧码）")
        void shouldCreateInviteCodeSuccessfully() {
            try (MockedStatic<SnowflakeIdUtil> mockedStatic = mockStatic(SnowflakeIdUtil.class)) {
                mockedStatic.when(SnowflakeIdUtil::nextId).thenReturn(8001L);

                when(inviteCodeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
                when(inviteCodeMapper.insert(any(InviteCode.class))).thenReturn(1);

                Result<?> result = operatorService.createInviteCode(OPERATOR_ID);

                assertEquals(ResultCode.SUCCESS, result.getCode());
                verify(inviteCodeMapper).insert(any(InviteCode.class));
            }
        }

        @Test
        @DisplayName("创建新邀请码成功（有旧码则作废）")
        void shouldInvalidateOldCodeAndCreateNew() {
            try (MockedStatic<SnowflakeIdUtil> mockedStatic = mockStatic(SnowflakeIdUtil.class)) {
                mockedStatic.when(SnowflakeIdUtil::nextId).thenReturn(8002L);

                InviteCode oldCode = createTestInviteCode(OPERATOR_ID, InviteCodeStatus.ACTIVE.getCode());
                when(inviteCodeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(oldCode);
                when(inviteCodeMapper.updateById(any(InviteCode.class))).thenReturn(1);
                when(inviteCodeMapper.insert(any(InviteCode.class))).thenReturn(1);

                Result<?> result = operatorService.createInviteCode(OPERATOR_ID);

                assertEquals(ResultCode.SUCCESS, result.getCode());
                verify(inviteCodeMapper).updateById(oldCode);
                verify(inviteCodeMapper).insert(any(InviteCode.class));
            }
        }
    }

    // ============ 消息测试 ============

    @Nested
    @DisplayName("getMessages 测试")
    class GetMessagesTests {

        @Test
        @DisplayName("获取消息列表成功")
        void shouldReturnMessagesSuccessfully() {
            Message message = new Message();
            message.setId(9001L);
            message.setUserId(OPERATOR_ID);
            message.setTitle("测试消息");
            message.setContent("内容");
            message.setType(MessageType.ORDER.getCode());
            message.setIsRead(0);
            message.setCreatedAt(LocalDateTime.now());
            message.setDeleted(0);

            List<Message> messages = Arrays.asList(message);

            when(messageMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(messages);

            PageRequest pageRequest = createPageRequest();
            Result<?> result = operatorService.getMessages(OPERATOR_ID, pageRequest);

            assertEquals(ResultCode.SUCCESS, result.getCode());
        }
    }

    @Nested
    @DisplayName("markMessageRead 测试")
    class MarkMessageReadTests {

        @Test
        @DisplayName("标记消息已读成功")
        void shouldMarkMessageReadSuccessfully() {
            Message message = new Message();
            message.setId(9001L);
            message.setUserId(OPERATOR_ID);
            message.setIsRead(0);

            when(messageMapper.selectById(9001L)).thenReturn(message);
            when(messageMapper.updateById(any(Message.class))).thenReturn(1);

            Result<?> result = operatorService.markMessageRead(OPERATOR_ID, 9001L);

            assertEquals(ResultCode.SUCCESS, result.getCode());
        }
    }

    // ============ 创建用户测试 ============

    @Nested
    @DisplayName("createUser 测试")
    class CreateUserTests {

        @Test
        @DisplayName("创建用户成功")
        void shouldCreateUserSuccessfully() {
            try (MockedStatic<SnowflakeIdUtil> mockedStatic = mockStatic(SnowflakeIdUtil.class)) {
                mockedStatic.when(SnowflakeIdUtil::nextId).thenReturn(USER_ID);

                Shop shop = createTestShop(OPERATOR_ID, ShopStatus.APPROVED.getCode(), 1);
                when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);
                when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
                when(userMapper.insert(any(User.class))).thenReturn(1);

                Result<?> result = operatorService.createUser(OPERATOR_ID, "newuser", "新用户", "password123");

                assertEquals(ResultCode.SUCCESS, result.getCode());
                verify(userMapper).insert(any(User.class));
            }
        }

        @Test
        @DisplayName("用户名已存在时返回失败")
        void shouldFailWhenUsernameExists() {
            Shop shop = createTestShop(OPERATOR_ID, ShopStatus.APPROVED.getCode(), 1);
            User existingUser = createTestUser(9999L, OPERATOR_ID, RoleEnum.NORMAL_USER.getCode(), UserStatus.NORMAL.getCode());

            when(shopMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shop);
            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingUser);

            Result<?> result = operatorService.createUser(OPERATOR_ID, "existinguser", "新用户", "password123");

            assertEquals(ResultCode.USERNAME_EXISTS, result.getCode());
        }
    }
}