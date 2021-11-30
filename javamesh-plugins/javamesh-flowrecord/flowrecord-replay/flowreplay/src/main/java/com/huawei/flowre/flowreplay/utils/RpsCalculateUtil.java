/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowre.flowreplay.utils;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 同步计算rps 累计各个线程每秒构造的请求数量
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-07-02
 */
public class RpsCalculateUtil {
    /**
     * 前一秒的RPS
     */
    private AtomicInteger lastRps;

    /**
     * 当前RPS
     */
    private AtomicInteger currentRps;

    /**
     * 当前时间 (s)
     */
    private AtomicLong currentSecond;

    public RpsCalculateUtil(AtomicInteger lastRps, AtomicInteger currentRps, AtomicLong currentSecond) {
        this.lastRps = lastRps;
        this.currentRps = currentRps;
        this.currentSecond = currentSecond;
    }

    /**
     * 同步方法计算当前rps
     */
    public synchronized void calculateRps() {
        if (this.currentSecond.get() == new Date().getTime() / 1000) {
            this.currentRps.incrementAndGet();
        } else {
            this.currentSecond.getAndSet(new Date().getTime() / 1000);
            if (this.currentRps.get() != 0) {
                if (this.lastRps.get() == 0) {
                    this.lastRps.set(this.currentRps.get());
                } else {
                    // 取两秒的平均
                    this.lastRps.set((this.lastRps.get() + this.currentRps.get()) / 2);
                }
            } else {
                this.lastRps.set(this.currentRps.get());
            }
            this.currentRps.getAndSet(0);
        }
    }
}
