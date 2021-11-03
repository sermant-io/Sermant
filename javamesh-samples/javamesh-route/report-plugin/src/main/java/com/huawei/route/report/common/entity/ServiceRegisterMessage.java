/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.report.common.entity;

import com.alibaba.fastjson.JSONArray;
import com.huawei.apm.bootstrap.lubanops.utils.StringUtils;

import java.util.Arrays;

/**
 * 服务注册信息的实体类
 *
 * @author zhengbin zhao
 * @version 1.0
 * @since 2021-07-14
 */
public class ServiceRegisterMessage {
    /**
     * 当前服务名称
     */
    private String serviceName;

    /**
     * 下游服务名称
     */
    private String downServiceName;

    /**
     * Ldc名称
     */
    private String ldc;

    /**
     * Zookeeper中代表根路径；nacos中代表分组；serviceComb中代表所属应用
     */
    private String root;

    /**
     * 注册中心的名称，有ZOOKEEPER、NACOS和SERVICECOMB三种
     */
    private String registry;

    /**
     * 协议，有dubbo和springcloud两种
     */
    private String protocol;

    /**
     * Ldc的businesses信息
     */
    private JSONArray businesses;

    /**
     * 注册中心中服务的名称，serviceComb独有，其余为null
     */
    private String registrarServiceName;

    /**
     * 实例ip地址
     */
    private String ip;

    /**
     * 端口
     */
    private int port;

    /**
     * 集群名称，nacos独有，其他为null
     */
    private String clusterName;

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

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getDownServiceName() {
        return downServiceName;
    }

    public void setDownServiceName(String downServiceName) {
        this.downServiceName = downServiceName;
    }

    public String getLdc() {
        return ldc;
    }

    public void setLdc(String ldc) {
        this.ldc = ldc;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public String getRegistry() {
        return registry;
    }

    public void setRegistry(String registry) {
        this.registry = registry;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public JSONArray getBusinesses() {
        return businesses;
    }

    public void setBusinesses(JSONArray businesses) {
        this.businesses = businesses;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getRegistrarServiceName() {
        return registrarServiceName;
    }

    public void setRegistrarServiceName(String registrarServiceName) {
        this.registrarServiceName = registrarServiceName;
    }

    @Override
    public String toString() {
        return "ServiceRegisterMessage{"
                + "serviceName='" + serviceName + '\''
                + ", downServiceName='" + downServiceName + '\''
                + ", ldc='" + ldc + '\''
                + ", root='" + root + '\''
                + ", registry='" + registry + '\''
                + ", protocol='" + protocol + '\''
                + ", businesses=" + businesses
                + ", clusterName='" + clusterName + '\''
                + ", registrarServiceName='" + registrarServiceName + '\''
                + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ServiceRegisterMessage that = (ServiceRegisterMessage) obj;
        return port == that.port
                && StringUtils.equals(serviceName, that.serviceName)
                && StringUtils.equals(ldc, that.ldc)
                && StringUtils.equals(root, that.root)
                && StringUtils.equals(ip, that.ip);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new Object[]{serviceName, ldc, root, ip, port});
    }
}
