package com.huawei.apm.core.log;


import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class LogBackInit {

    private static final String LOGBACK_SETTING_PATH_KEY = "logback.configurationFile";

    public static void init(String logbackSettingPath) {
        // 设置slf4j 日志 handle
        String defaultLogbackSettingPath = System.getProperty(LOGBACK_SETTING_PATH_KEY);
        System.setProperty(LOGBACK_SETTING_PATH_KEY, logbackSettingPath);
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        LogFactory.setLogger(java.util.logging.Logger.getLogger("java-mesh"));
        if (defaultLogbackSettingPath != null) {
            System.setProperty(LOGBACK_SETTING_PATH_KEY, defaultLogbackSettingPath);
        } else {
            System.clearProperty(logbackSettingPath);
        }
    }
}
