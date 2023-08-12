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

package com.huaweicloud.sermant.router.transmit.wrapper;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.router.common.request.RequestData;
import com.huaweicloud.sermant.router.common.request.RequestHeader;
import com.huaweicloud.sermant.router.common.utils.ThreadLocalUtils;

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Callable/Runnable包装抽象类
 *
 * @param <T> 泛型
 * @author provenceee
 * @since 2023-06-08
 */
public abstract class AbstractThreadWrapper<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final Runnable runnable;

    private final Callable<T> callable;

    private final RequestHeader requestHeader;

    private final RequestData requestData;

    private final boolean cannotTransmit;

    /**
     * 构造方法
     *
     * @param runnable runnable
     * @param callable callable
     * @param requestHeader 请求标记
     * @param requestData 请求数据
     * @param cannotTransmit 执行方法之前是否需要删除线程变量
     */
    public AbstractThreadWrapper(Runnable runnable, Callable<T> callable, RequestHeader requestHeader,
            RequestData requestData, boolean cannotTransmit) {
        this.runnable = runnable;
        this.callable = callable;
        if (cannotTransmit) {
            this.requestHeader = null;
            this.requestData = null;
        } else {
            this.requestHeader = requestHeader;
            this.requestData = requestData;
        }
        this.cannotTransmit = cannotTransmit;
    }

    /**
     * run方法
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
     * @return callable调用结果
     * @throws Exception 异常
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
            // 当开启普通线程透传，不开启线程池透传时，需要在执行方法之前，删除由InheritableThreadLocal透传的数据
            ThreadLocalUtils.removeRequestHeader();
            ThreadLocalUtils.removeRequestData();
        }
        if (requestHeader != null) {
            ThreadLocalUtils.setRequestHeader(requestHeader);
        }
        if (requestData != null) {
            ThreadLocalUtils.setRequestData(requestData);
        }
        LOGGER.log(Level.FINE, "Current thread is {0}, class name is {1}, hash code is {2}, requestTag is {3}, "
                + "requestData is {4}, will be executed.",
            new Object[]{Thread.currentThread().getName(), obj.getClass().getName(),
                Integer.toHexString(obj.hashCode()), requestHeader, requestData});
    }

    private void after() {
        ThreadLocalUtils.removeRequestHeader();
        ThreadLocalUtils.removeRequestData();
    }
}