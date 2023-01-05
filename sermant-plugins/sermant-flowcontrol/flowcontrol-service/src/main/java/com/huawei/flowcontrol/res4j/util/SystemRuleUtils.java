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
 * 配置工具类
 *
 * @author xuezechao1
 * @since 2022-12-12
 */
public class SystemRuleUtils {
    private static final FlowControlConfig CONFIG = PluginConfigManager.getPluginConfig(FlowControlConfig.class);

    private SystemRuleUtils() {
    }

    /**
     * 获取系统自适应开关情况
     *
     * @return 开关
     */
    public static boolean isEnableSystemAdaptive() {
        return CONFIG.isEnableSystemAdaptive();
    }

    /**
     * 获取系统规则流控开关情况
     *
     * @return 开发
     */
    public static boolean isEnableSystemRule() {
        return CONFIG.isEnableSystemRule();
    }

    /**
     * 获取qps
     *
     * @return qps
     */
    public static double getQps() {
        return SystemStatus.getInstance().getQps();
    }

    /**
     * 获取线程数
     *
     * @return 线程数
     */
    public static long getThreadNum() {
        return WindowsArray.INSTANCE.getThreadNum();
    }

    /**
     * 获取平均响应时间
     *
     * @return 平均响应时间
     */
    public static double getAveRt() {
        return SystemStatus.getInstance().getAveRt();
    }

    /**
     * 获取系统负载
     *
     * @return 系统负载
     */
    public static double getCurrentLoad() {
        return SystemStatus.getInstance().getCurrentLoad();
    }

    /**
     * 获取cpu使用率
     *
     * @return cpu使用率
     */
    public static double getCurrentCpuUsage() {
        return SystemStatus.getInstance().getCurrentCpuUsage();
    }

    /**
     * 获取最大线程数
     *
     * @return 最大线程数
     */
    public static double getMaxThreadNum() {
        return SystemStatus.getInstance().getMaxThreadNum();
    }

    /**
     * 获取最小响应时间
     *
     * @return 最小响应时间
     */
    public static double getMinRt() {
        return SystemStatus.getInstance().getMinRt();
    }

}
