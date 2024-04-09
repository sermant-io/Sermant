/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huawei.discovery.service.lb.stats;

import com.huawei.discovery.config.LbConfig;
import com.huawei.discovery.entity.Recorder;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Record the metric data of the current instance
 *
 * @author zhouss
 * @since 2022-09-26
 */
public class InstanceStats implements Recorder {
    /**
     * The number being requested
     */
    private final AtomicLong activeRequests = new AtomicLong();

    /**
     * Statistics on the number of all requests
     */
    private final AtomicLong allRequestCount = new AtomicLong();

    /**
     * The time spent on all request calls
     */
    private final AtomicLong allRequestConsumeTime = new AtomicLong();

    /**
     * The number of failed requests
     */
    private final AtomicLong failRequestCount = new AtomicLong();

    /**
     * Time window
     */
    private final long instanceStateTimeWindowMs;

    /**
     * The last time the time window was updated, as the left boundary of the time window
     */
    private volatile long lastLeftWindowTime;

    /**
     * The average response time over the time window
     */
    private volatile double responseAvgTime;

    /**
     * Constructor
     */
    public InstanceStats() {
        this.instanceStateTimeWindowMs =
                PluginConfigManager.getPluginConfig(LbConfig.class).getInstanceStatTimeWindowMs();
        lastLeftWindowTime = System.currentTimeMillis();
    }

    /**
     * Pre-call requests
     */
    @Override
    public void beforeRequest() {
        activeRequests.incrementAndGet();
        allRequestCount.incrementAndGet();
    }

    /**
     * Statistics on abnormal calls
     *
     * @param consumeTimeMs Consumption time
     * @param ex The type of exception
     */
    @Override
    public void errorRequest(Throwable ex, long consumeTimeMs) {
        baseStats(consumeTimeMs);
        failRequestCount.incrementAndGet();
    }

    /**
     * Result call
     *
     * @param consumeTimeMs Consumption time
     */
    @Override
    public void afterRequest(long consumeTimeMs) {
        baseStats(consumeTimeMs);
    }

    private void calculateResponseAvgTime() {
        final long allConsumeTime = allRequestConsumeTime.get();
        final long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis - lastLeftWindowTime >= this.instanceStateTimeWindowMs) {
            lastLeftWindowTime = currentTimeMillis;
            allRequestCount.set(0);
            allRequestConsumeTime.set(0);
        }
        this.responseAvgTime = allRequestCount.get() == 0 ? 0 : (allConsumeTime * 1d / allRequestCount.get());
    }

    private void baseStats(long consumeTimeMs) {
        final long request = activeRequests.decrementAndGet();
        if (request < 0) {
            activeRequests.set(0);
        }
        allRequestConsumeTime.addAndGet(consumeTimeMs);
        this.calculateResponseAvgTime();
    }

    /**
     * complete request
     */
    @Override
    public void completeRequest() {
    }

    public AtomicLong getAllRequestCount() {
        return allRequestCount;
    }

    public AtomicLong getAllRequestConsumeTime() {
        return allRequestConsumeTime;
    }

    /**
     * Get the number of concurrent transactions
     *
     * @return Number of concurrent transactions
     */
    public long getActiveRequests() {
        final long activeCount = activeRequests.get();
        final long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis - lastLeftWindowTime >= this.instanceStateTimeWindowMs) {
            lastLeftWindowTime = currentTimeMillis;
            this.activeRequests.set(0);
            return 0;
        }
        return activeCount;
    }

    public AtomicLong getFailRequestCount() {
        return failRequestCount;
    }

    /**
     * Get the average response time
     *
     * @return responseAvgTime
     */
    public double getResponseAvgTime() {
        return responseAvgTime;
    }
}
