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

package com.huawei.javamesh.core.lubanops.bootstrap.plugin.apm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.huawei.javamesh.core.lubanops.bootstrap.api.CircuitBreaker;
import com.huawei.javamesh.core.lubanops.bootstrap.api.HarvestListener;
import com.huawei.javamesh.core.lubanops.bootstrap.collector.CollectorManager;
import com.huawei.javamesh.core.lubanops.bootstrap.collector.api.Collector;
import com.huawei.javamesh.core.lubanops.bootstrap.collector.api.MetricSet;
import com.huawei.javamesh.core.lubanops.bootstrap.holder.AgentServiceContainerHolder;

public class APMCollector extends Collector {

    public static final String COLLECTOR_APM = "ProbeInfo";

    public static final APMCollector INSTANCE = new APMCollector();

    public static final DetailAggregator DETAIL_AGGREGATOR = new DetailAggregator();

    public static final ExceptionAggregator EXCEPTION_AGGREGATOR = new ExceptionAggregator();

    public static final TransferAggregator TRANSFER_AGGREGATOR = new TransferAggregator();

    public static final RepositoryAggregator REPOSITORY_AGGREGATOR = new RepositoryAggregator();

    static {
        INSTANCE.addModelAggregator(DETAIL_AGGREGATOR);
        INSTANCE.addModelAggregator(EXCEPTION_AGGREGATOR);
        INSTANCE.addModelAggregator(TRANSFER_AGGREGATOR);
        INSTANCE.addModelAggregator(REPOSITORY_AGGREGATOR);
        INSTANCE.register();
    }

    private List<HarvestListener> harvestListeners = new ArrayList<HarvestListener>();

    public static void init() {
        CollectorManager.TAGS.add(COLLECTOR_APM);
    }

    public static void onStart(String type, int queueSize) {
        DETAIL_AGGREGATOR.onStart(type, queueSize);
    }

    public static void onDiscard(String type, long bytes) {
        DETAIL_AGGREGATOR.onDiscard(type, bytes);
    }

    public static void onThrowable(String type, long bytes, Throwable t) {
        DETAIL_AGGREGATOR.onThrowable(type, bytes);
        EXCEPTION_AGGREGATOR.onThrowable(type, t);
        AgentServiceContainerHolder.get().getService(CircuitBreaker.class).markFailure();
    }

    public static void onFinally(String type, long useTime) {
        DETAIL_AGGREGATOR.onFinally(type, useTime);
    }

    public static void onSuccess(String dataType, long bytes) {
        DETAIL_AGGREGATOR.onSuccess(dataType, bytes);
        AgentServiceContainerHolder.get().getService(CircuitBreaker.class).markSuccess();
    }

    @Override
    public List<MetricSet> harvest() {
        for (HarvestListener listener : harvestListeners) {
            listener.onHarvest(this, System.currentTimeMillis());
        }
        return super.harvest();
    }

    @Override
    public void parseParameter(Map<String, String> parameters) {

    }

    @Override
    public String getCollectorName() {
        return COLLECTOR_APM;
    }

    public double successPercent(String type) {
        return DETAIL_AGGREGATOR.getSendSuccessPercent(type);
    }

    public double sendCount(String type) {
        return DETAIL_AGGREGATOR.getSendCount(type);
    }

    public double errorCount(String type) {
        return DETAIL_AGGREGATOR.getErrorCount(type);
    }

    public void listenHarvest(HarvestListener harvestListener) {
        this.harvestListeners.add(harvestListener);
    }

    public void monitorQueueSize(long monitorQueueSize, long monitorObjectSize) {
        REPOSITORY_AGGREGATOR.monitorQueueSize(monitorQueueSize, monitorObjectSize);
    }

    public void traceQueueSize(long traceQueueSize, long traceObjectSize) {
        REPOSITORY_AGGREGATOR.traceQueueSize(traceQueueSize, traceObjectSize);
    }

}
