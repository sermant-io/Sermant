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

package com.huaweicloud.sermant.database.utils;

/**
 * Thread tool class, used for thread data transfer
 *
 * @author zhp
 * @since 2024-02-06
 */
public class ThreadDatabaseUrlUtil {
    /**
     * Thread variable storage, mainly storing database url information
     */
    private static final ThreadLocal<String> DATABASE_URL_THREAD_LOCAL = new InheritableThreadLocal<>();

    /**
     * Constructor
     */
    private ThreadDatabaseUrlUtil() {
    }

    /**
     * Set database url information
     *
     * @param url Database url information
     */
    public static void setDatabaseUrl(String url) {
        DATABASE_URL_THREAD_LOCAL.set(url);
    }

    /**
     * Get database url information
     *
     * @return Database url information
     */
    public static String getDatabaseUrl() {
        return DATABASE_URL_THREAD_LOCAL.get();
    }

    /**
     * Remove database url information
     */
    public static void removeDatabaseUrl() {
        DATABASE_URL_THREAD_LOCAL.remove();
    }
}
