package com.sh.aishop.controller;

import com.sh.aishop.common.Result;
import com.sh.aishop.dto.PageRequest;
import com.sh.aishop.service.OperatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "运营端", description = "店铺运营人员操作：商品管理、订单处理、用户管理、积分调整")
@RestController
@RequestMapping("/api/operator")
public class OperatorController {
    @Autowired
    private OperatorService operatorService;

    @Operation(summary = "我的店铺", description = "获取当前运营人员关联的店铺信息")
    @GetMapping("/shop")
    public Result<?> getMyShop(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.getMyShop(userId);
    }

    @Operation(summary = "申请店铺", description = "运营人员申请开通店铺")
    @PostMapping("/shop")
    public Result<?> applyShop(HttpServletRequest request, @RequestBody Map<String, String> params) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.applyShop(userId, params.get("name"), params.get("description"));
    }

    @Operation(summary = "修改营业状态", description = "设置店铺是否营业，歇业时用户无法下单")
    @PutMapping("/shop/status")
    public Result<?> changeShopStatus(HttpServletRequest request, @RequestParam Integer isActive) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.changeShopStatus(userId, isActive);
    }

    @Operation(summary = "分类列表", description = "获取店铺的商品分类")
    @GetMapping("/categories")
    public Result<?> getCategories(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.getCategories(userId);
    }

    @Operation(summary = "创建分类", description = "新增商品分类")
    @PostMapping("/categories")
    public Result<?> createCategory(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.createCategory(userId,
                params.get("name").toString(),
                params.get("sort") != null ? Integer.valueOf(params.get("sort").toString()) : null);
    }

    @Operation(summary = "更新分类", description = "修改商品分类信息")
    @PutMapping("/categories/{id}")
    public Result<?> updateCategory(@PathVariable("id") Long id, HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.updateCategory(id, userId,
                params.get("name") != null ? params.get("name").toString() : null,
                params.get("sort") != null ? Integer.valueOf(params.get("sort").toString()) : null);
    }

    @Operation(summary = "删除分类", description = "删除商品分类")
    @DeleteMapping("/categories/{id}")
    public Result<?> deleteCategory(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.deleteCategory(id, userId);
    }

    @Operation(summary = "商品列表", description = "获取当前店铺的商品列表")
    @GetMapping("/products")
    public Result<?> getProducts(HttpServletRequest request, PageRequest pageRequest) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.getProducts(userId, pageRequest);
    }

    @Operation(summary = "创建商品", description = "新增商品上架")
    @PostMapping("/products")
    public Result<?> createProduct(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.createProduct(userId, params);
    }

    @Operation(summary = "商品详情", description = "获取商品详细信息")
    @GetMapping("/products/{id}")
    public Result<?> getProduct(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.getProduct(userId, id);
    }

    @Operation(summary = "更新商品", description = "修改商品信息")
    @PutMapping("/products/{id}")
    public Result<?> updateProduct(@PathVariable("id") Long id, HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.updateProduct(userId, id, params);
    }

    @Operation(summary = "删除商品", description = "删除商品")
    @DeleteMapping("/products/{id}")
    public Result<?> deleteProduct(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.deleteProduct(userId, id);
    }

    @Operation(summary = "订单列表", description = "获取店铺的订单列表，可按状态筛选")
    @GetMapping("/orders")
    public Result<?> getOrders(HttpServletRequest request, PageRequest pageRequest,
                               @Parameter(description = "订单状态：0已下单 1已确认 2已发货 3已完成 4已取消") @RequestParam(required = false) Integer status) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.getOrders(userId, pageRequest, status);
    }

    @Operation(summary = "确认订单", description = "运营人员确认用户订单")
    @PutMapping("/orders/{id}/confirm")
    public Result<?> confirmOrder(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.confirmOrder(userId, id);
    }

    @Operation(summary = "发货", description = "运营人员发货，填入物流信息")
    @PutMapping("/orders/{id}/ship")
    public Result<?> shipOrder(@PathVariable("id") Long id, HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.shipOrder(userId, id, params);
    }

    @Operation(summary = "关闭订单", description = "运营人员关闭订单，可填写原因")
    @PutMapping("/orders/{id}/close")
    public Result<?> closeOrder(@PathVariable("id") Long id, HttpServletRequest request,
                                @Parameter(description = "关闭原因") @RequestParam(required = false) String reason) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.closeOrder(userId, id, reason);
    }

    @Operation(summary = "完成订单", description = "运营人员完成订单（最终状态）")
    @PutMapping("/orders/{id}/complete")
    public Result<?> completeOrder(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.completeOrder(userId, id);
    }

    @Operation(summary = "用户列表", description = "获取当前店铺下的用户列表")
    @GetMapping("/users")
    public Result<?> getUsers(HttpServletRequest request, PageRequest pageRequest) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.getUsers(userId, pageRequest);
    }

    @Operation(summary = "调整积分", description = "运营人员为用户增加或扣除积分")
    @PostMapping("/users/{id}/points")
    public Result<?> adjustPoints(@PathVariable("id") Long id, HttpServletRequest request,
                                  @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.adjustPoints(userId, id,
                Integer.valueOf(params.get("amount").toString()),
                params.get("remark") != null ? params.get("remark").toString() : null);
    }

    @Operation(summary = "用户积分记录", description = "查看指定用户的积分变动明细")
    @GetMapping("/users/{id}/points/log")
    public Result<?> getPointsLog(@PathVariable("id") Long id, HttpServletRequest request, PageRequest pageRequest) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.getPointsLog(userId, id, pageRequest);
    }

    @Operation(summary = "审批用户", description = "审批普通用户注册申请")
    @PutMapping("/users/{id}/approve")
    public Result<?> approveUser(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.approveUser(userId, id);
    }

    @Operation(summary = "我的邀请码", description = "获取当前运营人员的邀请码")
    @GetMapping("/invite-code")
    public Result<?> getInviteCode(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.getInviteCode(userId);
    }

    @Operation(summary = "生成邀请码", description = "为当前运营人员生成新的邀请码")
    @PostMapping("/invite-code")
    public Result<?> createInviteCode(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.createInviteCode(userId);
    }

    @Operation(summary = "创建用户", description = "运营人员手动创建用户账号")
    @PostMapping("/users/create")
    public Result<?> createUser(HttpServletRequest request, @RequestBody Map<String, String> params) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.createUser(userId, params.get("username"), params.get("nickname"), params.get("password"));
    }

    @Operation(summary = "消息列表", description = "获取系统消息列表")
    @GetMapping("/messages")
    public Result<?> getMessages(HttpServletRequest request, PageRequest pageRequest) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.getMessages(userId, pageRequest);
    }

    @Operation(summary = "标记已读", description = "将消息标记为已读")
    @PutMapping("/messages/{id}/read")
    public Result<?> markMessageRead(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.markMessageRead(userId, id);
    }
}