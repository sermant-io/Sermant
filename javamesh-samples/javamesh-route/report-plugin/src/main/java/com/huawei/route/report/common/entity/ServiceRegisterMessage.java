/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.report.common.entity;

/**
 * 服务注册信息的实体类
 *
 * @author zhengbin zhao
 * @version 1.0
 * @since 2021-07-14
 */
public class ServiceRegisterMessage extends BaseReportMessage {
    /**
     * 下游服务名称
     */
    private String downServiceName;

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
    private Object businesses;

    /**
     * 注册中心中服务的名称，serviceComb独有，其余为null
     */
    private String registrarServiceName;

    /**
     * 集群名称，nacos独有，其他为null
     */
    private String clusterName;

    /**
     * 命名空间，nacos独有
     */
    private String namespaceId;

    public String getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }

    public String getDownServiceName() {
        return downServiceName;
    }

    public void setDownServiceName(String downServiceName) {
        this.downServiceName = downServiceName;
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

    public Object getBusinesses() {
        return businesses;
    }

    public void setBusinesses(Object businesses) {
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
        return "ServiceRegisterMessage{" +
                "serviceName='" + serviceName + '\'' +
                ", ldc='" + ldc + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", downServiceName='" + downServiceName + '\'' +
                ", root='" + root + '\'' +
                ", registry='" + registry + '\'' +
                ", protocol='" + protocol + '\'' +
                ", businesses=" + businesses +
                ", registrarServiceName='" + registrarServiceName + '\'' +
                ", clusterName='" + clusterName + '\'' +
                ", namespaceId='" + namespaceId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
