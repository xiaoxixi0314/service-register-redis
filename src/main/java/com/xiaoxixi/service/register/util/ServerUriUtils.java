package com.xiaoxixi.service.register.util;

import com.xiaoxixi.service.register.server.*;

public class ServerUriUtils {

    private static final UriExtractor TOMCAT_URI_EXTRACTOR = TomcatUriExtractor.newInstance();

    /**
     * get service scheme, host, port, context path
     * from server container
     * @return
     */
    public static ServerUri getServerUri() {
        return TOMCAT_URI_EXTRACTOR.extract();
    }

}
