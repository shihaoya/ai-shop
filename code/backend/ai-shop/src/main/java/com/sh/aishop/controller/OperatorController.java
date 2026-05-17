package com.sh.aishop.controller;

import com.alibaba.excel.EasyExcel;
import com.sh.aishop.common.Result;
import com.sh.aishop.dto.PageRequest;
import com.sh.aishop.dto.UserImportDTO;
import com.sh.aishop.service.OperatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Tag(name = "运营端", description = "店铺运营人员操作：商品管理、订单处理、用户管理、积分调整")
@RestController
@RequestMapping("/api/operator")
public class OperatorController {
    @Autowired
    private OperatorService operatorService;

    // ============ 商品管理 ============
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    @GetMapping("/products")
    public Result<?> getProducts(HttpServletRequest request, 
                                 @Parameter(description = "分页参数") PageRequest pageRequest) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.getProducts(userId, pageRequest);
    }

    @Operation(summary = "创建商品", description = "新增商品上架")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "创建成功"),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    @PostMapping("/products")
    public Result<?> createProduct(HttpServletRequest request, 
                                   @Parameter(description = "商品信息：name, categoryId, type, price, stock等") @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.createProduct(userId, params);
    }

    @Operation(summary = "商品详情", description = "获取商品详细信息")
    @GetMapping("/products/{id}")
    public Result<?> getProduct(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.getProduct(userId, id);
    }

    @Operation(summary = "更新商品", description = "修改商品信息")
    @PutMapping("/products/{id}")
    public Result<?> updateProduct(@PathVariable("id") Long id, HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.updateProduct(userId, id, params);
    }

    @Operation(summary = "删除商品", description = "删除商品")
    @DeleteMapping("/products/{id}")
    public Result<?> deleteProduct(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.deleteProduct(userId, id);
    }

    @Operation(summary = "订单列表", description = "获取店铺的订单列表，可按状态筛选")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未授权")
    })
    @GetMapping("/orders")
    public Result<?> getOrders(HttpServletRequest request, PageRequest pageRequest,
                               @Parameter(description = "订单状态：1已下单 2已确认 3已发货 4已完成 5已关闭", example = "1") @RequestParam(required = false) Integer status) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.getOrders(userId, pageRequest, status);
    }

    @Operation(summary = "确认订单", description = "运营人员确认用户订单")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "确认成功"),
        @ApiResponse(responseCode = "404", description = "订单不存在")
    })
    @PutMapping("/orders/{id}/confirm")
    public Result<?> confirmOrder(
            @Parameter(description = "订单ID", required = true) @PathVariable("id") Long id, 
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.confirmOrder(userId, id);
    }

    @Operation(summary = "发货", description = "运营人员发货，填入物流信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "发货成功"),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "404", description = "订单不存在")
    })
    @PutMapping("/orders/{id}/ship")
    public Result<?> shipOrder(@PathVariable("id") Long id, HttpServletRequest request, 
                               @Parameter(description = "物流信息：trackingNo(运单号), carrier(物流公司)") @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.shipOrder(userId, id, params);
    }

    @Operation(summary = "关闭订单", description = "运营人员关闭订单，可填写原因")
    @PutMapping("/orders/{id}/close")
    public Result<?> closeOrder(@PathVariable("id") Long id, HttpServletRequest request,
                                @Parameter(description = "关闭原因") @RequestParam(required = false) String reason) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.closeOrder(userId, id, reason);
    }

    @Operation(summary = "完成订单", description = "运营人员完成订单（最终状态）")
    @PutMapping("/orders/{id}/complete")
    public Result<?> completeOrder(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.completeOrder(userId, id);
    }

    @Operation(summary = "用户列表", description = "获取当前店铺下的用户列表")
    @GetMapping("/users")
    public Result<?> getUsers(HttpServletRequest request, PageRequest pageRequest) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.getUsers(userId, pageRequest);
    }

    @Operation(summary = "调整积分", description = "运营人员为用户增加或扣除积分")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "调整成功"),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "404", description = "用户不存在")
    })
    @PostMapping("/users/{id}/points")
    public Result<?> adjustPoints(@PathVariable("id") Long id, HttpServletRequest request,
                                  @Parameter(description = "积分参数：amount(积分数量，正数增加负数扣除), remark(备注)") @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.adjustPoints(userId, id,
                Integer.valueOf(params.get("amount").toString()),
                params.get("remark") != null ? params.get("remark").toString() : null);
    }

    @Operation(summary = "用户积分记录", description = "查看指定用户的积分变动明细")
    @GetMapping("/users/{id}/points/log")
    public Result<?> getPointsLog(@PathVariable("id") Long id, HttpServletRequest request, PageRequest pageRequest) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.getPointsLog(userId, id, pageRequest);
    }

    @Operation(summary = "审批用户", description = "审批普通用户注册申请")
    @PutMapping("/users/{id}/approve")
    public Result<?> approveUser(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.approveUser(userId, id);
    }

    @Operation(summary = "拒绝用户", description = "拒绝普通用户注册申请（软删除）")
    @PutMapping("/users/{id}/reject")
    public Result<?> rejectUser(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.rejectUser(userId, id);
    }

    @Operation(summary = "我的邀请码", description = "获取当前运营人员的邀请码")
    @GetMapping("/invite-code")
    public Result<?> getInviteCode(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.getInviteCode(userId);
    }

    @Operation(summary = "生成邀请码", description = "为当前运营人员生成新的邀请码")
    @PostMapping("/invite-code")
    public Result<?> createInviteCode(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.createInviteCode(userId);
    }

    @Operation(summary = "创建用户", description = "运营人员手动创建用户账号")
    @PostMapping("/users/create")
    public Result<?> createUser(HttpServletRequest request, @RequestBody Map<String, String> params) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.createUser(userId, params.get("username"), params.get("nickname"), params.get("password"));
    }

    @Operation(summary = "重置用户密码", description = "强制重置用户密码为新生成的随机密码")
    @PutMapping("/users/{id}/password/reset")
    public Result<?> resetUserPassword(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.resetUserPassword(userId, id);
    }

    @Operation(summary = "下载导入模板", description = "下载 Excel 导入用户模板")
    @GetMapping("/users/import/template")
    public void downloadImportTemplate(HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("用户导入模板.xlsx", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName);

            List<UserImportDTO> templateData = new ArrayList<>();
            EasyExcel.write(response.getOutputStream(), UserImportDTO.class)
                    .sheet("用户导入")
                    .doWrite(templateData);
        } catch (IOException e) {
            throw new RuntimeException("下载模板失败");
        }
    }

    @Operation(summary = "导入用户", description = "通过 Excel 批量导入用户")
    @PostMapping("/users/import")
    public Result<?> importUsers(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.importUsers(userId, file);
    }

    @Operation(summary = "消息列表", description = "获取系统消息列表")
    @GetMapping("/messages")
    public Result<?> getMessages(HttpServletRequest request, PageRequest pageRequest) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.getMessages(userId, pageRequest);
    }

    @Operation(summary = "标记已读", description = "将消息标记为已读")
    @PutMapping("/messages/{id}/read")
    public Result<?> markMessageRead(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return operatorService.markMessageRead(userId, id);
    }
}