package com.xiaoxixi.service.register.exception;

public class RegisterServiceException extends RuntimeException {

    public RegisterServiceException(){
        super("服务注册异常");
    }

    public RegisterServiceException(String message) {
        super(message);
    }

    public RegisterServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
