package com.sh.aishop.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sh.aishop.common.Result;
import com.sh.aishop.common.ResultCode;
import com.sh.aishop.common.entity.Category;
import com.sh.aishop.common.entity.Product;
import com.sh.aishop.common.enums.ProductStatus;
import com.sh.aishop.common.enums.ProductType;
import com.sh.aishop.common.dto.PageRequest;
import com.sh.aishop.product.dto.ProductRequest;
import com.sh.aishop.product.mapper.CategoryMapper;
import com.sh.aishop.product.mapper.ProductMapper;
import com.sh.aishop.shop.service.ShopService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ProductService 单元测试")
class ProductServiceTest {

    @Mock
    private ProductMapper productMapper;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private ShopService shopService;

    @InjectMocks
    private com.sh.aishop.product.service.ProductService productService;

    private Product createTestProduct(Long id, Long shopId, Integer status, Integer type) {
        Product product = new Product();
        product.setId(id);
        product.setShopId(shopId);
        product.setName("测试商品");
        product.setType(type != null ? type : ProductType.PHYSICAL.getCode());
        product.setPrice(100);
        product.setStock(10);
        product.setLimitPerUser(5);
        product.setStatus(status != null ? status : ProductStatus.ON_SALE.getCode());
        product.setDeleted(0);
        return product;
    }

    private Category createTestCategory(Long id, Long shopId, String name) {
        Category category = new Category();
        category.setId(id);
        category.setShopId(shopId);
        category.setName(name);
        category.setSort(0);
        category.setDeleted(0);
        return category;
    }

    private Map<String, Object> shopDataWithId(Long shopId) {
        Map<String, Object> data = new HashMap<>();
        data.put("hasShop", true);
        data.put("id", shopId.toString());
        return data;
    }

    private ProductRequest createProductRequest(Map<String, Object> params) {
        ProductRequest req = new ProductRequest();
        if (params.containsKey("name")) req.setName((String) params.get("name"));
        if (params.containsKey("type")) req.setType((Integer) params.get("type"));
        if (params.containsKey("price")) req.setPrice((Integer) params.get("price"));
        if (params.containsKey("stock")) req.setStock((Integer) params.get("stock"));
        if (params.containsKey("limitPerUser")) req.setLimitPerUser((Integer) params.get("limitPerUser"));
        if (params.containsKey("categoryId")) req.setCategoryId((String) params.get("categoryId"));
        if (params.containsKey("mainImage")) req.setMainImage((String) params.get("mainImage"));
        if (params.containsKey("detailImages")) req.setDetailImages((String) params.get("detailImages"));
        if (params.containsKey("description")) req.setDescription((String) params.get("description"));
        return req;
    }

    @Nested
    @DisplayName("getCategories() 获取分类列表")
    class GetCategoriesTests {

        @Test
        @DisplayName("获取分类成功")
        void getCategories_Success() {
            Category cat1 = createTestCategory(1L, 100L, "分类1");
            Category cat2 = createTestCategory(2L, 100L, "分类2");

            Result<?> shopResult = Result.success(shopDataWithId(100L));
            doReturn(shopResult).when(shopService).getMyShop(any());
            when(categoryMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(cat1, cat2));
            when(productMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

            Result<?> result = productService.getCategories(100L);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            assertNotNull(result.getData());
            assertTrue(result.getData() instanceof List);
        }

        @Test
        @DisplayName("获取分类失败 - 店铺不存在")
        void getCategories_Fail_NoShop() {
            Result<?> shopResult = Result.success(Collections.singletonMap("hasShop", false));
            doReturn(shopResult).when(shopService).getMyShop(any());

            Result<?> result = productService.getCategories(100L);

            assertEquals(ResultCode.SHOP_NOT_FOUND, result.getCode());
        }
    }

    @Nested
    @DisplayName("createCategory() 创建分类")
    class CreateCategoryTests {

        @Test
        @DisplayName("创建分类成功")
        void createCategory_Success() {
            Result<?> shopResult = Result.success(shopDataWithId(100L));
            doReturn(shopResult).when(shopService).getMyShop(any());
            when(categoryMapper.insert(any(Category.class))).thenReturn(1);

            Result<?> result = productService.createCategory(100L, "新分类", 1);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            verify(categoryMapper).insert(any(Category.class));
        }
    }

    @Nested
    @DisplayName("getProducts() 获取商品列表")
    class GetProductsTests {

        @Test
        @DisplayName("获取商品列表成功")
        void getProducts_Success() {
            Product product = createTestProduct(1L, 100L, ProductStatus.ON_SALE.getCode(), null);

            Result<?> shopResult = Result.success(shopDataWithId(100L));
            doReturn(shopResult).when(shopService).getMyShop(any());
            when(productMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(product));
            when(productMapper.selectById(any())).thenReturn(product);

            PageRequest pageRequest = new PageRequest();
            pageRequest.setPage(1);
            pageRequest.setPageSize(10);

            Result<?> result = productService.getProducts(100L, pageRequest);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            assertNotNull(result.getData());
        }

        @Test
        @DisplayName("获取商品列表失败 - 店铺不存在")
        void getProducts_Fail_NoShop() {
            Result<?> shopResult = Result.success(Collections.singletonMap("hasShop", false));
            doReturn(shopResult).when(shopService).getMyShop(any());

            PageRequest pageRequest = new PageRequest();
            pageRequest.setPage(1);
            pageRequest.setPageSize(10);

            Result<?> result = productService.getProducts(100L, pageRequest);

            assertEquals(ResultCode.SHOP_NOT_FOUND, result.getCode());
        }
    }

    @Nested
    @DisplayName("createProduct() 创建商品")
    class CreateProductTests {

        @Test
        @DisplayName("创建商品成功")
        void createProduct_Success() {
            Result<?> shopResult = Result.success(shopDataWithId(100L));
            doReturn(shopResult).when(shopService).getMyShop(any());
            when(productMapper.insert(any(Product.class))).thenReturn(1);

            Map<String, Object> params = new HashMap<>();
            params.put("name", "新商品");
            params.put("price", 100);
            params.put("stock", 10);

            Result<?> result = productService.createProduct(100L, createProductRequest(params));

            assertEquals(ResultCode.SUCCESS, result.getCode());
            verify(productMapper).insert(any(Product.class));
        }
    }

    @Nested
    @DisplayName("getProduct() 获取商品详情")
    class GetProductTests {

        @Test
        @DisplayName("获取商品详情成功")
        void getProduct_Success() {
            Product product = createTestProduct(1L, 100L, ProductStatus.ON_SALE.getCode(), null);

            Result<?> shopResult = Result.success(shopDataWithId(100L));
            doReturn(shopResult).when(shopService).getMyShop(any());
            when(productMapper.selectById(1L)).thenReturn(product);

            Result<?> result = productService.getProduct(100L, 1L);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            assertNotNull(result.getData());
        }

        @Test
        @DisplayName("获取商品详情失败 - 商品不属于该店铺")
        void getProduct_Fail_NotOwner() {
            Product product = createTestProduct(1L, 999L, ProductStatus.ON_SALE.getCode(), null);

            Result<?> shopResult = Result.success(shopDataWithId(100L));
            doReturn(shopResult).when(shopService).getMyShop(any());
            when(productMapper.selectById(1L)).thenReturn(product);

            Result<?> result = productService.getProduct(100L, 1L);

            assertEquals(ResultCode.PRODUCT_NOT_FOUND, result.getCode());
        }
    }

    @Nested
    @DisplayName("updateProduct() 更新商品")
    class UpdateProductTests {

        @Test
        @DisplayName("更新商品成功")
        void updateProduct_Success() {
            Product product = createTestProduct(1L, 100L, ProductStatus.ON_SALE.getCode(), null);

            Result<?> shopResult = Result.success(shopDataWithId(100L));
            doReturn(shopResult).when(shopService).getMyShop(any());
            when(productMapper.selectById(1L)).thenReturn(product);
            when(productMapper.updateById(any(Product.class))).thenReturn(1);

            Map<String, Object> params = new HashMap<>();
            params.put("name", "更新商品");
            params.put("price", 200);

            Result<?> result = productService.updateProduct(100L, 1L, createProductRequest(params));

            assertEquals(ResultCode.SUCCESS, result.getCode());
            verify(productMapper).updateById(any(Product.class));
        }
    }

    @Nested
    @DisplayName("deleteProduct() 删除商品")
    class DeleteProductTests {

        @Test
        @DisplayName("删除商品成功")
        void deleteProduct_Success() {
            Product product = createTestProduct(1L, 100L, ProductStatus.ON_SALE.getCode(), null);

            Result<?> shopResult = Result.success(shopDataWithId(100L));
            doReturn(shopResult).when(shopService).getMyShop(any());
            when(productMapper.selectById(1L)).thenReturn(product);
            when(productMapper.deleteById(1L)).thenReturn(1);

            Result<?> result = productService.deleteProduct(100L, 1L);

            assertEquals(ResultCode.SUCCESS, result.getCode());
            verify(productMapper).deleteById(1L);
        }
    }
}