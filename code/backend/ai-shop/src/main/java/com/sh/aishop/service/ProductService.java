package com.sh.aishop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sh.aishop.common.Result;
import com.sh.aishop.common.ResultCode;
import com.sh.aishop.dto.PageRequest;
import com.sh.aishop.dto.PageResult;
import com.sh.aishop.dto.ProductDTO;
import com.sh.aishop.entity.*;
import com.sh.aishop.entity.enums.ProductStatus;
import com.sh.aishop.entity.enums.ProductType;
import com.sh.aishop.entity.enums.ShopStatus;
import com.sh.aishop.mapper.*;
import com.sh.aishop.util.SnowflakeIdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ShopMapper shopMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private FileRecordMapper fileRecordMapper;

    // ============ 公开接口 ============

    /**
     * 商品列表（公开）- 只显示营业中店铺的上架商品
     */
    public Result<?> getProducts(PageRequest pageRequest) {
        // 1. 获取所有营业中且审核通过的店铺
        LambdaQueryWrapper<Shop> shopWrapper = new LambdaQueryWrapper<>();
        shopWrapper.eq(Shop::getIsActive, 1)
                   .eq(Shop::getStatus, ShopStatus.APPROVED.getCode())
                   .eq(Shop::getDeleted, 0);
        List<Shop> activeShops = shopMapper.selectList(shopWrapper);
        if (activeShops.isEmpty()) {
            return Result.success(new PageResult<>(Collections.emptyList(), 0L, pageRequest.getPage(), pageRequest.getPageSize()));
        }
        List<Long> shopIds = activeShops.stream().map(Shop::getId).collect(Collectors.toList());

        // 2. 查询这些店铺的上架商品
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

        // 3. 批量加载分类名
        List<Long> catIds = products.stream()
            .map(Product::getCategoryId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, String> catNameMap = new HashMap<>();
        if (!catIds.isEmpty()) {
            categoryMapper.selectBatchIds(catIds).forEach(c -> catNameMap.put(c.getId(), c.getName()));
        }

        // 4. 转换为DTO
        List<ProductDTO> dtos = products.stream().map(p -> {
            ProductDTO dto = toProductDTO(p);
            Shop shop = activeShops.stream().filter(s -> s.getId().equals(p.getShopId())).findFirst().orElse(null);
            if (shop != null) dto.setShopName(shop.getName());
            return dto;
        }).collect(Collectors.toList());

        // 5. 批量加载主图URL
        loadMainImageUrls(dtos);

        return Result.success(new PageResult<>(dtos, total, pageRequest.getPage(), pageRequest.getPageSize()));
    }

    /**
     * 商品详情（公开）
     */
    public Result<?> getProduct(String id) {
        Product product = productMapper.selectById(Long.valueOf(id));
        if (product == null || product.getDeleted() != 0) {
            return Result.fail(ResultCode.PRODUCT_NOT_FOUND, "商品不存在");
        }
        // 公开接口：只能看营业中店铺的上架商品
        Shop shop = shopMapper.selectById(product.getShopId());
        if (shop == null || shop.getIsActive() != 1 || shop.getStatus() != ShopStatus.APPROVED.getCode() || shop.getDeleted() != 0) {
            return Result.fail(ResultCode.PRODUCT_NOT_FOUND, "商品不存在");
        }
        if (product.getStatus() != ProductStatus.ON_SALE.getCode()) {
            return Result.fail(ResultCode.PRODUCT_NOT_FOUND, "商品不存在");
        }

        ProductDTO dto = toProductDTO(product);
        dto.setShopName(shop.getName());
        if (product.getCategoryId() != null) {
            Category category = categoryMapper.selectById(product.getCategoryId());
            if (category != null) dto.setCategoryName(category.getName());
        }
        loadMainImageUrls(Collections.singletonList(dto));

        return Result.success(dto);
    }

    // ============ 商家接口 ============

    /**
     * 获取商家的商品列表
     */
    public Result<?> getShopProducts(Long operatorId, PageRequest pageRequest) {
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
        loadMainImageUrls(dtos);

        return Result.success(new PageResult<>(dtos, total, pageRequest.getPage(), pageRequest.getPageSize()));
    }

    /**
     * 创建商品
     */
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

    /**
     * 修改商品
     */
    @Transactional
    public Result<?> updateProduct(String id, Long operatorId, Map<String, Object> params) {
        Shop shop = getApprovedShop(operatorId);
        if (shop == null) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        Product product = productMapper.selectById(Long.valueOf(id));
        if (product == null || !product.getShopId().equals(shop.getId()) || product.getDeleted() != 0) {
            return Result.fail(ResultCode.PRODUCT_NOT_FOUND, "商品不存在");
        }

        setProductFields(product, params);
        productMapper.updateById(product);
        return Result.success();
    }

    /**
     * 修改商品上下架状态
     */
    @Transactional
    public Result<?> changeProductStatus(String id, Long operatorId, Integer status) {
        Shop shop = getApprovedShop(operatorId);
        if (shop == null) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        Product product = productMapper.selectById(Long.valueOf(id));
        if (product == null || !product.getShopId().equals(shop.getId()) || product.getDeleted() != 0) {
            return Result.fail(ResultCode.PRODUCT_NOT_FOUND, "商品不存在");
        }

        product.setStatus(status);
        productMapper.updateById(product);
        return Result.success();
    }

    /**
     * 删除商品（软删除）
     */
    @Transactional
    public Result<?> deleteProduct(String id, Long operatorId) {
        Shop shop = getApprovedShop(operatorId);
        if (shop == null) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        Product product = productMapper.selectById(Long.valueOf(id));
        if (product == null || !product.getShopId().equals(shop.getId()) || product.getDeleted() != 0) {
            return Result.fail(ResultCode.PRODUCT_NOT_FOUND, "商品不存在");
        }

        productMapper.deleteById(Long.valueOf(id));
        return Result.success();
    }

    /**
     * 上传商品图片
     */
    public Result<?> uploadProductImages(String id, Long operatorId, MultipartFile mainImage, MultipartFile[] detailImages) {
        Shop shop = getApprovedShop(operatorId);
        if (shop == null) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        Product product = productMapper.selectById(Long.valueOf(id));
        if (product == null || !product.getShopId().equals(shop.getId()) || product.getDeleted() != 0) {
            return Result.fail(ResultCode.PRODUCT_NOT_FOUND, "商品不存在");
        }

        // TODO: 实际文件上传逻辑由 FileService 处理
        // 这里只更新商品的图片引用字段
        // 占位：实际实现需要调用 FileService 上传并获取 fileId

        return Result.success();
    }

    // ============ 分类管理 ============

    /**
     * 获取分类列表
     */
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

    /**
     * 创建分类
     */
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

    /**
     * 更新分类
     */
    @Transactional
    public Result<?> updateCategory(String categoryId, Long operatorId, String name, Integer sort) {
        Shop shop = getApprovedShop(operatorId);
        if (shop == null) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        Category category = categoryMapper.selectById(Long.parseLong(categoryId));
        if (category == null || !category.getShopId().equals(shop.getId())) {
            return Result.fail(ResultCode.CATEGORY_NOT_FOUND, "分类不存在");
        }

        if (StringUtils.hasText(name)) {
            category.setName(name);
        }
        if (sort != null) {
            category.setSort(sort);
        }
        categoryMapper.updateById(category);
        return Result.success();
    }

    /**
     * 删除分类
     */
    @Transactional
    public Result<?> deleteCategory(String categoryId, Long operatorId) {
        Shop shop = getApprovedShop(operatorId);
        if (shop == null) {
            return Result.fail(ResultCode.SHOP_NOT_FOUND, "店铺不存在或未通过审核");
        }

        Category category = categoryMapper.selectById(Long.parseLong(categoryId));
        if (category == null || !category.getShopId().equals(shop.getId())) {
            return Result.fail(ResultCode.CATEGORY_NOT_FOUND, "分类不存在");
        }

        // 检查分类下是否有商品
        long count = productMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getCategoryId, category.getId())
                .eq(Product::getDeleted, 0));
        if (count > 0) {
            return Result.fail(ResultCode.FAIL, "分类下有商品，无法删除");
        }

        categoryMapper.deleteById(category.getId());
        return Result.success();
    }

    // ============ 私有方法 ============

    private Shop getApprovedShop(Long operatorId) {
        return shopMapper.selectOne(new LambdaQueryWrapper<Shop>()
                .eq(Shop::getOperatorId, operatorId)
                .eq(Shop::getStatus, ShopStatus.APPROVED.getCode())
                .eq(Shop::getDeleted, 0));
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

    private void loadMainImageUrls(List<ProductDTO> dtos) {
        List<String> fileIds = dtos.stream()
            .map(ProductDTO::getMainImage).filter(Objects::nonNull).collect(Collectors.toList());
        if (!fileIds.isEmpty()) {
            Map<String, String> urlMap = fileRecordMapper.selectBatchIds(fileIds)
                .stream().collect(Collectors.toMap(f -> f.getId().toString(), FileRecord::getUrl));
            dtos.forEach(dto -> {
                if (dto.getMainImage() != null) dto.setMainImageUrl(urlMap.get(dto.getMainImage()));
            });
        }
    }
}