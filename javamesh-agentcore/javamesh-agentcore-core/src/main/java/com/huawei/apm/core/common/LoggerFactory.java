package com.huawei.apm.core.common;

import java.util.Map;
import java.util.logging.Logger;

import ch.qos.logback.classic.util.ContextInitializer;

import org.slf4j.bridge.SLF4JBridgeHandler;

import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;

public class LoggerFactory {
    /**
     * 默认的logback配置文件路径
     */
    private static String defaultLogbackSettingPath;

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
        LogFactory.setLogger(java.util.logging.Logger.getLogger("java-mesh"));
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
        return LogFactory.getLogger();
    }
}
