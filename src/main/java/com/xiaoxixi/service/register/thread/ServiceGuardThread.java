package com.xiaoxixi.service.register.thread;

import com.alibaba.fastjson.JSON;
import com.xiaoxixi.service.register.ServiceConfig;
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

    private ServiceConfig serviceConfig;

    private volatile boolean isBreak;

    public ServiceGuardThread(RedisService redisService) {
        this.redisService = redisService;
        this.serviceConfig = redisService.getServiceConfig();
    }

    @Override
    public void run(){
        LOGGER.info("start to guard service....");
        int interval = serviceConfig.getServiceTtl() / 2;
        String serviceKey = serviceConfig.getServiceKey();
        while (!isBreak && !interrupted()) {
            try {
                if (StringUtils.isEmpty(redisService.get(serviceKey))) {
                    LOGGER.warn("the service was dead, try to register..");
                    redisService.set(serviceKey,
                            JSON.toJSONString(serviceConfig),
                            serviceConfig.getServiceTtl());
                } else {
                    LOGGER.debug("refresh service ttl...");
                    redisService.expire(serviceConfig.getServiceKey(),
                            serviceConfig.getServiceTtl());
                }
                Thread.sleep(interval*1000);
            } catch (InterruptedException ie) {
                LOGGER.error("service guard thread interrupted.", ie);
            }
        }
    }
}
