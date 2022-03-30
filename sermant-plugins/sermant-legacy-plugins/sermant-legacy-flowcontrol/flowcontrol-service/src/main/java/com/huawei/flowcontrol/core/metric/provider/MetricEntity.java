/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http:www.apache.orglicensesLICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.flowcontrol.core.metric.provider;

/**
 * 指标数据
 *
 * @author zhouss
 * @since 2021-12-08
 */
public class MetricEntity {
    /**
     * 资源名
     */
    private String resource;

    /**
     * 资源哈希码 指标数据需要
     */
    private int resourceCode;

    /**
     * 时间戳
     */
    private long timestamp;

    /**
     * 通过QPS
     */
    private long passQps;

    /**
     * 阻塞QPS
     */
    private long blockQps;

    /**
     * 成功QPS
     */
    private long successQps;

    /**
     * 异常QPS
     */
    private long exceptionQps;

    /**
     * 响应时间 毫秒
     */
    private long rt;

    /**
     * 应用名称
     */
    private String app;

    /**
     * 分类 SQL / RPC
     */
    private int classification;

    public String getResource() {
        return resource;
    }

    /**
     * 设置资源名
     *
     * @param resource 资源名
     * @return MetricEntity
     */
    public MetricEntity setResource(String resource) {
        this.resource = resource;
        return this;
    }

    public long getTimestamp() {
        return timestamp;
    }

    /**
     * 设置时间戳
     *
     * @param timestamp 时间戳
     * @return MetricEntity
     */
    public MetricEntity setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public long getPassQps() {
        return passQps;
    }

    /**
     * 设置通过qps
     *
     * @param passQps 通过qps
     * @return MetricEntity
     */
    public MetricEntity setPassQps(long passQps) {
        this.passQps = passQps;
        return this;
    }

    public long getBlockQps() {
        return blockQps;
    }

    /**
     * 设置阻塞qps
     *
     * @param blockQps 阻塞qps
     * @return MetricEntity
     */
    public MetricEntity setBlockQps(long blockQps) {
        this.blockQps = blockQps;
        return this;
    }

    public long getSuccessQps() {
        return successQps;
    }

    /**
     * 设置成功qps
     *
     * @param successQps 成功qps
     * @return MetricEntity
     */
    public MetricEntity setSuccessQps(long successQps) {
        this.successQps = successQps;
        return this;
    }

    public long getExceptionQps() {
        return exceptionQps;
    }

    /**
     * 设置异常qps
     *
     * @param exceptionQps 异常qps
     * @return MetricEntity
     */
    public MetricEntity setExceptionQps(long exceptionQps) {
        this.exceptionQps = exceptionQps;
        return this;
    }

    public long getRt() {
        return rt;
    }

    /**
     * 设置响应时间
     *
     * @param rt 响应时间
     * @return MetricEntity
     */
    public MetricEntity setRt(long rt) {
        this.rt = rt;
        return this;
    }

    public String getApp() {
        return app;
    }

    /**
     * 设置目标APP
     *
     * @param app 目标APP
     * @return MetricEntity
     */
    public MetricEntity setApp(String app) {
        this.app = app;
        return this;
    }

    public int getClassification() {
        return classification;
    }

    /**
     * 设置分类
     *
     * @param classification 分类
     * @return MetricEntity
     */
    public MetricEntity setClassification(int classification) {
        this.classification = classification;
        return this;
    }

    public int getResourceCode() {
        return resourceCode;
    }

    /**
     * 设置资源编码 - hash
     *
     * @param resourceCode 资源编码
     * @return MetricEntity
     */
    public MetricEntity setResourceCode(int resourceCode) {
        this.resourceCode = resourceCode;
        return this;
    }
}
