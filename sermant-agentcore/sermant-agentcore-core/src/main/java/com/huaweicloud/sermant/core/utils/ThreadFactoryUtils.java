/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.core.utils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ThreadFactoryUtils
 *
 * @author zzf
 * @since 2022-03-26
 */
public class ThreadFactoryUtils implements ThreadFactory {

    private static final AtomicInteger FACTORY_NUMBER = new AtomicInteger(0);

    private static final int STRING_BUILDER_EXTEND_SIZE = 8;

    private final AtomicInteger threadNumber = new AtomicInteger(0);

    private final String threadPrefix;

    private final boolean isDaemon;

    /**
     * 构造函数
     *
     * @param threadName threadName
     */
    public ThreadFactoryUtils(String threadName) {
        this(threadName, true);
    }

    /**
     * ThreadFactoryUtils
     *
     * @param threadName threadName
     * @param isDaemon daemon
     */
    public ThreadFactoryUtils(String threadName, boolean isDaemon) {
        this.threadPrefix = prefix(threadName, FACTORY_NUMBER.getAndIncrement());
        this.isDaemon = isDaemon;
    }

    private String prefix(String threadName, int factoryId) {
        final StringBuilder buffer = new StringBuilder(32);
        buffer.append(threadName);
        buffer.append('(');
        buffer.append(factoryId);
        buffer.append('-');
        return buffer.toString();
    }

    @Override
    public Thread newThread(Runnable job) {
        String newThreadName = createThreadName();
        Thread thread = new Thread(job, newThreadName);
        if (isDaemon) {
            thread.setDaemon(true);
        }
        return thread;
    }

    private String createThreadName() {
        StringBuilder buffer = new StringBuilder(threadPrefix.length() + STRING_BUILDER_EXTEND_SIZE);
        buffer.append(threadPrefix);
        buffer.append(threadNumber.getAndIncrement());
        buffer.append(')');
        return buffer.toString();
    }
}