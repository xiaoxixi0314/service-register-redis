package com.xiaoxixi.service.register.server;

import lombok.Getter;
import lombok.Setter;

/**
 * 服务端信息
 */
@Getter
@Setter
public class ServerUri {

    private String scheme;
    private Integer port;
    private String contextPath;
    private String host;

}
