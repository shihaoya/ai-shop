package com.sh.aishop.user.controller;

import com.sh.aishop.common.Result;
import com.sh.aishop.common.dto.PageRequest;
import com.sh.aishop.user.service.PointsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "用户端-我的", description = "普通用户操作：用户信息、积分")
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private PointsService pointsService;

    @Operation(summary = "我的积分", description = "获取当前用户的积分余额")
    @GetMapping("/points")
    public Result<?> getPoints(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return pointsService.getPoints(userId);
    }

    @Operation(summary = "积分记录", description = "获取积分变动明细列表")
    @GetMapping("/points/log")
    public Result<?> getPointsLog(HttpServletRequest request,
                                  @Parameter(description = "分页参数") PageRequest pageRequest) {
        Long userId = (Long) request.getAttribute("userId");
        return pointsService.getPointsLog(userId, pageRequest);
    }
}