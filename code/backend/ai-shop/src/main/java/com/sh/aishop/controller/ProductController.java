package com.sh.aishop.controller;

import com.sh.aishop.common.Result;
import com.sh.aishop.dto.PageRequest;
import com.sh.aishop.dto.ProductDTO;
import com.sh.aishop.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "商品", description = "商品浏览和管理")
@RestController
@RequestMapping("/api/products")
public class ProductController {
    @Autowired
    private ProductService productService;

    @Operation(summary = "商品列表", description = "获取可购买的商品列表（公开）")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功")
    })
    @GetMapping
    public Result<?> getProducts(
            @Parameter(description = "分页参数") PageRequest pageRequest) {
        return productService.getProducts(pageRequest);
    }

    @Operation(summary = "商品详情", description = "获取单个商品的详细信息（公开）")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "404", description = "商品不存在")
    })
    @GetMapping("/{id}")
    public Result<?> getProduct(
            @Parameter(description = "商品ID", required = true, example = "1234567890") @PathVariable("id") String id) {
        return productService.getProduct(id);
    }

    @Operation(summary = "商家商品列表", description = "获取当前商家店铺的商品列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    @GetMapping("/shop")
    public Result<?> getShopProducts(HttpServletRequest request,
                                     @Parameter(description = "分页参数") PageRequest pageRequest) {
        Long userId = (Long) request.getAttribute("userId");
        return productService.getShopProducts(userId, pageRequest);
    }

    @Operation(summary = "创建商品", description = "商家新增商品上架")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "创建成功"),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    @PostMapping
    public Result<?> createProduct(HttpServletRequest request,
                                    @Parameter(description = "商品信息") @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        return productService.createProduct(userId, params);
    }

    @Operation(summary = "修改商品", description = "商家修改商品信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "修改成功"),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "404", description = "商品不存在")
    })
    @PutMapping("/{id}")
    public Result<?> updateProduct(
            @Parameter(description = "商品ID", required = true, example = "1234567890") @PathVariable("id") String id,
            HttpServletRequest request,
            @Parameter(description = "商品信息") @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        return productService.updateProduct(id, userId, params);
    }

    @Operation(summary = "上架/下架商品", description = "商家修改商品上下架状态")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "修改成功"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "404", description = "商品不存在")
    })
    @PatchMapping("/{id}/status")
    public Result<?> changeProductStatus(
            @Parameter(description = "商品ID", required = true, example = "1234567890") @PathVariable("id") String id,
            HttpServletRequest request,
            @Parameter(description = "状态：1上架 2下架", required = true) @RequestParam Integer status) {
        Long userId = (Long) request.getAttribute("userId");
        return productService.changeProductStatus(id, userId, status);
    }

    @Operation(summary = "删除商品", description = "商家删除商品（软删除）")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "删除成功"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "404", description = "商品不存在")
    })
    @DeleteMapping("/{id}")
    public Result<?> deleteProduct(
            @Parameter(description = "商品ID", required = true, example = "1234567890") @PathVariable("id") String id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return productService.deleteProduct(id, userId);
    }

    @Operation(summary = "上传商品图片", description = "为商品上传主图或详情图")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "上传成功"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "404", description = "商品不存在")
    })
    @PostMapping("/{id}/images")
    public Result<?> uploadProductImages(
            @Parameter(description = "商品ID", required = true, example = "1234567890") @PathVariable("id") String id,
            HttpServletRequest request,
            @Parameter(description = "主图") @RequestParam(required = false) MultipartFile mainImage,
            @Parameter(description = "详情图") @RequestParam(required = false) MultipartFile[] detailImages) {
        Long userId = (Long) request.getAttribute("userId");
        return productService.uploadProductImages(id, userId, mainImage, detailImages);
    }

    // ============ 分类管理 ============

    @Operation(summary = "分类列表", description = "获取当前店铺的商品分类")
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
    public Result<?> updateCategory(@PathVariable("id") String id, HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        return productService.updateCategory(id, userId,
                params.get("name") != null ? params.get("name").toString() : null,
                params.get("sort") != null ? Integer.valueOf(params.get("sort").toString()) : null);
    }

    @Operation(summary = "删除分类", description = "删除商品分类")
    @DeleteMapping("/categories/{id}")
    public Result<?> deleteCategory(@PathVariable("id") String id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return productService.deleteCategory(id, userId);
    }
}