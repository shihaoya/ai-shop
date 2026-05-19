package com.sh.aishop.product.service;

import com.sh.aishop.common.Result;
import com.sh.aishop.common.ResultCode;
import com.sh.aishop.common.entity.Category;
import com.sh.aishop.common.entity.Product;
import com.sh.aishop.product.mapper.CategoryMapper;
import com.sh.aishop.product.mapper.ProductMapper;
import com.sh.aishop.shop.service.ShopService;
import com.sh.aishop.util.SnowflakeIdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private ProductMapper productMapper;
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
            Map<String, Object> map = new HashMap<>();
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

        Category category = new Category();
        category.setId(SnowflakeIdUtil.nextId());
        category.setShopId(Long.parseLong(shopData.get("id").toString()));
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

        if (StringUtils.hasText(name)) category.setName(name);
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
}
