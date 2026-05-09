package com.sh.aishop.entity.enums;

public enum UserStatus {
    PENDING(1, "待审核"),
    NORMAL(2, "正常"),
    FROZEN(3, "已冻结");

    private final int code;
    private final String desc;

    UserStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() { return code; }
    public String getDesc() { return desc; }

    public static UserStatus fromCode(int code) {
        for (UserStatus e : values()) {
            if (e.code == code) return e;
        }
        return null;
    }
}