/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.common.report.print;

/**
 * 判断是否打印服务信息上报时候接口异常的错误信息
 *
 * @author zhengbin zhao
 * @version 1.0
 * @since 2021-08-09
 */
public class LoggerPrintManager {
    private int oneSecondCountMax = 60;
    private int tenSecondCountMax = 120;
    private int thirtySecondCountMax = 300;
    private int oneMinuteCountMax = 660;
    private int threeMinuteCountMax = 1740;
    private int fiveMinuteCountMax = 3540;
    private int tenMinuteCountMax = 7140;
    private int count = 0;

    private LoggerPrintManager() {
    }

    private static LoggerPrintManager loggerPrintManage = null;

    /**
     * 目前使用不存在多线程调用所以不做同步处理
     *
     * @return LoggerPrintManage对象
     */
    public static LoggerPrintManager getInstance() {
        if (loggerPrintManage == null) {
            loggerPrintManage = new LoggerPrintManager();
        }
        return loggerPrintManage;
    }

    /**
     * 不存再多线程调用，不考虑线程安全问题
     * 判断逻辑：
     * 1、开始1s上报一次错误，连续出错1min；
     * 2、修改为10s报一次错误，如果还是连续出错1min；
     * 3、修改为30s报一次错误，如果还是连续出错3min；
     * 4、修改为1min报一次错误，如果还是连续出错6min；
     * 5、修改为3min报一次错误，如果还是连续出错18min；
     * 6、修改为5min报一次错误，如果还是连续出错30min；
     * 7、修改为10min报一次错误，如果还是连续出错1h；
     * 8、修改为1h上报一次。
     *
     * @return 是否打印
     */
    public boolean shouldPrintLogger() {
        ++count;
        if (count <= oneSecondCountMax) {
            return true;
        }

        if (count <= tenSecondCountMax) {
            return count % 10 == 0;
        }

        if (count <= thirtySecondCountMax) {
            return count % 30 == 0;
        }

        if (count <= oneMinuteCountMax) {
            return count % 60 == 0;
        }

        if (count <= threeMinuteCountMax) {
            return count % 180 == 0;
        }

        if (count <= fiveMinuteCountMax) {
            return count % 300 == 0;
        }

        if (count <= tenMinuteCountMax) {
            return count % 600 == 0;
        } else {
            return count % 3600 == 0;
        }
    }
}
