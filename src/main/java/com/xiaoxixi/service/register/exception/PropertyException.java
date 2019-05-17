package com.xiaoxixi.service.register.exception;

public class PropertyException extends RuntimeException {

    public PropertyException(){
        super("can't get config value");
    }

    public PropertyException(String message) {
        super(message);
    }

    public PropertyException(String message, Throwable cause) {
        super(message, cause);
    }
}
