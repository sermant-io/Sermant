package com.lubanops.apm.bootstrap.utils;

/**
 * 日志安全处理工具类
 */
public class LogForgingUtil {

    private LogForgingUtil() {
    }

    public static String replace(String input) {
        if (input == null) {
            return null;
        }
        return input.replace("\r", "_").replace("\n", "_");
    }

}
