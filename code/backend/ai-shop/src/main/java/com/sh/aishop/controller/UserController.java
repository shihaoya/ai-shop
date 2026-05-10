package com.sh.aishop.controller;

import com.sh.aishop.common.Result;
import com.sh.aishop.dto.PageRequest;
import com.sh.aishop.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "用户端", description = "普通用户操作：商品浏览、订单管理、积分、地址")
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Operation(summary = "商品列表", description = "分页获取可购买的商品列表")
    @GetMapping("/products")
    public Result<?> getProducts(PageRequest pageRequest) {
        return userService.getProducts(pageRequest);
    }

    @Operation(summary = "商品详情", description = "获取单个商品的详细信息")
    @GetMapping("/products/{id}")
    public Result<?> getProduct(@PathVariable("id") Long id) {
        return userService.getProduct(id);
    }

    @Operation(summary = "创建订单", description = "用户购买商品创建订单")
    @PostMapping("/orders")
    public Result<?> createOrder(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        Long productId = Long.valueOf(params.get("productId").toString());
        Integer quantity = Integer.valueOf(params.getOrDefault("quantity", 1).toString());
        Long addressId = params.get("addressId") != null ? Long.valueOf(params.get("addressId").toString()) : null;
        return userService.createOrder(userId, productId, quantity, addressId);
    }

    @Operation(summary = "订单列表", description = "获取当前用户的订单列表，可按状态筛选")
    @GetMapping("/orders")
    public Result<?> getOrders(HttpServletRequest request, PageRequest pageRequest,
                              @Parameter(description = "订单状态：0已下单 1已确认 2已发货 3已完成 4已取消") @RequestParam(required = false) Integer status) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.getOrders(userId, pageRequest, status);
    }

    @Operation(summary = "订单详情", description = "获取订单详细信息")
    @GetMapping("/orders/{id}")
    public Result<?> getOrder(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.getOrder(userId, id);
    }

    @Operation(summary = "取消订单", description = "用户取消未完成的订单")
    @PutMapping("/orders/{id}/close")
    public Result<?> closeOrder(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.closeOrder(userId, id);
    }

    @Operation(summary = "确认收货", description = "用户确认已收到商品，完成订单")
    @PutMapping("/orders/{id}/complete")
    public Result<?> completeOrder(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.completeOrder(userId, id);
    }

    @Operation(summary = "我的积分", description = "获取当前用户的积分余额")
    @GetMapping("/points")
    public Result<?> getPoints(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.getPoints(userId);
    }

    @Operation(summary = "积分记录", description = "获取积分变动明细列表")
    @GetMapping("/points/log")
    public Result<?> getPointsLog(HttpServletRequest request, PageRequest pageRequest) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.getPointsLog(userId, pageRequest);
    }

    @Operation(summary = "地址列表", description = "获取用户的收货地址列表")
    @GetMapping("/addresses")
    public Result<?> getAddresses(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.getAddresses(userId);
    }

    @Operation(summary = "添加地址", description = "新增收货地址")
    @PostMapping("/addresses")
    public Result<?> createAddress(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.createAddress(userId, params);
    }

    @Operation(summary = "修改地址", description = "更新收货地址信息")
    @PutMapping("/addresses/{id}")
    public Result<?> updateAddress(@PathVariable("id") Long id, HttpServletRequest request,
                                  @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.updateAddress(userId, id, params);
    }

    @Operation(summary = "删除地址", description = "删除收货地址")
    @DeleteMapping("/addresses/{id}")
    public Result<?> deleteAddress(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.deleteAddress(userId, id);
    }

    @Operation(summary = "设为默认地址", description = "将指定地址设为默认收货地址")
    @PutMapping("/addresses/{id}/default")
    public Result<?> setDefaultAddress(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.setDefaultAddress(userId, id);
    }

    @Operation(summary = "消息列表", description = "获取用户的系统消息列表")
    @GetMapping("/messages")
    public Result<?> getMessages(HttpServletRequest request, PageRequest pageRequest) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.getMessages(userId, pageRequest);
    }

    @Operation(summary = "标记已读", description = "将消息标记为已读")
    @PutMapping("/messages/{id}/read")
    public Result<?> markMessageRead(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.markMessageRead(userId, id);
    }
}