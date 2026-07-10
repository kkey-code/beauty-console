package com.wkr.storecommon.exception;

/**
 * 系统处理异常，用于表示数据库更新失败等非正常业务结果。
 */
public class SystemException extends RuntimeException {

    public SystemException() {
        super();
    }

    public SystemException(String message) {
        super(message);
    }

    public SystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public SystemException(Throwable cause) {
        super(cause);
    }
}