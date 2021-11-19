/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.service.influxdb;

/**
 * 功能描述：sql使用到的参数
 *
 * @author z30009938
 * @since 2021-11-14
 */
public class SqlParam {
    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 指标名称
     */
    private String measurement;

    /**
     * 监控主机ip地址
     */
    private String ip;

    /**
     * 监控主机
     */
    private String host;

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

    public String getMeasurement() {
        return measurement;
    }

    public void setMeasurement(String measurement) {
        this.measurement = measurement;
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

    @Override
    public String toString() {
        return "{" +
                "startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", measurement='" + measurement + '\'' +
                ", ip='" + ip + '\'' +
                ", host='" + host + '\'' +
                '}';
    }
}
