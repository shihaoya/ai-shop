package com.sh.aishop.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("category")
public class Category extends BaseEntity<Category> {
    private Long shopId;
    private String name;
    private Integer sort;
}