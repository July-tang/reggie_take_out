package com.july.reggie.exception;

/**
 * 自定义业务异常
 *
 * @author july
 */
public class CustomException extends RuntimeException {

    public CustomException(String message){
        super(message);
    }
}
