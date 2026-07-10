package com.wkr.storecommon.exception;

/**
 * 服务层运行时异常基类，允许业务处理失败由全局异常处理器统一转换。
 */
public class ServiceException extends RuntimeException {

    public ServiceException(String msg) {
        super(msg);
    }

    public ServiceException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
