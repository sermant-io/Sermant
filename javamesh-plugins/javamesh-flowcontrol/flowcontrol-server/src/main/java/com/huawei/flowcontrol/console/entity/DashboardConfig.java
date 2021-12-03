/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.flowcontrol.console.entity;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 此处部分引用alibaba/Sentinel开源社区代码，诚挚感谢alibaba/Sentinel开源团队的慷慨贡献
 * <p>Dashboard local config support.</p>
 * <p>
 * Dashboard supports configuration loading by several ways by order:<br>
 * 1. System.properties<br>
 * 2. Env
 * </p>
 * huawei update log: 添加注解，读取配置文件
 *
 * @author jason
 * @since 1.5.0
 */
@Component
public class DashboardConfig {
    public static final int DEFAULT_MACHINE_HEALTHY_TIMEOUT_MS = 60_000;

    /**
     * huawei update log: 心跳健康时间读取配置文件
     */
    private static int healthyTimeout;

    @Value("${sentinel.dashboard.unhealthyMachineMillis}")
    public void setHealthyTimeout(int timeout) {
        healthyTimeout = timeout;
    }

    /**
     * Hide application name in sidebar when it has no healthy machines after specific period in millisecond.
     */
    public static final String CONFIG_HIDE_APP_NO_MACHINE_MILLIS = "sentinel.dashboard.app.hideAppNoMachineMillis";

    /**
     * Remove application when it has no healthy machines after specific period in millisecond.
     */
    public static final String CONFIG_REMOVE_APP_NO_MACHINE_MILLIS = "sentinel.dashboard.removeAppNoMachineMillis";

    /**
     * Timeout
     */
    public static final String CONFIG_UNHEALTHY_MACHINE_MILLIS = "sentinel.dashboard.unhealthyMachineMillis";

    /**
     * Auto remove unhealthy machine after specific period in millisecond.
     */
    public static final String CONFIG_AUTO_REMOVE_MACHINE_MILLIS = "sentinel.dashboard.autoRemoveMachineMillis";

    private static final ConcurrentMap<String, Object> CACHE_MAP = new ConcurrentHashMap<>();

    @NonNull
    private static String getConfig(String name) {
        // env
        String val = System.getenv(name);
        if (StringUtils.isNotEmpty(val)) {
            return val;
        }
        // properties
        val = System.getProperty(name);
        if (StringUtils.isNotEmpty(val)) {
            return val;
        }
        return "";
    }

    protected static int getConfigInt(String name, int defaultVal, int minVal) {
        if (CACHE_MAP.containsKey(name)) {
            return (int) CACHE_MAP.get(name);
        }
        int val = NumberUtils.toInt(getConfig(name));
        if (val == 0) {
            val = defaultVal;
        } else if (val < minVal) {
            val = minVal;
        }
        CACHE_MAP.put(name, val);
        return val;
    }

    public static int getHideAppNoMachineMillis() {
        return getConfigInt(CONFIG_HIDE_APP_NO_MACHINE_MILLIS, 0, 60000);
    }

    public static int getRemoveAppNoMachineMillis() {
        return getConfigInt(CONFIG_REMOVE_APP_NO_MACHINE_MILLIS, 0, 120000);
    }

    public static int getAutoRemoveMachineMillis() {
        return getConfigInt(CONFIG_AUTO_REMOVE_MACHINE_MILLIS, 0, 300000);
    }

    public static int getUnhealthyMachineMillis() {
        // huawei update change log: 心跳健康时间读取配置文件，配置为0，取默认值
        if (healthyTimeout != 0) {
            return healthyTimeout;
        } else {
            return getConfigInt(CONFIG_UNHEALTHY_MACHINE_MILLIS, DEFAULT_MACHINE_HEALTHY_TIMEOUT_MS, 30000);
        }
    }
}
