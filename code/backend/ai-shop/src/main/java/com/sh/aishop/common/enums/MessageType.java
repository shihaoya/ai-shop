package com.sh.aishop.common.enums;

public enum MessageType {
    POINTS(1, "积分通知"),
    ORDER(2, "订单通知");

    private final int code;
    private final String desc;

    MessageType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() { return code; }
    public String getDesc() { return desc; }

    public static MessageType fromCode(int code) {
        for (MessageType e : values()) {
            if (e.code == code) return e;
        }
        return null;
    }
}