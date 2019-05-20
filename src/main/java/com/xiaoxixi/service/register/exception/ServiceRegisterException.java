package com.xiaoxixi.service.register.exception;

public class ServiceRegisterException extends RuntimeException {

    public ServiceRegisterException(){
        super("service register error.");
    }

    public ServiceRegisterException(String message) {
        super(message);
    }

    public ServiceRegisterException(String message, Throwable cause) {
        super(message, cause);
    }
}
