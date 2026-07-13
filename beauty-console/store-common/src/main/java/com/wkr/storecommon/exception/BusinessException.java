package com.wkr.storecommon.exception;


/**
 * 可预期的业务异常，用于向调用方返回明确且可理解的业务失败原因。
 */
public class BusinessException extends RuntimeException{

    public BusinessException() {
    }

    public BusinessException(String msg) {
        super(msg);
    }
}
