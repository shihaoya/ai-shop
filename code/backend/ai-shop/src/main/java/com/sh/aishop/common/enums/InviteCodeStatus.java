package com.sh.aishop.common.enums;

public enum InviteCodeStatus {
    ACTIVE(1, "有效"),
    INVALID(2, "已作废");

    private final int code;
    private final String desc;

    InviteCodeStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() { return code; }
    public String getDesc() { return desc; }

    public static InviteCodeStatus fromCode(int code) {
        for (InviteCodeStatus e : values()) {
            if (e.code == code) return e;
        }
        return null;
    }
}