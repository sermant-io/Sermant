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

package com.huaweicloud.sermant.entity;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Instance information
 *
 * @author zhp
 * @since 2023-02-17
 */
public class InstanceInfo {
    /**
     * Number of requests
     */
    private AtomicInteger requestNum;

    /**
     * The number of failed requests
     */
    private AtomicInteger requestFailNum;

    /**
     * Removal status
     */
    private AtomicBoolean removalStatus;

    /**
     * Removal time
     */
    private long removalTime;

    /**
     * Recovery time
     */
    private long recoveryTime;

    /**
     * IP address and domain name of the instance
     */
    private String host;

    /**
     * Instance port
     */
    private String port;

    /**
     * Last call time
     */
    private long lastInvokeTime;

    /**
     * Service call statistics
     */
    private List<RequestCountData> countDataList;

    /**
     * Error rate
     */
    private float errorRate;

    /**
     * Constructor
     */
    public InstanceInfo() {
        this.requestNum = new AtomicInteger(0);
        this.requestFailNum = new AtomicInteger(0);
        this.removalStatus = new AtomicBoolean(false);
    }

    public AtomicInteger getRequestNum() {
        return requestNum;
    }

    public void setRequestNum(AtomicInteger requestNum) {
        this.requestNum = requestNum;
    }

    public AtomicInteger getRequestFailNum() {
        return requestFailNum;
    }

    public void setRequestFailNum(AtomicInteger requestFailNum) {
        this.requestFailNum = requestFailNum;
    }

    public AtomicBoolean getRemovalStatus() {
        return removalStatus;
    }

    public void setRemovalStatus(AtomicBoolean removalStatus) {
        this.removalStatus = removalStatus;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public List<RequestCountData> getCountDataList() {
        return countDataList;
    }

    public void setCountDataList(List<RequestCountData> countDataList) {
        this.countDataList = countDataList;
    }

    public long getRemovalTime() {
        return removalTime;
    }

    public void setRemovalTime(long removalTime) {
        this.removalTime = removalTime;
    }

    public long getLastInvokeTime() {
        return lastInvokeTime;
    }

    public void setLastInvokeTime(long lastInvokeTime) {
        this.lastInvokeTime = lastInvokeTime;
    }

    public long getRecoveryTime() {
        return recoveryTime;
    }

    public void setRecoveryTime(long recoveryTime) {
        this.recoveryTime = recoveryTime;
    }

    public float getErrorRate() {
        return errorRate;
    }

    public void setErrorRate(float errorRate) {
        this.errorRate = errorRate;
    }

    @Override
    public String toString() {
        return "InstanceInfo{"
                + "requestNum=" + requestNum
                + ", requestFailNum=" + requestFailNum
                + ", removalStatus=" + removalStatus
                + ", removalTime=" + removalTime
                + ", recoveryTime=" + recoveryTime
                + ", host='" + host + '\''
                + ", port='" + port + '\''
                + ", lastInvokeTime=" + lastInvokeTime
                + ", countDataList=" + countDataList
                + ", errorRate=" + errorRate
                + '}';
    }
}
