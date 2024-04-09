/*
 *   Copyright (C) 2023-2024 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.huaweicloud.sermant.tag.transmission.crossthread.enumeration;

/**
 * A special thread pool enumeration class that does not use new thread to execute task
 *
 * @author daizhenyu
 * @since 2023-09-04
 **/
public enum SpecialExecutor {
    /**
     * ThreadlessExecutor threadPool of grpc。
     * For this thread pool, the main thread creates child threads to submit thread tasks,
     * which are then executed by the main thread.
     * ThreadlessExecutor coordinates the tasks of two threads to form a production and consumption model
     */
    THREAD_LESS_EXECUTOR("ThreadlessExecutor"),

    /**
     * SynchronizationContext threadPool of grpc。
     * For this thread pool, new threads will not be used to execute thread tasks, but tasks in the thread pool queue
     * will be executed sequentially in the thread that calls the thread pool.
     */
    SYNCHRONIZATION_CONTEXT("SynchronizationContext"),

    /**
     * Used to return a return value when the getSpecialExecutorByName method cannot find a suitable
     * thread pool enumeration object.
     */
    OTHER_EXECUTORS("otherExecutors");

    private final String executorName;

    SpecialExecutor(String executorName) {
        this.executorName = executorName;
    }

    public String getExecutorName() {
        return this.executorName;
    }

    /**
     * Used to get a thread pool enumeration object based on the thread pool name
     *
     * @param name thread Pool name
     * @return SpecialExecutor
     */
    public static SpecialExecutor getSpecialExecutorByName(String name) {
        for (SpecialExecutor value : values()) {
            if (value.getExecutorName().equals(name)) {
                return value;
            }
        }
        return OTHER_EXECUTORS;
    }
}
