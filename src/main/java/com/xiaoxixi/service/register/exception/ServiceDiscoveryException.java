package com.xiaoxixi.service.register.exception;

public class ServiceDiscoveryException extends RuntimeException {

    public ServiceDiscoveryException(){
        super("all services are dead.");
    }

    public ServiceDiscoveryException(String message) {
        super(message);
    }

    public ServiceDiscoveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
