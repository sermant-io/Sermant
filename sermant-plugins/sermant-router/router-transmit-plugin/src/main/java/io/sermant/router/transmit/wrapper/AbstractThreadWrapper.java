/*
 * Copyright (C) 2024-2024 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.router.transmit.wrapper;

import io.sermant.core.common.LoggerFactory;
import io.sermant.router.common.request.RequestData;
import io.sermant.router.common.request.RequestTag;
import io.sermant.router.common.utils.ThreadLocalUtils;

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Callable/Runnable Wrapping abstract classes
 *
 * @param <T> Generics
 * @author provenceee
 * @since 2024-01-16
 */
public abstract class AbstractThreadWrapper<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final Runnable runnable;

    private final Callable<T> callable;

    private final RequestTag requestTag;

    private final RequestData requestData;

    private final boolean cannotTransmit;

    /**
     * Constructor
     *
     * @param runnable runnable
     * @param callable callable
     * @param requestTag Request tags
     * @param requestData Request data
     * @param cannotTransmit Whether the thread variable needs to be deleted before the method can be executed
     */
    public AbstractThreadWrapper(Runnable runnable, Callable<T> callable, RequestTag requestTag,
            RequestData requestData, boolean cannotTransmit) {
        this.runnable = runnable;
        this.callable = callable;
        if (cannotTransmit) {
            this.requestTag = null;
            this.requestData = null;
        } else {
            this.requestTag = requestTag;
            this.requestData = requestData;
        }
        this.cannotTransmit = cannotTransmit;
    }

    /**
     * run method
     */
    public void run() {
        try {
            before(runnable);
            runnable.run();
        } finally {
            after();
        }
    }

    /**
     * call
     *
     * @return The call result of the callable method
     * @throws Exception Exception
     */
    public T call() throws Exception {
        try {
            before(callable);
            return callable.call();
        } finally {
            after();
        }
    }

    private void before(Object obj) {
        if (cannotTransmit) {
            // If you enable normal thread pass through but do not enable thread pool pass through,
            // you need to delete the data that is pass passed by InheritableThreadLocal before executing the method
            ThreadLocalUtils.removeRequestTag();
            ThreadLocalUtils.removeRequestData();
        }
        if (requestTag != null) {
            ThreadLocalUtils.setRequestTag(requestTag);
        }
        if (requestData != null) {
            ThreadLocalUtils.setRequestData(requestData);
        }
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Current thread is {0}, class name is {1}, hash code is {2}, requestTag is {3}, "
                            + "requestData is {4}, will be executed.",
                    new Object[]{Thread.currentThread().getName(), obj.getClass().getName(),
                            Integer.toHexString(obj.hashCode()), requestTag, requestData});
        }
    }

    private void after() {
        ThreadLocalUtils.removeRequestTag();
        ThreadLocalUtils.removeRequestData();
    }
}