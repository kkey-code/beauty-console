package com.wkr.storecommon.common;

import lombok.Getter;

import java.io.Serializable;

/**
 * 后端统一响应体，固定输出 code、message 和 data。
 *
 * @param <T> 响应数据类型
 */
@Getter
public final class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Integer code;
    private final String message;
    private final T data;

    private Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getDefaultMessage(), data);
    }

    public static <T> Result<T> error(String message) {
        return error(ResultCode.BUSINESS_ERROR, message);
    }

    public static <T> Result<T> error(ResultCode resultCode, String message) {
        String resolvedMessage = message == null || message.isBlank()
                ? resultCode.getDefaultMessage()
                : message;
        return new Result<>(resultCode.getCode(), resolvedMessage, null);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }
}
