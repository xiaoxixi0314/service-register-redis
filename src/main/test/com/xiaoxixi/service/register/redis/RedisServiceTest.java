package com.xiaoxixi.service.register.redis;

import com.xiaoxixi.service.register.ServiceConfig;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class RedisServiceTest {

    private static final Logger LOGGER =LoggerFactory.getLogger(RedisServiceTest.class);
    private RedisService redisService;

    public void init(){
        ServiceConfig serviceConfig =new ServiceConfig();
        try {
            serviceConfig.setRedisHost("192.168.18.102");
            serviceConfig.setRedisPort(6379);
            serviceConfig.setRedisPwd("911314");
            redisService = new RedisService(serviceConfig);
        } catch (Exception e) {
            LOGGER.error("init redis client error:", e);
        }
    }

    @Test
    public void getByKeyPrefix() {
        init();
        List<String> values = redisService.getByKeyPrefix("test");
        for (String value : values) {
            System.out.println(value);
        }
    }

}