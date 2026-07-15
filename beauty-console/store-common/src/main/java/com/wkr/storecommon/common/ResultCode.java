package com.wkr.storecommon.common;

/**
 * 统一响应业务码，与对应的 HTTP 状态码保持一致。
 */
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    BUSINESS_ERROR(409, "业务处理失败"),
    TOO_MANY_REQUESTS(429, "请求太频繁，请稍后再试"),
    INTERNAL_SERVER_ERROR(500, "系统错误");

    private final int code;
    private final String defaultMessage;

    ResultCode(int code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public int getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
