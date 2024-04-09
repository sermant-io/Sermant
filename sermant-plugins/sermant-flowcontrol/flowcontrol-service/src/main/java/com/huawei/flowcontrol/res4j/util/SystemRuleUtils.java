/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.huawei.flowcontrol.res4j.util;

import com.huawei.flowcontrol.common.config.FlowControlConfig;
import com.huawei.flowcontrol.res4j.windows.SystemStatus;
import com.huawei.flowcontrol.res4j.windows.WindowsArray;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

/**
 * configuration tool class
 *
 * @author xuezechao1
 * @since 2022-12-12
 */
public class SystemRuleUtils {
    private static final FlowControlConfig CONFIG = PluginConfigManager.getPluginConfig(FlowControlConfig.class);

    private SystemRuleUtils() {
    }

    /**
     * obtain the system adaptive switch status
     *
     * @return switch
     */
    public static boolean isEnableSystemAdaptive() {
        return CONFIG.isEnableSystemAdaptive();
    }

    /**
     * Obtain the system rule flow control switch information
     *
     * @return isEnableSystemRule
     */
    public static boolean isEnableSystemRule() {
        return CONFIG.isEnableSystemRule();
    }

    /**
     * get qps
     *
     * @return qps
     */
    public static double getQps() {
        return SystemStatus.getInstance().getQps();
    }

    /**
     * get thread count
     *
     * @return thread count
     */
    public static long getThreadNum() {
        return WindowsArray.INSTANCE.getThreadNum();
    }

    /**
     * get the average response time
     *
     * @return average response time
     */
    public static double getAveRt() {
        return SystemStatus.getInstance().getAveRt();
    }

    /**
     * get system load
     *
     * @return system load
     */
    public static double getCurrentLoad() {
        return SystemStatus.getInstance().getCurrentLoad();
    }

    /**
     * get cpu usage
     *
     * @return cpu usage
     */
    public static double getCurrentCpuUsage() {
        return SystemStatus.getInstance().getCurrentCpuUsage();
    }

    /**
     * gets the maximum number of threads
     *
     * @return the maximum number of threads
     */
    public static double getMaxThreadNum() {
        return SystemStatus.getInstance().getMaxThreadNum();
    }

    /**
     * gets the minimum response time
     *
     * @return minimum response time
     */
    public static double getMinRt() {
        return SystemStatus.getInstance().getMinRt();
    }
}
