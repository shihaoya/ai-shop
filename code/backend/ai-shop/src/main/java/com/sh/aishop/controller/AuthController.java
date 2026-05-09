package com.sh.aishop.controller;

import com.sh.aishop.common.Result;
import com.sh.aishop.dto.*;
import com.sh.aishop.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @GetMapping("/info")
    public Result<?> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return authService.getUserInfo(userId);
    }

    @PutMapping("/password")
    public Result<?> changePassword(HttpServletRequest request,
                                    @Valid @RequestBody PasswordRequest passwordRequest) {
        Long userId = (Long) request.getAttribute("userId");
        return authService.changePassword(userId, passwordRequest);
    }

    @PostMapping("/logout")
    public Result<?> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        return authService.logout(token);
    }
}