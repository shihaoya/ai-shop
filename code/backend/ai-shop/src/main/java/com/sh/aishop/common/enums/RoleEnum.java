package com.sh.aishop.common.enums;

public enum RoleEnum {
    ADMIN(1, "管理员"),
    SHOP_USER(2, "店铺用户"),
    NORMAL_USER(3, "普通用户");

    private final int code;
    private final String desc;

    RoleEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() { return code; }
    public String getDesc() { return desc; }

    public static RoleEnum fromCode(int code) {
        for (RoleEnum e : values()) {
            if (e.code == code) return e;
        }
        return null;
    }
}