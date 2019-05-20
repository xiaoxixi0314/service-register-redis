package com.xiaoxixi.service.register.redis;

import com.xiaoxixi.service.register.ServiceProperty;
import com.xiaoxixi.service.register.util.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


public class RedisServiceTest {

    private static final Logger LOGGER =LoggerFactory.getLogger(RedisServiceTest.class);
    private RedisService redisService;

    public void init(){
        ServiceProperty serviceProperty =new ServiceProperty();
        try {
            serviceProperty.setRedisHost("192.168.18.102");
            serviceProperty.setRedisPort(6379);
            serviceProperty.setRedisPwd("911314");
            redisService = new RedisService(serviceProperty);
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