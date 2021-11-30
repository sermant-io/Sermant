/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.gray.feign.context;

/**
 * 当前实例信息
 *
 * @author lilai
 * @since 2021-11-03
 */
public class CurrentInstance {

    private static CurrentInstance INSTANCE = null;

    /**
     * 所属服务名称
     */
    private String appName;

    /**
     * 当前实例ip地址
     */
    private String ip;

    /**
     * 当前实例端口
     */
    private int port;

    private CurrentInstance() {
    }

    private CurrentInstance(String appName, String ip, int port) {
        this.appName = appName;
        this.ip = ip;
        this.port = port;
    }

    /**
     * 获取实例，仅在启动的时候调用，不存在线程安全问题
     *
     */
    public static void newInstance(String appName, String ip, int port) {
        if (INSTANCE == null) {
            INSTANCE = new CurrentInstance(appName, ip, port);
        }
    }

    public static CurrentInstance getInstance() {
        return INSTANCE;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
