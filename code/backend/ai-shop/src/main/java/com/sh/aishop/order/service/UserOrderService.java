package com.sh.aishop.order.service;

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
import com.sh.aishop.common.enums.MessageType;
import com.sh.aishop.order.dto.OrderDTO;
import com.sh.aishop.common.dto.PageRequest;
import com.sh.aishop.common.dto.PageResult;
import com.sh.aishop.order.mapper.OrderMapper;
import com.sh.aishop.user.mapper.PointsMapper;
import com.sh.aishop.product.mapper.ProductMapper;
import com.sh.aishop.shop.mapper.ShopMapper;
import com.sh.aishop.message.mapper.MessageMapper;
import com.sh.aishop.util.SnowflakeIdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserOrderService {
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private PointsMapper pointsMapper;
    @Autowired
    private ShopMapper shopMapper;
    @Autowired
    private MessageMapper messageMapper;

    @Transactional
    public Result<?> createOrder(Long userId, Long productId, Integer quantity, Map<String, Object> addressInfo) {
        Product product = productMapper.selectById(productId);
        if (product == null || product.getDeleted() != 0) {
            return Result.fail(ResultCode.PRODUCT_NOT_FOUND, "商品不存在");
        }
        if (product.getStatus() != ProductStatus.ON_SALE.getCode()) {
            return Result.fail(ResultCode.PRODUCT_OFF_SALE, "商品已下架");
        }

        if (product.getStock() == 0) {
            return Result.fail(ResultCode.PRODUCT_STOCK_ZERO, "商品库存为0");
        }
        if (product.getStock() > 0 && product.getStock() < quantity) {
            return Result.fail(ResultCode.FAIL, "库存不足");
        }

        if (product.getLimitPerUser() > 0) {
            Long boughtCount = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                    .eq(Order::getUserId, userId)
                    .eq(Order::getProductId, productId)
                    .ne(Order::getStatus, OrderStatus.CLOSED.getCode())
                    .ne(Order::getDeleted, 1));
            if (boughtCount >= product.getLimitPerUser()) {
                return Result.fail(ResultCode.FAIL, "您已超过该商品的单用户限制");
            }
        }

        Shop shop = shopMapper.selectById(product.getShopId());
        if (shop == null || shop.getIsActive() != 1 || shop.getDeleted() != 0) {
            return Result.fail(ResultCode.FAIL, "店铺不可用");
        }
        if (shop.getStatus() != 1) {
            return Result.fail(ResultCode.FAIL, "店铺已歇业");
        }

        Order order = new Order();
        order.setId(SnowflakeIdUtil.nextId());
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setShopId(product.getShopId());
        order.setProductId(productId);
        order.setPoints(product.getPrice());
        order.setQuantity(quantity);
        order.setStatus(OrderStatus.CREATED.getCode());

        if (addressInfo != null && product.getType() == ProductType.PHYSICAL.getCode()) {
            Object receiver = addressInfo.get("receiver");
            Object phone = addressInfo.get("phone");
            Object province = addressInfo.get("province");
            Object city = addressInfo.get("city");
            Object district = addressInfo.get("district");
            Object detail = addressInfo.get("detail");

            if (receiver == null || phone == null || province == null || city == null || district == null || detail == null) {
                return Result.fail(ResultCode.FAIL, "收货地址信息不完整");
            }

            order.setReceiverName(receiver.toString());
            order.setReceiverPhone(phone.toString());
            order.setReceiverProvince(province.toString());
            order.setReceiverCity(city.toString());
            order.setReceiverDistrict(district.toString());
            order.setReceiverDetail(detail.toString());
        }

        orderMapper.insert(order);

        if (product.getStock() > 0) {
            product.setStock(product.getStock() - quantity);
            productMapper.updateById(product);
        }

        sendOrderMessageToShop(product.getShopId(), order.getId(), "新订单：" + product.getName() + " x" + quantity);

        Map<String, String> result = new HashMap<>();
        result.put("orderId", order.getId().toString());
        result.put("orderNo", order.getOrderNo());
        return Result.success(result);
    }

    public Result<?> getMyOrders(Long userId, PageRequest pageRequest, Integer status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId).eq(Order::getDeleted, 0);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreatedAt);

        List<Order> orders = orderMapper.selectList(wrapper);
        Long total = (long) orders.size();

        int offset = pageRequest.getOffset().intValue();
        orders = orders.stream().skip(offset).limit(pageRequest.getPageSize()).collect(Collectors.toList());

        List<OrderDTO> dtos = orders.stream().map(order -> {
            OrderDTO dto = toOrderDTO(order);
            Shop shop = shopMapper.selectById(order.getShopId());
            if (shop != null) dto.setShopName(shop.getName());
            return dto;
        }).collect(Collectors.toList());

        return Result.success(new PageResult<>(dtos, total, pageRequest.getPage(), pageRequest.getPageSize()));
    }

    public Result<?> getOrder(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            return Result.fail(ResultCode.ORDER_NOT_FOUND, "订单不存在");
        }
        return Result.success(toOrderDTO(order));
    }

    @Transactional
    public Result<?> cancelOrder(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            return Result.fail(ResultCode.ORDER_NOT_FOUND, "订单不存在");
        }
        if (order.getStatus() != OrderStatus.CREATED.getCode()) {
            return Result.fail(ResultCode.ORDER_STATUS_ERROR, "只有已下单状态可以关闭");
        }

        refundPoints(order);

        order.setStatus(OrderStatus.CLOSED.getCode());
        order.setClosedAt(LocalDateTime.now());
        order.setCloseReason("用户取消");
        orderMapper.updateById(order);

        Product product = productMapper.selectById(order.getProductId());
        if (product != null && product.getStock() >= 0) {
            product.setStock(product.getStock() + order.getQuantity());
            productMapper.updateById(product);
        }

        sendOrderMessage(userId, orderId, "您的订单已取消");

        return Result.success();
    }

    @Transactional
    public Result<?> completeOrder(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            return Result.fail(ResultCode.ORDER_NOT_FOUND, "订单不存在");
        }
        if (order.getStatus() != OrderStatus.SHIPPED.getCode()) {
            return Result.fail(ResultCode.ORDER_STATUS_ERROR, "只有已发货状态可以确认完成");
        }

        order.setStatus(OrderStatus.COMPLETED.getCode());
        order.setCompletedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        sendOrderMessage(userId, orderId, "您的订单已完成，感谢您的购买！");

        return Result.success();
    }

    private void sendOrderMessage(Long userId, Long orderId, String content) {
        Message msg = new Message();
        msg.setId(SnowflakeIdUtil.nextId());
        msg.setUserId(userId);
        msg.setTitle("订单通知");
        msg.setContent(content);
        msg.setType(MessageType.ORDER.getCode());
        msg.setRelatedId(orderId);
        msg.setIsRead(0);
        messageMapper.insert(msg);
    }

    private void sendOrderMessageToShop(Long shopId, Long orderId, String content) {
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null) return;

        Message msg = new Message();
        msg.setId(SnowflakeIdUtil.nextId());
        msg.setUserId(shop.getOperatorId());
        msg.setTitle("新订单提醒");
        msg.setContent(content);
        msg.setType(MessageType.ORDER.getCode());
        msg.setRelatedId(orderId);
        msg.setIsRead(0);
        messageMapper.insert(msg);
    }

    private String generateOrderNo() {
        return "P" + System.currentTimeMillis() + String.format("%04d", new Random().nextInt(10000));
    }

    private void refundPoints(Order order) {
        Points latest = pointsMapper.selectOne(new LambdaQueryWrapper<Points>()
                .eq(Points::getUserId, order.getUserId())
                .orderByDesc(Points::getCreatedAt).last("LIMIT 1"));
        int currentBalance = latest != null ? latest.getBalance() : 0;
        int refundAmount = order.getPoints() * order.getQuantity();
        int newBalance = currentBalance + refundAmount;

        Points points = new Points();
        points.setId(SnowflakeIdUtil.nextId());
        points.setUserId(order.getUserId());
        points.setAmount(refundAmount);
        points.setBalance(newBalance);
        points.setType(PointsType.REFUND.getCode());
        points.setRemark("订单关闭退款：" + order.getOrderNo());
        pointsMapper.insert(points);
    }

    OrderDTO toOrderDTO(Order o) {
        OrderDTO dto = new OrderDTO();
        dto.setId(o.getId().toString());
        dto.setOrderNo(o.getOrderNo());
        dto.setUserId(o.getUserId().toString());
        dto.setShopId(o.getShopId().toString());
        dto.setProductId(o.getProductId().toString());
        dto.setPoints(o.getPoints());
        dto.setTotalPoints(o.getPoints() * o.getQuantity());
        dto.setQuantity(o.getQuantity());
        dto.setStatus(o.getStatus());
        dto.setCreatedAt(o.getCreatedAt() != null ? o.getCreatedAt().toString() : null);
        dto.setCompletedAt(o.getCompletedAt() != null ? o.getCompletedAt().toString() : null);
        dto.setClosedAt(o.getClosedAt() != null ? o.getClosedAt().toString() : null);
        dto.setCloseReason(o.getCloseReason());
        dto.setReceiverName(o.getReceiverName());
        dto.setReceiverPhone(o.getReceiverPhone());
        dto.setReceiverProvince(o.getReceiverProvince());
        dto.setReceiverCity(o.getReceiverCity());
        dto.setReceiverDistrict(o.getReceiverDistrict());
        dto.setReceiverDetail(o.getReceiverDetail());
        dto.setExpressCompany(o.getExpressCompany());
        dto.setExpressNo(o.getExpressNo());
        dto.setDeliveryContent(o.getDeliveryContent());

        Product product = productMapper.selectById(o.getProductId());
        if (product != null) {
            dto.setProductName(product.getName());
            dto.setProductImage(product.getMainImage() != null ? product.getMainImage().toString() : null);
        }
        return dto;
    }
}
