package com.sh.aishop.entity.enums;

public enum ProductStatus {
    ON_SALE(1, "上架"),
    OFF_SALE(2, "下架");

    private final int code;
    private final String desc;

    ProductStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() { return code; }
    public String getDesc() { return desc; }

    public static ProductStatus fromCode(int code) {
        for (ProductStatus e : values()) {
            if (e.code == code) return e;
        }
        return null;
    }
}