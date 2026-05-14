package com.sh.aishop.entity.enums;

/**
 * 文件存储类型枚举
 */
public enum FileStorageType {
    LOCAL(1, "本地存储"),
    OSS(2, "OSS对象存储");

    private final int code;
    private final String description;

    FileStorageType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}