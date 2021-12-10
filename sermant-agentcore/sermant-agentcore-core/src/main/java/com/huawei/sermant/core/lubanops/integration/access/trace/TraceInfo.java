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

package com.huawei.sermant.core.lubanops.integration.access.trace;

import com.huawei.sermant.core.lubanops.integration.utils.JSON;

/**
 * 一个调用链的信息描述，主要是调用链的id以及如果有vtraceId的时候对应vpath
 * @author
 * @since 2020/9/18
 **/
public class TraceInfo {

    /**
     * vTraceId，虚拟traceId，一个vTraceId对应多个实际的traceId， vTraceId会从开始一直往下应用传输
     */
    private String globalTraceId;

    /*
     * 虚拟traceId经过的path路径
     */
    private String globalPath;

    /*
     * 虚拟路径
     */
    private String traceId;

    /**
     * 产生trace的实例ID
     */
    private long instanceId;

    /**
     * 产品trace的环境ID
     */
    private long envId;

    /**
     * app的ID
     */
    private long appId;

    /**
     * 产生trace的业务ID
     */
    private long bizId;

    /**
     * 产生trace的租户ID
     */
    private int domainId;

    private long timestamp;

    /**
     * 从事件中截取信息
     * @param event
     * @return
     */
    public static TraceInfo build(SpanEventExtend event) {
        TraceInfo info = new TraceInfo();

        info.setGlobalTraceId(event.getGlobalTraceId());
        info.setGlobalPath(event.getGlobalPath());
        info.setTraceId(event.getTraceId());

        info.setInstanceId(event.getInstanceId());
        info.setEnvId(event.getEnvId());
        info.setBizId(event.getBizId());
        info.setAppId(event.getAppId());
        info.setDomainId(event.getDomainId());
        info.setTimestamp(event.getStartTime());
        return info;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getAppId() {
        return appId;
    }

    public void setAppId(long appId) {
        this.appId = appId;
    }

    public long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(long instanceId) {
        this.instanceId = instanceId;
    }

    public long getEnvId() {
        return envId;
    }

    public void setEnvId(long envId) {
        this.envId = envId;
    }

    public long getBizId() {
        return bizId;
    }

    public void setBizId(long bizId) {
        this.bizId = bizId;
    }

    public int getDomainId() {
        return domainId;
    }

    public void setDomainId(int domainId) {
        this.domainId = domainId;
    }

    public String getGlobalTraceId() {
        return globalTraceId;
    }

    public void setGlobalTraceId(String globalTraceId) {
        this.globalTraceId = globalTraceId;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getGlobalPath() {
        return globalPath;
    }

    public void setGlobalPath(String globalPath) {
        this.globalPath = globalPath;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
