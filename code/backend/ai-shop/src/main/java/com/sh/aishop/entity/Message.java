package com.sh.aishop.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("message")
public class Message extends BaseEntity<Message> {
    private Long userId;
    private String title;
    private String content;
    private Integer type;
    private Long relatedId;
    private Integer isRead;
}