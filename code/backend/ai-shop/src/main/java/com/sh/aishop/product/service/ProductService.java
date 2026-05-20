package com.sh.aishop.product.service;

import com.sh.aishop.common.Result;
import com.sh.aishop.common.ResultCode;
import com.sh.aishop.common.entity.Category;
import com.sh.aishop.common.entity.FileRecord;
import com.sh.aishop.common.entity.Product;
import com.sh.aishop.common.enums.ProductStatus;
import com.sh.aishop.common.enums.ProductType;
import com.sh.aishop.common.dto.PageRequest;
import com.sh.aishop.common.dto.PageResult;
import com.sh.aishop.product.dto.ProductDTO;
import com.sh.aishop.product.dto.ProductRequest;
import com.sh.aishop.product.mapper.CategoryMapper;
import com.sh.aishop.file.mapper.FileRecordMapper;
import com.sh.aishop.product.mapper.ProductMapper;
import com.sh.aishop.shop.service.ShopService;
import com.sh.aishop.util.SnowflakeIdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ProductService {
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private FileRecordMapper fileRecordMapper;
    @Autowired
    private ShopService shopService;

    public Result<?> getCategories(Long operatorId) {
        var shopResult = shopService.getMyShop(operatorId);
        if (shopResult.getData() == null || !((Map<?, ?>) shopResult.getData()).containsKey("hasShop")) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }
        Map<?, ?> shopData = (Map<?, ?>) shopResult.getData();
        if (!Boolean.TRUE.equals(shopData.get("hasShop"))) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        List<Category> categories = categoryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Category>()
                        .eq(Category::getShopId, Long.parseLong(shopData.get("id").toString()))
                        .eq(Category::getDeleted, 0)
                        .orderByAsc(Category::getSort));

        List<Map<String, Object>> result = categories.stream().map(c -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", c.getId().toString());
            map.put("name", c.getName());
            map.put("sort", c.getSort());
            long count = productMapper.selectCount(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Product>()
                            .eq(Product::getCategoryId, c.getId())
                            .eq(Product::getDeleted, 0));
            map.put("productCount", count);
            return map;
        }).collect(Collectors.toList());

        return Result.success(result);
    }

    @Transactional
    public Result<?> createCategory(Long operatorId, String name, Integer sort) {
        var shopResult = shopService.getMyShop(operatorId);
        if (shopResult.getData() == null || !((Map<?, ?>) shopResult.getData()).containsKey("hasShop")) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }
        Map<?, ?> shopData = (Map<?, ?>) shopResult.getData();
        if (!Boolean.TRUE.equals(shopData.get("hasShop"))) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        Long shopId = Long.parseLong(shopData.get("id").toString());
        if (categoryMapper.existsByShopIdAndName(shopId, name)) {
            return Result.fail(ResultCode.CATEGORY_DUPLICATE, "分类名称已存在");
        }

        Category category = new Category();
        category.setId(SnowflakeIdUtil.nextId());
        category.setShopId(shopId);
        category.setName(name);
        category.setSort(sort != null ? sort : 0);
        categoryMapper.insert(category);

        return Result.success(category.getId().toString());
    }

    @Transactional
    public Result<?> updateCategory(Long categoryId, Long operatorId, String name, Integer sort) {
        var shopResult = shopService.getMyShop(operatorId);
        if (shopResult.getData() == null || !((Map<?, ?>) shopResult.getData()).containsKey("hasShop")) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }
        Map<?, ?> shopData = (Map<?, ?>) shopResult.getData();
        if (!Boolean.TRUE.equals(shopData.get("hasShop"))) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        Category category = categoryMapper.selectById(categoryId);
        if (category == null || !category.getShopId().toString().equals(shopData.get("id").toString())) {
            return Result.fail(ResultCode.CATEGORY_NOT_FOUND, "分类不存在");
        }

        if (StringUtils.hasText(name)) {
            if (categoryMapper.existsByShopIdAndNameExcluding(category.getShopId(), name, categoryId)) {
                return Result.fail(ResultCode.CATEGORY_DUPLICATE, "分类名称已存在");
            }
            category.setName(name);
        }
        if (sort != null) category.setSort(sort);
        categoryMapper.updateById(category);

        return Result.success();
    }

    @Transactional
    public Result<?> deleteCategory(Long categoryId, Long operatorId) {
        var shopResult = shopService.getMyShop(operatorId);
        if (shopResult.getData() == null || !((Map<?, ?>) shopResult.getData()).containsKey("hasShop")) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }
        Map<?, ?> shopData = (Map<?, ?>) shopResult.getData();
        if (!Boolean.TRUE.equals(shopData.get("hasShop"))) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        Category category = categoryMapper.selectById(categoryId);
        if (category == null || !category.getShopId().toString().equals(shopData.get("id").toString())) {
            return Result.fail(ResultCode.CATEGORY_NOT_FOUND, "分类不存在");
        }

        categoryMapper.deleteById(categoryId);
        return Result.success();
    }

    public Result<?> getProducts(Long operatorId, PageRequest pageRequest) {
        var shopResult = shopService.getMyShop(operatorId);
        if (shopResult.getData() == null || !((Map<?, ?>) shopResult.getData()).containsKey("hasShop")) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }
        Map<?, ?> shopData = (Map<?, ?>) shopResult.getData();
        if (!Boolean.TRUE.equals(shopData.get("hasShop"))) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Product> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(Product::getShopId, Long.parseLong(shopData.get("id").toString()))
               .eq(Product::getDeleted, 0);
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

    @Transactional
    public Result<?> createProduct(Long operatorId, ProductRequest req) {
        var shopResult = shopService.getMyShop(operatorId);
        if (shopResult.getData() == null || !((Map<?, ?>) shopResult.getData()).containsKey("hasShop")) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }
        Map<?, ?> shopData = (Map<?, ?>) shopResult.getData();
        if (!Boolean.TRUE.equals(shopData.get("hasShop"))) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        if (StringUtils.hasText(req.getName())) {
            Long shopId = Long.parseLong(shopData.get("id").toString());
            if (productMapper.existsByShopIdAndName(shopId, req.getName())) {
                return Result.fail(ResultCode.PRODUCT_DUPLICATE, "商品名称已存在");
            }
        }

        Product product = new Product();
        product.setId(SnowflakeIdUtil.nextId());
        product.setShopId(Long.parseLong(shopData.get("id").toString()));
        setProductFields(product, req);
        product.setStatus(ProductStatus.ON_SALE.getCode());
        productMapper.insert(product);

        return Result.success(product.getId().toString());
    }

    public Result<?> getProduct(Long operatorId, Long productId) {
        var shopResult = shopService.getMyShop(operatorId);
        if (shopResult.getData() == null || !((Map<?, ?>) shopResult.getData()).containsKey("hasShop")) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }
        Map<?, ?> shopData = (Map<?, ?>) shopResult.getData();
        if (!Boolean.TRUE.equals(shopData.get("hasShop"))) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        Product product = productMapper.selectById(productId);
        if (product == null || !product.getShopId().toString().equals(shopData.get("id").toString())) {
            return Result.fail(ResultCode.PRODUCT_NOT_FOUND, "商品不存在");
        }

        return Result.success(toProductDTO(product));
    }

    @Transactional
    public Result<?> updateProduct(Long operatorId, Long productId, ProductRequest req) {
        var shopResult = shopService.getMyShop(operatorId);
        if (shopResult.getData() == null || !((Map<?, ?>) shopResult.getData()).containsKey("hasShop")) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }
        Map<?, ?> shopData = (Map<?, ?>) shopResult.getData();
        if (!Boolean.TRUE.equals(shopData.get("hasShop"))) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        Product product = productMapper.selectById(productId);
        if (product == null || !product.getShopId().toString().equals(shopData.get("id").toString())) {
            return Result.fail(ResultCode.PRODUCT_NOT_FOUND, "商品不存在");
        }

        if (StringUtils.hasText(req.getName())) {
            if (productMapper.existsByShopIdAndNameExcluding(product.getShopId(), req.getName(), productId)) {
                return Result.fail(ResultCode.PRODUCT_DUPLICATE, "商品名称已存在");
            }
        }

        setProductFields(product, req);
        productMapper.updateById(product);
        return Result.success();
    }

    @Transactional
    public Result<?> deleteProduct(Long operatorId, Long productId) {
        var shopResult = shopService.getMyShop(operatorId);
        if (shopResult.getData() == null || !((Map<?, ?>) shopResult.getData()).containsKey("hasShop")) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }
        Map<?, ?> shopData = (Map<?, ?>) shopResult.getData();
        if (!Boolean.TRUE.equals(shopData.get("hasShop"))) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        Product product = productMapper.selectById(productId);
        if (product == null || !product.getShopId().toString().equals(shopData.get("id").toString())) {
            return Result.fail(ResultCode.PRODUCT_NOT_FOUND, "商品不存在");
        }

        productMapper.deleteById(productId);
        return Result.success();
    }

    private void setProductFields(Product product, ProductRequest req) {
        if (req.getCategoryId() != null && !req.getCategoryId().isEmpty()) {
            product.setCategoryId(Long.valueOf(req.getCategoryId()));
        }
        if (req.getName() != null) product.setName(req.getName());
        if (req.getType() != null) product.setType(req.getType());
        if (req.getPrice() != null) product.setPrice(req.getPrice());
        if (req.getStock() != null) product.setStock(req.getStock());
        if (req.getLimitPerUser() != null) product.setLimitPerUser(req.getLimitPerUser());
        if (req.getMainImage() != null && !req.getMainImage().isEmpty()) product.setMainImage(req.getMainImage());
        if (req.getDetailImages() != null && !req.getDetailImages().isEmpty()) product.setDetailImages(req.getDetailImages());
        if (req.getDescription() != null) product.setDescription(req.getDescription());
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
}