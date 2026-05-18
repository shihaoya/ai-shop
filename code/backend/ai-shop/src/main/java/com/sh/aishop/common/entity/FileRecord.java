package com.sh.aishop.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("file_record")
public class FileRecord extends BaseEntity<FileRecord> {
    /** 存储类型常量 */
    public static final int STORAGE_LOCAL = 1;
    public static final int STORAGE_OSS = 2;

    /** 原始文件名 */
    private String fileName;

    /** 存储路径 */
    private String filePath;

    /** 文件大小(字节) */
    private Long fileSize;

    /** 文件MIME类型 */
    private String fileType;

    /** 文件扩展名 */
    private String fileExt;

    /** 存储类型: 1=本地 2=OSS */
    private Integer storageType = 1;

    /** 业务类型: product, avatar, etc. */
    private String businessType;

    /** 关联业务ID */
    private Long businessId;

    /** 访问URL */
    private String url;
}