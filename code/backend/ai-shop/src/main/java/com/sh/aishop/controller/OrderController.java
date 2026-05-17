package com.sh.aishop.controller;

import com.sh.aishop.common.Result;
import com.sh.aishop.dto.PageRequest;
import com.sh.aishop.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "订单", description = "订单操作")
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    // ============ 用户接口 ============

    @Operation(summary = "创建订单", description = "用户购买商品创建订单")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "订单创建成功"),
        @ApiResponse(responseCode = "400", description = "参数错误或库存不足"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    @PostMapping
    public Result<?> createOrder(HttpServletRequest request,
                                 @Parameter(description = "订单参数：productId(商品ID), quantity(数量), addressInfo(地址信息)") @RequestBody Map<String, Object> params) {
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
    @GetMapping
    public Result<?> getOrders(HttpServletRequest request, PageRequest pageRequest,
                               @Parameter(description = "订单状态：1已下单 2已确认 3已发货 4已完成 5已关闭", example = "1") @RequestParam(required = false) Integer status) {
        Long userId = (Long) request.getAttribute("userId");
        return orderService.getOrders(userId, pageRequest, status);
    }

    @Operation(summary = "订单详情", description = "获取订单详细信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "404", description = "订单不存在")
    })
    @GetMapping("/{id}")
    public Result<?> getOrder(
            @Parameter(description = "订单ID", required = true) @PathVariable("id") String id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return orderService.getOrder(userId, Long.valueOf(id));
    }

    @Operation(summary = "取消订单", description = "用户取消未完成的订单")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "取消成功"),
        @ApiResponse(responseCode = "400", description = "订单状态不允许取消"),
        @ApiResponse(responseCode = "404", description = "订单不存在")
    })
    @PutMapping("/{id}/close")
    public Result<?> closeOrder(
            @Parameter(description = "订单ID", required = true) @PathVariable("id") String id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return orderService.closeOrder(userId, Long.valueOf(id));
    }

    @Operation(summary = "确认收货", description = "用户确认已收到商品，完成订单")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "确认成功"),
        @ApiResponse(responseCode = "400", description = "订单状态不允许"),
        @ApiResponse(responseCode = "404", description = "订单不存在")
    })
    @PutMapping("/{id}/complete")
    public Result<?> completeOrder(
            @Parameter(description = "订单ID", required = true) @PathVariable("id") String id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return orderService.completeOrder(userId, Long.valueOf(id));
    }

    // ============ 商家接口 ============

    @Operation(summary = "商家订单列表", description = "获取当前商家店铺的订单列表，可按状态筛选")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    @GetMapping("/shop")
    public Result<?> getShopOrders(HttpServletRequest request, PageRequest pageRequest,
                                  @Parameter(description = "订单状态：1已下单 2已确认 3已发货 4已完成 5已关闭", example = "1") @RequestParam(required = false) Integer status) {
        Long userId = (Long) request.getAttribute("userId");
        return orderService.getShopOrders(userId, pageRequest, status);
    }

    @Operation(summary = "确认订单", description = "运营人员确认用户订单")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "确认成功"),
        @ApiResponse(responseCode = "400", description = "订单状态不允许"),
        @ApiResponse(responseCode = "404", description = "订单不存在")
    })
    @PutMapping("/{id}/confirm")
    public Result<?> confirmOrder(
            @Parameter(description = "订单ID", required = true) @PathVariable("id") String id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return orderService.confirmOrder(userId, Long.valueOf(id));
    }

    @Operation(summary = "发货", description = "运营人员发货，填入物流信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "发货成功"),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "404", description = "订单不存在")
    })
    @PutMapping("/{id}/ship")
    public Result<?> shipOrder(
            @Parameter(description = "订单ID", required = true) @PathVariable("id") String id,
            HttpServletRequest request,
            @Parameter(description = "物流信息：trackingNo(运单号), carrier(物流公司), deliveryContent(发货备注)") @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        return orderService.shipOrder(userId, Long.valueOf(id), params);
    }

    @Operation(summary = "关闭订单", description = "运营人员关闭订单，可填写原因")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "关闭成功"),
        @ApiResponse(responseCode = "400", description = "订单已无法关闭"),
        @ApiResponse(responseCode = "404", description = "订单不存在")
    })
    @PutMapping("/{id}/close")
    public Result<?> closeShopOrder(
            @Parameter(description = "订单ID", required = true) @PathVariable("id") String id,
            HttpServletRequest request,
            @Parameter(description = "关闭原因") @RequestParam(required = false) String reason) {
        Long userId = (Long) request.getAttribute("userId");
        return orderService.closeShopOrder(userId, Long.valueOf(id), reason);
    }
}