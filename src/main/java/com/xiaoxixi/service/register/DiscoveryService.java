package com.xiaoxixi.service.register;

import com.alibaba.fastjson.JSON;
import com.xiaoxixi.service.register.constants.Constants;
import com.xiaoxixi.service.register.exception.ServiceDiscoveryException;
import com.xiaoxixi.service.register.redis.RedisService;
import com.xiaoxixi.service.register.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class DiscoveryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryService.class);

    private RedisService redisService;

    public DiscoveryService(RedisService redisService){
        this.redisService = redisService;
    }

    public List<ServiceProperty> discoveryAllServices(){
        List<String> servicesJson = redisService.getByKeyPrefix(Constants.REDIS_KEY_PREFIX);
        List<ServiceProperty> services = new ArrayList<>();
        if (!CollectionUtils.isEmpty(servicesJson)) {
            for (String serviceJson : servicesJson) {
                ServiceProperty service = JSON.parseObject(serviceJson, ServiceProperty.class);
                services.add(service);
            }
        }
        return services;
    }

    /**
     * discovery register service by service name
     * @return
     */
    public List<ServiceProperty> discoveryServices(String servicePrefix, String serviceName) {
        String serviceKey;
        if (StringUtils.isAnyEmpty(servicePrefix, serviceName)) {
            serviceKey = Constants.REDIS_KEY_PREFIX + ":" + redisService.getServiceConfig().getServiceKey();
        } else {
            serviceKey = Constants.REDIS_KEY_PREFIX + ":" +servicePrefix + ":" + serviceName;
        }

        List<String> servicesJson = redisService.getByKeyPrefix(serviceKey);
        List<ServiceProperty> services = new ArrayList<>();
        if (!CollectionUtils.isEmpty(servicesJson)) {
            for (String serviceJson : servicesJson) {
                ServiceProperty service = JSON.parseObject(serviceJson, ServiceProperty.class);
                services.add(service);
            }
        }
        return services;
    }

    /**
     * discovery service by weight
     * @param servicePrefix
     * @param serviceName
     * @return
     */
    public ServiceProperty discoveryService(String servicePrefix, String serviceName) {
        List<ServiceProperty> services = discoveryServices(servicePrefix, serviceName);
        if (CollectionUtils.isEmpty(services)) {
            LOGGER.error("can't find service by service prefix:{},service name:{}", servicePrefix, serviceName);
            throw new ServiceDiscoveryException("can't find service by service prefix:"+servicePrefix+", service name:"+serviceName);
        }
        Integer sumWeight = services.stream().mapToInt(service -> service.getWeight()).sum();
        if (sumWeight <= 0) {
            LOGGER.error("service weight error, service prefix:{}, service name:{}, sum weight:{}", servicePrefix, serviceName, sumWeight);
            throw new ServiceDiscoveryException("service weight error");
        }
        Random random = new Random();
        int randomWeight = random.nextInt(sumWeight);
        int tmp = 0;
        for (ServiceProperty service :  services) {
            if (tmp <= randomWeight && randomWeight < tmp + service.getWeight()) {
                return service;
            }
            tmp += service.getWeight();
        }
        return services.get(0);
    }

    public Boolean changeServcieWeight(String serviceKey, Integer weight) {
        String serviceJson = redisService.get(serviceKey);
        if (StringUtils.isEmpty(serviceJson)) {
            throw new ServiceDiscoveryException("can't find this service, check service key");
        }
        ServiceProperty service = JSON.parseObject(serviceJson, ServiceProperty.class);
        if (Objects.isNull(service)) {
            throw new ServiceDiscoveryException("can't find this service, check service key");
        }
        service.setWeight(weight);
        redisService.setnx(serviceKey, JSON.toJSONString(service), service.getServiceTtl());
        return true;
    }
}
