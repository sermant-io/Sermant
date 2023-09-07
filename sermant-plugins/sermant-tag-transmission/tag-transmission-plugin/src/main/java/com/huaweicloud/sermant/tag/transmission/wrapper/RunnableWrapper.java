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

package com.huaweicloud.sermant.tag.transmission.wrapper;

import com.huaweicloud.sermant.tag.transmission.enumeration.SpecialExecutor;
import com.huaweicloud.sermant.tag.transmission.pojo.TrafficMessage;

/**
 * Runnable包装类
 *
 * @param <T> 泛型
 * @author provenceee
 * @since 2023-04-21
 */
public class RunnableWrapper<T> extends AbstractThreadWrapper<T> implements Runnable {
    /**
     * 构造方法
     *
     * @param runnable runnable
     * @param trafficMessage 流量信息
     * @param cannotTransmit 执行方法之前是否需要删除线程变量
     * @param executorName 线程池名称
     */
    public RunnableWrapper(Runnable runnable, TrafficMessage trafficMessage, boolean cannotTransmit,
            String executorName) {
        super(runnable, null, trafficMessage, cannotTransmit, executorName);
    }

    @Override
    protected void before(Object obj) {
        // 处理特殊线程池，以下两类线程池会在调用对应线程池执行方法的线程中依次执行线程池队列中的任务，不需要重新设置流量标签
        switch (SpecialExecutor.getSpecialExecutorByName(this.executorName)) {
            case THREAD_LESS_EXECUTOR:
            case SYNCHRONIZATION_CONTEXT:
                return;
            default:
                super.before(obj);
        }
    }

    @Override
    protected void after() {
        // 处理特殊线程池，以下两类线程池会在调用对应线程池执行方法的线程中依次执行线程池队列中的任务，防止误删流量标签
        switch (SpecialExecutor.getSpecialExecutorByName(this.executorName)) {
            case THREAD_LESS_EXECUTOR:
            case SYNCHRONIZATION_CONTEXT:
                return;
            default:
                super.after();
        }
    }
}