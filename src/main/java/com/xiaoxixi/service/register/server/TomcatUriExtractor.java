package com.xiaoxixi.service.register.server;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.Query;
import javax.management.QueryExp;
import java.lang.management.ManagementFactory;
import java.util.Set;

public class TomcatUriExtractor extends AbstractUriExtractor {

    /**
     * 协议
     */
    private static final String PROTOCOL = "protocol";

    /**
     * 协议 HTTP/1.1
     */
    private static final String HTTP11 = "HTTP/1.1";

    /**
     * 协议 Http11
     */
    private static final String HTTP11_OTHER = "Http11";

    /**
     * 应用服务容器
     */
    private static final String CONNECTOR = "*:type=Connector,*";

    /**
     * scheme
     */
    private static final String SCHEME = "scheme";

    /**
     * 端口
     */
    private static final String PORT = "port";
    /**
     * 绑定地址
     */
    private static final String ADDR = "address";

    private TomcatUriExtractor() {}

    public static TomcatUriExtractor newInstance() {
        return new TomcatUriExtractor();
    }


    private static Set<ObjectName> getConnectors(MBeanServer mbs) throws  Exception {
        QueryExp subQueryHttp = Query.match(Query.attr(PROTOCOL), Query.value(HTTP11));
        QueryExp subQueryHttpOther = Query.anySubString(Query.attr(PROTOCOL), Query.value(HTTP11_OTHER));
        QueryExp query = Query.or(subQueryHttp, subQueryHttpOther);
        return mbs.queryNames(new ObjectName(CONNECTOR), query);
    }

    @Override
    public ServerUri extract() {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        try {
            ServerUri uri = new ServerUri();
            Set<ObjectName> connectors = getConnectors(mbs);

            // 默认为 http
            String scheme = null;
            for (ObjectName obj : connectors) {
                scheme = mbs.getAttribute(obj, SCHEME).toString();
                if (scheme != null) {
                    break;
                }
            }
            if (scheme != null) {
                LOGGER.info("use tomcat scheme: {}", scheme);
                uri.setScheme(scheme);
            }

            String host = null;
            for (ObjectName obj : connectors) {
                host = obj.getKeyProperty(ADDR);
                if (host != null) {
                    break;
                }
            }
            if (host != null) {
                LOGGER.info("use tomcat  host:{}", host);
                uri.setHost(host);
            }

            String port = null;
            for (ObjectName obj : connectors) {
                port = obj.getKeyProperty(PORT);
                if (port != null) {
                    break;
                }
            }
            if (port != null) {
                LOGGER.info("use tomcat port:{}", port);
                uri.setPort(Integer.valueOf(port));
            }

            return uri;
        } catch (Exception e) {
            LOGGER.warn("failed to export server uri from tomcat");
        }

        return null;
    }
}
