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

public class RedisService {

    private JedisPoolConfig jedisPoolConfig;

    private JedisConnectionFactory jedisConnectionFactory;

    @Getter
    private StringRedisTemplate stringRedisTemplate;

    public void init(ServiceProperty property) {
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
     * 根据key前缀获取redis中的值列表
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

    public String get(String key) {
        if (StringUtils.isEmpty(key)) {
            return "";
        }
        return stringRedisTemplate.opsForValue().get(key);
    }
}