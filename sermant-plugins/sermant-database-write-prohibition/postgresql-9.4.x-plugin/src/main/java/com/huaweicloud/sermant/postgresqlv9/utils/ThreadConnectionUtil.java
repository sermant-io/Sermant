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

package com.huaweicloud.sermant.postgresqlv9.utils;

import java.sql.Connection;

/**
 * 线程工具类，用于线程数据传递
 *
 * @author zhp
 * @since 2024-02-06
 */
public class ThreadConnectionUtil {
    /**
     * 线程变量存储,主要存储链接信息
     */
    private static final ThreadLocal<Connection> CONNECTION_THREAD_LOCAL = new InheritableThreadLocal<>();

    /**
     * 构造函数
     */
    private ThreadConnectionUtil() {
    }

    /**
     * 设置链接信息
     *
     * @param connection 连接信息
     */
    public static void setConnection(Connection connection) {
        CONNECTION_THREAD_LOCAL.set(connection);
    }

    /**
     * 获取链接信息
     *
     * @return 请求开始时间
     */
    public static Connection getConnection() {
        return CONNECTION_THREAD_LOCAL.get();
    }

    /**
     * 移除链接信息
     */
    public static void removeConnection() {
        CONNECTION_THREAD_LOCAL.remove();
    }
}
