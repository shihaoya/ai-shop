package com.sh.aishop.shop.controller;

import com.sh.aishop.common.Result;
import com.sh.aishop.shop.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "店铺管理", description = "运营人员操作：我的店铺、申请店铺、营业状态")
@RestController
@RequestMapping("/api/operator")
public class ShopController {
    @Autowired
    private ShopService shopService;

    @Operation(summary = "我的店铺", description = "获取当前运营人员关联的店铺信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未授权"),
        @ApiResponse(responseCode = "404", description = "店铺不存在")
    })
    @GetMapping("/shop")
    public Result<?> getMyShop(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return shopService.getMyShop(userId);
    }

    @Operation(summary = "申请店铺", description = "运营人员申请开通店铺")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "申请成功，待管理员审核"),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "409", description = "已存在店铺")
    })
    @PostMapping("/shop")
    public Result<?> applyShop(HttpServletRequest request,
                               @Parameter(description = "店铺参数：name(店铺名称), description(店铺描述)") @RequestBody Map<String, String> params) {
        Long userId = (Long) request.getAttribute("userId");
        return shopService.applyShop(userId, params.get("name"), params.get("description"));
    }

    @Operation(summary = "修改营业状态", description = "设置店铺是否营业，歇业时用户无法下单")
    @PutMapping("/shop/status")
    public Result<?> changeShopStatus(HttpServletRequest request, @RequestParam Integer isActive) {
        Long userId = (Long) request.getAttribute("userId");
        return shopService.changeShopStatus(userId, isActive);
    }
}