/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.controller.perftest;

/**
 * 功能描述：监控主机中key定义
 *
 * @author z30009938
 * @since 2021-11-22
 */
public enum MonitorHostKey {
    ID("id", "id"),
    HOST("host", "host"),
    IP("ip", "ip"),
    IS_MONITOR_JVM("is_monitor_jvm", "monitor_jvm"),
    JVM_TYPE("jvm_type", "jvm_type"),
    TEST_ID("test_id", "test_id");

    /**
     * 前端展示字段
     */
    private final String showKey;

    /**
     * 后端服务器响应字段
     */
    private final String serverKey;

    MonitorHostKey(String showName, String serverName) {
        this.showKey = showName;
        this.serverKey = serverName;
    }

    public String getShowKey() {
        return showKey;
    }

    public String getServerKey() {
        return serverKey;
    }

    /**
     * 根据serverKey获取前端展示key
     *
     * @param serverKey serverKey
     * @return 前端展示key
     */
    public static String getShowKey(String serverKey) {
        for (MonitorHostKey monitorHostKey : values()) {
            if (monitorHostKey.serverKey.equals(serverKey)) {
                return monitorHostKey.showKey;
            }
        }
        return "";
    }

    /**
     * 根据前端展示key获取server的key
     *
     * @param showKey 前端展示key
     * @return server的key
     */
    public static String getServerKey(String showKey) {
        for (MonitorHostKey monitorHostKey : values()) {
            if (monitorHostKey.showKey.equals(showKey)) {
                return monitorHostKey.serverKey;
            }
        }
        return "";
    }
}
