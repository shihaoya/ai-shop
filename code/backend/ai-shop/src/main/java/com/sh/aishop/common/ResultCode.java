package com.sh.aishop.common;

public class ResultCode {
    public static final int SUCCESS = 200;
    public static final int FAIL = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int SERVER_ERROR = 500;

    // 认证模块 1000-1099
    public static final int USERNAME_PASSWORD_ERROR = 1001;
    public static final int USER_DISABLED = 1002;
    public static final int USER_NOT_FOUND = 1003;
    public static final int TOKEN_EXPIRED = 1004;
    public static final int TOKEN_INVALID = 1005;
    public static final int INVITE_CODE_INVALID = 1006;
    public static final int USERNAME_EXISTS = 1007;
    public static final int PASSWORD_ERROR = 1008;
    public static final int PARAM_VALID_FAIL = 1009;       // 参数校验失败
    public static final int PASSWORD_MISMATCH = 1010;     // 两次密码输入不一致
    public static final int REQUEST_FORMAT_ERROR = 1101;  // 请求格式错误
    public static final int MISSING_PARAM = 1102;          // 缺少必要参数

    // 业务错误 2000-2999
    public static final int SHOP_NOT_FOUND = 2001;
    public static final int SHOP_NOT_APPROVED = 2002;
    public static final int PRODUCT_NOT_FOUND = 2003;
    public static final int PRODUCT_OFF_SALE = 2004;
    public static final int PRODUCT_STOCK_ZERO = 2005;
    public static final int POINTS_INSUFFICIENT = 2006;
    public static final int ORDER_NOT_FOUND = 2007;
    public static final int ORDER_STATUS_ERROR = 2008;
    public static final int ADDRESS_NOT_FOUND = 2009;
    public static final int CATEGORY_NOT_FOUND = 2010;
    public static final int USER_NOT_APPROVED = 2011;
}