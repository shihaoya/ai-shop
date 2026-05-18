package com.sh.aishop.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "用户信息")
public class UserDTO {
    @Schema(description = "用户ID", example = "1234567890")
    private String id;

    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    @Schema(description = "昵称", example = "张三")
    private String nickname;

    @Schema(description = "角色：1管理员 2店铺运营 3普通用户", example = "3")
    private Integer role;

    @Schema(description = "状态：1待审核 2正常 3冻结", example = "2")
    private Integer status;

    @Schema(description = "积分余额", example = "1000")
    private String pointsBalance; // 积分余额，String类型避免精度丢失

    @Schema(description = "注册时间", example = "2024-01-01 12:00:00")
    private String createdAt;

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public Integer getRole() { return role; }
    public void setRole(Integer role) { this.role = role; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getPointsBalance() { return pointsBalance; }
    public void setPointsBalance(String pointsBalance) { this.pointsBalance = pointsBalance; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}