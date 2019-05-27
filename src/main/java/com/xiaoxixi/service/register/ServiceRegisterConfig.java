package com.xiaoxixi.service.register;

import com.xiaoxixi.service.register.constants.Constants;
import com.xiaoxixi.service.register.exception.PropertyException;
import com.xiaoxixi.service.register.redis.RedisService;
import com.xiaoxixi.service.register.server.ServerUri;
import com.xiaoxixi.service.register.thread.ServiceHealthCheckThread;
import com.xiaoxixi.service.register.util.ServerUriUtils;
import com.xiaoxixi.service.register.util.StringUtils;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 * register entry class
 */
@Setter
@Getter
public class ServiceRegisterConfig implements InitializingBean, DisposableBean{

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegisterConfig.class);

    /**
     * enable service register
     * false: skip register service
     * true: register service
     */
    @Value("${register.service.enabled}")
    private Boolean serviceRegisterEnabled;

    /**
     * redis host
     */
    @Value("${register.service.redis.host}")
    private String redisHost;

    /**
     * redis port，default 6379
     */
    @Value("${register.service.redis.port:6379}")
    private Integer redisPort;

    /**
     * redis password, default blank
     */
    @Value("${register.service.redis.pwd:}")
    private String redisPwd;

    /**
     * service prefix, for build redis service key
     */
    @Value("${register.service.prefix}")
    private String servicePrefix;

    /**
     * service name, for build redis key
     */
    @Value("${register.service.name}")
    private String serviceName;

    /**
     * service version, default v1
     */
    @Value("${register.service.version:v1}")
    private String serviceVersion;

    /**
     * service ttl
     * redis key expire time
     * unit: second
     */
    @Value("${register.service.ttl:2}")
    private Integer serviceTtl;

    /**
     * service bind ip
     * if find multiple server ip,
     * use this prefix to match ip
     */
    @Value("${register.service.bind.ip.prefix}")
    private String serviceBindIpPrefix;

    /**
     * service bind ip and port
     * if this value was set, bind ip and port use this value first
     */
    @Value("${register.service.bind.ip.port:}")
    private String serviceBindIp;

    /**
     * service context path
     */
    @Value("${register.service.context.path:}")
    private String serviceContextPath;

    /**
     * service discovery weight
     * default 100
     */
    @Value("${register.service.weight:100}")
    private Integer serviceWeight;

    /**
     *  health url suffix, build with service bind ip
     */
    @Value("${register.service.health.uri.suffix:}")
    private String serviceHealthUriSuffix;

    private ServiceConfig serviceConfig;

    @Getter
    private DiscoveryService discoveryService;

    @Getter
    private RedisService redisService;

    private ServiceHealthCheckThread healthCheckThread;

    @Override
    public void afterPropertiesSet(){
        if (!serviceRegisterEnabled) {
            LOGGER.warn("service register not enabled, skip to register service.");
            return;
        }
        // 初始化服务信息
        initServiceProperty();
        checkServiceProperty();
        redisService = new RedisService(serviceConfig);
        discoveryService = new DiscoveryService(redisService);
        RegisterService service = new RegisterService(redisService);
        healthCheckThread = new ServiceHealthCheckThread(service);
        healthCheckThread.setName("health-check-thread");
        healthCheckThread.setDaemon(true);
        healthCheckThread.start();
    }

    @Override
    public void destroy(){
        // 清除服务注册信息
        // 关闭服务守护进程
    }

    private void initServiceProperty(){
        String serviceBindUrl = buildServerBindUrl();
        serviceConfig = ServiceConfig.builder()
                .redisHost(redisHost)
                .redisPort(redisPort)
                .redisPwd(redisPwd)
                .serviceKey(buildServiceKey(serviceBindUrl))
                .serviceBindUrl(serviceBindUrl)
                .serviceHealthUrl(buildServiceHealthUrl(serviceBindUrl))
                .serviceTtl(serviceTtl)
                .version(serviceVersion)
                .weight(serviceWeight)
                .build();
    }

    private void checkServiceProperty(){
        if (serviceConfig == null) {
            throw new PropertyException();
        }
        if (StringUtils.isEmpty(redisHost) || redisPort== null) {
            throw new PropertyException("can't get redis config");
        }
    }

    private String buildServiceKey(String serviceBindUrl) {
        StringBuilder sb = new StringBuilder();
        return sb.append(Constants.REDIS_KEY_PREFIX)
                .append(":")
                .append(servicePrefix)
                .append(":")
                .append(serviceName)
                .append(":")
                .append(serviceVersion)
                .append(":")
                // service id
                .append(DigestUtils.md5DigestAsHex(serviceBindUrl.getBytes(StandardCharsets.UTF_8)))
                .toString();
    }

    private String buildServiceHealthUrl(String serviceBindUrl) {
        StringBuilder sb = new StringBuilder();
        return sb.append(serviceBindUrl).append("/")
                .append(serviceHealthUriSuffix)
                .toString();
    }

    private String buildServerBindUrl(){
        ServerUri uri = ServerUriUtils.getServerUri();
        String scheme = StringUtils.isEmpty(uri.getScheme()) ? "http" : uri.getScheme();
        String host = StringUtils.isEmpty(uri.getHost()) ? serviceBindIp : uri.getHost();
        String port = uri.getPort() == null ? "" : uri.getPort().toString();
        String contextPath = uri.getContextPath();
        StringBuilder sb = new StringBuilder();
        sb.append(scheme).append("://").append(host);
        if (StringUtils.isNotEmpty(port)) {
            sb.append(":").append(port);
        }
        if (StringUtils.isNotEmpty(contextPath)){
            sb.append("/").append(contextPath);
        }
        return sb.toString();
    }

}
