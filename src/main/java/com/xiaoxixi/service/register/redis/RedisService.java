package com.xiaoxixi.service.register.redis;

import com.xiaoxixi.service.register.ServiceProperty;
import com.xiaoxixi.service.register.util.StringUtils;
import lombok.Getter;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class RedisService {

    private JedisPoolConfig jedisPoolConfig;

    private JedisConnectionFactory jedisConnectionFactory;

    @Getter
    private StringRedisTemplate stringRedisTemplate;

    @Getter
    private ServiceProperty serviceProperty;

    public RedisService(ServiceProperty property) {

        this.serviceProperty = property;

        jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(8);
        jedisPoolConfig.setMaxTotal(8);
        jedisPoolConfig.setMinIdle(0);
        jedisPoolConfig.setMaxWaitMillis(-1);

        jedisConnectionFactory.setPoolConfig(jedisPoolConfig);
        jedisConnectionFactory.setHostName(property.getRedisHost());
        jedisConnectionFactory.setPort(property.getRedisPort());
        if (!StringUtils.isEmpty(property.getRedisPwd())) {
            jedisConnectionFactory.setPassword(property.getRedisPwd());
        }
        jedisConnectionFactory.setDatabase(0);

        stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(jedisConnectionFactory);
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
        Set<String> keys = stringRedisTemplate.keys(keyPrefix);
        for (String key: keys) {
            values.add(stringRedisTemplate.opsForValue().get(key));
        }
        return values;
    }

    public void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, String value, Integer ttl) {
        stringRedisTemplate.opsForValue().set(key, value, ttl, TimeUnit.SECONDS);
    }

    public String get(String key) {
        if (StringUtils.isEmpty(key)) {
            return "";
        }
        return stringRedisTemplate.opsForValue().get(key);
    }

    public void expire(String key, Integer ttl) {
        stringRedisTemplate.expire(key, ttl, TimeUnit.SECONDS);
    }

}
