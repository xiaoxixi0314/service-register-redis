package com.xiaoxixi.service.register.redis;

import com.alibaba.fastjson.JSON;
import com.xiaoxixi.service.register.ServiceProperty;
import com.xiaoxixi.service.register.exception.PropertyException;
import com.xiaoxixi.service.register.exception.ServiceRegisterException;
import com.xiaoxixi.service.register.util.StringUtils;
import lombok.Getter;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class RedisService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisService.class);

    @Getter
    private ServiceProperty serviceProperty;

    @Getter
    private Jedis jedis;

    public RedisService(ServiceProperty property) {

        this.serviceProperty = property;

        try {
            GenericObjectPoolConfig jedisPoolConfig = new GenericObjectPoolConfig();
            jedisPoolConfig.setMaxIdle(8);
            jedisPoolConfig.setMaxTotal(8);
            jedisPoolConfig.setMinIdle(0);
            jedisPoolConfig.setMaxWaitMillis(-1);

            JedisPool pool = new JedisPool(jedisPoolConfig,
                    property.getRedisHost(),
                    property.getRedisPort(),
                    0,
                    property.getRedisPwd());
            jedis = pool.getResource();
            if (Objects.isNull(jedis)) {
                LOGGER.error("can't connect to redis host:{}", JSON.toJSONString(property));
                throw new PropertyException("redis connect error");
            }
        } catch (Exception e) {
            LOGGER.error("redis init error:", e);
            throw new ServiceRegisterException("redis init error:", e);
        }

    }

    /**
     * use key prefix to find values
     * @param keyPrefix
     * @return
     */
    public List<String> getByKeyPrefix(String keyPrefix) {
        if (StringUtils.isEmpty(keyPrefix)) {
            return  null;
        }
        if (!keyPrefix.endsWith("*")) {
            keyPrefix = keyPrefix + "*";
        }
        List<String> values = new ArrayList<>();
        Set<String> keys = jedis.keys(keyPrefix);
        for (String key: keys) {
            values.add(jedis.get(key));
        }
        return values;
    }

    public void set(String key, String value) {
        jedis.set(key, value);
    }

    public void set(String key, String value, Integer ttl) {
        jedis.set(key, value);
        jedis.expire(key, ttl);
    }

    public String get(String key) {
        if (StringUtils.isEmpty(key)) {
            return "";
        }
        return jedis.get(key);
    }

    public void expire(String key, Integer ttl) {
        jedis.expire(key, ttl);
    }

}
