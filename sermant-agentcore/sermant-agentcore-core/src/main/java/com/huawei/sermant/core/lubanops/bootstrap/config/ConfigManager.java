/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.core.lubanops.bootstrap.config;

import java.util.Map;

import com.huawei.sermant.core.lubanops.bootstrap.api.EventDispatcher;
import com.huawei.sermant.core.lubanops.bootstrap.event.SecureChangeEvent;
import com.huawei.sermant.core.lubanops.bootstrap.holder.AgentServiceContainerHolder;
import com.huawei.sermant.core.lubanops.bootstrap.utils.ExceptionUtil;
import com.huawei.sermant.core.lubanops.bootstrap.utils.StringUtils;

/**
 * 保存系统配置的一个类，系统配置主要包含几种：启动参数读取的配置，服务器端心跳下发的配置，还有身份信息的配置等 <br>
 * @author
 * @since 2020年3月9日
 */
public class ConfigManager {
    public final static int DEFAULT_MAX_ROW = 500;

    public final static int MAX_ROW_H = 2000;

    public final static int MAX_ROW_L = 10;

    public final static int DEFAULT_SLOW_REQUEST_THRESHOLD = 800;

    public final static int SLOW_REQUEST_THRESHOLD_H = 20000;

    public final static int SLOW_REQUEST_THRESHOLD_L = 100;

    public static final boolean DEFAULT_SECURE_CHANNEL = true;

    public static final boolean DEFAULT_PARSE_REDIS_BODY = true;

    public static final int DEFAULT_PARSE_REDIS_LENGTH = 100;

    /**
     * 慢请求的阈值，比如800毫秒，或者1秒，由用户自己定义,这个定义是通用的，对于单个url也可以自己定义自己的慢请求的阈值
     */
    private static int slowRequestThreshold = DEFAULT_SLOW_REQUEST_THRESHOLD;

    /**
     * 慢请求的trace个数的阈值
     */
    private static Stats slowRequestTraceCountStats = Stats.SLOW_DEFAULT;

    /**
     * 错误请求的trace个数的阈值
     */
    private static Stats errorRequestTraceCounStats = Stats.ERROR_DEFAULT;

    /**
     * 正常请求的trace个数的阈值
     */
    private static Stats requestTranceCountStats = Stats.NORMAL_DEFAULT;

    /**
     * 是否屏蔽异常堆栈的exception信息
     */
    private static boolean hideExceptionMessage = false;

    /**
     * 是否停止javaagent拦截字节码
     */
    private static boolean stopAgent = false;

    /**
     * aksk是否验证失败
     */
    private static boolean isValidated = true;

    /**
     * 采集异常堆栈的长度
     */
    private static int maxExceptionLength = ExceptionUtil.MAX_ERROR_STACK_LENGTH;

    /**
     * 监控项的最大行数。默认值 500
     */
    private static int maxRow = DEFAULT_MAX_ROW;

    // ~~ redis global config

    /**
     * whether parse body or not
     */
    private static boolean parseRedisBody = DEFAULT_PARSE_REDIS_BODY;

    /**
     * parse redis body length
     */
    private static int parseRedisLength = DEFAULT_PARSE_REDIS_LENGTH;

    // ~~ transfer config

    /**
     * use secure channel or not
     */
    private static boolean secureChannel = DEFAULT_SECURE_CHANNEL;

    private static int stackThreshold = 0;

    public static void setSystemProperties(Map<String, String> propMap) {
        // 解析通用配置
        setAgentConfig(propMap);

        setStats(propMap);

        String s = propMap.get(SysConfigKey.getHideExceptionMessage());
        if ("1".equals(s)) {
            setHideExceptionMessage(true);
        } else {
            setHideExceptionMessage(false);
        }

        s = propMap.get(SysConfigKey.getMaxExceptionLength());
        if (s == null) {
            setMaxExceptionLength(ExceptionUtil.MAX_ERROR_STACK_LENGTH);
        } else {
            setMaxExceptionLength(Integer.parseInt(s));
        }

        s = propMap.get(SysConfigKey.getCollectorParseRedisBody());
        if (!StringUtils.isBlank(s)) {
            setParseRedisBody(Boolean.parseBoolean(s));
        } else {
            setParseRedisBody(DEFAULT_PARSE_REDIS_BODY);
        }

        s = propMap.get(SysConfigKey.getCollectorParseRedisLength());
        if (!StringUtils.isBlank(s)) {
            setParseRedisLength(Integer.parseInt(s));
        } else {
            setParseRedisLength(DEFAULT_PARSE_REDIS_LENGTH);
        }

        s = propMap.get(SysConfigKey.getUseSecureChannel());
        if (!StringUtils.isBlank(s)) {
            setSecureChannel(Boolean.parseBoolean(s));
        } else {
            setSecureChannel(DEFAULT_SECURE_CHANNEL);
        }
    }

    private static void setStats(Map<String, String> propMap) {
        // 慢请求
        String s = propMap.get(SysConfigKey.getSlowRequestTraceCount());
        if (StringUtils.isBlank(s)) {
            setSlowRequestTraceCountStats(Stats.SLOW_DEFAULT);
        } else {
            setSlowRequestTraceCountStats(Stats.parseValue(s));
        }

        // 错误请求
        s = propMap.get(SysConfigKey.getErrorRequestTranceCount());
        if (StringUtils.isBlank(s)) {
            setErrorRequestTraceCounStats(Stats.ERROR_DEFAULT);
        } else {
            setErrorRequestTraceCounStats(Stats.parseValue(s));
        }

        // 正常的请求
        s = propMap.get(SysConfigKey.getRequestTranceCount());
        if (StringUtils.isBlank(s)) {
            setRequestTranceCountStats(Stats.NORMAL_DEFAULT);
        } else {
            setRequestTranceCountStats(Stats.parseValue(s));
        }
    }

    private static void setAgentConfig(Map<String, String> propMap) {
        /*
         * 慢请求阈值
         */
        String s = propMap.get(SysConfigKey.getSlowRequestThreshold());
        if (StringUtils.isBlank(s)) {
            setSlowRequestThreshold(DEFAULT_SLOW_REQUEST_THRESHOLD);
        } else {
            int iv = Integer.parseInt(s);
            setSlowRequestThreshold(iv);
        }

        s = propMap.get(SysConfigKey.getStopAgent());
        if ("1".equals(s)) {
            setStopAgent(true);
        } else {
            setStopAgent(false);
        }

        s = propMap.get(SysConfigKey.getMaxRows());
        if (StringUtils.isBlank(s)) {
            setMaxRow(DEFAULT_MAX_ROW);
        } else {
            setMaxRow(Integer.parseInt(s));
        }
    }

    public static int getMaxExceptionLength() {
        return maxExceptionLength;
    }

    public static void setMaxExceptionLength(int maxExceptionLength) {
        ConfigManager.maxExceptionLength = maxExceptionLength;
    }

    public static boolean isHideExceptionMessage() {
        return hideExceptionMessage;
    }

    public static void setHideExceptionMessage(boolean hideExceptionMessage) {
        ConfigManager.hideExceptionMessage = hideExceptionMessage;
    }

    public static boolean isSecureChannel() {
        return secureChannel;
    }

    public static void setSecureChannel(boolean secureChannel) {
        boolean origSecureChannel = ConfigManager.secureChannel;
        ConfigManager.secureChannel = secureChannel;
        // notify secure channel changed.
        if (origSecureChannel != secureChannel) {
            AgentServiceContainerHolder.get()
                    .getService(EventDispatcher.class)
                    .dispatch(new SecureChangeEvent(origSecureChannel, secureChannel));
        }
    }

    public static boolean isParseRedisBody() {
        return parseRedisBody;
    }

    public static void setParseRedisBody(boolean parseRedisBody) {
        ConfigManager.parseRedisBody = parseRedisBody;
    }

    public static int getParseRedisLength() {
        return parseRedisLength;
    }

    public static void setParseRedisLength(int parseRedisLength) {
        ConfigManager.parseRedisLength = parseRedisLength;
    }

    public static int getMaxRow() {
        return maxRow;
    }

    public static void setMaxRow(int maxRow) {
        if (maxRow > MAX_ROW_H || maxRow < MAX_ROW_L) {
            maxRow = DEFAULT_MAX_ROW;
        }
        ConfigManager.maxRow = maxRow;
    }

    public static int getSlowRequestThreshold() {
        return slowRequestThreshold;
    }

    public static void setSlowRequestThreshold(int slowRequestThreshold) {
        if (slowRequestThreshold > SLOW_REQUEST_THRESHOLD_H || slowRequestThreshold < SLOW_REQUEST_THRESHOLD_L) {
            slowRequestThreshold = DEFAULT_SLOW_REQUEST_THRESHOLD;
        }
        ConfigManager.slowRequestThreshold = slowRequestThreshold;
    }

    public static Stats getSlowRequestTraceCountStats() {
        return slowRequestTraceCountStats;
    }

    public static void setSlowRequestTraceCountStats(Stats slowRequestTraceCountStats) {
        ConfigManager.slowRequestTraceCountStats = slowRequestTraceCountStats;
    }

    public static Stats getErrorRequestTraceCounStats() {
        return errorRequestTraceCounStats;
    }

    public static void setErrorRequestTraceCounStats(Stats errorRequestTraceCounStats) {
        ConfigManager.errorRequestTraceCounStats = errorRequestTraceCounStats;
    }

    public static Stats getRequestTranceCountStats() {
        return requestTranceCountStats;
    }

    public static void setRequestTranceCountStats(Stats requestTranceCountStats) {
        ConfigManager.requestTranceCountStats = requestTranceCountStats;
    }

    public static boolean isStopAgent() {
        return stopAgent;
    }

    public static void setStopAgent(boolean stopAgent) {
        ConfigManager.stopAgent = stopAgent;
    }

    public static boolean isValidated() {
        return isValidated;
    }

    public static void setValidated(boolean isValidated) {
        ConfigManager.isValidated = isValidated;
    }

    public static int getStackThreshold() {
        return stackThreshold;
    }

    public static void setStackThreshold(Integer stackThreshold) {
        if (stackThreshold == null) {
            ConfigManager.stackThreshold = 0;
        } else {
            ConfigManager.stackThreshold = stackThreshold;
        }
    }

}
