package com.sh.aishop.controller;

import com.sh.aishop.common.Result;
import com.sh.aishop.dto.PageRequest;
import com.sh.aishop.service.AddressService;
import com.sh.aishop.service.PointsService;
import com.sh.aishop.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "用户", description = "用户信息、地址、积分管理")
@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private AddressService addressService;
    @Autowired
    private PointsService pointsService;

    // ============ 用户信息 ============

    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的基本信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    @GetMapping("/me")
    public Result<?> getCurrentUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.getUserInfo(userId);
    }

    @Operation(summary = "更新用户信息", description = "修改当前用户信息（昵称等）")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    @PutMapping("/me")
    public Result<?> updateCurrentUser(HttpServletRequest request, @RequestBody Map<String, String> body) {
        Long userId = (Long) request.getAttribute("userId");
        String nickname = body.get("nickname");
        return userService.updateUserInfo(userId, nickname);
    }

    @Operation(summary = "修改密码", description = "修改当前用户的登录密码")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "修改成功"),
        @ApiResponse(responseCode = "400", description = "旧密码错误"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    @PutMapping("/me/password")
    public Result<?> changePassword(HttpServletRequest request, @RequestBody Map<String, String> body) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.changePassword(userId, body.get("oldPassword"), body.get("newPassword"));
    }

    // ============ 积分 ============

    @Operation(summary = "我的积分", description = "获取当前用户的积分余额")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    @GetMapping("/points")
    public Result<?> getPoints(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return pointsService.getPointsBalance(userId);
    }

    @Operation(summary = "积分记录", description = "获取积分变动明细列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    @GetMapping("/points/log")
    public Result<?> getPointsLog(HttpServletRequest request, PageRequest pageRequest) {
        Long userId = (Long) request.getAttribute("userId");
        return pointsService.getPointsLog(userId, pageRequest);
    }

    // ============ 收货地址 ============

    @Operation(summary = "地址列表", description = "获取用户的收货地址列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    @GetMapping("/addresses")
    public Result<?> getAddresses(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return addressService.getAddresses(userId);
    }

    @Operation(summary = "添加地址", description = "新增收货地址")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "添加成功"),
        @ApiResponse(responseCode = "400", description = "地址数量已达上限"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    @PostMapping("/addresses")
    public Result<?> createAddress(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        return addressService.createAddress(userId, params);
    }

    @Operation(summary = "修改地址", description = "更新收货地址信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "404", description = "地址不存在"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    @PutMapping("/addresses/{id}")
    public Result<?> updateAddress(
            @Parameter(description = "地址ID", required = true) @PathVariable("id") String id,
            HttpServletRequest request,
            @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        return addressService.updateAddress(userId, id, params);
    }

    @Operation(summary = "删除地址", description = "删除收货地址")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "删除成功"),
        @ApiResponse(responseCode = "404", description = "地址不存在"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    @DeleteMapping("/addresses/{id}")
    public Result<?> deleteAddress(
            @Parameter(description = "地址ID", required = true) @PathVariable("id") String id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return addressService.deleteAddress(userId, id);
    }

    @Operation(summary = "设为默认地址", description = "将指定地址设为默认收货地址")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "设置成功"),
        @ApiResponse(responseCode = "404", description = "地址不存在"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    @PutMapping("/addresses/{id}/default")
    public Result<?> setDefaultAddress(
            @Parameter(description = "地址ID", required = true) @PathVariable("id") String id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return addressService.setDefaultAddress(userId, id);
    }
}