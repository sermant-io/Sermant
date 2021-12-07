/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.hercules.controller.monitor.dto;

/**
 * 功能描述：sql使用到的参数
 *
 * 
 * @since 2021-11-14
 */
public class MonitorHostDTO {
    /**
     * 测试任务id
     */
    private Long testId;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * influxdb中数据所在bucket
     */
    private String bucket;

    /**
     * 监控主机ip地址
     */
    private String ip;

    /**
     * 监控主机
     */
    private String host;

    /**
     * 是否监控jvm数据
     */
    private Boolean isMonitorJvm;

    /**
     * jvm类型
     */
    private String jvmType;

    /**
     * 主机服务名称
     */
    private String service;

    /**
     * 主机服务中一个实例名称
     */
    private String serviceInstance;

    public Long getTestId() {
        return testId;
    }

    public void setTestId(Long testId) {
        this.testId = testId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Boolean getMonitorJvm() {
        return isMonitorJvm;
    }

    public void setMonitorJvm(Boolean monitorJvm) {
        isMonitorJvm = monitorJvm;
    }

    public String getJvmType() {
        return jvmType;
    }

    public void setJvmType(String jvmType) {
        this.jvmType = jvmType;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getServiceInstance() {
        return serviceInstance;
    }

    public void setServiceInstance(String serviceInstance) {
        this.serviceInstance = serviceInstance;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }
}
