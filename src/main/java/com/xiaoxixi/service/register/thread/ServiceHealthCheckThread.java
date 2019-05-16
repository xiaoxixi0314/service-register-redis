package com.xiaoxixi.service.register.thread;

/**
 * 健康检查线程
 * 当服务完全启动的时候才向redis中注册服务信息
 * 使用url connection 向当前服务发起连接
 * status code 为 >=200 <400时说明服务已启动即可向redis中写入服务信息
 */
public class ServiceHealthCheckThread extends Thread {
}
