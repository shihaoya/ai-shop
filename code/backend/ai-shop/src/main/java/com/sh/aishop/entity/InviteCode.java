package com.sh.aishop.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("invite_code")
public class InviteCode extends BaseEntity<InviteCode> {
    private String code;
    private Integer role;
    private Long creatorId;
    private Integer status;
}