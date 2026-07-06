package com.wkr.storecommon.exception;


public class BusinessException extends RuntimeException{

    public BusinessException() {
    }

    public BusinessException(String msg) {
        super(msg);
    }
}
