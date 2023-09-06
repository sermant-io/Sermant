/*
 *   Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.tag.transmission.enumeration;

/**
 * 特殊的线程池枚举类，该类线程池不会使用新线程执行方法
 *
 * @author daizhenyu
 * @since 2023-09-04
 **/
public enum SpecialExecutor {
    /**
     * grpc的ThreadlessExecutor线程池。
     * 对于该线程池，主线程会创建子线程提交线程任务，然后主线程执行。
     * ThreadlessExecutor协调两个线程的任务,形成生产消费的模式
     */
    THREAD_LESS_EXECUTOR("ThreadlessExecutor"),

    /**
     * grpc的SynchronizationContext线程池。
     * 对于该线程池，并不会使用新的线程执行线程任务，而是在调用该线程池的线程中依次执行线程池队列的任务。
     */
    SYNCHRONIZATION_CONTEXT("SynchronizationContext"),

    /**
     * 用于getSpecialExecutorByName方法寻找不到合适的线程池枚举对象时返回返回值
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
     * 用于根据线程池名称获取线程池枚举对象
     *
     * @param name 线程池名称
     * @return SpecialExecutor对象
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
