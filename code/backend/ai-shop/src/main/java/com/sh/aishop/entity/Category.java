package com.sh.aishop.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("category")
public class Category extends BaseEntity<Category> {
    private Long shopId;
    private String name;
    private Integer sort;
}