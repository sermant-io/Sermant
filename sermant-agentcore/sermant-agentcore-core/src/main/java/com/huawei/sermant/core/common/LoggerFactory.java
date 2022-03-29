/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.core.common;

import ch.qos.logback.classic.util.ContextInitializer;

import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.Map;
import java.util.logging.Logger;

/**
 * LoggerFactory
 *
 * @author luanwenfei
 * @version 1.0.0
 * @since 2022-03-26
 */
public class LoggerFactory {
    private static Logger logger = java.util.logging.Logger.getLogger("sermant");

    /**
     * 默认的logback配置文件路径
     */
    private static String defaultLogbackSettingPath;

    private LoggerFactory() {
    }

    /**
     * 从启动参数中获取logback的配置文件路径
     *
     * @param argsMap 启动参数
     * @return logback的配置文件路径
     */
    private static String getLogbackSettingFile(Map<String, Object> argsMap) {
        return argsMap.get(CommonConstant.LOG_SETTING_FILE_KEY).toString();
    }

    /**
     * 初始化logback配置文件路径
     *
     * @param argsMap 启动参数
     */
    public static void init(Map<String, Object> argsMap) {
        final String logbackSettingPath = getLogbackSettingFile(argsMap);

        // 设置slf4j 日志 handle
        defaultLogbackSettingPath = System.getProperty(ContextInitializer.CONFIG_FILE_PROPERTY);
        System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, logbackSettingPath);
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        logger = java.util.logging.Logger.getLogger("sermant");
    }

    /**
     * 回滚默认的logback配置文件路径
     */
    public static void rollback() {
        if (defaultLogbackSettingPath != null) {
            System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, defaultLogbackSettingPath);
        } else {
            System.clearProperty(ContextInitializer.CONFIG_FILE_PROPERTY);
        }
    }

    /**
     * 获取jul日志
     *
     * @return jul日志
     */
    public static Logger getLogger() {
        return logger;
    }
}
