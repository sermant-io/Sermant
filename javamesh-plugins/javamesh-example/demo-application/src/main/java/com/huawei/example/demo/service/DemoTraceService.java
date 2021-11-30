/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.example.demo.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 链路功能的模拟通信示例
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/10/25
 */
public class DemoTraceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoNameService.class);

    private final static ThreadLocal<Map<String, String>> END_POINT = new ThreadLocal<Map<String, String>>();

    /**
     * 主要方法
     */
    public static void trace() {
        final Map<String, String> message = receive();
        service(message);
        send(message);
    }

    /**
     * 模拟接收数据，直接从ThreadLocal中获取
     *
     * @return 数据集
     */
    private static Map<String, String> receive() {
        final Map<String, String> message = END_POINT.get();
        if (message == null) {
            return new HashMap<String, String>();
        }
        return message;
    }

    /**
     * 模拟进行业务操作，直接输出数据集，观察链路监控对数据集的操作
     *
     * @param message 数据集
     */
    private static void service(Map<String, String> message) {
        LOGGER.info("DemoTraceService: " + message);
    }

    /**
     * 模拟发送消息，直接写到ThreadLocal中
     *
     * @param message 数据集
     */
    private static void send(Map<String, String> message) {
        END_POINT.set(message);
    }
}
