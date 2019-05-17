package com.xiaoxixi.service.register.thread;

import com.alibaba.fastjson.JSON;
import com.xiaoxixi.service.register.ServiceProperty;
import com.xiaoxixi.service.register.redis.RedisService;
import com.xiaoxixi.service.register.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * service guard thread
 * report to redis the service is alive
 */
public class ServiceGuardThread extends Thread{

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceGuardThread.class);

    private RedisService redisService;

    private ServiceProperty serviceProperty;

    private volatile boolean isBreak;

    public ServiceGuardThread(RedisService redisService) {
        this.redisService = redisService;
        this.serviceProperty = redisService.getServiceProperty();
    }

    @Override
    public void run(){
        int interval = serviceProperty.getServiceTtl() / 2;
        String serviceKey = serviceProperty.getServiceKey();
        while (!isBreak && !interrupted()) {
            try {
                if (StringUtils.isEmpty(redisService.get(serviceKey))) {
                    redisService.set(serviceKey,
                            JSON.toJSONString(serviceProperty),
                            serviceProperty.getServiceTtl());
                } else {
                    redisService.expire(serviceProperty.getServiceKey(),
                            serviceProperty.getServiceTtl());
                }
                Thread.sleep(interval);
            } catch (InterruptedException ie) {
                LOGGER.error("service guard thread interrupted.", ie);
            } catch (Exception e) {
                LOGGER.error("service guard thread unknown error", e);
                this.isBreak = true;
            }
        }
    }
}
