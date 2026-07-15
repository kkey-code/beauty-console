package com.wkr.storecommon.exception;

/**
 * 请求超过接口限流阈值时抛出的异常。
 */
public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException(String message) {
        super(message);
    }
}
