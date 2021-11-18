/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.sample.servermonitor.collecttask;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 采集任务
 *
 * <p>一个采集任务会按{@link #collectInterval}指定的时间间隔采集由{@link MetricProvider#collect()}
 * 提供的数据，然后按{@link #consumeInterval}指定的时间间隔批量把数据传递给{@link MetricProvider#consume}
 * 消费（或者可以写死为发送给服务端）。<p/>
 *
 * <p>从{@link MetricProvider#collect()}采集的数据在消费前会存在{@link #writeBuffer}中</p>
 * @param <M> 采集数据类型
 */
public class CollectTask<M> {

    private final AtomicReference<List<M>> writeBuffer;

    private final MetricProvider<M> metricProvider;

    private final long collectInterval;

    private final long consumeInterval;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    private CollectTask(MetricProvider<M> metricProvider, long collectInterval, long consumeInterval, TimeUnit timeUnit) {
        this.metricProvider = metricProvider;
        this.collectInterval = TimeUnit.NANOSECONDS.convert(collectInterval, timeUnit);
        this.consumeInterval = TimeUnit.NANOSECONDS.convert(consumeInterval, timeUnit);
        writeBuffer = new AtomicReference<List<M>>(new LinkedList<M>());
    }

    public void start() {
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
    }

    private void doCollect() {
        final M m = metricProvider.collect();
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
        scheduler.shutdown();
    }

    public static <M> CollectTask<M> create(MetricProvider<M> collectVendor, long collectInterval,
                                            long consumeInterval, TimeUnit timeUnit) {
        return new CollectTask<M>(collectVendor, collectInterval, consumeInterval, timeUnit);
    }
}
