/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.javamesh.core.lubanops.bootstrap.plugin.common;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.huawei.javamesh.core.lubanops.bootstrap.collector.api.MonitorDataRow;
import com.huawei.javamesh.core.lubanops.bootstrap.collector.api.StatsBase;
import com.huawei.javamesh.core.lubanops.bootstrap.collector.interceptor.StatsAroundInterceptor;
import com.huawei.javamesh.core.lubanops.bootstrap.utils.ConcurrentUtil;

/**
 * 默认针对方法的性能统计
 * <p>
 * 在方法执行前调用 {@code onStart} 方法； 在异常抛出后调用 {@code onThrowable} 方法； 在方法结束后调用
 * {@code onFinally} 方法； 获取数据时调用 {@code getStatus} 方法； 收割数据时调用 {@code harvest}
 * 方法
 * </p>
 */
public class DefaultStats implements StatsBase, StatsAroundInterceptor {
    public final static long NANO_TO_MILLI = 1000000L;

    /**
     * 调用次数
     */
    protected final AtomicLong invokeCount = new AtomicLong(0L);

    /**
     * 调用总时间
     */
    protected final AtomicLong totalTimeInNano = new AtomicLong(0L);

    /**
     * 最慢调用
     */
    protected final AtomicLong maxTime = new AtomicLong(0L);

    /**
     * 错误次数
     */
    protected final AtomicLong errorCount = new AtomicLong(0L);

    /**
     * 当前线程数
     */
    protected final AtomicInteger runningCount = new AtomicInteger(0);

    /**
     * 最大并发
     */
    protected final AtomicInteger concurrentMax = new AtomicInteger(0);

    /**
     * 记录上一次读取的值，用于获取差值
     */
    protected volatile long invokeCountOld;

    protected volatile long totalTimeInNanoOld;

    protected volatile long errorCountOld;

    protected static void merge(MonitorDataRow dataRowFrom, MonitorDataRow dataRowDes) {
        dataRowDes.put("invokeCount", dataRowDes.get("invokeCount") == null
                ? (Long) dataRowFrom.get("invokeCount")
                : (Long) dataRowDes.get("invokeCount") + (Long) dataRowFrom.get("invokeCount"));
        dataRowDes.put("totalTime", dataRowDes.get("totalTime") == null
                ? (Long) dataRowFrom.get("totalTime")
                : (Long) dataRowDes.get("totalTime") + (Long) dataRowFrom.get("totalTime"));
        dataRowDes.put("maxTime", dataRowDes.get("maxTime") == null
                ? (Long) dataRowFrom.get("maxTime")
                : (Long) dataRowDes.get("maxTime") + (Long) dataRowFrom.get("maxTime"));
        dataRowDes.put("errorCount", dataRowDes.get("errorCount") == null
                ? (Long) dataRowFrom.get("errorCount")
                : (Long) dataRowDes.get("errorCount") + (Long) dataRowFrom.get("errorCount"));
        if (dataRowFrom.get("concurrentMax") != null) {
            dataRowDes.put("concurrentMax", dataRowDes.get("concurrentMax") == null
                    ? (Integer) dataRowFrom.get("concurrentMax")
                    : (Integer) dataRowDes.get("concurrentMax") + (Integer) dataRowFrom.get("concurrentMax"));
        }
    }

    @Override
    public long onStart() {
        long t = System.nanoTime();
        ConcurrentUtil.setMaxValue(concurrentMax, runningCount.incrementAndGet());
        return t;
    }

    @Override
    public void onThrowable(Throwable t) {
        errorCount.incrementAndGet();
    }

    /**
     * 方法抛出异常调用
     */
    public void onError() {
        errorCount.incrementAndGet();
    }

    @Override
    public boolean onFinally(long timeInNanos) {
        invokeCount.incrementAndGet();
        runningCount.decrementAndGet();
        totalTimeInNano.addAndGet(timeInNanos);
        return ConcurrentUtil.setMaxValue(maxTime, timeInNanos);
    }

    public boolean onFinallyNoRunningCount(long timeInNanos) {
        invokeCount.incrementAndGet();
        totalTimeInNano.addAndGet(timeInNanos);
        return ConcurrentUtil.setMaxValue(maxTime, timeInNanos);
    }

    @Override
    public MonitorDataRow getStatus() {
        MonitorDataRow row = new MonitorDataRow();
        row.put("invokeCount", invokeCount.get());
        row.put("totalTime", totalTimeInNano.get() / NANO_TO_MILLI);
        row.put("maxTime", maxTime.get() / NANO_TO_MILLI);
        row.put("errorCount", errorCount.get());
        row.put("concurrentMax", concurrentMax.get());
        return row;
    }

    @Override
    public MonitorDataRow harvest() {
        long invokeCountNew = invokeCount.get();
        long invokeCountDelta;
        if ((invokeCountDelta = invokeCountNew - invokeCountOld) > 0) {
            MonitorDataRow row = new MonitorDataRow();
            row.put("invokeCount", invokeCountDelta);
            long totalTimeInNanoNew = totalTimeInNano.get();
            row.put("totalTime", (totalTimeInNanoNew - totalTimeInNanoOld) / NANO_TO_MILLI);
            row.put("maxTime", maxTime.getAndSet(0) / NANO_TO_MILLI);
            long errorCountNew = errorCount.get();
            row.put("errorCount", errorCountNew - errorCountOld);
            int concurrentMaxInt = concurrentMax.getAndSet(0);
            if (concurrentMaxInt > 0) {
                row.put("concurrentMax", concurrentMaxInt);
            }

            // reset
            invokeCountOld = invokeCountNew;
            totalTimeInNanoOld = totalTimeInNanoNew;
            errorCountOld = errorCountNew;
            return row;
        }
        return null;
    }
}
