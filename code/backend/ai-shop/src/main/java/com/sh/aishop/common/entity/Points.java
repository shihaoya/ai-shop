package com.sh.aishop.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("points")
public class Points extends BaseEntity<Points> {
    private Long userId;
    private Integer amount;
    private Integer balance;
    private Integer type;
    private String remark;
    private Long operatorId;
}