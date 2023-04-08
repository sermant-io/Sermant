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
 * 实例信息
 *
 * @author zhp
 * @since 2023-02-17
 */
public class InstanceInfo {
    /**
     * 请求数量
     */
    private AtomicInteger requestNum;

    /**
     * 请求失败数量
     */
    private AtomicInteger requestFailNum;

    /**
     * 摘除状态
     */
    private AtomicBoolean removalStatus;

    /**
     * 摘除时间
     */
    private long removalTime;

    /**
     * 摘除时间
     */
    private long recoveryTime;

    /**
     * 实例IP和域名
     */
    private String host;

    /**
     * 实例端口
     */
    private String port;

    /**
     * 最后调用时间
     */
    private long lastInvokeTime;

    /**
     * 服务调用统计信息
     */
    private List<RequestCountData> countDataList;

    /**
     * 构造方法
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
                + '}';
    }
}
