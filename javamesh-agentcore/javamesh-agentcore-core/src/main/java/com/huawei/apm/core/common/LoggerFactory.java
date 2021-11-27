package com.huawei.apm.core.common;


import java.util.Map;
import java.util.logging.Logger;

import ch.qos.logback.classic.util.ContextInitializer;

import org.slf4j.bridge.SLF4JBridgeHandler;

import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;

public class LoggerFactory {
    /**
     * logback配置的键
     */
    public static String JAVAMESH_LOGBACK_SETTING_FILE = "javamesh.log.setting.file";

    private static String defaultLogbackSettingPath;

    private static String getLogbackSettingFile(Map<String, Object> argsMap) {
        return argsMap.get(JAVAMESH_LOGBACK_SETTING_FILE).toString();
    }

    public static void init(Map<String, Object> argsMap) {
        final String logbackSettingPath = getLogbackSettingFile(argsMap);
        // 设置slf4j 日志 handle
        defaultLogbackSettingPath = System.getProperty(ContextInitializer.CONFIG_FILE_PROPERTY);
        System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, logbackSettingPath);
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        LogFactory.setLogger(java.util.logging.Logger.getLogger("java-mesh"));
    }

    public static void rollback() {
        if (defaultLogbackSettingPath != null) {
            System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, defaultLogbackSettingPath);
        } else {
            System.clearProperty(ContextInitializer.CONFIG_FILE_PROPERTY);
        }
    }

    public static Logger getLogger() {
        return LogFactory.getLogger();
    }
}
