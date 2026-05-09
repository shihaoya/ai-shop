package com.sh.aishop.entity.enums;

public enum ShopStatus {
    PENDING(1, "待审核"),
    APPROVED(2, "已通过"),
    REJECTED(3, "已拒绝"),
    DISABLED(4, "已禁用");

    private final int code;
    private final String desc;

    ShopStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() { return code; }
    public String getDesc() { return desc; }

    public static ShopStatus fromCode(int code) {
        for (ShopStatus e : values()) {
            if (e.code == code) return e;
        }
        return null;
    }
}