package com.sh.aishop.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("product")
public class Product extends BaseEntity<Product> {
    private Long shopId;
    private Long categoryId;
    private String name;
    private Integer type;
    private Integer price;
    private Integer stock;
    private Integer limitPerUser;
    private String mainImage;
    private String detailImages;
    private String description;
    private String deliveryInfo;
    private Integer status;
}