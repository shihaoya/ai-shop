package com.sh.aishop.controller;

import com.sh.aishop.common.Result;
import com.sh.aishop.dto.PageRequest;
import com.sh.aishop.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;

    @GetMapping("/shops")
    public Result<?> getShops(PageRequest pageRequest) {
        return adminService.getShops(pageRequest);
    }

    @PutMapping("/shops/{id}/audit")
    public Result<?> auditShop(@PathVariable("id") Long shopId, @RequestParam Integer status) {
        return adminService.auditShop(shopId, status);
    }

    @GetMapping("/users")
    public Result<?> getUsers(PageRequest pageRequest) {
        return adminService.getUsers(pageRequest);
    }

    @PutMapping("/users/{id}/status")
    public Result<?> changeUserStatus(@PathVariable("id") Long userId, @RequestParam Integer status) {
        return adminService.changeUserStatus(userId, status);
    }

    @PutMapping("/users/{id}/approve")
    public Result<?> approveUser(@PathVariable("id") Long userId) {
        return adminService.approveUser(userId);
    }

    @GetMapping("/invite-code")
    public Result<?> getInviteCode(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return adminService.getInviteCode(userId);
    }

    @PostMapping("/invite-code")
    public Result<?> createInviteCode(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return adminService.createInviteCode(userId);
    }
}