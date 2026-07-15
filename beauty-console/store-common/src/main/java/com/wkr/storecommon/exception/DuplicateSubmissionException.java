package com.wkr.storecommon.exception;

/**
 * 同一个幂等令牌被重复使用、正在处理中或已经失效时抛出的业务异常。
 */
public class DuplicateSubmissionException extends BusinessException {

    public DuplicateSubmissionException(String message) {
        super(message);
    }
}
