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

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.utils.tag.TrafficData;
import com.huaweicloud.sermant.core.utils.tag.TrafficTag;
import com.huaweicloud.sermant.core.utils.tag.TrafficUtils;
import com.huaweicloud.sermant.tag.transmission.pojo.TrafficMessage;

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 执行线程的包装抽象类
 *
 * @param <T> 泛型
 * @author provenceee
 * @since 2023-06-08
 */
public abstract class AbstractThreadWrapper<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 执行该线程对象的线程池名称
     */
    protected final String executorName;

    private final Runnable runnable;

    private final Callable<T> callable;

    private final TrafficTag trafficTag;

    private final TrafficData trafficData;

    private final boolean cannotTransmit;

    /**
     * 构造方法
     *
     * @param runnable runnable
     * @param callable callable
     * @param trafficMessage 流量信息
     * @param cannotTransmit 执行方法之前是否需要删除线程变量
     * @param executorName
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
     * run方法, Runnable的实现类继承该方法
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
     * call方法，Callable的实现类继承该方法
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

    /**
     * 线程对象执行的前置方法
     *
     * @param obj 线程对象
     */
    protected void before(Object obj) {
        if (cannotTransmit) {
            // 当开启普通线程透传，不开启线程池透传时，需要在执行方法之前，删除由InheritableThreadLocal透传的数据
            TrafficUtils.removeTrafficTag();
            TrafficUtils.removeTrafficData();
        }
        if (trafficTag != null) {
            TrafficUtils.setTrafficTag(trafficTag);
        }
        if (trafficData != null) {
            TrafficUtils.setTrafficData(trafficData);
        }
        LOGGER.log(Level.FINE, "Current thread is {0}, class name is {1}, hash code is {2}, trafficTag is {3}, "
                        + "trafficData is {4}, will be executed.",
                new Object[]{Thread.currentThread().getName(), obj.getClass().getName(),
                        Integer.toHexString(obj.hashCode()), trafficTag, trafficData});
    }

    /**
     * 线程对象执行的后置方法
     */
    protected void after() {
        TrafficUtils.removeTrafficTag();
        TrafficUtils.removeTrafficData();
    }
}