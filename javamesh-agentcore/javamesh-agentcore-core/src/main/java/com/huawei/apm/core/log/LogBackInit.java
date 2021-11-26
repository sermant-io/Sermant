package com.huawei.apm.core.log;


import java.util.Map;

import ch.qos.logback.classic.util.ContextInitializer;

import com.huawei.apm.core.common.PathIndexer;
import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class LogBackInit {
    public static void init(Map<String, Object> argsMap) {
        final String logbackSettingPath = argsMap.get(PathIndexer.JAVAMESH_LOGBACK_SETTING_FILE).toString();
        // 设置slf4j 日志 handle
        String defaultLogbackSettingPath = System.getProperty(ContextInitializer.CONFIG_FILE_PROPERTY);
        System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, logbackSettingPath);
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        LogFactory.setLogger(java.util.logging.Logger.getLogger("java-mesh"));
        if (defaultLogbackSettingPath != null) {
            System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, defaultLogbackSettingPath);
        } else {
            System.clearProperty(ContextInitializer.CONFIG_FILE_PROPERTY);
        }
    }
}
