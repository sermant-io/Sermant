/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.huawei.flowcontrol.common.entity;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 指标信息
 *
 * @author zhp
 * @since 2022-09-14
 */
public class MetricEntity {
    /**
     * 名称
     */
    private String name;

    /**
     * 服务端请求数量
     */
    private AtomicLong serverRequest;

    /**
     * 客户端请求数量
     */
    private AtomicLong clientRequest;

    /**
     * 客户端请求耗时
     */
    private AtomicLong consumeClientTime;

    /**
     * 服务段请求耗时
     */
    private AtomicLong consumeServerTime;

    /**
     * 服务端请求成功数量
     */
    private AtomicLong successServerRequest;

    /**
     * 客户端请求成功数量
     */
    private AtomicLong successClientRequest;

    /**
     * 服务段请求失败数量
     */
    private AtomicLong failedServerRequest;

    /**
     * 客户端请求失败数量
     */
    private AtomicLong failedClientRequest;

    /**
     * 上次采集时间
     */
    private AtomicLong lastTime;

    /**
     * 熔断耗时
     */
    private AtomicLong fuseTime;

    /**
     * 熔断失败请求数量
     */
    private AtomicLong failedFuseRequest;

    /**
     * 熔断成功数量
     */
    private AtomicLong successFulFuseRequest;

    /**
     * 熔断断开数量
     */
    private AtomicLong permittedFulFuseRequest;

    /**
     * 熔断忽略异常数量
     */
    private AtomicLong ignoreFulFuseRequest;

    /**
     * 熔断慢调用数量
     */
    private AtomicLong slowFuseRequest;

    /**
     * 熔断调用数量
     */
    private AtomicLong fuseRequest;

    /**
     * 熔断调用数量
     */
    private long reportTime;

    /**
     * 构造初始化
     */
    public MetricEntity() {
        this.serverRequest = new AtomicLong();
        this.clientRequest = new AtomicLong();
        this.consumeClientTime = new AtomicLong();
        this.consumeServerTime = new AtomicLong();
        this.successServerRequest = new AtomicLong();
        this.successClientRequest = new AtomicLong();
        this.failedServerRequest = new AtomicLong();
        this.failedClientRequest = new AtomicLong();
        this.lastTime = new AtomicLong();
        this.fuseTime = new AtomicLong();
        this.failedFuseRequest = new AtomicLong();
        this.successFulFuseRequest = new AtomicLong();
        this.permittedFulFuseRequest = new AtomicLong();
        this.ignoreFulFuseRequest = new AtomicLong();
        this.slowFuseRequest = new AtomicLong();
        this.fuseRequest = new AtomicLong();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AtomicLong getServerRequest() {
        return serverRequest;
    }

    public void setServerRequest(AtomicLong serverRequest) {
        this.serverRequest = serverRequest;
    }

    public AtomicLong getClientRequest() {
        return clientRequest;
    }

    public void setClientRequest(AtomicLong clientRequest) {
        this.clientRequest = clientRequest;
    }

    public AtomicLong getConsumeClientTime() {
        return consumeClientTime;
    }

    public void setConsumeClientTime(AtomicLong consumeClientTime) {
        this.consumeClientTime = consumeClientTime;
    }

    public AtomicLong getConsumeServerTime() {
        return consumeServerTime;
    }

    public void setConsumeServerTime(AtomicLong consumeServerTime) {
        this.consumeServerTime = consumeServerTime;
    }

    public AtomicLong getSuccessServerRequest() {
        return successServerRequest;
    }

    public void setSuccessServerRequest(AtomicLong successServerRequest) {
        this.successServerRequest = successServerRequest;
    }

    public AtomicLong getSuccessClientRequest() {
        return successClientRequest;
    }

    public void setSuccessClientRequest(AtomicLong successClientRequest) {
        this.successClientRequest = successClientRequest;
    }

    public AtomicLong getFailedServerRequest() {
        return failedServerRequest;
    }

    public void setFailedServerRequest(AtomicLong failedServerRequest) {
        this.failedServerRequest = failedServerRequest;
    }

    public AtomicLong getFailedClientRequest() {
        return failedClientRequest;
    }

    public void setFailedClientRequest(AtomicLong failedClientRequest) {
        this.failedClientRequest = failedClientRequest;
    }

    public AtomicLong getLastTime() {
        return lastTime;
    }

    public void setLastTime(AtomicLong lastTime) {
        this.lastTime = lastTime;
    }

    public AtomicLong getFuseTime() {
        return fuseTime;
    }

    public void setFuseTime(AtomicLong fuseTime) {
        this.fuseTime = fuseTime;
    }

    public AtomicLong getFailedFuseRequest() {
        return failedFuseRequest;
    }

    public void setFailedFuseRequest(AtomicLong failedFuseRequest) {
        this.failedFuseRequest = failedFuseRequest;
    }

    public AtomicLong getSuccessFulFuseRequest() {
        return successFulFuseRequest;
    }

    public void setSuccessFulFuseRequest(AtomicLong successFulFuseRequest) {
        this.successFulFuseRequest = successFulFuseRequest;
    }

    public AtomicLong getPermittedFulFuseRequest() {
        return permittedFulFuseRequest;
    }

    public void setPermittedFulFuseRequest(AtomicLong permittedFulFuseRequest) {
        this.permittedFulFuseRequest = permittedFulFuseRequest;
    }

    public AtomicLong getIgnoreFulFuseRequest() {
        return ignoreFulFuseRequest;
    }

    public void setIgnoreFulFuseRequest(AtomicLong ignoreFulFuseRequest) {
        this.ignoreFulFuseRequest = ignoreFulFuseRequest;
    }

    public AtomicLong getSlowFuseRequest() {
        return slowFuseRequest;
    }

    public void setSlowFuseRequest(AtomicLong slowFuseRequest) {
        this.slowFuseRequest = slowFuseRequest;
    }

    public AtomicLong getFuseRequest() {
        return fuseRequest;
    }

    public void setFuseRequest(AtomicLong fuseRequest) {
        this.fuseRequest = fuseRequest;
    }

    public long getReportTime() {
        return reportTime;
    }

    public void setReportTime(long reportTime) {
        this.reportTime = reportTime;
    }
}