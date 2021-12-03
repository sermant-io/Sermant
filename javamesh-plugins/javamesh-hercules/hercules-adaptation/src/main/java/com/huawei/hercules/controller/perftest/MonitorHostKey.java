/*
 * Copyright (C) Huawei Technologies Co., Ltd. $YEAR$-$YEAR$. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
