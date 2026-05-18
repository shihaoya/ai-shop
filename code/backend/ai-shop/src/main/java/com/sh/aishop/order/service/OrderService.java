package com.sh.aishop.order.service;

import com.sh.aishop.common.Result;
import com.sh.aishop.common.ResultCode;
import com.sh.aishop.common.entity.Category;
import com.sh.aishop.common.entity.FileRecord;
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
import com.sh.aishop.common.enums.ShopStatus;
import com.sh.aishop.dto.OrderDTO;
import com.sh.aishop.dto.PageRequest;
import com.sh.aishop.dto.PageResult;
import com.sh.aishop.dto.ProductDTO;
import com.sh.aishop.mapper.CategoryMapper;
import com.sh.aishop.mapper.FileRecordMapper;
import com.sh.aishop.mapper.MessageMapper;
import com.sh.aishop.mapper.OrderMapper;
import com.sh.aishop.mapper.PointsMapper;
import com.sh.aishop.mapper.ProductMapper;
import com.sh.aishop.mapper.ShopMapper;
import com.sh.aishop.mapper.UserMapper;
import com.sh.aishop.shop.service.ShopService;
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
public class OrderService {
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private PointsMapper pointsMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ShopMapper shopMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private FileRecordMapper fileRecordMapper;
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private ShopService shopService;

    // ============ 店铺端订单管理 ============
    public Result<?> getShopOrders(Long operatorId, PageRequest pageRequest, Integer status) {
        var shopResult = shopService.getMyShop(operatorId);
        if (shopResult.getData() == null || !((Map<?, ?>) shopResult.getData()).containsKey("hasShop")) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }
        Map<?, ?> shopData = (Map<?, ?>) shopResult.getData();
        if (!Boolean.TRUE.equals(shopData.get("hasShop"))) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getShopId, Long.parseLong(shopData.get("id").toString()))
               .eq(Order::getDeleted, 0);
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
            var user = userMapper.selectById(order.getUserId());
            if (user != null) dto.setUserNickname(user.getNickname());
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

    // ============ 用户端商品浏览 ============
    public Result<?> getProducts(PageRequest pageRequest) {
        LambdaQueryWrapper<Shop> shopWrapper = new LambdaQueryWrapper<>();
        shopWrapper.eq(Shop::getIsActive, 1)
                   .eq(Shop::getStatus, ShopStatus.APPROVED.getCode())
                   .eq(Shop::getDeleted, 0);
        List<Shop> activeShops = shopMapper.selectList(shopWrapper);
        if (activeShops.isEmpty()) {
            return Result.success(new PageResult<>(Collections.emptyList(), 0L, pageRequest.getPage(), pageRequest.getPageSize()));
        }
        List<Long> shopIds = activeShops.stream().map(Shop::getId).collect(Collectors.toList());

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Product::getShopId, shopIds)
               .eq(Product::getStatus, ProductStatus.ON_SALE.getCode())
               .eq(Product::getDeleted, 0);
        if (StringUtils.hasText(pageRequest.getKeyword())) {
            wrapper.like(Product::getName, pageRequest.getKeyword());
        }
        wrapper.orderByDesc(Product::getCreatedAt);

        List<Product> products = productMapper.selectList(wrapper);
        Long total = (long) products.size();

        int offset = pageRequest.getOffset().intValue();
        products = products.stream().skip(offset).limit(pageRequest.getPageSize()).collect(Collectors.toList());

        // 批量加载分类名
        List<Long> catIds = products.stream()
            .map(Product::getCategoryId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, String> catNameMap = new HashMap<>();
        if (!catIds.isEmpty()) {
            categoryMapper.selectBatchIds(catIds).forEach(c -> catNameMap.put(c.getId(), c.getName()));
        }

        List<ProductDTO> dtos = products.stream().map(p -> {
            ProductDTO dto = new ProductDTO();
            dto.setId(p.getId().toString());
            dto.setShopId(p.getShopId().toString());
            Shop shop = activeShops.stream().filter(s -> s.getId().equals(p.getShopId())).findFirst().orElse(null);
            if (shop != null) dto.setShopName(shop.getName());
            dto.setCategoryId(p.getCategoryId() != null ? p.getCategoryId().toString() : null);
            dto.setCategoryName(p.getCategoryId() != null ? catNameMap.get(p.getCategoryId()) : null);
            dto.setName(p.getName());
            dto.setType(p.getType());
            dto.setTypeDesc(p.getType() == ProductType.VIRTUAL.getCode() ? "虚拟" : "实体");
            dto.setPrice(p.getPrice());
            dto.setStock(p.getStock());
            dto.setLimitPerUser(p.getLimitPerUser());
            dto.setMainImage(p.getMainImage() != null ? p.getMainImage().toString() : null);
            dto.setDetailImages(p.getDetailImages());
            dto.setDescription(p.getDescription());
            dto.setDeliveryInfo(p.getDeliveryInfo());
            dto.setStatus(p.getStatus());
            dto.setStatusDesc("上架");
            return dto;
        }).collect(Collectors.toList());

        // 批量加载 mainImage URL
        List<String> fileIds = dtos.stream()
            .map(ProductDTO::getMainImage)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        if (!fileIds.isEmpty()) {
            Map<String, String> urlMap = fileRecordMapper.selectBatchIds(fileIds)
                .stream()
                .collect(Collectors.toMap(f -> f.getId().toString(), FileRecord::getUrl));
            dtos.forEach(dto -> {
                if (dto.getMainImage() != null) {
                    dto.setMainImageUrl(urlMap.get(dto.getMainImage()));
                }
            });
        }

        return Result.success(new PageResult<>(dtos, total, pageRequest.getPage(), pageRequest.getPageSize()));
    }

    public Result<?> getProduct(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null || product.getDeleted() != 0) {
            return Result.fail(ResultCode.PRODUCT_NOT_FOUND, "商品不存在");
        }
        if (product.getStatus() != ProductStatus.ON_SALE.getCode()) {
            return Result.fail(ResultCode.PRODUCT_OFF_SALE, "商品已下架");
        }

        Shop shop = shopMapper.selectById(product.getShopId());
        if (shop == null || shop.getIsActive() != 1 || shop.getStatus() != ShopStatus.APPROVED.getCode()) {
            return Result.fail(ResultCode.PRODUCT_OFF_SALE, "商品不可购买");
        }

        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId().toString());
        dto.setShopId(product.getShopId().toString());
        dto.setShopName(shop.getName());
        dto.setCategoryId(product.getCategoryId() != null ? product.getCategoryId().toString() : null);
        dto.setName(product.getName());
        dto.setType(product.getType());
        dto.setTypeDesc(product.getType() == ProductType.VIRTUAL.getCode() ? "虚拟" : "实体");
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setLimitPerUser(product.getLimitPerUser());
        dto.setMainImage(product.getMainImage() != null ? product.getMainImage().toString() : null);
        dto.setDetailImages(product.getDetailImages());
        dto.setDescription(product.getDescription());
        dto.setDeliveryInfo(product.getDeliveryInfo());
        dto.setStatus(product.getStatus());
        dto.setStatusDesc("上架");

        return Result.success(dto);
    }

    // ============ 用户端下单 ============
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
                    .notIn(Order::getStatus, OrderStatus.CLOSED.getCode())
                    .eq(Order::getDeleted, 0));
            if (boughtCount + quantity > product.getLimitPerUser()) {
                return Result.fail(ResultCode.FAIL, "超出购买限制");
            }
        }

        int totalPoints = product.getPrice() * quantity;

        Points latest = pointsMapper.selectOne(new LambdaQueryWrapper<Points>()
                .eq(Points::getUserId, userId)
                .orderByDesc(Points::getCreatedAt).last("LIMIT 1"));
        int currentBalance = latest != null ? latest.getBalance() : 0;
        if (currentBalance < totalPoints) {
            return Result.fail(ResultCode.POINTS_INSUFFICIENT, "积分不足，当前积分：" + currentBalance);
        }
        int newBalance = currentBalance - totalPoints;

        Points points = new Points();
        points.setId(SnowflakeIdUtil.nextId());
        points.setUserId(userId);
        points.setAmount(-totalPoints);
        points.setBalance(newBalance);
        points.setType(PointsType.EXCHANGE.getCode());
        points.setRemark("兑换商品：" + product.getName());
        pointsMapper.insert(points);

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

    // ============ 用户端订单查询 ============
    public Result<?> getUserOrders(Long userId, PageRequest pageRequest, Integer status) {
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
    public Result<?> closeUserOrder(Long userId, Long orderId) {
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
    public Result<?> completeUserOrder(Long userId, Long orderId) {
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

    // ============ 私有方法 ============
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