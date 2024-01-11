/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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
 * HttpUrlConnection工具类，用于线程数据传递
 *
 * @author zhp
 * @since 2023-12-15
 */
public class HttpUrlConnectionUtil {
    /**
     * 线程变量存储,主要存储请求开始时间
     */
    private static final ThreadLocal<Long> START_TIME_THREAD_LOCAL = new InheritableThreadLocal<>();

    /**
     * 构造函数
     */
    private HttpUrlConnectionUtil() {
    }

    /**
     * 设置线程变量
     *
     * @param value 变量信息
     */
    public static void setValue(Long value) {
        START_TIME_THREAD_LOCAL.set(value);
    }

    /**
     * 获取线程变量
     *
     * @return 变量信息
     */
    public static Long getValue() {
        return START_TIME_THREAD_LOCAL.get();
    }

    /**
     * 删除线程变量信息
     */
    public static void remove() {
        START_TIME_THREAD_LOCAL.remove();
    }
}
