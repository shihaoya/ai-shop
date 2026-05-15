package com.sh.aishop.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
public abstract class BaseEntity<T extends Model<T>> extends Model<T> {
    @TableId(type = IdType.ASSIGN_ID)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonSerialize(using = ToStringSerializer.class)
    protected Long id;

    @TableField(fill = FieldFill.INSERT)
    protected LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    protected LocalDateTime updatedAt;

    /** 创建人ID */
    @TableField(fill = FieldFill.INSERT)
    protected Long createdBy;

    /** 更新人ID */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    protected Long updatedBy;

    @TableLogic
    protected Integer deleted = 0;
}