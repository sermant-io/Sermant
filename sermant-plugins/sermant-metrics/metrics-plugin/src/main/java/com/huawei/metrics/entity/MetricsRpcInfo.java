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

package com.huawei.metrics.entity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * RPC调用信息
 *
 * @author zhp
 * @since 2023-10-17
 */
public class MetricsRpcInfo extends MetricsInfo {
    private AtomicInteger reqCount = new AtomicInteger();

    private AtomicInteger responseCount = new AtomicInteger();

    private AtomicLong sumLatency = new AtomicLong();

    private AtomicInteger reqErrorCount = new AtomicInteger();

    private List<Long> latencyList = new CopyOnWriteArrayList<>();

    public AtomicInteger getReqCount() {
        return reqCount;
    }

    public void setReqCount(AtomicInteger reqCount) {
        this.reqCount = reqCount;
    }

    public AtomicInteger getResponseCount() {
        return responseCount;
    }

    public void setResponseCount(AtomicInteger responseCount) {
        this.responseCount = responseCount;
    }

    public AtomicLong getSumLatency() {
        return sumLatency;
    }

    public void setSumLatency(AtomicLong sumLatency) {
        this.sumLatency = sumLatency;
    }

    public AtomicInteger getReqErrorCount() {
        return reqErrorCount;
    }

    public void setReqErrorCount(AtomicInteger reqErrorCount) {
        this.reqErrorCount = reqErrorCount;
    }

    public List<Long> getLatencyList() {
        return latencyList;
    }

    public void setLatencyList(List<Long> latencyList) {
        this.latencyList = latencyList;
    }
}
