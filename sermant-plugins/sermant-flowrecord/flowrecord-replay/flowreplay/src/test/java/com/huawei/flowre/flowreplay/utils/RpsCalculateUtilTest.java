package com.huawei.flowre.flowreplay.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-07-26
 */
public class RpsCalculateUtilTest {

    @Test
    public void calculateRps() throws InterruptedException {
        AtomicInteger lastRps = new AtomicInteger(0);
        AtomicInteger currentRps = new AtomicInteger(0);
        AtomicLong currentSecond = new AtomicLong(new Date().getTime() / 1000);
        RpsCalculateUtil rpsCalculateUtil = new RpsCalculateUtil(lastRps, currentRps, currentSecond);
        rpsCalculateUtil.calculateRps();

        /**
         * 验证一次计数
         */
        Assert.assertEquals(1, currentRps.get());
        Assert.assertEquals(0, lastRps.get());
        Thread.sleep(1000);
        rpsCalculateUtil.calculateRps();

        /**
         * 验证两次计数
         */
        Assert.assertEquals(1, lastRps.get());

        /**
         * 验证计数为零的情况
         */
        Thread.sleep(2000);
        currentRps.getAndSet(0);
        rpsCalculateUtil.calculateRps();
        Assert.assertEquals(0, lastRps.get());
    }
}