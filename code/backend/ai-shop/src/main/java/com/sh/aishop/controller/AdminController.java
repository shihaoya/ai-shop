package com.sh.aishop.controller;

import com.sh.aishop.common.Result;
import com.sh.aishop.dto.PageRequest;
import com.sh.aishop.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理端", description = "超级管理员操作：店铺审核、用户管理、邀请码管理")
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;

    @Operation(summary = "店铺列表", description = "分页获取所有店铺列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未授权，需要管理员权限")
    })
    @GetMapping("/shops")
    public Result<?> getShops(
            @Parameter(description = "分页参数") PageRequest pageRequest) {
        return adminService.getShops(pageRequest);
    }

    @Operation(summary = "审核店铺", description = "审批店铺的注册申请")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "审核成功"),
        @ApiResponse(responseCode = "404", description = "店铺不存在")
    })
    @PutMapping("/shops/{id}/audit")
    public Result<?> auditShop(
            @Parameter(description = "店铺ID", required = true, example = "100") @PathVariable("id") Long shopId, 
            @Parameter(description = "审核状态：1通过 2拒绝", required = true, example = "1") @RequestParam Integer status) {
        return adminService.auditShop(shopId, status);
    }

    @Operation(summary = "用户列表", description = "分页获取所有用户列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    @GetMapping("/users")
    public Result<?> getUsers(
            @Parameter(description = "分页参数，支持按角色和状态筛选") PageRequest pageRequest) {
        return adminService.getUsers(pageRequest);
    }

    @Operation(summary = "修改用户状态", description = "启用或禁用用户账号")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "修改成功"),
        @ApiResponse(responseCode = "404", description = "用户不存在")
    })
    @PutMapping("/users/{id}/status")
    public Result<?> changeUserStatus(
            @Parameter(description = "用户ID", required = true) @PathVariable("id") Long userId, 
            @Parameter(description = "状态：1待审核 2正常 3冻结", required = true, example = "2") @RequestParam Integer status) {
        return adminService.changeUserStatus(userId, status);
    }

    @Operation(summary = "审批用户", description = "审批普通用户的注册申请")
    @PutMapping("/users/{id}/approve")
    public Result<?> approveUser(@PathVariable("id") Long userId) {
        return adminService.approveUser(userId);
    }

    @Operation(summary = "拒绝用户", description = "拒绝用户的注册申请（软删除）")
    @PutMapping("/users/{id}/reject")
    public Result<?> rejectUser(@PathVariable("id") Long userId) {
        return adminService.rejectUser(userId);
    }

    @Operation(summary = "我的邀请码", description = "获取管理员的邀请码")
    @GetMapping("/invite-code")
    public Result<?> getInviteCode(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return adminService.getInviteCode(userId);
    }

    @Operation(summary = "生成邀请码", description = "为管理员生成新的邀请码")
    @PostMapping("/invite-code")
    public Result<?> createInviteCode(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return adminService.createInviteCode(userId);
    }
}