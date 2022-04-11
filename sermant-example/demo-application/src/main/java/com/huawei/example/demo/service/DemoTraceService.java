/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 链路功能的模拟通信示例
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-10-25
 */
public class DemoTraceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoTraceService.class);

    private static final ThreadLocal<Map<String, String>> END_POINT = new ThreadLocal<Map<String, String>>();

    private DemoTraceService() {
    }

    /**
     * 主要方法
     */
    public static void trace() {
        // 链路追踪入口跨度（根跨度场景）
        provider(new HashMap<String, String>());
        service(END_POINT.get());

        // 调用进程内普通方法
        normal();

        // 链路追踪出口跨度
        Map<String, String> message = consumer();
        service(message);

        // 链路追踪入口跨度（非根跨度场景）
        provider(message);
        service(END_POINT.get());

        // 调用进程内的普通方法
        normal();

        // 链路追踪出口跨度
        service(consumer());
    }

    /**
     * 模拟发送消息，直接写到ThreadLocal中
     *
     * @param message 数据集
     */
    private static void provider(Map<String, String> message) {
        END_POINT.set(message);
    }

    /**
     * 模拟进程内的普通方法
     */
    private static void normal() {
    }

    /**
     * 模拟接收数据，直接从ThreadLocal中获取
     *
     * @return 数据集
     */
    private static Map<String, String> consumer() {
        final Map<String, String> message = END_POINT.get();
        if (message == null) {
            return new HashMap<>();
        }
        return message;
    }

    /**
     * 模拟进行业务操作，直接输出数据集，观察链路监控对数据集的操作
     *
     * @param message 数据集
     */
    private static void service(Map<String, String> message) {
        LOGGER.info("DemoTraceService: {}", message);
    }
}
