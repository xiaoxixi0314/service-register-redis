package com.xiaoxixi.service.register.thread;

import com.xiaoxixi.service.register.RegisterService;
import com.xiaoxixi.service.register.ServiceProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 健康检查线程
 * 当服务完全启动的时候才向redis中注册服务信息
 * 使用url connection 向当前服务发起连接
 * status code 为 >=200 <400时说明服务已启动即可向redis中写入服务信息
 */
public class ServiceHealthCheckThread extends Thread {

    private final static Logger LOGGER = LoggerFactory.getLogger(ServiceHealthCheckThread.class);

    private RegisterService registerService;

    private ServiceProperty serviceProperty;

    private String healthCheckUrl;

    private volatile boolean isBreak;

    private int retryCount;

    public ServiceHealthCheckThread(RegisterService service,
                                    ServiceProperty property){
        this.registerService = service;
        this.serviceProperty = property;
        this.healthCheckUrl = property.getServiceHealthUrl();
    }

    @Override
    public void run(){
        LOGGER.info("start check if the server started...");
        int interval = 2 * 1000;
        while (!isBreak && interrupted()) {
            try {
                this.retryCount ++;
                LOGGER.info("service register retry count:{}", this.retryCount);
                URL url = new URL(this.healthCheckUrl);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.connect();
                int status = connection.getResponseCode();
                if (status >= 200 && status < 400) {
                    LOGGER.info("server has been started, start register service to redis...");
                    this.isBreak = registerService.registerServiceToRedis(serviceProperty);
                    LOGGER.info("register service to redis success");
                }
                if (this.retryCount > 100) {
                    this.isBreak = true;
                    LOGGER.error("retry count over 100 times, skip to register service.");
                }
                Thread.sleep(interval);
            } catch (InterruptedException ie) {
                LOGGER.error("service health check thread interrupted!", ie);
            } catch (Exception e) {
                this.isBreak = true;
                LOGGER.error("service health check unknown error:", e);
            }
        }

    }
}
