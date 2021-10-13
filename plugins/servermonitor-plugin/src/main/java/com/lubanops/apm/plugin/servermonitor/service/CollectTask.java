package com.lubanops.apm.plugin.servermonitor.service;

import com.lubanops.apm.plugin.servermonitor.common.Consumer;
import com.lubanops.apm.plugin.servermonitor.common.Supplier;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 采集任务
 *
 * <p>一个采集任务会按{@link #collectInterval}指定的时间间隔采集由{@link #supplier}提供的数据，
 * 然后按{@link #consumeInterval}指定的时间间隔批量把数据传递给{@link #consumer}消费（或者可以
 * 写死为发送给服务端）。<p/>
 *
 * <p>从{@link #supplier}采集的数据在消费前会存在{@link #writeBuffer}中</p>
 * @param <T> 采集数据类型
 */
public class CollectTask<T> {

    private final AtomicReference<List<T>> writeBuffer;

    private final Supplier<T> supplier;

    private final Consumer<List<T>> consumer;

    private final long collectInterval;

    private final long consumeInterval;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    private CollectTask(Supplier<T> supplier, long collectInterval, Consumer<List<T>> consumer,
                        long consumeInterval, TimeUnit timeUnit) {
        this.supplier = supplier;
        this.consumer = consumer;
        this.collectInterval = TimeUnit.NANOSECONDS.convert(collectInterval, timeUnit);
        this.consumeInterval = TimeUnit.NANOSECONDS.convert(consumeInterval, timeUnit);
        writeBuffer = new AtomicReference<List<T>>(new LinkedList<T>());
    }

    public void start() {
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                doSupply();
            }
        }, 0, collectInterval, TimeUnit.NANOSECONDS);
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                doConsume();
            }
        }, consumeInterval, consumeInterval, TimeUnit.NANOSECONDS);
    }

    private void doSupply() {
        final T t = supplier.get();
        if (t == null) {
            return;
        }
        writeBuffer.get().add(t);
    }

    private void doConsume() {
        final List<T> readBuffer = writeBuffer.getAndSet(new LinkedList<T>());
        consumer.accept(readBuffer);
    }

    public void stop() {
        scheduler.shutdown();
    }

    public static <T> CollectTask<T> create(Supplier<T> supplier, long collectInterval, Consumer<List<T>> consumer,
                                            long consumeInterval, TimeUnit timeUnit) {
        return new CollectTask<T>(supplier, collectInterval, consumer, consumeInterval, timeUnit);
    }

}
