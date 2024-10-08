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

package io.sermant.tag.transmission.crossthread.wrapper;

import io.sermant.tag.transmission.crossthread.enumeration.SpecialExecutor;
import io.sermant.tag.transmission.crossthread.pojo.TrafficMessage;

/**
 * Runnable Wrapper
 *
 * @param <T> Generics
 * @author provenceee
 * @since 2023-04-21
 */
public class RunnableWrapper<T> extends AbstractThreadWrapper<T> implements Runnable {
    /**
     * constructor
     *
     * @param runnable runnable
     * @param trafficMessage traffic message
     * @param cannotTransmit Whether thread variables need to be deleted before executing the method
     * @param executorName thread pool name
     */
    public RunnableWrapper(Runnable runnable, TrafficMessage trafficMessage, boolean cannotTransmit,
            String executorName) {
        super(runnable, null, trafficMessage, cannotTransmit, executorName);
    }

    @Override
    protected void before(Object obj) {
        // To handle special thread pools, the following two types of thread pools will sequentially execute tasks in
        // the thread pool queue in the thread that calls the corresponding thread pool execution method, without
        // resetting the traffic tag.
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
        // To handle special thread pools, the following two types of thread pools will sequentially execute tasks in
        // the thread pool queue in the thread that calls the corresponding thread pool execution method to prevent
        // accidental deletion of traffic tags.
        switch (SpecialExecutor.getSpecialExecutorByName(this.executorName)) {
            case THREAD_LESS_EXECUTOR:
            case SYNCHRONIZATION_CONTEXT:
                return;
            default:
                super.after();
        }
    }
}
