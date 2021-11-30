/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.example.demo.common;

import java.util.logging.Logger;

import com.huawei.apm.core.common.LoggerFactory;

/**
 * 示例插件的日志类
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/11/26
 */
public class DemoLogger {
    private static final Logger logger = LoggerFactory.getLogger();

    /**
     * 输出日志到控制台，采用ERROR级别，否则会被屏蔽
     *
     * @param message 日志信息
     */
    public static void println(String message) {
        logger.severe(message);
    }
}
