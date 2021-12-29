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

package com.huawei.sermant.plugin.monitor.common.collect;

import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.plugin.monitor.common.utils.CommonUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/**
 * 采集任务
 *
 * <p>一个采集任务会按{@link #collectInterval}指定的时间间隔采集由{@link MetricProvider#collect()}
 * 提供的数据，然后按{@link #consumeInterval}指定的时间间隔批量把数据传递给{@link MetricProvider#consume}
 * 消费（或者可以写死为发送给服务端）。<p/>
 *
 * <p>从{@link MetricProvider#collect()}采集的数据在消费前会存在{@link #writeBuffer}中</p>
 *
 * @param <M> 采集数据类型
 */
public class CollectTask<M> {

    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final AtomicReference<List<M>> writeBuffer;

    private final MetricProvider<M> metricProvider;

    private final long collectInterval;

    private final long consumeInterval;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    private volatile boolean started = false;

    private final Object lock = new Object();

    private CollectTask(MetricProvider<M> metricProvider, long collectInterval, long consumeInterval, TimeUnit timeUnit) {
        this.metricProvider = metricProvider;
        this.collectInterval = TimeUnit.NANOSECONDS.convert(collectInterval, timeUnit);
        this.consumeInterval = TimeUnit.NANOSECONDS.convert(consumeInterval, timeUnit);
        writeBuffer = new AtomicReference<List<M>>(new LinkedList<M>());
    }

    public void start() {
        if (!started)
            synchronized (lock) {
                if (!started) {
                    scheduler.scheduleAtFixedRate(new Runnable() {
                        @Override
                        public void run() {
                            doCollect();
                        }
                    }, 0, collectInterval, TimeUnit.NANOSECONDS);
                    scheduler.scheduleAtFixedRate(new Runnable() {
                        @Override
                        public void run() {
                            doConsume();
                        }
                    }, consumeInterval, consumeInterval, TimeUnit.NANOSECONDS);
                    started = true;
                }
            }
    }

    private void doCollect() {
        final M m;
        try {
            m = metricProvider.collect();
        } catch (Exception e) {
            LOGGER.severe(String.format("Failed to collect metric caused by: %s", CommonUtil.getStackTrace(e)));
            return;
        }
        if (m == null) {
            return;
        }
        writeBuffer.get().add(m);
    }

    private void doConsume() {
        final List<M> readBuffer = writeBuffer.getAndSet(new LinkedList<M>());
        metricProvider.consume(readBuffer);
    }

    public void stop() {
        if (started) {
            synchronized (lock) {
                if (started) {
                    scheduler.shutdown();
                }
            }
        }
    }

    public static <M> CollectTask<M> create(MetricProvider<M> collectVendor, long collectInterval,
                                            long consumeInterval, TimeUnit timeUnit) {
        return new CollectTask<M>(collectVendor, collectInterval, consumeInterval, timeUnit);
    }
}
