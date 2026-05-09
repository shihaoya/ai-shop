package com.sh.aishop.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("shop")
public class Shop extends BaseEntity<Shop> {
    private Long operatorId;
    private String name;
    private String description;
    private Integer status;
    private Integer isActive;
}