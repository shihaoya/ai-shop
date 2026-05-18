package com.sh.aishop.order.controller;

import com.sh.aishop.common.Result;
import com.sh.aishop.common.dto.PageRequest;
import com.sh.aishop.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "订单管理(用户)", description = "普通用户操作：商品浏览、订单管理")
@RestController
@RequestMapping("/api/user")
public class UserOrderController {
    @Autowired
    private OrderService orderService;

    @Operation(summary = "商品列表", description = "分页获取可购买的商品列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    @GetMapping("/products")
    public Result<?> getProducts(
            @Parameter(description = "分页参数") PageRequest pageRequest) {
        return orderService.getProducts(pageRequest);
    }

    @Operation(summary = "商品详情", description = "获取单个商品的详细信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "404", description = "商品不存在")
    })
    @GetMapping("/products/{id}")
    public Result<?> getProduct(
            @Parameter(description = "商品ID", required = true, example = "1234567890") @PathVariable("id") Long id) {
        return orderService.getProduct(id);
    }

    @Operation(summary = "创建订单", description = "用户购买商品创建订单")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "订单创建成功"),
        @ApiResponse(responseCode = "400", description = "参数错误或库存不足"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    @PostMapping("/orders")
    public Result<?> createOrder(HttpServletRequest request,
                                 @Parameter(description = "订单参数：productId(商品ID), quantity(数量), addressInfo(地址信息，可选)") @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        Long productId = Long.valueOf(params.get("productId").toString());
        Integer quantity = Integer.valueOf(params.getOrDefault("quantity", 1).toString());
        @SuppressWarnings("unchecked")
        Map<String, Object> addressInfo = (Map<String, Object>) params.get("addressInfo");
        return orderService.createOrder(userId, productId, quantity, addressInfo);
    }

    @Operation(summary = "订单列表", description = "获取当前用户的订单列表，可按状态筛选")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    @GetMapping("/orders")
    public Result<?> getOrders(HttpServletRequest request, PageRequest pageRequest,
                              @Parameter(description = "订单状态：1已下单 2已确认 3已发货 4已完成 5已关闭", example = "1") @RequestParam(required = false) Integer status) {
        Long userId = (Long) request.getAttribute("userId");
        return orderService.getUserOrders(userId, pageRequest, status);
    }

    @Operation(summary = "订单详情", description = "获取订单详细信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "404", description = "订单不存在")
    })
    @GetMapping("/orders/{id}")
    public Result<?> getOrder(
            @Parameter(description = "订单ID", required = true) @PathVariable("id") Long id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return orderService.getOrder(userId, id);
    }

    @Operation(summary = "取消订单", description = "用户取消未完成的订单")
    @PutMapping("/orders/{id}/close")
    public Result<?> closeOrder(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return orderService.closeUserOrder(userId, id);
    }

    @Operation(summary = "确认收货", description = "用户确认已收到商品，完成订单")
    @PutMapping("/orders/{id}/complete")
    public Result<?> completeOrder(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return orderService.completeUserOrder(userId, id);
    }
}