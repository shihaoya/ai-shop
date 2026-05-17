package com.sh.aishop.controller;

import com.sh.aishop.common.Result;
import com.sh.aishop.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "店铺", description = "店铺信息管理：我的店铺、申请店铺、营业状态")
@RestController
@RequestMapping("/api/shops")
public class ShopController {
    @Autowired
    private ShopService shopService;

    @Operation(summary = "我的店铺", description = "获取当前用户关联的店铺信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    @GetMapping("/me")
    public Result<?> getMyShop(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return shopService.getMyShop(userId);
    }

    @Operation(summary = "申请店铺", description = "店铺用户申请开通店铺")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "申请成功，待管理员审核"),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "409", description = "已存在店铺")
    })
    @PostMapping
    public Result<?> applyShop(HttpServletRequest request,
                               @Parameter(description = "店铺参数：name(店铺名称), description(店铺描述)")
                               @RequestBody Map<String, String> params) {
        Long userId = (Long) request.getAttribute("userId");
        return shopService.applyShop(userId, params.get("name"), params.get("description"));
    }

    @Operation(summary = "修改营业状态", description = "设置店铺是否营业，歇业时用户无法下单")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "修改成功"),
        @ApiResponse(responseCode = "404", description = "店铺不存在或未通过审核")
    })
    @PutMapping("/status")
    public Result<?> changeShopStatus(HttpServletRequest request,
                                      @Parameter(description = "营业状态：1营业 0歇业", required = true)
                                      @RequestParam Integer isActive) {
        Long userId = (Long) request.getAttribute("userId");
        return shopService.changeShopStatus(userId, isActive);
    }

    @Operation(summary = "重新申请店铺", description = "店铺申请被拒绝后，重新提交申请")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "重新申请成功"),
        @ApiResponse(responseCode = "404", description = "店铺不存在"),
        @ApiResponse(responseCode = "409", description = "店铺已通过审核，无需重新申请")
    })
    @PutMapping("/reapply")
    public Result<?> reapplyShop(HttpServletRequest request,
                                 @Parameter(description = "店铺参数：name(店铺名称), description(店铺描述)")
                                 @RequestBody Map<String, String> params) {
        Long userId = (Long) request.getAttribute("userId");
        return shopService.reapplyShop(userId, params.get("name"), params.get("description"));
    }
}