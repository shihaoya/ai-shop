package com.sh.aishop.product.service;

import com.sh.aishop.common.Result;
import com.sh.aishop.common.ResultCode;
import com.sh.aishop.common.dto.PageRequest;
import com.sh.aishop.common.dto.PageResult;
import com.sh.aishop.common.entity.FileRecord;
import com.sh.aishop.common.entity.Product;
import com.sh.aishop.common.entity.Shop;
import com.sh.aishop.common.enums.ProductStatus;
import com.sh.aishop.common.enums.ProductType;
import com.sh.aishop.common.enums.ShopStatus;
import com.sh.aishop.product.dto.ProductDTO;
import com.sh.aishop.product.mapper.CategoryMapper;
import com.sh.aishop.file.mapper.FileRecordMapper;
import com.sh.aishop.product.mapper.ProductMapper;
import com.sh.aishop.shop.mapper.ShopMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductQueryService {
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ShopMapper shopMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private FileRecordMapper fileRecordMapper;

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

    public Result<?> getCategories(Long operatorId) {
        return Result.success(Collections.emptyList());
    }

    public Result<?> getCategoryProducts(Long operatorId, Long categoryId, PageRequest pageRequest) {
        return getProducts(pageRequest);
    }
}
