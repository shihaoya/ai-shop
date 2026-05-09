package com.sh.aishop.controller;

import com.sh.aishop.common.Result;
import com.sh.aishop.dto.PageRequest;
import com.sh.aishop.service.OperatorService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/operator")
public class OperatorController {
    @Autowired
    private OperatorService operatorService;

    @GetMapping("/shop")
    public Result<?> getMyShop(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.getMyShop(userId);
    }

    @PostMapping("/shop")
    public Result<?> applyShop(HttpServletRequest request, @RequestBody Map<String, String> params) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.applyShop(userId, params.get("name"), params.get("description"));
    }

    @PutMapping("/shop/status")
    public Result<?> changeShopStatus(HttpServletRequest request, @RequestParam Integer isActive) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.changeShopStatus(userId, isActive);
    }

    @GetMapping("/categories")
    public Result<?> getCategories(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.getCategories(userId);
    }

    @PostMapping("/categories")
    public Result<?> createCategory(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.createCategory(userId,
                params.get("name").toString(),
                params.get("sort") != null ? Integer.valueOf(params.get("sort").toString()) : null);
    }

    @PutMapping("/categories/{id}")
    public Result<?> updateCategory(@PathVariable("id") Long id, HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.updateCategory(id, userId,
                params.get("name") != null ? params.get("name").toString() : null,
                params.get("sort") != null ? Integer.valueOf(params.get("sort").toString()) : null);
    }

    @DeleteMapping("/categories/{id}")
    public Result<?> deleteCategory(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.deleteCategory(id, userId);
    }

    @GetMapping("/products")
    public Result<?> getProducts(HttpServletRequest request, PageRequest pageRequest) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.getProducts(userId, pageRequest);
    }

    @PostMapping("/products")
    public Result<?> createProduct(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.createProduct(userId, params);
    }

    @GetMapping("/products/{id}")
    public Result<?> getProduct(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.getProduct(userId, id);
    }

    @PutMapping("/products/{id}")
    public Result<?> updateProduct(@PathVariable("id") Long id, HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.updateProduct(userId, id, params);
    }

    @DeleteMapping("/products/{id}")
    public Result<?> deleteProduct(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.deleteProduct(userId, id);
    }

    @GetMapping("/orders")
    public Result<?> getOrders(HttpServletRequest request, PageRequest pageRequest,
                               @RequestParam(required = false) Integer status) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.getOrders(userId, pageRequest, status);
    }

    @PutMapping("/orders/{id}/confirm")
    public Result<?> confirmOrder(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.confirmOrder(userId, id);
    }

    @PutMapping("/orders/{id}/ship")
    public Result<?> shipOrder(@PathVariable("id") Long id, HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.shipOrder(userId, id, params);
    }

    @PutMapping("/orders/{id}/close")
    public Result<?> closeOrder(@PathVariable("id") Long id, HttpServletRequest request,
                                @RequestParam(required = false) String reason) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.closeOrder(userId, id, reason);
    }

    @PutMapping("/orders/{id}/complete")
    public Result<?> completeOrder(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.completeOrder(userId, id);
    }

    @GetMapping("/users")
    public Result<?> getUsers(HttpServletRequest request, PageRequest pageRequest) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.getUsers(userId, pageRequest);
    }

    @PostMapping("/users/{id}/points")
    public Result<?> adjustPoints(@PathVariable("id") Long id, HttpServletRequest request,
                                  @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.adjustPoints(userId, id,
                Integer.valueOf(params.get("amount").toString()),
                params.get("remark") != null ? params.get("remark").toString() : null);
    }

    @GetMapping("/users/{id}/points/log")
    public Result<?> getPointsLog(@PathVariable("id") Long id, HttpServletRequest request, PageRequest pageRequest) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.getPointsLog(userId, id, pageRequest);
    }

    @PutMapping("/users/{id}/approve")
    public Result<?> approveUser(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.approveUser(userId, id);
    }

    @GetMapping("/invite-code")
    public Result<?> getInviteCode(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.getInviteCode(userId);
    }

    @PostMapping("/invite-code")
    public Result<?> createInviteCode(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.createInviteCode(userId);
    }

    @PostMapping("/users/create")
    public Result<?> createUser(HttpServletRequest request, @RequestBody Map<String, String> params) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.createUser(userId, params.get("username"), params.get("nickname"), params.get("password"));
    }

    @GetMapping("/messages")
    public Result<?> getMessages(HttpServletRequest request, PageRequest pageRequest) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.getMessages(userId, pageRequest);
    }

    @PutMapping("/messages/{id}/read")
    public Result<?> markMessageRead(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.markMessageRead(userId, id);
    }
}