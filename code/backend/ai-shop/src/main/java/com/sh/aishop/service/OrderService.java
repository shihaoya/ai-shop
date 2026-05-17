package com.sh.aishop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sh.aishop.common.Result;
import com.sh.aishop.common.ResultCode;
import com.sh.aishop.dto.OrderDTO;
import com.sh.aishop.dto.PageRequest;
import com.sh.aishop.dto.PageResult;
import com.sh.aishop.entity.*;
import com.sh.aishop.entity.enums.OrderStatus;
import com.sh.aishop.entity.enums.ProductStatus;
import com.sh.aishop.entity.enums.ShopStatus;
import com.sh.aishop.mapper.*;
import com.sh.aishop.util.SnowflakeIdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ShopMapper shopMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PointsMapper pointsMapper;
    @Autowired
    private FileRecordMapper fileRecordMapper;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ============ 用户接口 ============

    /**
     * 创建订单
     */
    @Transactional
    public Result<?> createOrder(Long userId, Long productId, Integer quantity, Map<String, Object> addressInfo) {
        // 1. 校验商品
        Product product = productMapper.selectById(productId);
        if (product == null || product.getDeleted() != 0) {
            return Result.fail(ResultCode.PRODUCT_NOT_FOUND, "商品不存在");
        }
        // 公开接口：只能买营业中店铺的上架商品
        Shop shop = shopMapper.selectById(product.getShopId());
        if (shop == null || shop.getIsActive() != 1 || shop.getStatus() != ShopStatus.APPROVED.getCode() || shop.getDeleted() != 0) {
            return Result.fail(ResultCode.PRODUCT_NOT_FOUND, "商品不存在");
        }
        if (product.getStatus() != ProductStatus.ON_SALE.getCode()) {
            return Result.fail(ResultCode.PRODUCT_NOT_FOUND, "商品不存在或已下架");
        }

        // 2. 校验库存
        if (product.getStock() < quantity) {
            return Result.fail(ResultCode.STOCK_NOT_ENOUGH, "库存不足");
        }

        // 3. 校验用户积分
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail(ResultCode.USER_NOT_FOUND, "用户不存在");
        }
        int totalPoints = product.getPrice() * quantity;
        if (user.getPoints() < totalPoints) {
            return Result.fail(ResultCode.POINTS_NOT_ENOUGH, "积分不足");
        }

        // 4. 创建订单
        Order order = new Order();
        order.setId(SnowflakeIdUtil.nextId());
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setShopId(product.getShopId());
        order.setProductId(productId);
        order.setPoints(product.getPrice());
        order.setQuantity(quantity);
        order.setStatus(OrderStatus.CREATED.getCode());

        // 收货信息
        if (addressInfo != null) {
            if (addressInfo.get("receiverName") != null) order.setReceiverName(addressInfo.get("receiverName").toString());
            if (addressInfo.get("receiverPhone") != null) order.setReceiverPhone(addressInfo.get("receiverPhone").toString());
            if (addressInfo.get("receiverProvince") != null) order.setReceiverProvince(addressInfo.get("receiverProvince").toString());
            if (addressInfo.get("receiverCity") != null) order.setReceiverCity(addressInfo.get("receiverCity").toString());
            if (addressInfo.get("receiverDistrict") != null) order.setReceiverDistrict(addressInfo.get("receiverDistrict").toString());
            if (addressInfo.get("receiverDetail") != null) order.setReceiverDetail(addressInfo.get("receiverDetail").toString());
        }

        orderMapper.insert(order);

        // 5. 扣减库存和积分
        product.setStock(product.getStock() - quantity);
        productMapper.updateById(product);

        user.setPoints(user.getPoints() - totalPoints);
        userMapper.updateById(user);

        // 6. 记录积分变动
        Points log = new Points();
        log.setId(SnowflakeIdUtil.nextId());
        log.setUserId(userId);
        log.setAmount(-totalPoints);
        log.setBalance(user.getPoints());
        log.setRemark("兑换商品：" + product.getName());
        pointsLogMapper.insert(log);

        return Result.success(order.getId().toString());
    }

    /**
     * 用户订单列表
     */
    public Result<?> getOrders(Long userId, PageRequest pageRequest, Integer status) {
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

        List<OrderDTO> dtos = orders.stream().map(o -> toOrderDTO(o, false)).collect(Collectors.toList());

        return Result.success(new PageResult<>(dtos, total, pageRequest.getPage(), pageRequest.getPageSize()));
    }

    /**
     * 用户订单详情
     */
    public Result<?> getOrder(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getDeleted() != 0) {
            return Result.fail(ResultCode.ORDER_NOT_FOUND, "订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            return Result.fail(ResultCode.ORDER_NOT_FOUND, "订单不存在");
        }
        return Result.success(toOrderDTO(order, true));
    }

    /**
     * 用户取消订单
     */
    @Transactional
    public Result<?> closeOrder(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getDeleted() != 0) {
            return Result.fail(ResultCode.ORDER_NOT_FOUND, "订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            return Result.fail(ResultCode.ORDER_NOT_FOUND, "订单不存在");
        }
        if (order.getStatus() != OrderStatus.CREATED.getCode()) {
            return Result.fail(ResultCode.ORDER_STATUS_ERROR, "只能取消已下单的订单");
        }

        // 退回库存和积分
        Product product = productMapper.selectById(order.getProductId());
        if (product != null) {
            product.setStock(product.getStock() + order.getQuantity());
            productMapper.updateById(product);
        }

        User user = userMapper.selectById(userId);
        int refund = order.getPoints() * order.getQuantity();
        user.setPoints(user.getPoints() + refund);
        userMapper.updateById(user);

        Points log = new Points();
        log.setId(SnowflakeIdUtil.nextId());
        log.setUserId(userId);
        log.setAmount(refund);
        log.setBalance(user.getPoints());
        log.setRemark("订单取消退款：" + order.getOrderNo());
        pointsMapper.insert(log);

        order.setStatus(OrderStatus.CLOSED.getCode());
        order.setClosedAt(LocalDateTime.now());
        order.setCloseReason("用户取消");
        orderMapper.updateById(order);

        return Result.success();
    }

    /**
     * 用户确认收货
     */
    @Transactional
    public Result<?> completeOrder(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getDeleted() != 0) {
            return Result.fail(ResultCode.ORDER_NOT_FOUND, "订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            return Result.fail(ResultCode.ORDER_NOT_FOUND, "订单不存在");
        }
        if (order.getStatus() != OrderStatus.SHIPPED.getCode()) {
            return Result.fail(ResultCode.ORDER_STATUS_ERROR, "只能确认已发货的订单");
        }

        order.setStatus(OrderStatus.COMPLETED.getCode());
        order.setCompletedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        return Result.success();
    }

    // ============ 商家接口 ============

    /**
     * 商家订单列表
     */
    public Result<?> getShopOrders(Long operatorId, PageRequest pageRequest, Integer status) {
        Shop shop = getApprovedShop(operatorId);
        if (shop == null) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getShopId, shop.getId()).eq(Order::getDeleted, 0);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreatedAt);

        List<Order> orders = orderMapper.selectList(wrapper);
        Long total = (long) orders.size();

        int offset = pageRequest.getOffset().intValue();
        orders = orders.stream().skip(offset).limit(pageRequest.getPageSize()).collect(Collectors.toList());

        List<OrderDTO> dtos = orders.stream().map(o -> toOrderDTO(o, false)).collect(Collectors.toList());

        return Result.success(new PageResult<>(dtos, total, pageRequest.getPage(), pageRequest.getPageSize()));
    }

    /**
     * 商家确认订单
     */
    @Transactional
    public Result<?> confirmOrder(Long operatorId, Long orderId) {
        Shop shop = getApprovedShop(operatorId);
        if (shop == null) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getShopId().equals(shop.getId()) || order.getDeleted() != 0) {
            return Result.fail(ResultCode.ORDER_NOT_FOUND, "订单不存在");
        }
        if (order.getStatus() != OrderStatus.CREATED.getCode()) {
            return Result.fail(ResultCode.ORDER_STATUS_ERROR, "只能确认已下单的订单");
        }

        order.setStatus(OrderStatus.CONFIRMED.getCode());
        orderMapper.updateById(order);

        return Result.success();
    }

    /**
     * 商家发货
     */
    @Transactional
    public Result<?> shipOrder(Long operatorId, Long orderId, Map<String, Object> params) {
        Shop shop = getApprovedShop(operatorId);
        if (shop == null) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getShopId().equals(shop.getId()) || order.getDeleted() != 0) {
            return Result.fail(ResultCode.ORDER_NOT_FOUND, "订单不存在");
        }
        if (order.getStatus() != OrderStatus.CONFIRMED.getCode()) {
            return Result.fail(ResultCode.ORDER_STATUS_ERROR, "只能发货已确认的订单");
        }

        if (params.get("trackingNo") != null) order.setExpressNo(params.get("trackingNo").toString());
        if (params.get("carrier") != null) order.setExpressCompany(params.get("carrier").toString());
        if (params.get("deliveryContent") != null) order.setDeliveryContent(params.get("deliveryContent").toString());

        order.setStatus(OrderStatus.SHIPPED.getCode());
        orderMapper.updateById(order);

        return Result.success();
    }

    /**
     * 商家关闭订单
     */
    @Transactional
    public Result<?> closeShopOrder(Long operatorId, Long orderId, String reason) {
        Shop shop = getApprovedShop(operatorId);
        if (shop == null) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getShopId().equals(shop.getId()) || order.getDeleted() != 0) {
            return Result.fail(ResultCode.ORDER_NOT_FOUND, "订单不存在");
        }
        if (order.getStatus() == OrderStatus.COMPLETED.getCode() || order.getStatus() == OrderStatus.CLOSED.getCode()) {
            return Result.fail(ResultCode.ORDER_STATUS_ERROR, "订单已无法关闭");
        }

        // 退回库存和积分
        Product product = productMapper.selectById(order.getProductId());
        if (product != null) {
            product.setStock(product.getStock() + order.getQuantity());
            productMapper.updateById(product);
        }

        User user = userMapper.selectById(order.getUserId());
        int refund = order.getPoints() * order.getQuantity();
        user.setPoints(user.getPoints() + refund);
        userMapper.updateById(user);

        Points log = new Points();
        log.setId(SnowflakeIdUtil.nextId());
        log.setUserId(order.getUserId());
        log.setAmount(refund);
        log.setBalance(user.getPoints());
        log.setRemark("订单关闭退款：" + order.getOrderNo());
        pointsMapper.insert(log);

        order.setStatus(OrderStatus.CLOSED.getCode());
        order.setClosedAt(LocalDateTime.now());
        order.setCloseReason(StringUtils.hasText(reason) ? reason : "商家关闭");
        orderMapper.updateById(order);

        return Result.success();
    }

    // ============ 私有方法 ============

    private Shop getApprovedShop(Long operatorId) {
        return shopMapper.selectOne(new LambdaQueryWrapper<Shop>()
                .eq(Shop::getOperatorId, operatorId)
                .eq(Shop::getStatus, ShopStatus.APPROVED.getCode())
                .eq(Shop::getDeleted, 0));
    }

    private String generateOrderNo() {
        return "P" + System.currentTimeMillis() + (int) (Math.random() * 10000);
    }

    private OrderDTO toOrderDTO(Order o, boolean loadDetail) {
        OrderDTO dto = new OrderDTO();
        dto.setId(o.getId().toString());
        dto.setOrderNo(o.getOrderNo());
        dto.setUserId(o.getUserId().toString());
        dto.setShopId(o.getShopId().toString());
        dto.setProductId(o.getProductId().toString());
        dto.setPoints(o.getPoints());
        dto.setQuantity(o.getQuantity());
        dto.setTotalPoints(o.getPoints() * o.getQuantity());
        dto.setStatus(o.getStatus());
        dto.setCreatedAt(o.getCreatedAt() != null ? o.getCreatedAt().format(DTF) : null);
        dto.setCompletedAt(o.getCompletedAt() != null ? o.getCompletedAt().format(DTF) : null);
        dto.setClosedAt(o.getClosedAt() != null ? o.getClosedAt().format(DTF) : null);
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

        // 加载商品名称
        Product product = productMapper.selectById(o.getProductId());
        if (product != null) {
            dto.setProductName(product.getName());
            if (product.getMainImage() != null) {
                FileRecord file = fileRecordMapper.selectById(Long.parseLong(product.getMainImage()));
                if (file != null) dto.setProductImageUrl(file.getUrl());
            }
        }

        // 加载店铺名称
        Shop shop = shopMapper.selectById(o.getShopId());
        if (shop != null) dto.setShopName(shop.getName());

        // 加载买家昵称
        User user = userMapper.selectById(o.getUserId());
        if (user != null) dto.setUserNickname(user.getNickname());

        return dto;
    }
}