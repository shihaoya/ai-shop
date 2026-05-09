package com.sh.aishop.dto;

public class UserDTO {
    private String id;
    private String username;
    private String nickname;
    private Integer role;
    private Integer status;
    private String pointsBalance; // 积分余额，String类型避免精度丢失

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
}