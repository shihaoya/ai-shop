package com.sh.aishop.user.controller;

import com.sh.aishop.common.Result;
import com.sh.aishop.user.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "地址管理", description = "普通用户操作：收货地址管理")
@RestController
@RequestMapping("/api/user")
public class AddressController {
    @Autowired
    private AddressService addressService;

    @Operation(summary = "地址列表", description = "获取用户的收货地址列表")
    @GetMapping("/addresses")
    public Result<?> getAddresses(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return addressService.getAddresses(userId);
    }

    @Operation(summary = "添加地址", description = "新增收货地址")
    @PostMapping("/addresses")
    public Result<?> createAddress(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        return addressService.createAddress(userId, params);
    }

    @Operation(summary = "修改地址", description = "更新收货地址信息")
    @PutMapping("/addresses/{id}")
    public Result<?> updateAddress(@PathVariable("id") Long id, HttpServletRequest request,
                                  @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        return addressService.updateAddress(userId, id, params);
    }

    @Operation(summary = "删除地址", description = "删除收货地址")
    @DeleteMapping("/addresses/{id}")
    public Result<?> deleteAddress(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return addressService.deleteAddress(userId, id);
    }

    @Operation(summary = "设为默认地址", description = "将指定地址设为默认收货地址")
    @PutMapping("/addresses/{id}/default")
    public Result<?> setDefaultAddress(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return addressService.setDefaultAddress(userId, id);
    }
}