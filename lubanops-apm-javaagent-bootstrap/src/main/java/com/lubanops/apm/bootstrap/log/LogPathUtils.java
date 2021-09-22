package com.lubanops.apm.bootstrap.log;

import com.lubanops.apm.bootstrap.utils.StringUtils;

import java.io.File;

/**
 * 日志目录工具类
 * @author
 */
public class LogPathUtils {

    private static String appName;

    private static String instanceName;

    /**
     * 获取临时目录
     * @return
     */
    public static String getLogPath() {
        String userHome = System.getProperty("user.home");
        StringBuilder logPath = new StringBuilder();
        logPath.append(userHome).append(File.separator).append("apm").append(File.separator);
        if ((!StringUtils.isBlank(appName)) && (!StringUtils.isBlank(instanceName))) {
            logPath.append("instances")
                .append(File.separator)
                .append(appName)
                .append("-")
                .append(instanceName)
                .append(File.separator);
        }
        return logPath.toString();
    }

    public static void build(String appName, String instanceName) {
        LogPathUtils.appName = appName;
        LogPathUtils.instanceName = instanceName;
    }
}
