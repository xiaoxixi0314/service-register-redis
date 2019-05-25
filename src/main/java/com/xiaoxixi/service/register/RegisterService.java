package com.xiaoxixi.service.register;

import com.alibaba.fastjson.JSON;
import com.xiaoxixi.service.register.redis.RedisService;
import com.xiaoxixi.service.register.thread.ServiceGuardThread;
import lombok.Getter;

public class RegisterService {

    @Getter
    private RedisService redisService;

    @Getter
    private ServiceConfig serviceConfig;


    public RegisterService(RedisService redisService) {
        this.redisService = redisService;
        this.serviceConfig = redisService.getServiceConfig();
    }

    public boolean registerServiceToRedis(){
        ServiceProperty serviceProperty = ServiceProperty.builder()
                .serviceBindUrl(serviceConfig.getServiceBindUrl())
                .serviceKey(serviceConfig.getServiceKey())
                .serviceTtl(serviceConfig.getServiceTtl())
                .serviceHealthUrl(serviceConfig.getServiceHealthUrl())
                .weight(serviceConfig.getWeight())
                .version(serviceConfig.getVersion())
                .build();
        redisService.set(serviceProperty.getServiceKey(),
                JSON.toJSONString(serviceProperty),
                serviceProperty.getServiceTtl());
        ServiceGuardThread guardThread = new ServiceGuardThread(redisService);
        guardThread.setName("service-guard-thread");
        guardThread.setDaemon(true);
        guardThread.start();
        return true;
    }
}
