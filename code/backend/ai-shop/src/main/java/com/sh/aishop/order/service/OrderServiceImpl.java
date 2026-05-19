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
import com.sh.aishop.shop.service.ShopService;
import com.sh.aishop.util.SnowflakeIdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements IOrderService {
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
    @Autowired
    private ShopService shopService;

    public Result<?> getShopOrders(Long operatorId, PageRequest pageRequest, Integer status) {
        var shopResult = shopService.getMyShop(operatorId);
        if (shopResult.getData() == null || !((Map<?, ?>) shopResult.getData()).containsKey("hasShop")) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "您还没有店铺");
        }
        Map<?, ?> shopData = (Map<?, ?>) shopResult.getData();
        if (!Boolean.TRUE.equals(shopData.get("hasShop"))) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "您还没有店铺");
        }

        Long shopId = Long.valueOf(shopData.get("id").toString());

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getShopId, shopId).eq(Order::getDeleted, 0);
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

    @Transactional
    public Result<?> confirmOrder(Long operatorId, Long orderId) {
        Order order = getOrderByOperatorId(operatorId, orderId);
        if (order == null) {
            return Result.fail(ResultCode.ORDER_NOT_FOUND, "订单不存在");
        }
        if (order.getStatus() != OrderStatus.CREATED.getCode()) {
            return Result.fail(ResultCode.ORDER_STATUS_ERROR, "订单状态不正确");
        }
        order.setStatus(OrderStatus.CONFIRMED.getCode());
        orderMapper.updateById(order);
        sendOrderMessage(order.getUserId(), orderId, "您的订单已确认");
        return Result.success();
    }

    @Transactional
    public Result<?> shipOrder(Long operatorId, Long orderId, Map<String, Object> params) {
        Order order = getOrderByOperatorId(operatorId, orderId);
        if (order == null) {
            return Result.fail(ResultCode.ORDER_NOT_FOUND, "订单不存在");
        }
        if (order.getStatus() != OrderStatus.CONFIRMED.getCode()) {
            return Result.fail(ResultCode.ORDER_STATUS_ERROR, "订单状态不正确");
        }

        Product product = productMapper.selectById(order.getProductId());
        boolean isVirtual = product != null && product.getType() == ProductType.VIRTUAL.getCode();

        order.setStatus(OrderStatus.SHIPPED.getCode());
        if (isVirtual) {
            order.setDeliveryContent(params.get("deliveryContent") != null ? params.get("deliveryContent").toString() : null);
        } else {
            order.setExpressCompany(params.get("expressCompany") != null ? params.get("expressCompany").toString() : null);
            order.setExpressNo(params.get("expressNo") != null ? params.get("expressNo").toString() : null);
        }
        orderMapper.updateById(order);

        sendOrderMessage(order.getUserId(), orderId, "您的订单已发货");

        return Result.success();
    }

    @Transactional
    public Result<?> closeShopOrder(Long operatorId, Long orderId, String reason) {
        Order order = getOrderByOperatorId(operatorId, orderId);
        if (order == null) {
            return Result.fail(ResultCode.ORDER_NOT_FOUND, "订单不存在");
        }
        if (order.getStatus() == OrderStatus.COMPLETED.getCode() || order.getStatus() == OrderStatus.CLOSED.getCode()) {
            return Result.fail(ResultCode.ORDER_STATUS_ERROR, "订单已结束");
        }

        refundPoints(order);

        order.setStatus(OrderStatus.CLOSED.getCode());
        order.setClosedAt(LocalDateTime.now());
        order.setCloseReason(reason);
        orderMapper.updateById(order);

        sendOrderMessage(order.getUserId(), orderId, "您的订单已关闭：" + reason);

        return Result.success();
    }

    @Transactional
    public Result<?> completeShopOrder(Long operatorId, Long orderId) {
        Order order = getOrderByOperatorId(operatorId, orderId);
        if (order == null) {
            return Result.fail(ResultCode.ORDER_NOT_FOUND, "订单不存在");
        }
        if (order.getStatus() != OrderStatus.SHIPPED.getCode()) {
            return Result.fail(ResultCode.ORDER_STATUS_ERROR, "订单状态不正确");
        }

        order.setStatus(OrderStatus.COMPLETED.getCode());
        order.setCompletedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        sendOrderMessage(order.getUserId(), orderId, "您的订单已完成");

        return Result.success();
    }

    private Order getOrderByOperatorId(Long operatorId, Long orderId) {
        var shopResult = shopService.getMyShop(operatorId);
        if (shopResult.getData() == null || !((Map<?, ?>) shopResult.getData()).containsKey("hasShop")) {
            return null;
        }
        Map<?, ?> shopData = (Map<?, ?>) shopResult.getData();
        if (!Boolean.TRUE.equals(shopData.get("hasShop"))) {
            return null;
        }
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getShopId().toString().equals(shopData.get("id").toString())) {
            return null;
        }
        return order;
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
