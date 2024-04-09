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

package com.huaweicloud.sermant.tag.transmission.crossthread.wrapper;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.utils.tag.TrafficData;
import com.huaweicloud.sermant.core.utils.tag.TrafficTag;
import com.huaweicloud.sermant.core.utils.tag.TrafficUtils;
import com.huaweicloud.sermant.tag.transmission.crossthread.pojo.TrafficMessage;

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * the wrapper abstract class of the execution thread
 *
 * @param <T> Generics
 * @author provenceee
 * @since 2023-06-08
 */
public abstract class AbstractThreadWrapper<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * The name of the thread pool that executes the thread object
     */
    protected final String executorName;

    private final Runnable runnable;

    private final Callable<T> callable;

    private final TrafficTag trafficTag;

    private final TrafficData trafficData;

    private final boolean cannotTransmit;

    /**
     * constructor
     *
     * @param runnable runnable
     * @param callable callable
     * @param trafficMessage traffic message
     * @param cannotTransmit Whether thread variables need to be deleted before executing the method
     * @param executorName thread name
     */
    public AbstractThreadWrapper(Runnable runnable, Callable<T> callable, TrafficMessage trafficMessage,
            boolean cannotTransmit, String executorName) {
        this.runnable = runnable;
        this.callable = callable;
        if (cannotTransmit) {
            this.trafficTag = null;
            this.trafficData = null;
        } else {
            this.trafficTag = trafficMessage.getTrafficTag();
            this.trafficData = trafficMessage.getTrafficData();
        }
        this.cannotTransmit = cannotTransmit;
        this.executorName = executorName;
    }

    /**
     * run method, The Runnable implementation class inherits this method
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
     * call method，Callable's implementation class inherits this method
     *
     * @return callable call result
     * @throws Exception exception
     */
    public T call() throws Exception {
        try {
            before(callable);
            return callable.call();
        } finally {
            after();
        }
    }

    /**
     * Pre-method executed by thread object
     *
     * @param obj thread object
     */
    protected void before(Object obj) {
        if (cannotTransmit) {
            // When ordinary thread transparent transmission is enabled and thread pool transparent transmission is not
            // enabled, the data transparently transmitted by InheritableThreadLocal needs to be deleted before
            // executing the method.
            TrafficUtils.removeTrafficTag();
            TrafficUtils.removeTrafficData();
        }
        if (trafficTag != null) {
            TrafficUtils.setTrafficTag(trafficTag);
        }
        if (trafficData != null) {
            TrafficUtils.setTrafficData(trafficData);
        }
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Current thread is {0}, class name is {1}, hash code is {2}, trafficTag is {3}, "
                            + "trafficData is {4}, will be executed.",
                    new Object[]{Thread.currentThread().getName(), obj.getClass().getName(),
                            Integer.toHexString(obj.hashCode()), trafficTag, trafficData});
        }
    }

    /**
     * Post method executed by thread object
     */
    protected void after() {
        TrafficUtils.removeTrafficTag();
        TrafficUtils.removeTrafficData();
    }
}