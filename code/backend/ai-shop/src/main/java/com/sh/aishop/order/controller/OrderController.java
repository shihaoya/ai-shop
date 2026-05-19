package com.sh.aishop.order.controller;

import com.sh.aishop.common.Result;
import com.sh.aishop.common.dto.PageRequest;
import com.sh.aishop.order.service.IOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "订单管理(运营)", description = "运营人员操作：订单列表、确认、发货、关闭、完成")
@RestController
@RequestMapping("/api/operator")
public class OrderController {
    @Autowired
    private IOrderService orderService;

    @Operation(summary = "订单列表", description = "获取店铺的订单列表，可按状态筛选")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    @GetMapping("/orders")
    public Result<?> getOrders(HttpServletRequest request, PageRequest pageRequest,
                               @Parameter(description = "订单状态：1已下单 2已确认 3已发货 4已完成 5已关闭", example = "1") @RequestParam(required = false) Integer status) {
        Long userId = (Long) request.getAttribute("userId");
        return orderService.getShopOrders(userId, pageRequest, status);
    }

    @Operation(summary = "确认订单", description = "运营人员确认用户订单")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "确认成功"),
        @ApiResponse(responseCode = "404", description = "订单不存在")
    })
    @PutMapping("/orders/{id}/confirm")
    public Result<?> confirmOrder(
            @Parameter(description = "订单ID", required = true) @PathVariable("id") Long id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return orderService.confirmOrder(userId, id);
    }

    @Operation(summary = "发货", description = "运营人员发货，填入物流信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "发货成功"),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "404", description = "订单不存在")
    })
    @PutMapping("/orders/{id}/ship")
    public Result<?> shipOrder(@PathVariable("id") Long id, HttpServletRequest request,
                               @Parameter(description = "物流信息：trackingNo(运单号), carrier(物流公司)") @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        return orderService.shipOrder(userId, id, params);
    }

    @Operation(summary = "关闭订单", description = "运营人员关闭订单，可填写原因")
    @PutMapping("/orders/{id}/close")
    public Result<?> closeOrder(@PathVariable("id") Long id, HttpServletRequest request,
                                @Parameter(description = "关闭原因") @RequestParam(required = false) String reason) {
        Long userId = (Long) request.getAttribute("userId");
        return orderService.closeShopOrder(userId, id, reason);
    }

    @Operation(summary = "完成订单", description = "运营人员完成订单（最终状态）")
    @PutMapping("/orders/{id}/complete")
    public Result<?> completeOrder(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return orderService.completeShopOrder(userId, id);
    }
}