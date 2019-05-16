package com.xiaoxixi.service.register.exception;

public class PropertyException extends RuntimeException {

    public PropertyException(){
        super("无法获取相关配置值");
    }

    public PropertyException(String message) {
        super(message);
    }

    public PropertyException(String message, Throwable cause) {
        super(message, cause);
    }
}
