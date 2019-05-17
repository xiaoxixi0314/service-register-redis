package com.xiaoxixi.service.register.server;

import lombok.Getter;
import lombok.Setter;

/**
 * service container info
 * contains scheme, port, contextPath and host
 */
@Getter
@Setter
public class ServerUri {

    private String scheme;
    private Integer port;
    private String contextPath;
    private String host;

}
