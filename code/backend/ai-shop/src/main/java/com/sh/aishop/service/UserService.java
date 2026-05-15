package com.sh.aishop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sh.aishop.common.Result;
import com.sh.aishop.common.ResultCode;
import com.sh.aishop.dto.*;
import com.sh.aishop.entity.*;
import com.sh.aishop.entity.enums.*;
import com.sh.aishop.mapper.*;
import com.sh.aishop.util.SnowflakeIdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private PointsMapper pointsMapper;
    @Autowired
    private AddressMapper addressMapper;
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ShopMapper shopMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private FileRecordMapper fileRecordMapper;

    // ============ 商品 ============
    public Result<?> getProducts(PageRequest pageRequest) {
        // 只显示营业中店铺的上架商品
        LambdaQueryWrapper<Shop> shopWrapper = new LambdaQueryWrapper<>();
        shopWrapper.eq(Shop::getIsActive, 1).eq(Shop::getStatus, ShopStatus.APPROVED.getCode()).eq(Shop::getDeleted, 0);
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
            .map(ProductDTO::getMainImage).filter(Objects::nonNull).collect(Collectors.toList());
        if (!fileIds.isEmpty()) {
            Map<String, String> urlMap = fileRecordMapper.selectBatchIds(fileIds)
                .stream().collect(Collectors.toMap(f -> f.getId().toString(), FileRecord::getUrl));
            dtos.forEach(dto -> {
                if (dto.getMainImage() != null) dto.setMainImageUrl(urlMap.get(dto.getMainImage()));
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

    // ============ 下单 ============
    @Transactional
    public Result<?> createOrder(Long userId, Long productId, Integer quantity, Long addressId) {
        // 检查商品
        Product product = productMapper.selectById(productId);
        if (product == null || product.getDeleted() != 0) {
            return Result.fail(ResultCode.PRODUCT_NOT_FOUND, "商品不存在");
        }
        if (product.getStatus() != ProductStatus.ON_SALE.getCode()) {
            return Result.fail(ResultCode.PRODUCT_OFF_SALE, "商品已下架");
        }

        // 检查库存
        if (product.getStock() == 0) {
            return Result.fail(ResultCode.PRODUCT_STOCK_ZERO, "商品库存为0");
        }
        if (product.getStock() > 0 && product.getStock() < quantity) {
            return Result.fail(ResultCode.FAIL, "库存不足");
        }

        // 检查限购
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

        // 计算积分
        int totalPoints = product.getPrice() * quantity;

        // 扣减积分
        Points latest = pointsMapper.selectOne(new LambdaQueryWrapper<Points>()
                .eq(Points::getUserId, userId)
                .orderByDesc(Points::getCreatedAt).last("LIMIT 1"));
        int currentBalance = latest != null ? latest.getBalance() : 0;
        if (currentBalance < totalPoints) {
            return Result.fail(ResultCode.POINTS_INSUFFICIENT, "积分不足");
        }
        int newBalance = currentBalance - totalPoints;

        // 记录积分扣减
        Points points = new Points();
        points.setId(SnowflakeIdUtil.nextId());
        points.setUserId(userId);
        points.setAmount(-totalPoints);
        points.setBalance(newBalance);
        points.setType(PointsType.EXCHANGE.getCode());
        points.setRemark("兑换商品：" + product.getName());
        pointsMapper.insert(points);

        // 创建订单
        Order order = new Order();
        order.setId(SnowflakeIdUtil.nextId());
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setShopId(product.getShopId());
        order.setProductId(productId);
        order.setPoints(product.getPrice());
        order.setQuantity(quantity);
        order.setStatus(OrderStatus.CREATED.getCode());

        // 地址快照
        if (addressId != null && product.getType() == ProductType.PHYSICAL.getCode()) {
            Address addr = addressMapper.selectById(addressId);
            if (addr == null || !addr.getUserId().equals(userId)) {
                return Result.fail(ResultCode.ADDRESS_NOT_FOUND, "地址不存在");
            }
            order.setReceiverName(addr.getName());
            order.setReceiverPhone(addr.getPhone());
            order.setReceiverProvince(addr.getProvince());
            order.setReceiverCity(addr.getCity());
            order.setReceiverDistrict(addr.getDistrict());
            order.setReceiverDetail(addr.getDetail());
        }

        orderMapper.insert(order);

        // 扣减库存
        if (product.getStock() > 0) {
            product.setStock(product.getStock() - quantity);
            productMapper.updateById(product);
        }

        // 发送消息给店铺
        sendOrderMessageToShop(product.getShopId(), order.getId(), "新订单：" + product.getName() + " x" + quantity);

        Map<String, String> result = new HashMap<>();
        result.put("orderId", order.getId().toString());
        result.put("orderNo", order.getOrderNo());
        return Result.success(result);
    }

    // ============ 订单 ============
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
    public Result<?> closeOrder(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            return Result.fail(ResultCode.ORDER_NOT_FOUND, "订单不存在");
        }
        if (order.getStatus() != OrderStatus.CREATED.getCode()) {
            return Result.fail(ResultCode.ORDER_STATUS_ERROR, "只有已下单状态可以关闭");
        }

        // 退回积分
        refundPoints(order);

        order.setStatus(OrderStatus.CLOSED.getCode());
        order.setClosedAt(LocalDateTime.now());
        order.setCloseReason("用户取消");
        orderMapper.updateById(order);

        // 恢复库存
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

    // ============ 积分 ============
    public Result<?> getPoints(Long userId) {
        Points latest = pointsMapper.selectOne(new LambdaQueryWrapper<Points>()
                .eq(Points::getUserId, userId)
                .orderByDesc(Points::getCreatedAt).last("LIMIT 1"));
        return Result.success(java.util.Collections.singletonMap("balance",
                latest != null ? latest.getBalance() : 0));
    }

    public Result<?> getPointsLog(Long userId, PageRequest pageRequest) {
        LambdaQueryWrapper<Points> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Points::getUserId, userId).eq(Points::getDeleted, 0)
               .orderByDesc(Points::getCreatedAt);

        List<Points> list = pointsMapper.selectList(wrapper);
        Long total = (long) list.size();

        int offset = pageRequest.getOffset().intValue();
        list = list.stream().skip(offset).limit(pageRequest.getPageSize()).collect(Collectors.toList());

        List<Map<String, Object>> result = list.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId().toString());
            map.put("amount", p.getAmount());
            map.put("balance", p.getBalance());
            map.put("type", p.getType());
            map.put("typeDesc", getPointsTypeDesc(p.getType()));
            map.put("remark", p.getRemark());
            map.put("createdAt", p.getCreatedAt().toString());
            return map;
        }).collect(Collectors.toList());

        return Result.success(new PageResult<>(result, total, pageRequest.getPage(), pageRequest.getPageSize()));
    }

    // ============ 地址 ============
    public Result<?> getAddresses(Long userId) {
        List<Address> addresses = addressMapper.selectList(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId).eq(Address::getDeleted, 0)
                .orderByDesc(Address::getIsDefault).orderByDesc(Address::getCreatedAt));

        List<Map<String, Object>> result = addresses.stream().map(a -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", a.getId().toString());
            map.put("name", a.getName());
            map.put("phone", a.getPhone());
            map.put("province", a.getProvince());
            map.put("city", a.getCity());
            map.put("district", a.getDistrict());
            map.put("detail", a.getDetail());
            map.put("isDefault", a.getIsDefault());
            return map;
        }).collect(Collectors.toList());

        return Result.success(result);
    }

    @Transactional
    public Result<?> createAddress(Long userId, Map<String, Object> params) {
        Address address = new Address();
        address.setId(SnowflakeIdUtil.nextId());
        address.setUserId(userId);
        address.setName(params.get("name").toString());
        address.setPhone(params.get("phone").toString());
        address.setProvince(params.get("province").toString());
        address.setCity(params.get("city").toString());
        address.setDistrict(params.get("district").toString());
        address.setDetail(params.get("detail").toString());
        address.setIsDefault(params.get("isDefault") != null ? Integer.valueOf(params.get("isDefault").toString()) : 0);
        addressMapper.insert(address);

        // 如果是默认地址，取消其他的默认
        if (address.getIsDefault() == 1) {
            addressMapper.update(null, new LambdaUpdateWrapper<Address>()
                    .eq(Address::getUserId, userId)
                    .ne(Address::getId, address.getId())
                    .set(Address::getIsDefault, 0));
        }

        return Result.success(address.getId().toString());
    }

    @Transactional
    public Result<?> updateAddress(Long userId, Long addressId, Map<String, Object> params) {
        Address address = addressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            return Result.fail(ResultCode.ADDRESS_NOT_FOUND, "地址不存在");
        }

        if (params.get("name") != null) address.setName(params.get("name").toString());
        if (params.get("phone") != null) address.setPhone(params.get("phone").toString());
        if (params.get("province") != null) address.setProvince(params.get("province").toString());
        if (params.get("city") != null) address.setCity(params.get("city").toString());
        if (params.get("district") != null) address.setDistrict(params.get("district").toString());
        if (params.get("detail") != null) address.setDetail(params.get("detail").toString());

        addressMapper.updateById(address);

        // 处理默认地址
        if (params.get("isDefault") != null && Integer.valueOf(params.get("isDefault").toString()) == 1) {
            addressMapper.update(null, new LambdaUpdateWrapper<Address>()
                    .eq(Address::getUserId, userId)
                    .ne(Address::getId, addressId)
                    .set(Address::getIsDefault, 0));
        }

        return Result.success();
    }

    @Transactional
    public Result<?> deleteAddress(Long userId, Long addressId) {
        Address address = addressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            return Result.fail(ResultCode.ADDRESS_NOT_FOUND, "地址不存在");
        }
        addressMapper.deleteById(addressId);
        return Result.success();
    }

    @Transactional
    public Result<?> setDefaultAddress(Long userId, Long addressId) {
        Address address = addressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            return Result.fail(ResultCode.ADDRESS_NOT_FOUND, "地址不存在");
        }

        addressMapper.update(null, new LambdaUpdateWrapper<Address>()
                .eq(Address::getUserId, userId)
                .set(Address::getIsDefault, 0));

        address.setIsDefault(1);
        addressMapper.updateById(address);

        return Result.success();
    }

    // ============ 消息 ============
    public Result<?> getMessages(Long userId, PageRequest pageRequest) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getUserId, userId).eq(Message::getDeleted, 0)
               .orderByDesc(Message::getCreatedAt);

        List<Message> messages = messageMapper.selectList(wrapper);
        Long total = (long) messages.size();

        int offset = pageRequest.getOffset().intValue();
        messages = messages.stream().skip(offset).limit(pageRequest.getPageSize()).collect(Collectors.toList());

        List<Map<String, Object>> result = messages.stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", m.getId().toString());
            map.put("title", m.getTitle());
            map.put("content", m.getContent());
            map.put("type", m.getType());
            map.put("isRead", m.getIsRead());
            map.put("createdAt", m.getCreatedAt().toString());
            return map;
        }).collect(Collectors.toList());

        return Result.success(new PageResult<>(result, total, pageRequest.getPage(), pageRequest.getPageSize()));
    }

    @Transactional
    public Result<?> markMessageRead(Long userId, Long messageId) {
        Message message = messageMapper.selectById(messageId);
        if (message != null && message.getUserId().equals(userId)) {
            message.setIsRead(1);
            messageMapper.updateById(message);
        }
        return Result.success();
    }

    // ============ 私有方法 ============
    private OrderDTO toOrderDTO(Order o) {
        OrderDTO dto = new OrderDTO();
        dto.setId(o.getId().toString());
        dto.setOrderNo(o.getOrderNo());
        dto.setUserId(o.getUserId().toString());
        dto.setShopId(o.getShopId().toString());
        dto.setProductId(o.getProductId().toString());
        dto.setPoints(o.getPoints());
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

    private void refundPoints(Order order) {
        Points latest = pointsMapper.selectOne(new LambdaQueryWrapper<Points>()
                .eq(Points::getUserId, order.getUserId())
                .orderByDesc(Points::getCreatedAt).last("LIMIT 1"));
        int currentBalance = latest != null ? latest.getBalance() : 0;
        int refundAmount = order.getPoints() * order.getQuantity();

        Points points = new Points();
        points.setId(SnowflakeIdUtil.nextId());
        points.setUserId(order.getUserId());
        points.setAmount(refundAmount);
        points.setBalance(currentBalance + refundAmount);
        points.setType(PointsType.REFUND.getCode());
        points.setRemark("订单取消退款：" + order.getOrderNo());
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

    private String getPointsTypeDesc(Integer type) {
        if (type == null) return "";
        switch (type) {
            case 1: return "发放";
            case 2: return "扣除";
            case 3: return "兑换";
            case 4: return "退款";
            default: return "";
        }
    }
}