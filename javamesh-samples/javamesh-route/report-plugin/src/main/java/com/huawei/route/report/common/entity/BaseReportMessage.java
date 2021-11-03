/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.report.common.entity;

/**
 * 基础信息上报类
 *
 * @author zhouss
 * @since 2021-11-03
 */
public class BaseReportMessage {
    /**
     * 当前服务名称
     */
    protected String serviceName;

    /**
     * Ldc名称
     */
    protected String ldc;

    /**
     * 实例ip地址
     */
    protected String ip;

    /**
     * 端口
     */
    protected int port;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getLdc() {
        return ldc;
    }

    public void setLdc(String ldc) {
        this.ldc = ldc;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BaseReportMessage that = (BaseReportMessage) o;

        if (port != that.port) {
            return false;
        }
        if (serviceName != null ? !serviceName.equals(that.serviceName) : that.serviceName != null) {
            return false;
        }
        return ip.equals(that.ip);
    }

    @Override
    public int hashCode() {
        int result = serviceName != null ? serviceName.hashCode() : 0;
        result = 31 * result + ip.hashCode();
        result = 31 * result + port;
        return result;
    }
}
