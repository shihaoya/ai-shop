package com.sh.aishop.user.controller;

import com.alibaba.excel.EasyExcel;
import com.sh.aishop.common.Result;
import com.sh.aishop.dto.PageRequest;
import com.sh.aishop.dto.UserImportDTO;
import com.sh.aishop.user.service.PointsService;
import com.sh.aishop.user.service.UserService;
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

@Tag(name = "用户积分管理", description = "运营人员操作：用户列表、积分调整、积分记录、用户审批")
@RestController
@RequestMapping("/api/operator")
public class PointsController {
    @Autowired
    private PointsService pointsService;

    @Autowired
    private UserService userService;

    @Operation(summary = "用户列表", description = "获取当前店铺下的用户列表")
    @GetMapping("/users")
    public Result<?> getUsers(HttpServletRequest request, PageRequest pageRequest) {
        Long userId = (Long) request.getAttribute("userId");
        return pointsService.getUsers(userId, pageRequest);
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
        return pointsService.adjustPoints(userId, id,
                Integer.valueOf(params.get("amount").toString()),
                params.get("remark") != null ? params.get("remark").toString() : null);
    }

    @Operation(summary = "用户积分记录", description = "查看指定用户的积分变动明细")
    @GetMapping("/users/{id}/points/log")
    public Result<?> getPointsLog(@PathVariable("id") Long id, HttpServletRequest request, PageRequest pageRequest) {
        Long userId = (Long) request.getAttribute("userId");
        return pointsService.getPointsLog(userId, id, pageRequest);
    }

    @Operation(summary = "审批用户", description = "审批普通用户注册申请")
    @PutMapping("/users/{id}/approve")
    public Result<?> approveUser(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return pointsService.approveUser(userId, id);
    }

    @Operation(summary = "拒绝用户", description = "拒绝普通用户注册申请（软删除）")
    @PutMapping("/users/{id}/reject")
    public Result<?> rejectUser(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return pointsService.rejectUser(userId, id);
    }

    @Operation(summary = "我的邀请码", description = "获取当前运营人员的邀请码")
    @GetMapping("/invite-code")
    public Result<?> getInviteCode(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.getInviteCode(userId);
    }

    @Operation(summary = "生成邀请码", description = "为当前运营人员生成新的邀请码")
    @PostMapping("/invite-code")
    public Result<?> createInviteCode(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.createInviteCode(userId);
    }

    @Operation(summary = "创建用户", description = "运营人员手动创建用户账号")
    @PostMapping("/users/create")
    public Result<?> createUser(HttpServletRequest request, @RequestBody Map<String, String> params) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.createUser(userId, params.get("username"), params.get("nickname"), params.get("password"));
    }

    @Operation(summary = "重置用户密码", description = "强制重置用户密码为新生成的随机密码")
    @PutMapping("/users/{id}/password/reset")
    public Result<?> resetUserPassword(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.resetUserPassword(userId, id);
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
        return userService.importUsers(userId, file);
    }
}