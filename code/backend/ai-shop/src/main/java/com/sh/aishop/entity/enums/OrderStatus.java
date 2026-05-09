package com.sh.aishop.entity.enums;

public enum OrderStatus {
    CREATED(1, "已下单"),
    CONFIRMED(2, "已确认"),
    SHIPPED(3, "已发货"),
    COMPLETED(4, "已完成"),
    CLOSED(5, "已关闭");

    private final int code;
    private final String desc;

    OrderStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() { return code; }
    public String getDesc() { return desc; }

    public static OrderStatus fromCode(int code) {
        for (OrderStatus e : values()) {
            if (e.code == code) return e;
        }
        return null;
    }
}