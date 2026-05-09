package com.sh.aishop.entity.enums;

public enum ProductType {
    VIRTUAL(1, "虚拟"),
    PHYSICAL(2, "实体");

    private final int code;
    private final String desc;

    ProductType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() { return code; }
    public String getDesc() { return desc; }

    public static ProductType fromCode(int code) {
        for (ProductType e : values()) {
            if (e.code == code) return e;
        }
        return null;
    }
}