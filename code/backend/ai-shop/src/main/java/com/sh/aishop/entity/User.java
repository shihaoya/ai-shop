package com.sh.aishop.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user")
public class User extends BaseEntity<User> {
    private String username;
    private String nickname;
    private String password;
    private Integer role;
    private Long parentId;
    private Integer status;
}