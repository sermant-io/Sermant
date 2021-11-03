/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.report.common.entity;

/**
 * 服务基本信息实体类
 *
 * @author zhengbin zhao
 * @version 1.0
 * @since 2021-07-14
 */
public class ServiceEssentialMessage {
    /**
     * 当前服务名称
     */
    private String serviceName;

    /**
     * Zookeeper中代表根路径；nacos中代表分组；serviceComb中代表所属应用
     */
    private String root;

    /**
     * 注册中心中服务的名称，serviceComb独有，其余为null
     */
    private String registrarServiceName;

    /**
     * 集群名称，nacos独有，其他为null
     */
    private String clusterName;

    /**
     * 注册中心的名称，有ZOOKEEPER、NACOS和SERVICECOMB三种
     */
    private String registry;

    /**
     * 协议，有dubbo和springcloud两种
     */
    private String protocol;

    /**
     * 实例ip地址
     */
    private String ip;

    /**
     * 端口
     */
    private int port;

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

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public String getRegistrarServiceName() {
        return registrarServiceName;
    }

    public void setRegistrarServiceName(String registrarServiceName) {
        this.registrarServiceName = registrarServiceName;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    @Override
    public String toString() {
        return "ServiceEssentialMessage{"
            + "serviceName='" + serviceName + '\''
            + ", root='" + root + '\''
            + ", registrarServiceName='" + registrarServiceName + '\''
            + ", clusterName='" + clusterName + '\''
            + ", registry='" + registry + '\''
            + ", protocol='" + protocol + '\''
            + '}';
    }
}
