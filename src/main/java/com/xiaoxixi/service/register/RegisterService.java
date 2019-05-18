package com.xiaoxixi.service.register;

import com.alibaba.fastjson.JSON;
import com.xiaoxixi.service.register.constants.Constants;
import com.xiaoxixi.service.register.redis.RedisService;
import com.xiaoxixi.service.register.thread.ServiceGuardThread;
import lombok.Getter;

public class RegisterService {

    private RedisService redisService;

    @Getter
    private ServiceProperty serviceProperty;


    public RegisterService(RedisService redisService) {
        this.redisService = redisService;
        this.serviceProperty = redisService.getServiceProperty();
    }

    public boolean registerServiceToRedis(){
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
