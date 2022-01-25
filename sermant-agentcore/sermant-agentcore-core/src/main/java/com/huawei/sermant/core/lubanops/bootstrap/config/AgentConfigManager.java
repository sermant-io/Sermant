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

import java.io.File;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.huawei.sermant.core.lubanops.bootstrap.commons.LubanApmConstants;
import com.huawei.sermant.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.sermant.core.lubanops.bootstrap.utils.FileUtils;
import com.huawei.sermant.core.lubanops.bootstrap.utils.ParamCheckUtils;
import com.huawei.sermant.core.lubanops.bootstrap.utils.StringUtils;

/**
 * agent的配置文件以及系统启动参数的配置存放的类 <br>
 * @author
 * @since 2020年3月9日
 */
public class AgentConfigManager {

    public final static String MASTER_ADDRESS = "master.address";

    public final static String ACCESS_ADDRESS = "access.address";

    public final static String LOG_LEVEL = "log.level";

    public final static String EVENT_THREAD_COUNT = "event.thread.count";

    public final static String MASTER_ACCESS_KEY = "access.key";

    public final static String MASTER_SECRET_KEY = "secret.key";

    public final static String FILTER_LOG_MESSAGE = "filter.log.message";

    public final static String RUN_ENVIRONMENT = "run_environment";

    public final static String PROXY = "luban_apm_proxy";

    /**
     * 运行 backend 模块机器的ip
     */
    public static final String BACKEND_IP = "backendIp";

    /**
     * backend 模块 netty 服务 监听的端口
     */
    public static final String BACKEND_PORT = "backendPort";

    /**
     * master地址
     */
    private static String masterAddress = "";

    private static String accessAddress = "";

    private static String logLevel = "info";

    private static int eventThreadCount = 3;

    /**
     * proxy地址
     */
    private static String[] proxyList = {};

    private static String masterAuthAk;

    private static String masterAuthSk;

    /**
     * backend ip
     */
    private static String backendIp;

    /**
     * backend port
     */
    private static String backendPort;


    /**
     * 忽略Exception监控项的异常的消息，只打印堆栈信息，一旦agent配置了这个值，那么监控项的配置就失效
     */
    private static boolean filterLogMessage = false;

    public static void init(String configPath) {
        Properties properties = FileUtils.readFilePropertyByPath(
                configPath + File.separator + "config"+ File.separator + LubanApmConstants.CONFIG_FILENAME);
        masterAuthAk = properties.getProperty(MASTER_ACCESS_KEY);
        masterAuthSk = properties.getProperty(MASTER_SECRET_KEY);
        masterAddress = properties.getProperty(MASTER_ADDRESS);
        backendIp = properties.getProperty(BACKEND_IP, LubanApmConstants.BACKEND_IP_COMMON);
        backendPort = properties.getProperty(BACKEND_PORT, LubanApmConstants.BACKEND_PORT_COMMON);
        setAccessAddress(properties.getProperty(ACCESS_ADDRESS));
        setLogLevel(properties.getProperty(LOG_LEVEL));
        setEventThreadCount(StringUtils.string2Int(properties.getProperty(EVENT_THREAD_COUNT), 3));
        String filterLogMessage = properties.getProperty(FILTER_LOG_MESSAGE);
        if ("true".equals(filterLogMessage)) {
            AgentConfigManager.filterLogMessage = true;
        }
        checkAgentConfig();
    }

    private static void checkAgentConfig() {

        if (!ParamCheckUtils.isUrl(masterAddress)) {
            throw new IllegalArgumentException(
                    String.format("[AGENT CONFIG]master address[%s] illegal.", masterAddress));
        }
        if (StringUtils.isBlank(masterAuthAk) || masterAuthAk.length() > 500 || !masterAuthAk.matches("[a-zA-Z0-9]+")) {
            throw new IllegalArgumentException("[AGENT CONFIG]access key illegal.");
        }
        if (StringUtils.isBlank(masterAuthSk) || masterAuthSk.length() > 500 || !masterAuthAk.matches("[a-zA-Z0-9]+")) {
            throw new IllegalArgumentException("[AGENT CONFIG]secret key illegal.");
        }
        if (getProxyList() != null && getProxyList().length > 10) {
            throw new IllegalArgumentException(String.format("[AGENT CONFIG]proxy list[%s] illegal.",
                    getProxyList() == null ? "" : Arrays.asList(getProxyList()).toString()));
        }
        if (getProxyList() != null) {
            for (String proxy : getProxyList()) {
                if (!ParamCheckUtils.isUrl(proxy)) {
                    throw new IllegalArgumentException(
                            String.format("[AGENT CONFIG]proxy address[%s] illegal.", proxy));
                }

            }
        }
    }

    public static String getMasterAddress() {
        return masterAddress;
    }

    public static void setMasterAddress(String masterAddress) {
        AgentConfigManager.masterAddress = masterAddress;
    }

    public static boolean isFilterLogMessage() {
        return filterLogMessage;
    }

    public static void setFilterLogMessage(boolean filterLogMessage) {
        AgentConfigManager.filterLogMessage = filterLogMessage;
    }

    public static String getMasterAuthAk() {
        return masterAuthAk;
    }

    public static String getMasterAuthSk() {
        return masterAuthSk;
    }

    public static String[] getProxyList() {
        return proxyList.clone();
    }

    public static void setProxyList(String[] proxyList) {
        AgentConfigManager.proxyList = proxyList.clone();
    }

    public static String getLogLevel() {
        return logLevel;
    }

    public static void setLogLevel(String logLevel) {
        Level level = getLevel(logLevel);
        Logger logger = LogFactory.getLogger();
        if (logger != null) {
            logger.setLevel(level);
            Handler[] handlers = logger.getHandlers();
            for (Handler handler : handlers) {
                handler.setLevel(level);
            }
        }
        AgentConfigManager.logLevel = logLevel;
    }

    public static int getEventThreadCount() {
        return eventThreadCount;
    }

    public static void setEventThreadCount(int eventThreadCount) {
        AgentConfigManager.eventThreadCount = eventThreadCount;
    }

    public static Level getLevel(String levelStr) {
        if (levelStr != null) {
            if ("error".equals(levelStr.toLowerCase())) {
                return Level.SEVERE;
            } else if ("debug".equals(levelStr.toLowerCase())) {
                return Level.FINE;
            }
        }
        return Level.INFO;
    }

    public static String getAccessAddress() {
        return accessAddress;
    }

    public static void setAccessAddress(String accessAddress) {
        AgentConfigManager.accessAddress = accessAddress;
    }

    public static String getNettyServerIp() {
        return backendIp;
    }

    public static String getNettyServerPort() {
        return backendPort;
    }
}
