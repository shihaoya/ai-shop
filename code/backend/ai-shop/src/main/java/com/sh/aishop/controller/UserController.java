package com.sh.aishop.controller;

import com.sh.aishop.common.Result;
import com.sh.aishop.dto.PageRequest;
import com.sh.aishop.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/products")
    public Result<?> getProducts(PageRequest pageRequest) {
        return userService.getProducts(pageRequest);
    }

    @GetMapping("/products/{id}")
    public Result<?> getProduct(@PathVariable("id") Long id) {
        return userService.getProduct(id);
    }

    @PostMapping("/orders")
    public Result<?> createOrder(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        Long productId = Long.valueOf(params.get("productId").toString());
        Integer quantity = Integer.valueOf(params.getOrDefault("quantity", 1).toString());
        Long addressId = params.get("addressId") != null ? Long.valueOf(params.get("addressId").toString()) : null;
        return userService.createOrder(userId, productId, quantity, addressId);
    }

    @GetMapping("/orders")
    public Result<?> getOrders(HttpServletRequest request, PageRequest pageRequest,
                              @RequestParam(required = false) Integer status) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.getOrders(userId, pageRequest, status);
    }

    @GetMapping("/orders/{id}")
    public Result<?> getOrder(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.getOrder(userId, id);
    }

    @PutMapping("/orders/{id}/close")
    public Result<?> closeOrder(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.closeOrder(userId, id);
    }

    @PutMapping("/orders/{id}/complete")
    public Result<?> completeOrder(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.completeOrder(userId, id);
    }

    @GetMapping("/points")
    public Result<?> getPoints(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.getPoints(userId);
    }

    @GetMapping("/points/log")
    public Result<?> getPointsLog(HttpServletRequest request, PageRequest pageRequest) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.getPointsLog(userId, pageRequest);
    }

    @GetMapping("/addresses")
    public Result<?> getAddresses(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.getAddresses(userId);
    }

    @PostMapping("/addresses")
    public Result<?> createAddress(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.createAddress(userId, params);
    }

    @PutMapping("/addresses/{id}")
    public Result<?> updateAddress(@PathVariable("id") Long id, HttpServletRequest request,
                                  @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.updateAddress(userId, id, params);
    }

    @DeleteMapping("/addresses/{id}")
    public Result<?> deleteAddress(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.deleteAddress(userId, id);
    }

    @PutMapping("/addresses/{id}/default")
    public Result<?> setDefaultAddress(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.setDefaultAddress(userId, id);
    }

    @GetMapping("/messages")
    public Result<?> getMessages(HttpServletRequest request, PageRequest pageRequest) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.getMessages(userId, pageRequest);
    }

    @PutMapping("/messages/{id}/read")
    public Result<?> markMessageRead(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.markMessageRead(userId, id);
    }
}