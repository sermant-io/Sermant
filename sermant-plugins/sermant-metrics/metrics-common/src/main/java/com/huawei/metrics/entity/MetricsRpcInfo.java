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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

    private AtomicInteger clientErrorCount = new AtomicInteger();

    private AtomicInteger serverErrorCount = new AtomicInteger();

    private Map<String, AtomicInteger> latencyCounts = new ConcurrentHashMap<>();

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

    public Map<String, AtomicInteger> getLatencyCounts() {
        return latencyCounts;
    }

    public void setLatencyCounts(Map<String, AtomicInteger> latencyCounts) {
        this.latencyCounts = latencyCounts;
    }

    public AtomicInteger getClientErrorCount() {
        return clientErrorCount;
    }

    public void setClientErrorCount(AtomicInteger clientErrorCount) {
        this.clientErrorCount = clientErrorCount;
    }

    public AtomicInteger getServerErrorCount() {
        return serverErrorCount;
    }

    public void setServerErrorCount(AtomicInteger serverErrorCount) {
        this.serverErrorCount = serverErrorCount;
    }
}
