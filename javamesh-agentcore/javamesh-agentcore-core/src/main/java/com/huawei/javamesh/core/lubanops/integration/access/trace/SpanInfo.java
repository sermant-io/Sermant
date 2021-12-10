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

package com.huawei.javamesh.core.lubanops.integration.access.trace;

import java.util.HashMap;
import java.util.Map;

/**
 * span信息,span信息是rpc调用的根信息 <br>
 * @author
 * @since 2020年3月4日
 */
public class SpanInfo {

    /**
     * vTraceId，虚拟traceId，一个vTraceId对应多个实际的traceId， vTraceId会从开始一直往下应用传输
     */
    private String globalTraceId;

    /**
     * 虚拟traceId经过的path路径
     */
    private String globalPath;

    /**
     * 在root的span调用产生的全局id，以此往后透传
     */
    private String traceId;

    /**
     * 代表一次rpc的调用的id，对于root的调用，值为字符串1，对于当前span调用的下一个spanId编号为1-1,1-2等格式，以此往后类推
     */
    private String spanId;

    /**
     * 环境ID
     */
    private long envId;

    /**
     * 实例ID
     */
    private long instanceId;

    /**
     * 应用ID
     */
    private long appId;

    /**
     * 业务ID
     */
    private long bizId;

    /**
     * 租户ID
     */
    private int domainId;

    /**
     * 只有是根event也就是span的时候有值
     */
    private String resource;

    /**
     * 根event 的时候存在，实际调用的url
     */
    private String realSource;

    /**
     * 开始时间
     */

    private long startTime;

    /**
     * 耗时
     */
    private long timeUsed;

    /**
     * 状态码，针对http的调用有效
     */
    private int statusCode;

    /**
     * 业务状态码的采集
     */
    private String bizCode;

    private String className;

    /**
     * 这里的method实际上是tags里面的http_method，只有url监控项才有
     */
    private String method;

    /**
     * 是否异步的event
     */
    private boolean isAsync = false;

    /**
     * 包含用户自定义参数，header或body体里的内容，httpMethod, bizCode，以及后续可能新增参数
     */
    private Map<String, String> tags = new HashMap<String, String>();

    /**
     * 是否有错误，主要用在span的场景，如果一个span的event调用有log.error或者抛出异常，（根据用户的配置来）都认为是有错误
     */
    private Boolean hasError;

    /**
     * 错误类型 主要有这么几种 ErrorType枚举的几种，可以逗号分隔多种类型
     */
    private String errorReasons;

    public String getGlobalPath() {
        return globalPath;
    }

    public void setGlobalPath(String globalPath) {
        this.globalPath = globalPath;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getGlobalTraceId() {
        return globalTraceId;
    }

    public void setGlobalTraceId(String globalTraceId) {
        this.globalTraceId = globalTraceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }

    public long getEnvId() {
        return envId;
    }

    public void setEnvId(long envId) {
        this.envId = envId;
    }

    public long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(long instanceId) {
        this.instanceId = instanceId;
    }

    public long getAppId() {
        return appId;
    }

    public void setAppId(long appId) {
        this.appId = appId;
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

    public String getRealSource() {
        return realSource;
    }

    public void setRealSource(String realSource) {
        this.realSource = realSource;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getTimeUsed() {
        return timeUsed;
    }

    public void setTimeUsed(long timeUsed) {
        this.timeUsed = timeUsed;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getBizCode() {
        return bizCode;
    }

    public void setBizCode(String bizCode) {
        this.bizCode = bizCode;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public Boolean getHasError() {
        return hasError;
    }

    public void setHasError(Boolean hasError) {
        this.hasError = hasError;
    }

    public String getErrorReasons() {
        return errorReasons;
    }

    public void setErrorReasons(String errorReasons) {
        this.errorReasons = errorReasons;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public boolean isAsync() {
        return isAsync;
    }

    public void setAsync(boolean async) {
        isAsync = async;
    }
}
