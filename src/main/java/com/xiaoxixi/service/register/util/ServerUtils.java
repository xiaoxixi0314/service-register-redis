package com.xiaoxixi.service.register.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 获取服务器ip和应用服务器端口信息
 */
public class ServerUtils {

    private ServerUtils() {
        // hide
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerUtils.class);

    /**
     * 端口80
     */
    private static final String PORT_80 = "80";

    /**
     * http shcema
     */
    private static final String HTTP_SCHEMA = "http://";

    /**
     * https shcema
     */
    private static final String HTTPS_SCHEMA = "https://";

    /**
     * 匹配10.0.0.0 - 10.255.255.255的网段
     */
    private static final String IP_PATTERN_10 = "^(\\D)*10(\\.([2][0-4]\\d|[2][5][0-5]|[01]?\\d?\\d)){3}";

    /**
     * 匹配192.168.0.0 - 192.168.255.255的网段
     */
    private static final String IP_PATTERN_192 = "192\\.168(\\.([2][0-4]\\d|[2][5][0-5]|[01]?\\d?\\d)){2}";

    /**
     * 匹配172.16.0.0 - 172.31.255.255的网段
     */
    private static final String IP_PATTERN_172 = "172\\.([1][6-9]|[2]\\d|3[01])(\\.([2][0-4]\\d|[2][5][0-5]|[01]?\\d?\\d)){2}";

    /**
     * 去除url的Schema
     * @param url
     * @return
     */
    public static String handleSchema(String url) {
        String urlHandled = "";
        if (!StringUtils.isEmpty(url)) {
            urlHandled = url.toLowerCase();
            if (urlHandled.startsWith(HTTP_SCHEMA)) {
                urlHandled = urlHandled.substring(HTTP_SCHEMA.length());
            }
            if (urlHandled.startsWith(HTTPS_SCHEMA)) {
                urlHandled = urlHandled.substring(HTTPS_SCHEMA.length());
            }
        }
        return urlHandled;
    }


    /**
     * 组合服务url地址
     */
    public static String buildServiceUrl(String scheme, String bindAddress, String port, String contextPath, boolean isIp) throws Exception {

        String serviceUrl = scheme + "://" + handleSchema(bindAddress);
        // 如果是内网地址没经过代理，则获取tomcat的端口
        // 如果是经过类似代理转发的服务，则直接获取bindAddress，端口由bindAddress指定，因为代理的端口很可能和tomcat的端口不一致
        if (!StringUtils.isEmpty(port) && isIp) {
            serviceUrl = serviceUrl + ":" + port;
        }
        if (!StringUtils.isEmpty(contextPath)){
            serviceUrl = serviceUrl + "/" + contextPath;
        }
        return serviceUrl;
    }

    /**
     * 组合健康检查url地址
     */
    public static String buildHealthUrl(String scheme, String bindAddress, String port, String contextPath, String healthUrlSuffix) throws Exception {
        String healthUrl = scheme + "://" + handleSchema(bindAddress);
        if (!StringUtils.isEmpty(port)) {
            healthUrl = healthUrl + ":" + port;
        }
        if (!StringUtils.isEmpty(contextPath)){
            healthUrl = healthUrl + "/" + contextPath;
        }
        if (!StringUtils.isEmpty(healthUrlSuffix) && !healthUrlSuffix.startsWith("/")){
            healthUrlSuffix = "/" + healthUrlSuffix;
        }
        healthUrl = healthUrl + healthUrlSuffix;
        return healthUrl;
    }

    /**
     * 获取服务器的ip地址
     * 优先获取网段顺序为
     * <pre>
     *     * 10.0.0.0 - 10.255.255.255
     *     * 192.168.0.0 - 192.168.255.255
     *     * 172.16.0.0 - 172.31.255.255
     * </pre>
     *
     * @param bindIpPrefix ip匹配前缀，如果ip匹配前缀为空，则按上述网段规则获取，
     * 如果不为空，则返回符合匹配规则的ip
     */
    public static List<String> getServerIP(String bindIpPrefix) {
        List<InetAddress> addresses = getAllIpAddress();
        List<String> ip10 = new ArrayList<>();
        List<String> ip192 = new ArrayList<>();
        List<String> ip172 = new ArrayList<>();
        List<String> ipPrefix = new ArrayList<>();
        for (InetAddress address : addresses) {
            if (address.isAnyLocalAddress() || address.isLoopbackAddress()
                    || address.isMulticastAddress()) {
                continue;
            }
            if (address.getAddress().length == 4) {
                String ip = address.getHostAddress();
                if (matchIP(ip, IP_PATTERN_10)) {
                    ip10.add(ip);
                }
                if (matchIP(ip, IP_PATTERN_192)) {
                    ip192.add(ip);
                }
                if (matchIP(ip, IP_PATTERN_172)) {
                    ip172.add(ip);
                }
                if (!StringUtils.isEmpty(bindIpPrefix) && ip.startsWith(bindIpPrefix)) {
                    ipPrefix.add(ip);
                }
            }
        }
        if (!ipPrefix.isEmpty()) {
            return ipPrefix;
        }
        if (!ip10.isEmpty()) {
            return ip10;
        }
        if (!ip192.isEmpty()) {
            return ip192;
        }
        if (!ip172.isEmpty()) {
            return ip172;
        }
        return ipPrefix;
    }

    public static List<InetAddress> getIpAddress() {
        Set<InetAddress> addresses = new LinkedHashSet<>(8);
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ifc = interfaces.nextElement();
                if (ifc.isUp()) {
                    Enumeration<InetAddress> addrs = ifc.getInetAddresses();
                    while (addrs.hasMoreElements()) {
                        InetAddress address = addrs.nextElement();
                        if (address instanceof Inet4Address
                                && !address.isLoopbackAddress()
                                && !address.isMulticastAddress()
                                && !address.isAnyLocalAddress()) {
                            addresses.add(address);
                        }
                    }
                }
            }
            // the last choice
            if (addresses.isEmpty()) {
                addresses.add(InetAddress.getLocalHost());
            }
        } catch (Exception e) {
            LOGGER.warn("Unable to find non-loopback address", e);
        }

        return new ArrayList<>(addresses);
    }

    public static List<InetAddress> getAllIpAddress() {

        return getIpAddress();

    }

    public static List<String> getIpv4Address() {
        List<InetAddress> addrList = getAllIpAddress();

        List<String> list = new ArrayList<>(10);
        for (InetAddress addr : addrList) {
            if (addr.isAnyLocalAddress()
                    || addr.isLoopbackAddress()
                    || addr.isMulticastAddress()) {
                continue;
            }

            list.add(addr.getHostAddress());
        }

        return list;
    }

    /**
     * 判断ip地址是否属于某个网段
     */
    private static boolean matchIP(String ip, String regexp) {
        Pattern p = Pattern.compile(regexp);
        Matcher matcher = p.matcher(ip);
        return matcher.find();
    }
}
