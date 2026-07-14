package com.wkr.storecommon.exception;

/**
 * 状态非法转换异常
 */
public class IllegalStatusTransitionException extends RuntimeException{

    public IllegalStatusTransitionException(String msg){
        super(msg);
    }

    public IllegalStatusTransitionException(){
        super();
    }


}
