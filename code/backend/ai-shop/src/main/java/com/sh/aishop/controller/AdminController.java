package com.sh.aishop.controller;

import com.sh.aishop.common.Result;
import com.sh.aishop.dto.PageRequest;
import com.sh.aishop.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    @GetMapping("/shops")
    public Result<?> getShops(PageRequest pageRequest) {
        return adminService.getShops(pageRequest);
    }

    @Operation(summary = "审核店铺", description = "审批店铺的注册申请")
    @PutMapping("/shops/{id}/audit")
    public Result<?> auditShop(@PathVariable("id") Long shopId, @RequestParam Integer status) {
        return adminService.auditShop(shopId, status);
    }

    @Operation(summary = "用户列表", description = "分页获取所有用户列表")
    @GetMapping("/users")
    public Result<?> getUsers(PageRequest pageRequest) {
        return adminService.getUsers(pageRequest);
    }

    @Operation(summary = "修改用户状态", description = "启用或禁用用户账号")
    @PutMapping("/users/{id}/status")
    public Result<?> changeUserStatus(@PathVariable("id") Long userId, @RequestParam Integer status) {
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