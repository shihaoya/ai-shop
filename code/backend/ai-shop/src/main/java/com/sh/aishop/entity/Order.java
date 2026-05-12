package com.sh.aishop.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("orders")
public class Order extends BaseEntity<Order> {
    private String orderNo;
    private Long userId;
    private Long shopId;
    private Long productId;
    private Integer points;
    private Integer quantity;
    private Integer status;
    private LocalDateTime completedAt;
    private LocalDateTime closedAt;
    private String closeReason;
    private String receiverName;
    private String receiverPhone;
    private String receiverProvince;
    private String receiverCity;
    private String receiverDistrict;
    private String receiverDetail;
    private String expressCompany;
    private String expressNo;
    private String deliveryContent;
}