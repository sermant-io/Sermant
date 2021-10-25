/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package org.apache.skywalking.oap.server.core.analysis.manual.segment;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * TPS统计
 *
 * @author l30007003
 * @since 2021-06-01
 */
@Slf4j
public class SegmentCounter {

    private static final SegmentCounter INSTANCE = new SegmentCounter();

    private volatile long last;
    private final AtomicLong segmentSize = new AtomicLong();
    private final Object lock = new Object();

    public static SegmentCounter getInstance() {
        return INSTANCE;
    }

    public void increase(long num) {
        segmentSize.addAndGet(num);
        long current = System.currentTimeMillis();
        if (TimeUnit.SECONDS.convert(current - last, TimeUnit.MILLISECONDS) > 60) {
            synchronized (lock) {
                long s;
                if ((s = TimeUnit.SECONDS.convert(current - last, TimeUnit.MILLISECONDS)) > 60) {
                    long size = segmentSize.getAndSet(0);
                    last = current;
                    log.info("SegmentSize: {}, time: {}s, tps: {}/s", size, s, size / s);
                }
            }
        }
    }
}
