package com.xiaoxixi.service.register;

import lombok.*;

/**
 * 服务属性
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderClassName = "ParamBuilder")
public class ServiceProperty {

    private String redisHost;

    private Integer redisPort;

    private String redisPwd;

    /**
     * service prefix + service version + service name + service id
     */
    private String serviceKey;

    /**
     * service bind ip
     */
    private String serviceBindUrl;

    /**
     * service ip + service suffix
     */
    private String serviceHealthUrl;

    private String version;

    private Integer serviceTtl;

}
