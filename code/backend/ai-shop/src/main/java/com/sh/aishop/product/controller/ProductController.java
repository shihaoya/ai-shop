package com.sh.aishop.product.controller;

import com.sh.aishop.common.Result;
import com.sh.aishop.dto.PageRequest;
import com.sh.aishop.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "商品管理", description = "运营人员操作：分类管理、商品管理")
@RestController
@RequestMapping("/api/operator")
public class ProductController {
    @Autowired
    private ProductService productService;

    @Operation(summary = "分类列表", description = "获取店铺的商品分类")
    @GetMapping("/categories")
    public Result<?> getCategories(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return productService.getCategories(userId);
    }

    @Operation(summary = "创建分类", description = "新增商品分类")
    @PostMapping("/categories")
    public Result<?> createCategory(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        return productService.createCategory(userId,
                params.get("name").toString(),
                params.get("sort") != null ? Integer.valueOf(params.get("sort").toString()) : null);
    }

    @Operation(summary = "更新分类", description = "修改商品分类信息")
    @PutMapping("/categories/{id}")
    public Result<?> updateCategory(@PathVariable("id") Long id, HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        return productService.updateCategory(id, userId,
                params.get("name") != null ? params.get("name").toString() : null,
                params.get("sort") != null ? Integer.valueOf(params.get("sort").toString()) : null);
    }

    @Operation(summary = "删除分类", description = "删除商品分类")
    @DeleteMapping("/categories/{id}")
    public Result<?> deleteCategory(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return productService.deleteCategory(id, userId);
    }

    @Operation(summary = "商品列表", description = "获取当前店铺的商品列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    @GetMapping("/products")
    public Result<?> getProducts(HttpServletRequest request,
                                 @Parameter(description = "分页参数") PageRequest pageRequest) {
        Long userId = (Long) request.getAttribute("userId");
        return productService.getProducts(userId, pageRequest);
    }

    @Operation(summary = "创建商品", description = "新增商品上架")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "创建成功"),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    @PostMapping("/products")
    public Result<?> createProduct(HttpServletRequest request,
                                   @Parameter(description = "商品信息：name, categoryId, type, price, stock等") @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        return productService.createProduct(userId, params);
    }

    @Operation(summary = "商品详情", description = "获取商品详细信息")
    @GetMapping("/products/{id}")
    public Result<?> getProduct(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return productService.getProduct(userId, id);
    }

    @Operation(summary = "更新商品", description = "修改商品信息")
    @PutMapping("/products/{id}")
    public Result<?> updateProduct(@PathVariable("id") Long id, HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        return productService.updateProduct(userId, id, params);
    }

    @Operation(summary = "删除商品", description = "删除商品")
    @DeleteMapping("/products/{id}")
    public Result<?> deleteProduct(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return productService.deleteProduct(userId, id);
    }
}