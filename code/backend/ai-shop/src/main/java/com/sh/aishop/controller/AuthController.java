package com.sh.aishop.controller;

import com.sh.aishop.common.Result;
import com.sh.aishop.dto.LoginRequest;
import com.sh.aishop.dto.PasswordRequest;
import com.sh.aishop.dto.RegisterRequest;
import com.sh.aishop.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import java.util.Map;

@Tag(name = "认证管理", description = "用户登录、注册、密码修改、登出")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @Operation(summary = "用户登录", description = "使用用户名和密码登录，返回JWT Token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "登录成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "用户名或密码错误")
    })
    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @Operation(summary = "用户注册", description = "注册新用户，需要邀请码或管理员审批")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "注册成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误或邀请码无效"),
        @ApiResponse(responseCode = "409", description = "用户名已存在")
    })
    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @Operation(summary = "获取用户信息", description = "获取当前登录用户的基本信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未授权，Token无效或过期")
    })
    @GetMapping("/info")
    public Result<?> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return authService.getUserInfo(userId);
    }

    @Operation(summary = "修改密码", description = "修改当前用户的登录密码")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "修改成功"),
        @ApiResponse(responseCode = "400", description = "旧密码错误"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    @PutMapping("/password")
    public Result<?> changePassword(HttpServletRequest request,
                                    @Valid @RequestBody PasswordRequest passwordRequest) {
        Long userId = (Long) request.getAttribute("userId");
        return authService.changePassword(userId, passwordRequest);
    }

    @Operation(summary = "修改用户信息", description = "修改当前用户信息（昵称等）")
    @PutMapping("/info")
    public Result<?> updateUserInfo(HttpServletRequest request,
                                    @RequestBody Map<String, String> body) {
        Long userId = (Long) request.getAttribute("userId");
        String nickname = body.get("nickname");
        return authService.updateUserInfo(userId, nickname);
    }

    @Operation(summary = "用户登出", description = "退出登录，将Token加入黑名单")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "登出成功")
    })
    @PostMapping("/logout")
    public Result<?> logout(
            @Parameter(description = "Authorization Header", required = true)
            @RequestHeader("Authorization") String token) {
        return authService.logout(token);
    }
}