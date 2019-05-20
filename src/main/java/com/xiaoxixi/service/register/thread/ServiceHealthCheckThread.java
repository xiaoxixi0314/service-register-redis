package com.xiaoxixi.service.register.thread;

import com.xiaoxixi.service.register.RegisterService;
import com.xiaoxixi.service.register.ServiceProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * health check url
 * if the service started, register service info to redis
 */
public class ServiceHealthCheckThread extends Thread {

    private final static Logger LOGGER = LoggerFactory.getLogger(ServiceHealthCheckThread.class);

    private RegisterService registerService;

    private ServiceProperty serviceProperty;

    private String healthCheckUrl;

    private volatile boolean isBreak;

    private int retryCount;

    public ServiceHealthCheckThread(RegisterService service){
        this.registerService = service;
        this.serviceProperty = service.getServiceProperty();
        this.healthCheckUrl = serviceProperty.getServiceHealthUrl();
    }

    @Override
    public void run(){
        LOGGER.info("start check if the server started...");
        int interval = 2 * 1000;
        while (!isBreak && !interrupted()) {
            try {
                this.retryCount ++;
                LOGGER.info("service register retry count:{}", this.retryCount);
                URL url = new URL(this.healthCheckUrl);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.connect();
                int status = connection.getResponseCode();
                if (status >= 200 && status < 400) {
                    LOGGER.info("server has been started, start register service to redis...");
                    this.isBreak = registerService.registerServiceToRedis();
                    LOGGER.info("register service to redis success");
                }
                if (this.retryCount > 100) {
                    this.isBreak = true;
                    LOGGER.error("retry count over 100 times, skip to register service.");
                }
                Thread.sleep(interval);
            } catch (InterruptedException ie) {
                LOGGER.error("service health check thread interrupted!", ie);
            } catch (IOException ioe) {
                LOGGER.warn("service health check IO error:{}", ioe.getCause());
            }
        }
    }
}
