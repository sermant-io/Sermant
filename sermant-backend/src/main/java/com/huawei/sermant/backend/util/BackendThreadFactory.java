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

package com.huawei.sermant.backend.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class BackendThreadFactory implements ThreadFactory {
    private final static AtomicInteger FACTORY_NUMBER = new AtomicInteger(0);

    private final AtomicInteger threadNumber = new AtomicInteger(0);

    private final String threadPrefix;

    private final boolean daemon;

    public BackendThreadFactory() {
        this("backend", true);
    }

    public BackendThreadFactory(String threadName) {
        this(threadName, true);
    }

    public BackendThreadFactory(String threadName, boolean daemon) {
        this.threadPrefix = prefix(threadName, FACTORY_NUMBER.getAndIncrement());
        this.daemon = daemon;
    }

    public static ThreadFactory createThreadFactory(String threadName) {
        return createThreadFactory(threadName, false);
    }

    public static ThreadFactory createThreadFactory(String threadName, boolean daemon) {
        return new BackendThreadFactory(threadName, daemon);
    }

    private String prefix(String threadName, int factoryId) {
        return threadName + '(' + factoryId + ')';
    }

    @Override
    public Thread newThread(Runnable job) {
        String newThreadName = createThreadName();
        Thread thread = new Thread(job, newThreadName);
        if (daemon) {
            thread.setDaemon(true);
        }
        return thread;
    }

    private String createThreadName() {
        return threadPrefix + threadNumber.getAndIncrement() + ')';
    }
}
