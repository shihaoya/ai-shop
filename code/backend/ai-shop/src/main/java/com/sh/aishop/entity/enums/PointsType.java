package com.sh.aishop.entity.enums;

public enum PointsType {
    GRANT(1, "发放"),
    DEDUCT(2, "扣除"),
    EXCHANGE(3, "兑换"),
    REFUND(4, "退款");

    private final int code;
    private final String desc;

    PointsType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() { return code; }
    public String getDesc() { return desc; }

    public static PointsType fromCode(int code) {
        for (PointsType e : values()) {
            if (e.code == code) return e;
        }
        return null;
    }
}