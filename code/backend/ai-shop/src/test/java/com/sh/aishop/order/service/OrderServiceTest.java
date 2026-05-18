package com.sh.aishop.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sh.aishop.common.Result;
import com.sh.aishop.common.ResultCode;
import com.sh.aishop.common.entity.Message;
import com.sh.aishop.common.entity.Order;
import com.sh.aishop.common.entity.Points;
import com.sh.aishop.common.entity.Product;
import com.sh.aishop.common.entity.Shop;
import com.sh.aishop.common.enums.OrderStatus;
import com.sh.aishop.common.enums.PointsType;
import com.sh.aishop.common.enums.ProductStatus;
import com.sh.aishop.common.enums.ProductType;
import com.sh.aishop.common.enums.ShopStatus;
import com.sh.aishop.common.dto.PageRequest;
import com.sh.aishop.order.mapper.OrderMapper;
import com.sh.aishop.user.mapper.PointsMapper;
import com.sh.aishop.user.mapper.UserMapper;
import com.sh.aishop.shop.mapper.ShopMapper;
import com.sh.aishop.product.mapper.ProductMapper;
import com.sh.aishop.product.mapper.CategoryMapper;
import com.sh.aishop.file.mapper.FileRecordMapper;
import com.sh.aishop.message.mapper.MessageMapper;
import com.sh.aishop.shop.service.ShopService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("OrderService 单元测试")
class OrderServiceTest {

    @Mock
    private ProductMapper productMapper;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private PointsMapper pointsMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private ShopMapper shopMapper;
    @Mock
    private CategoryMapper categoryMapper;
    @Mock
    private FileRecordMapper fileRecordMapper;
    @Mock
    private MessageMapper messageMapper;
    @Mock
    private ShopService shopService;

    @InjectMocks
    private com.sh.aishop.order.service.OrderService orderService;

    private Order createTestOrder(Long id, Long userId, Long shopId, Long productId, Integer status) {
        Order order = new Order();
        order.setId(id);
        order.setUserId(userId);
        order.setShopId(shopId);
        order.setProductId(productId);
        order.setPoints(100);
        order.setQuantity(1);
        order.setStatus(status);
        order.setOrderNo("P" + System.currentTimeMillis());
        order.setDeleted(0);
        return order;
    }

    private Product createTestProduct(Long id, Long shopId, Integer price, Integer stock, Integer status) {
        Product product = new Product();
        product.setId(id);
        product.setShopId(shopId);
        product.setName("测试商品");
        product.setType(ProductType.PHYSICAL.getCode());
        product.setPrice(price);
        product.setStock(stock);
        product.setLimitPerUser(0);
        product.setStatus(status);
        product.setDeleted(0);
        return product;
    }

    private Points createTestPoints(Long userId, Integer balance) {
        Points points = new Points();
        points.setId(1L);
        points.setUserId(userId);
        points.setAmount(0);
        points.setBalance(balance);
        points.setType(PointsType.GRANT.getCode());
        points.setDeleted(0);
        return points;
    }

    private Map<String, Object> shopDataWithId(Long shopId) {
        Map<String, Object> data = new HashMap<>();
        data.put("hasShop", true);
        data.put("id", shopId.toString());
        return data;
    }

    @Nested
    @DisplayName("getShopOrders() 获取店铺订单")
    class GetShopOrdersTests {

        @Test
        @DisplayName("获取店铺订单成功")
        void getShopOrders_Success() {
            Order order = createTestOrder(1L, 200L, 100L, 1L, OrderStatus.CREATED.getCode());

            Result<?> shopResult = Result.success(shopDataWithId(100L));
            doReturn(shopResult).when(shopService).getMyShop(any());
            when(orderMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(order));
            when(userMapper.selectById(200L)).thenReturn(null);

            PageRequest pageRequest = new PageRequest();
            pageRequest.setPage(1);
            pageRequest.setPageSize(10);

            Result<?> result = orderService.getShopOrders(100L, pageRequest, null);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            assertNotNull(result.getData());
        }

        @Test
        @DisplayName("获取店铺订单失败 - 店铺不存在")
        void getShopOrders_Fail_NoShop() {
            Result<?> shopResult = Result.success(Collections.singletonMap("hasShop", false));
            doReturn(shopResult).when(shopService).getMyShop(any());

            PageRequest pageRequest = new PageRequest();
            pageRequest.setPage(1);
            pageRequest.setPageSize(10);

            Result<?> result = orderService.getShopOrders(100L, pageRequest, null);

            assertEquals(ResultCode.SHOP_NOT_FOUND, result.getCode());
        }
    }

    @Nested
    @DisplayName("confirmOrder() 确认订单")
    class ConfirmOrderTests {

        @Test
        @DisplayName("确认订单成功")
        void confirmOrder_Success() {
            Order order = createTestOrder(1L, 200L, 100L, 1L, OrderStatus.CREATED.getCode());

            Result<?> shopResult = Result.success(shopDataWithId(100L));
            doReturn(shopResult).when(shopService).getMyShop(any());
            when(orderMapper.selectById(1L)).thenReturn(order);
            when(orderMapper.updateById(any(Order.class))).thenReturn(1);
            when(messageMapper.insert(any(Message.class))).thenReturn(1);

            Result<?> result = orderService.confirmOrder(100L, 1L);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            verify(orderMapper).updateById(any(Order.class));
        }

        @Test
        @DisplayName("确认订单失败 - 订单状态不正确")
        void confirmOrder_Fail_WrongStatus() {
            Order order = createTestOrder(1L, 200L, 100L, 1L, OrderStatus.CONFIRMED.getCode());

            Result<?> shopResult = Result.success(shopDataWithId(100L));
            doReturn(shopResult).when(shopService).getMyShop(any());
            when(orderMapper.selectById(1L)).thenReturn(order);

            Result<?> result = orderService.confirmOrder(100L, 1L);

            assertEquals(ResultCode.ORDER_STATUS_ERROR, result.getCode());
        }
    }

    @Nested
    @DisplayName("shipOrder() 发货")
    class ShipOrderTests {

        @Test
        @DisplayName("发货成功 - 虚拟商品")
        void shipOrder_Success_Virtual() {
            Order order = createTestOrder(1L, 200L, 100L, 1L, OrderStatus.CONFIRMED.getCode());
            Product product = createTestProduct(1L, 100L, 100, 10, ProductStatus.ON_SALE.getCode());
            product.setType(ProductType.VIRTUAL.getCode());

            Result<?> shopResult = Result.success(shopDataWithId(100L));
            doReturn(shopResult).when(shopService).getMyShop(any());
            when(orderMapper.selectById(1L)).thenReturn(order);
            when(productMapper.selectById(1L)).thenReturn(product);
            when(orderMapper.updateById(any(Order.class))).thenReturn(1);
            when(messageMapper.insert(any(Message.class))).thenReturn(1);

            Map<String, Object> params = new HashMap<>();
            params.put("deliveryContent", "卡密：ABCD-1234");

            Result<?> result = orderService.shipOrder(100L, 1L, params);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            verify(orderMapper).updateById(any(Order.class));
        }

        @Test
        @DisplayName("发货失败 - 订单状态不正确")
        void shipOrder_Fail_WrongStatus() {
            Order order = createTestOrder(1L, 200L, 100L, 1L, OrderStatus.CREATED.getCode());

            Result<?> shopResult = Result.success(shopDataWithId(100L));
            doReturn(shopResult).when(shopService).getMyShop(any());
            when(orderMapper.selectById(1L)).thenReturn(order);

            Result<?> result = orderService.shipOrder(100L, 1L, new HashMap<>());

            assertEquals(ResultCode.ORDER_STATUS_ERROR, result.getCode());
        }
    }

    @Nested
    @DisplayName("createOrder() 创建订单")
    class CreateOrderTests {

        @Test
        @DisplayName("创建订单成功")
        void createOrder_Success() {
            Product product = createTestProduct(1L, 100L, 100, 10, ProductStatus.ON_SALE.getCode());
            Points points = createTestPoints(200L, 500);

            when(productMapper.selectById(1L)).thenReturn(product);
            when(orderMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
            when(pointsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(points);
            when(pointsMapper.insert(any(Points.class))).thenReturn(1);
            when(orderMapper.insert(any(Order.class))).thenReturn(1);
            when(productMapper.updateById(any(Product.class))).thenReturn(1);
            when(messageMapper.insert(any(Message.class))).thenReturn(1);

            Map<String, Object> addressInfo = new HashMap<>();
            addressInfo.put("receiver", "张三");
            addressInfo.put("phone", "13800138000");
            addressInfo.put("province", "广东省");
            addressInfo.put("city", "深圳市");
            addressInfo.put("district", "南山区");
            addressInfo.put("detail", "科技园");

            Result<?> result = orderService.createOrder(200L, 1L, 1, addressInfo);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            verify(orderMapper).insert(any(Order.class));
        }

        @Test
        @DisplayName("创建订单失败 - 商品不存在")
        void createOrder_Fail_ProductNotFound() {
            when(productMapper.selectById(1L)).thenReturn(null);

            Result<?> result = orderService.createOrder(200L, 1L, 1, null);

            assertEquals(ResultCode.PRODUCT_NOT_FOUND, result.getCode());
        }

        @Test
        @DisplayName("创建订单失败 - 积分不足")
        void createOrder_Fail_PointsInsufficient() {
            Product product = createTestProduct(1L, 100L, 100, 10, ProductStatus.ON_SALE.getCode());
            Points points = createTestPoints(200L, 50);

            when(productMapper.selectById(1L)).thenReturn(product);
            when(orderMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
            when(pointsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(points);

            Result<?> result = orderService.createOrder(200L, 1L, 1, null);

            assertEquals(ResultCode.POINTS_INSUFFICIENT, result.getCode());
        }

        @Test
        @DisplayName("创建订单失败 - 超出购买限制")
        void createOrder_Fail_ExceedLimit() {
            Product product = createTestProduct(1L, 100L, 100, 10, ProductStatus.ON_SALE.getCode());
            product.setLimitPerUser(2);
            Points points = createTestPoints(200L, 500);

            when(productMapper.selectById(1L)).thenReturn(product);
            when(orderMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);
            when(pointsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(points);

            Result<?> result = orderService.createOrder(200L, 1L, 1, null);

            assertEquals(ResultCode.FAIL, result.getCode());
            assertEquals("超出购买限制", result.getMessage());
        }
    }

    @Nested
    @DisplayName("getUserOrders() 获取用户订单")
    class GetUserOrdersTests {

        @Test
        @DisplayName("获取用户订单成功")
        void getUserOrders_Success() {
            Order order = createTestOrder(1L, 200L, 100L, 1L, OrderStatus.CREATED.getCode());
            Shop shop = new Shop();
            shop.setId(100L);
            shop.setName("测试店铺");

            when(orderMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(order));
            when(shopMapper.selectById(100L)).thenReturn(shop);

            PageRequest pageRequest = new PageRequest();
            pageRequest.setPage(1);
            pageRequest.setPageSize(10);

            Result<?> result = orderService.getUserOrders(200L, pageRequest, null);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            assertNotNull(result.getData());
        }
    }

    @Nested
    @DisplayName("closeUserOrder() 关闭订单")
    class CloseUserOrderTests {

        @Test
        @DisplayName("关闭订单成功")
        void closeUserOrder_Success() {
            Order order = createTestOrder(1L, 200L, 100L, 1L, OrderStatus.CREATED.getCode());
            order.setPoints(100);
            order.setQuantity(1);
            Product product = createTestProduct(1L, 100L, 100, 9, ProductStatus.ON_SALE.getCode());
            Points points = createTestPoints(200L, 400);

            when(orderMapper.selectById(1L)).thenReturn(order);
            when(pointsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(points);
            when(pointsMapper.insert(any(Points.class))).thenReturn(1);
            when(orderMapper.updateById(any(Order.class))).thenReturn(1);
            when(productMapper.selectById(1L)).thenReturn(product);
            when(productMapper.updateById(any(Product.class))).thenReturn(1);
            when(messageMapper.insert(any(Message.class))).thenReturn(1);

            Result<?> result = orderService.closeUserOrder(200L, 1L);

            assertEquals(ResultCode.SUCCESS, result.getCode());
        }

        @Test
        @DisplayName("关闭订单失败 - 只有已下单状态可以关闭")
        void closeUserOrder_Fail_WrongStatus() {
            Order order = createTestOrder(1L, 200L, 100L, 1L, OrderStatus.CONFIRMED.getCode());

            when(orderMapper.selectById(1L)).thenReturn(order);

            Result<?> result = orderService.closeUserOrder(200L, 1L);

            assertEquals(ResultCode.ORDER_STATUS_ERROR, result.getCode());
        }
    }
}