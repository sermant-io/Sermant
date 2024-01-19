/*
 * Copyright (C) 2023-2024 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.metrics.util;

/**
 * 线程工具类，用于线程数据传递
 *
 * @author zhp
 * @since 2023-12-15
 */
public class ThreadMetricsUtil {
    /**
     * 线程变量存储,主要存储请求开始时间
     */
    private static final ThreadLocal<Long> START_TIME_THREAD_LOCAL = new InheritableThreadLocal<>();

    /**
     * 线程变量存储,主要存储预编译SQL
     */
    private static final ThreadLocal<String> SQL_THREAD_LOCAL = new InheritableThreadLocal<>();

    /**
     * 构造函数
     */
    private ThreadMetricsUtil() {
    }

    /**
     * 设置请求开始时间
     *
     * @param value 请求开始时间
     */
    public static void setStartTime(Long value) {
        START_TIME_THREAD_LOCAL.set(value);
    }

    /**
     * 获取请求开始时间
     *
     * @return 请求开始时间
     */
    public static Long getStartTime() {
        return START_TIME_THREAD_LOCAL.get();
    }

    /**
     * 设置sql
     *
     * @param value SQL信息
     */
    public static void setSql(String value) {
        SQL_THREAD_LOCAL.set(value);
    }

    /**
     * 获取sql
     *
     * @return SQL信息
     */
    public static String getSql() {
        return SQL_THREAD_LOCAL.get();
    }

    /**
     * 删除开始时间
     */
    public static void removeStartTime() {
        START_TIME_THREAD_LOCAL.remove();
    }

    /**
     * 删除SQL信息
     */
    public static void removeSql() {
        START_TIME_THREAD_LOCAL.remove();
    }
}
