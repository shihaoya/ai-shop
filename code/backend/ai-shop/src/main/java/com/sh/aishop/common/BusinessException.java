package com.sh.aishop.common;

/**
 * 业务异常类
 * 用于处理业务逻辑中的错误情况
 */
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.SUCCESS;
    }

    public int getCode() {
        return code;
    }
}