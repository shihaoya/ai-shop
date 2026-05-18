package com.sh.aishop.service;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sh.aishop.common.Result;
import com.sh.aishop.common.ResultCode;
import com.sh.aishop.dto.*;
import com.sh.aishop.common.entity.*;
import com.sh.aishop.common.enums.*;
import com.sh.aishop.mapper.*;
import com.sh.aishop.util.SecurityUtil;
import com.sh.aishop.util.SnowflakeIdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OperatorService {
    @Autowired
    private ShopMapper shopMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PointsMapper pointsMapper;
    @Autowired
    private InviteCodeMapper inviteCodeMapper;
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private AddressMapper addressMapper;
    @Autowired
    private FileRecordMapper fileRecordMapper;

    // ============ 店铺管理 ============
    public Result<?> getMyShop(Long operatorId) {
        Shop shop = shopMapper.selectOne(new LambdaQueryWrapper<Shop>()
                .eq(Shop::getOperatorId, operatorId)
                .eq(Shop::getDeleted, 0));
        if (shop == null) {
            return Result.success(Collections.singletonMap("hasShop", false));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("hasShop", true);
        result.put("id", shop.getId().toString());
        result.put("name", shop.getName());
        result.put("description", shop.getDescription());
        result.put("status", shop.getStatus());
        result.put("isActive", shop.getIsActive());
        result.put("rejectReason", shop.getRejectReason());
        return Result.success(result);
    }

    @Transactional
    public Result<?> applyShop(Long operatorId, String name, String description) {
        // 检查是否已有店铺
        Shop existShop = shopMapper.selectOne(new LambdaQueryWrapper<Shop>()
                .eq(Shop::getOperatorId, operatorId)
                .eq(Shop::getDeleted, 0));
        if (existShop != null) {
            // 已通过审核的店铺不能重复申请
            if (existShop.getStatus() == ShopStatus.APPROVED.getCode()) {
                return Result.fail(ResultCode.FAIL, "已有通过审核的店铺，无需重复申请");
            }
            // 重新提交：更新信息并设为待审核，清空拒绝原因
            existShop.setName(name);
            existShop.setDescription(description);
            existShop.setStatus(ShopStatus.PENDING.getCode());
            existShop.setRejectReason(null);
            shopMapper.updateById(existShop);
            return Result.success(existShop.getId().toString());
        }

        Shop shop = new Shop();
        shop.setId(SnowflakeIdUtil.nextId());
        shop.setOperatorId(operatorId);
        shop.setName(name);
        shop.setDescription(description);
        shop.setStatus(ShopStatus.PENDING.getCode());
        shop.setIsActive(0); // 默认为歇业
        shopMapper.insert(shop);

        return Result.success(shop.getId().toString());
    }

    @Transactional
    public Result<?> changeShopStatus(Long operatorId, Integer isActive) {
        Shop shop = getApprovedShop(operatorId);
        if (shop == null) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        shop.setIsActive(isActive);
        shopMapper.updateById(shop);
        return Result.success();
    }

    // ============ 分类管理 ============
    public Result<?> getCategories(Long operatorId) {
        Shop shop = getApprovedShop(operatorId);
        if (shop == null) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        List<Category> categories = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getShopId, shop.getId())
                .eq(Category::getDeleted, 0)
                .orderByAsc(Category::getSort));

        List<Map<String, Object>> result = categories.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId().toString());
            map.put("name", c.getName());
            map.put("sort", c.getSort());
            long count = productMapper.selectCount(new LambdaQueryWrapper<Product>()
                    .eq(Product::getCategoryId, c.getId())
                    .eq(Product::getDeleted, 0));
            map.put("productCount", count);
            return map;
        }).collect(Collectors.toList());

        return Result.success(result);
    }

    @Transactional
    public Result<?> createCategory(Long operatorId, String name, Integer sort) {
        Shop shop = getApprovedShop(operatorId);
        if (shop == null) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        Category category = new Category();
        category.setId(SnowflakeIdUtil.nextId());
        category.setShopId(shop.getId());
        category.setName(name);
        category.setSort(sort != null ? sort : 0);
        categoryMapper.insert(category);

        return Result.success(category.getId().toString());
    }

    @Transactional
    public Result<?> updateCategory(Long categoryId, Long operatorId, String name, Integer sort) {
        Shop shop = getApprovedShop(operatorId);
        if (shop == null) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        Category category = categoryMapper.selectById(categoryId);
        if (category == null || !category.getShopId().equals(shop.getId())) {
            return Result.fail(ResultCode.CATEGORY_NOT_FOUND, "分类不存在");
        }

        if (StringUtils.hasText(name)) category.setName(name);
        if (sort != null) category.setSort(sort);
        categoryMapper.updateById(category);

        return Result.success();
    }

    @Transactional
    public Result<?> deleteCategory(Long categoryId, Long operatorId) {
        Shop shop = getApprovedShop(operatorId);
        if (shop == null) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        Category category = categoryMapper.selectById(categoryId);
        if (category == null || !category.getShopId().equals(shop.getId())) {
            return Result.fail(ResultCode.CATEGORY_NOT_FOUND, "分类不存在");
        }

        categoryMapper.deleteById(categoryId);
        return Result.success();
    }

    // ============ 商品管理 ============
    public Result<?> getProducts(Long operatorId, PageRequest pageRequest) {
        Shop shop = getApprovedShop(operatorId);
        if (shop == null) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getShopId, shop.getId()).eq(Product::getDeleted, 0);
        if (StringUtils.hasText(pageRequest.getKeyword())) {
            wrapper.like(Product::getName, pageRequest.getKeyword());
        }
        wrapper.orderByDesc(Product::getCreatedAt);

        List<Product> products = productMapper.selectList(wrapper);
        Long total = (long) products.size();

        int offset = pageRequest.getOffset().intValue();
        products = products.stream().skip(offset).limit(pageRequest.getPageSize()).collect(Collectors.toList());

        List<ProductDTO> dtos = products.stream().map(this::toProductDTO).collect(Collectors.toList());

        // 批量加载 mainImage 的访问URL
        List<Long> fileIds = dtos.stream()
            .map(ProductDTO::getMainImage)
            .filter(Objects::nonNull)
            .map(Long::parseLong)
            .collect(Collectors.toList());
        if (!fileIds.isEmpty()) {
            Map<Long, String> urlMap = fileRecordMapper.selectByIds(fileIds)
                .stream()
                .collect(Collectors.toMap(FileRecord::getId, FileRecord::getUrl));
            dtos.forEach(dto -> {
                if (dto.getMainImage() != null) {
                    dto.setMainImageUrl(urlMap.get(Long.parseLong(dto.getMainImage())));
                }
            });
        }

        return Result.success(new PageResult<>(dtos, total, pageRequest.getPage(), pageRequest.getPageSize()));
    }

    @Transactional
    public Result<?> createProduct(Long operatorId, Map<String, Object> params) {
        Shop shop = getApprovedShop(operatorId);
        if (shop == null) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        Product product = new Product();
        product.setId(SnowflakeIdUtil.nextId());
        product.setShopId(shop.getId());
        setProductFields(product, params);
        product.setStatus(ProductStatus.ON_SALE.getCode()); // 默认上架
        productMapper.insert(product);

        return Result.success(product.getId().toString());
    }

    public Result<?> getProduct(Long operatorId, Long productId) {
        Shop shop = getApprovedShop(operatorId);
        if (shop == null) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        Product product = productMapper.selectById(productId);
        if (product == null || !product.getShopId().equals(shop.getId())) {
            return Result.fail(ResultCode.PRODUCT_NOT_FOUND, "商品不存在");
        }

        return Result.success(toProductDTO(product));
    }

    @Transactional
    public Result<?> updateProduct(Long operatorId, Long productId, Map<String, Object> params) {
        Shop shop = getApprovedShop(operatorId);
        if (shop == null) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        Product product = productMapper.selectById(productId);
        if (product == null || !product.getShopId().equals(shop.getId())) {
            return Result.fail(ResultCode.PRODUCT_NOT_FOUND, "商品不存在");
        }

        setProductFields(product, params);
        productMapper.updateById(product);
        return Result.success();
    }

    @Transactional
    public Result<?> deleteProduct(Long operatorId, Long productId) {
        Shop shop = getApprovedShop(operatorId);
        if (shop == null) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        Product product = productMapper.selectById(productId);
        if (product == null || !product.getShopId().equals(shop.getId())) {
            return Result.fail(ResultCode.PRODUCT_NOT_FOUND, "商品不存在");
        }

        productMapper.deleteById(productId);
        return Result.success();
    }

    private void setProductFields(Product product, Map<String, Object> params) {
        if (params.get("categoryId") != null) product.setCategoryId(Long.valueOf(params.get("categoryId").toString()));
        if (params.get("name") != null) product.setName(params.get("name").toString());
        if (params.get("type") != null) product.setType(Integer.valueOf(params.get("type").toString()));
        if (params.get("price") != null) product.setPrice(Integer.valueOf(params.get("price").toString()));
        if (params.get("stock") != null) product.setStock(Integer.valueOf(params.get("stock").toString()));
        if (params.get("limitPerUser") != null) product.setLimitPerUser(Integer.valueOf(params.get("limitPerUser").toString()));
        if (params.get("mainImage") != null) product.setMainImage(params.get("mainImage").toString());
        if (params.get("detailImages") != null) product.setDetailImages(params.get("detailImages").toString());
        if (params.get("description") != null) product.setDescription(params.get("description").toString());
        if (params.get("deliveryInfo") != null) product.setDeliveryInfo(params.get("deliveryInfo").toString());
        if (params.get("status") != null) product.setStatus(Integer.valueOf(params.get("status").toString()));
    }

    private ProductDTO toProductDTO(Product p) {
        ProductDTO dto = new ProductDTO();
        dto.setId(p.getId().toString());
        dto.setShopId(p.getShopId().toString());
        dto.setCategoryId(p.getCategoryId() != null ? p.getCategoryId().toString() : null);
        if (p.getCategoryId() != null) {
            Category category = categoryMapper.selectById(p.getCategoryId());
            dto.setCategoryName(category != null ? category.getName() : null);
        }
        dto.setName(p.getName());
        dto.setType(p.getType());
        dto.setTypeDesc(p.getType() == ProductType.VIRTUAL.getCode() ? "虚拟" : "实体");
        dto.setPrice(p.getPrice());
        dto.setStock(p.getStock());
        dto.setLimitPerUser(p.getLimitPerUser());
        dto.setMainImage(p.getMainImage());
        if (p.getMainImage() != null) {
            FileRecord file = fileRecordMapper.selectById(Long.parseLong(p.getMainImage()));
            if (file != null) dto.setMainImageUrl(file.getUrl());
        }
        dto.setDetailImages(p.getDetailImages());
        dto.setDescription(p.getDescription());
        dto.setDeliveryInfo(p.getDeliveryInfo());
        dto.setStatus(p.getStatus());
        dto.setStatusDesc(p.getStatus() == ProductStatus.ON_SALE.getCode() ? "上架" : "下架");
        return dto;
    }

    // ============ 订单管理 ============
    public Result<?> getOrders(Long operatorId, PageRequest pageRequest, Integer status) {
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

        List<OrderDTO> dtos = orders.stream().map(order -> {
            OrderDTO dto = toOrderDTO(order);
            User user = userMapper.selectById(order.getUserId());
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

        // 发送消息
        sendOrderMessage(order.getUserId(), orderId, "您的订单已发货");

        return Result.success();
    }

    @Transactional
    public Result<?> closeOrder(Long operatorId, Long orderId, String reason) {
        Order order = getOrderByOperatorId(operatorId, orderId);
        if (order == null) {
            return Result.fail(ResultCode.ORDER_NOT_FOUND, "订单不存在");
        }
        if (order.getStatus() == OrderStatus.COMPLETED.getCode() || order.getStatus() == OrderStatus.CLOSED.getCode()) {
            return Result.fail(ResultCode.ORDER_STATUS_ERROR, "订单已结束");
        }

        // 退回积分
        refundPoints(order);

        order.setStatus(OrderStatus.CLOSED.getCode());
        order.setClosedAt(LocalDateTime.now());
        order.setCloseReason(reason);
        orderMapper.updateById(order);

        sendOrderMessage(order.getUserId(), orderId, "您的订单已关闭：" + reason);

        return Result.success();
    }

    @Transactional
    public Result<?> completeOrder(Long operatorId, Long orderId) {
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

    // ============ 用户管理 ============
    public Result<?> getUsers(Long operatorId, PageRequest pageRequest) {
        Shop shop = getApprovedShop(operatorId);
        if (shop == null) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        // 查找属于该店铺的普通用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getParentId, operatorId)
               .eq(User::getRole, RoleEnum.NORMAL_USER.getCode())
               .eq(User::getDeleted, 0);
        if (StringUtils.hasText(pageRequest.getKeyword())) {
            wrapper.and(w -> w.like(User::getUsername, pageRequest.getKeyword())
                    .or().like(User::getNickname, pageRequest.getKeyword()));
        }
        wrapper.orderByDesc(User::getCreatedAt);

        List<User> users = userMapper.selectList(wrapper);
        Long total = (long) users.size();

        int offset = pageRequest.getOffset().intValue();
        users = users.stream().skip(offset).limit(pageRequest.getPageSize()).collect(Collectors.toList());

        List<UserDTO> dtos = users.stream().map(u -> {
            UserDTO dto = new UserDTO();
            dto.setId(u.getId().toString());
            dto.setUsername(u.getUsername());
            dto.setNickname(u.getNickname());
            dto.setRole(u.getRole());
            dto.setStatus(u.getStatus());
            dto.setCreatedAt(u.getCreatedAt() != null ? u.getCreatedAt().toString() : null);
            // 查询积分
            Points latest = pointsMapper.selectOne(new LambdaQueryWrapper<Points>()
                    .eq(Points::getUserId, u.getId())
                    .orderByDesc(Points::getCreatedAt)
                    .last("LIMIT 1"));
            dto.setPointsBalance(latest != null ? latest.getBalance().toString() : "0");
            return dto;
        }).collect(Collectors.toList());

        return Result.success(new PageResult<>(dtos, total, pageRequest.getPage(), pageRequest.getPageSize()));
    }

    @Transactional
    public Result<?> adjustPoints(Long operatorId, Long userId, Integer amount, String remark) {
        Shop shop = getApprovedShop(operatorId);
        if (shop == null) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        User user = userMapper.selectById(userId);
        if (user == null || !user.getParentId().equals(operatorId)) {
            return Result.fail(ResultCode.USER_NOT_FOUND, "用户不存在");
        }

        // 计算新余额
        Points latest = pointsMapper.selectOne(new LambdaQueryWrapper<Points>()
                .eq(Points::getUserId, userId)
                .orderByDesc(Points::getCreatedAt)
                .last("LIMIT 1"));
        int currentBalance = latest != null ? latest.getBalance() : 0;
        int newBalance = currentBalance + amount;
        if (newBalance < 0) {
            return Result.fail(ResultCode.FAIL, "积分不足");
        }

        // 记录积分变动
        Points points = new Points();
        points.setId(SnowflakeIdUtil.nextId());
        points.setUserId(userId);
        points.setAmount(amount);
        points.setBalance(newBalance);
        points.setType(amount > 0 ? PointsType.GRANT.getCode() : PointsType.DEDUCT.getCode());
        points.setRemark(remark);
        points.setOperatorId(operatorId);
        pointsMapper.insert(points);

        // 发送消息
        String title = amount > 0 ? "积分发放" : "积分扣除";
        String content = amount > 0 ? "您收到了" + amount + "积分" : "您被扣除了" + Math.abs(amount) + "积分";
        if (remark != null) content += "，" + remark;
        sendPointsMessage(userId, points.getId(), title, content);

        return Result.success(newBalance);
    }

    public Result<?> getPointsLog(Long operatorId, Long userId, PageRequest pageRequest) {
        Shop shop = getApprovedShop(operatorId);
        if (shop == null) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

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

    @Transactional
    public Result<?> approveUser(Long operatorId, Long userId) {
        Shop shop = getApprovedShop(operatorId);
        if (shop == null) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        User user = userMapper.selectById(userId);
        if (user == null || !user.getParentId().equals(operatorId)) {
            return Result.fail(ResultCode.USER_NOT_FOUND, "用户不存在");
        }
        if (user.getStatus() != UserStatus.PENDING.getCode()) {
            return Result.fail(ResultCode.FAIL, "用户不是待审核状态");
        }

        user.setStatus(UserStatus.NORMAL.getCode());
        userMapper.updateById(user);

        sendOrderMessage(userId, null, "您的账号已审核通过");

        return Result.success();
    }

    @Transactional
    public Result<?> rejectUser(Long operatorId, Long userId) {
        Shop shop = getApprovedShop(operatorId);
        if (shop == null) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        User user = userMapper.selectById(userId);
        if (user == null || !user.getParentId().equals(operatorId)) {
            return Result.fail(ResultCode.USER_NOT_FOUND, "用户不存在");
        }
        if (user.getStatus() != UserStatus.PENDING.getCode()) {
            return Result.fail(ResultCode.FAIL, "用户不是待审核状态");
        }

        // 软删除用户
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, userId).set(User::getDeleted, 1);
        userMapper.update(null, updateWrapper);

        // 作废该用户的邀请码
        InviteCode code = inviteCodeMapper.selectOne(new LambdaQueryWrapper<InviteCode>()
                .eq(InviteCode::getCreatorId, userId)
                .eq(InviteCode::getStatus, InviteCodeStatus.ACTIVE.getCode())
                .eq(InviteCode::getDeleted, 0));
        if (code != null) {
            code.setStatus(InviteCodeStatus.INVALID.getCode());
            inviteCodeMapper.updateById(code);
        }

        return Result.success();
    }

    // ============ 邀请码 ============
    public Result<?> getInviteCode(Long operatorId) {
        InviteCode code = inviteCodeMapper.selectOne(new LambdaQueryWrapper<InviteCode>()
                .eq(InviteCode::getCreatorId, operatorId)
                .eq(InviteCode::getRole, RoleEnum.NORMAL_USER.getCode())
                .eq(InviteCode::getStatus, InviteCodeStatus.ACTIVE.getCode())
                .eq(InviteCode::getDeleted, 0));

        return Result.success(code != null ? code.getCode() : null);
    }

    @Transactional
    public Result<?> createInviteCode(Long operatorId) {
        InviteCode old = inviteCodeMapper.selectOne(new LambdaQueryWrapper<InviteCode>()
                .eq(InviteCode::getCreatorId, operatorId)
                .eq(InviteCode::getRole, RoleEnum.NORMAL_USER.getCode())
                .eq(InviteCode::getStatus, InviteCodeStatus.ACTIVE.getCode())
                .eq(InviteCode::getDeleted, 0));

        if (old != null) {
            old.setStatus(InviteCodeStatus.INVALID.getCode());
            inviteCodeMapper.updateById(old);
        }

        InviteCode newCode = new InviteCode();
        newCode.setId(SnowflakeIdUtil.nextId());
        newCode.setCode(generateCode());
        newCode.setRole(RoleEnum.NORMAL_USER.getCode());
        newCode.setCreatorId(operatorId);
        newCode.setStatus(InviteCodeStatus.ACTIVE.getCode());
        inviteCodeMapper.insert(newCode);

        return Result.success(newCode.getCode());
    }

    // ============ 创建/导入用户 ============
    @Transactional
    public Result<?> createUser(Long operatorId, String username, String nickname, String password) {
        Shop shop = getApprovedShop(operatorId);
        if (shop == null) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        User exist = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username).eq(User::getDeleted, 0));
        if (exist != null) {
            return Result.fail(ResultCode.USERNAME_EXISTS, "用户名已存在");
        }

        User user = new User();
        user.setId(SnowflakeIdUtil.nextId());
        user.setUsername(username);
        user.setNickname(nickname != null ? nickname : username);
        user.setPassword(com.sh.aishop.util.SecurityUtil.encryptPassword(password));
        user.setRole(RoleEnum.NORMAL_USER.getCode());
        user.setParentId(operatorId);
        user.setStatus(UserStatus.NORMAL.getCode()); // 直接正常
        userMapper.insert(user);

        return Result.success(user.getId().toString());
    }

    @Transactional
    public Result<?> resetUserPassword(Long operatorId, Long userId) {
        Shop shop = getApprovedShop(operatorId);
        if (shop == null) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail(ResultCode.USER_NOT_FOUND, "用户不存在");
        }

        // 确保用户属于当前运营人员的店铺
        if (user.getParentId() == null || !user.getParentId().equals(operatorId)) {
            return Result.fail(ResultCode.FAIL, "无权重置该用户密码");
        }

        String newPassword = generateRandomPassword();
        user.setPassword(com.sh.aishop.util.SecurityUtil.encryptPassword(newPassword));
        userMapper.updateById(user);

        return Result.success(Collections.singletonMap("password", newPassword));
    }

    // ============ Excel 导入用户 ============
    public Result<?> importUsers(Long operatorId, MultipartFile file) {
        Shop shop = getApprovedShop(operatorId);
        if (shop == null) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        // 1. 解析 Excel
        List<UserImportDTO> rows;
        try {
            rows = EasyExcel.read(file.getInputStream())
                    .head(UserImportDTO.class)
                    .sheet()
                    .doReadSync();
        } catch (IOException e) {
            return Result.fail("文件读取失败，请检查文件格式");
        } catch (Exception e) {
            return Result.fail("文件解析失败，请确认上传的是正确的 Excel 文件");
        }

        // 2. 检查是否有数据
        if (rows == null || rows.isEmpty()) {
            return Result.fail("文件中没有数据，请填写后再上传");
        }

        // 3. 逐行校验
        List<Map<String, Object>> errors = new ArrayList<>();
        Set<String> importUsernames = new HashSet<>();     // 文件内检查重复
        Set<String> existingUsernames = new HashSet<>();

        // 查询所有已存在的用户名
        List<User> allUsers = userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getDeleted, 0)
                .select(User::getUsername));
        allUsers.forEach(u -> existingUsernames.add(u.getUsername()));

        for (int i = 0; i < rows.size(); i++) {
            UserImportDTO row = rows.get(i);
            int rowNum = i + 2; // Excel 行号从1开始，表头占1行，数据从第2行开始
            List<String> rowErrors = new ArrayList<>();

            String username = row.getUsername() != null ? row.getUsername().trim() : "";
            String nickname = row.getNickname() != null ? row.getNickname().trim() : "";

            // 校验用户名
            if (username.isEmpty()) {
                rowErrors.add("用户名为空");
            } else if (username.length() > 50) {
                rowErrors.add("用户名不能超过50个字符");
            } else if (importUsernames.contains(username)) {
                rowErrors.add("文件中存在重复的用户名: " + username);
            } else if (existingUsernames.contains(username)) {
                rowErrors.add("用户名已存在: " + username);
            }

            // 校验昵称（可选，为空时自动使用用户名）
            if (!nickname.isEmpty() && nickname.length() > 50) {
                rowErrors.add("昵称超过50个字符");
            }

            if (!rowErrors.isEmpty()) {
                Map<String, Object> err = new HashMap<>();
                err.put("row", rowNum);
                err.put("message", String.join("; ", rowErrors));
                errors.add(err);
            }

            if (!username.isEmpty()) {
                importUsernames.add(username);
            }
        }

        // 4. 有错误 → 返回错误列表
        if (!errors.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("hasErrors", true);
            result.put("errors", errors);
            return Result.success(result);
        }

        // 5. 全部校验通过 → 事务批量导入
        return doImportUsers(operatorId, rows);
    }

    @Transactional
    public Result<?> doImportUsers(Long operatorId, List<UserImportDTO> rows) {
        List<Map<String, String>> importedUsers = new ArrayList<>();

        for (UserImportDTO row : rows) {
            String username = row.getUsername().trim();
            String nickname = row.getNickname() != null && !row.getNickname().trim().isEmpty()
                    ? row.getNickname().trim() : username;

            String password = generateRandomPassword();

            User user = new User();
            user.setId(SnowflakeIdUtil.nextId());
            user.setUsername(username);
            user.setNickname(nickname);
            user.setPassword(SecurityUtil.encryptPassword(password));
            user.setRole(RoleEnum.NORMAL_USER.getCode());
            user.setParentId(operatorId);
            user.setStatus(UserStatus.NORMAL.getCode());
            userMapper.insert(user);

            Map<String, String> userMap = new HashMap<>();
            userMap.put("username", username);
            userMap.put("nickname", nickname);
            userMap.put("password", password);
            importedUsers.add(userMap);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("hasErrors", false);
        result.put("success", true);
        result.put("users", importedUsers);
        return Result.success(result);
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    // ============ 消息 ============
    public Result<?> getMessages(Long operatorId, PageRequest pageRequest) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getUserId, operatorId).eq(Message::getDeleted, 0)
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
    public Result<?> markMessageRead(Long operatorId, Long messageId) {
        Message message = messageMapper.selectById(messageId);
        if (message != null && message.getUserId().equals(operatorId)) {
            message.setIsRead(1);
            messageMapper.updateById(message);
        }
        return Result.success();
    }

    // ============ 私有方法 ============
    private Shop getShopByOperatorId(Long operatorId) {
        return shopMapper.selectOne(new LambdaQueryWrapper<Shop>()
                .eq(Shop::getOperatorId, operatorId).eq(Shop::getDeleted, 0));
    }

    // 仅返回已审核通过的店铺（status === 1）
    private Shop getApprovedShop(Long operatorId) {
        return shopMapper.selectOne(new LambdaQueryWrapper<Shop>()
                .eq(Shop::getOperatorId, operatorId)
                .eq(Shop::getDeleted, 0)
                .eq(Shop::getStatus, ShopStatus.APPROVED.getCode()));
    }

    private Order getOrderByOperatorId(Long operatorId, Long orderId) {
        Shop shop = getShopByOperatorId(operatorId);
        if (shop == null) return null;
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getShopId().equals(shop.getId())) return null;
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
        points.setOperatorId(order.getShopId());
        pointsMapper.insert(points);
    }

    private OrderDTO toOrderDTO(Order o) {
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

    private void sendPointsMessage(Long userId, Long pointsId, String title, String content) {
        Message msg = new Message();
        msg.setId(SnowflakeIdUtil.nextId());
        msg.setUserId(userId);
        msg.setTitle(title);
        msg.setContent(content);
        msg.setType(MessageType.POINTS.getCode());
        msg.setRelatedId(pointsId);
        msg.setIsRead(0);
        messageMapper.insert(msg);
    }

    private String generateCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
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