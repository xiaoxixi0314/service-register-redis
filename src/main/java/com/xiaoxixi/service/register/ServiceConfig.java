package com.xiaoxixi.service.register;

import com.xiaoxixi.service.register.util.StringUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 * 注册入口文件
 */
@Setter
@Getter
public class ServiceConfig implements DisposableBean, InitializingBean, ApplicationListener<ApplicationEvent> {

    /**
     * 是否启用服务注册
     */
    @Value("${register.service.enabled}")
    private Boolean serviceRegisterEnabled;

    /**
     * redis host
     */
    @Value("${register.service.redis.host}")
    private String redisHost;

    /**
     * redis端口，默认6379
     */
    @Value("${register.service.redis.port:6379}")
    private Integer redisPort;

    /**
     * redis password
     */
    @Value("${register.service.redis.pwd:}")
    private String redisPwd;

    /**
     * 服务前缀
     */
    @Value("${register.service.prefix}")
    private String servicePrefix;

    /**
     * 服务名，和服务前缀组成redis的key
     * 服务发现时按照key查找该服务是否存在
     */
    @Value("${register.service.name}")
    private String serviceName;

    /**
     * 服务版本默认v1
     */
    @Value("${register.service.version:v1}")
    private String serviceVersion;

    /**
     * 服务存活时间，对应redis key的过期时间
     */
    @Value("${register.service.ttl:2}")
    private Integer serviceTtl;

    /**
     * 服务绑定ip前缀，
     * 防止多网卡时能匹配到一个合适的ip
     */
    @Value("${register.service.bind.ip.prefix}")
    private String serviceBindIpPrefix;

    /**
     * 服务绑定的ip+端口，默认空白
     * 不设置该属性的时候，会根据绑定url前缀自动获取tomcat容器的ip和端口
     */
    @Value("${register.service.bind.ip:}")
    private String serviceBindIp;

    /**
     * service context path
     */
    @Value("${register.service.context.path}")
    private String serviceContextPath;

    /**
     * 服务健康检查后缀，与绑定ip组成健康检查url
     * 只有当当前服务完全启动时才会注册服务到redis中
     */
    @Value("${register.service.health.uri.suffix}")
    private String serviceHealthUriSuffix;

    private ServiceProperty serviceProperty;

    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void afterPropertiesSet(){
        // 初始化服务信息
        initServiceProperty();
        // 开启健康检查进程
        // 开启服务守护进程
    }

    @Override
    public void destroy(){
        // 清除服务注册信息
        // 关闭服务守护进程
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {

    }

    private void initServiceProperty(){
        serviceProperty = ServiceProperty.builder()
                .redisHost(redisHost)
                .redisPort(redisPort)
                .redisPwd(redisPwd)
                .serviceKey(buildServiceKey())
                .serviceHealthUrl(buildServiceHealthUrl())
                .serviceTtl(serviceTtl)
                .version(serviceVersion)
                .build();
    }

    private String findServiceBindIp() {
        if(StringUtils.isEmpty(serviceBindIp)) {

        }
        return "";
    }

    private String buildServiceKey() {
        StringBuilder sb = new StringBuilder();
        if (!servicePrefix.endsWith(":")) {
            servicePrefix = serviceBindIpPrefix + ":";
        }
        return sb.append(servicePrefix)
                .append(serviceName)
                .append(":")
                .append(serviceVersion)
                .append(":")
                // service id
                .append(DigestUtils.md5DigestAsHex(serviceBindIp.getBytes(StandardCharsets.UTF_8)))
                .toString();
    }

    private String buildServiceHealthUrl() {
        StringBuilder sb = new StringBuilder();
        if (!serviceBindIp.endsWith("/")) {
            serviceBindIp = serviceBindIp + "/";
        }
        return sb.append(serviceBindIp)
                .append(serviceHealthUriSuffix)
                .toString();
    }

}
