/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.common.report.cache;

import com.huawei.route.common.report.common.entity.ServiceEssentialMessage;

/**
 * 服务注册的基本信息缓存
 *
 * @author zhengbin zhao
 * @version 1.0
 * @since 2021-07-14
 */
public class ServiceEssentialMessageCache {
    private static final ServiceEssentialMessage SERVICE_ESSENTIAL_MESSAGE = new ServiceEssentialMessage();
    private static final ServiceEssentialMessageCache ESSENTIAL_MESSAGE_CACHE = new ServiceEssentialMessageCache();

    /**
     * 获取缓存单例
     *
     * @return 单例
     */
    public static ServiceEssentialMessageCache getInstance() {
        return ESSENTIAL_MESSAGE_CACHE;
    }

    /**
     * 设置服务名
     *
     * @param serviceName 服务名
     */
    public void setServiceName(String serviceName) {
        SERVICE_ESSENTIAL_MESSAGE.setServiceName(serviceName);
    }

    /**
     * 设置命名空间或者路径
     *
     * @param root 命名空间或者路径
     */
    public void setRoot(String root) {
        SERVICE_ESSENTIAL_MESSAGE.setRoot(root);
    }

    /**
     * 获取服务名
     *
     * @return 服务名
     */
    public String getServiceName() {
        return SERVICE_ESSENTIAL_MESSAGE.getServiceName();
    }

    /**
     * 获取命名空间或者根路径
     *
     * @return 命名空间或者根路径
     */
    public String getRoot() {
        return SERVICE_ESSENTIAL_MESSAGE.getRoot();
    }

    /**
     * 获取注册服务名
     *
     * @return 注册服务名
     */
    public String getRegistrarServiceName() {
        return SERVICE_ESSENTIAL_MESSAGE.getRegistrarServiceName();
    }

    /**
     * 设置注册服务名
     *
     * @param registrarServiceName 注册服务名
     */
    public void setRegistrarServiceName(String registrarServiceName) {
        SERVICE_ESSENTIAL_MESSAGE.setRegistrarServiceName(registrarServiceName);
    }

    /**
     * 获取集群名
     *
     * @return 集群名
     */
    public String getClusterName() {
        return SERVICE_ESSENTIAL_MESSAGE.getClusterName();
    }

    /**
     * 设置集群名
     *
     * @param clusterName 集群名
     */
    public void setClusterName(String clusterName) {
        SERVICE_ESSENTIAL_MESSAGE.setClusterName(clusterName);
    }

    /**
     * 获取注册中心类型
     *
     * @return 注册中心类型
     */
    public String getRegistry() {
        return SERVICE_ESSENTIAL_MESSAGE.getRegistry();
    }

    /**
     * 设置注册中心类型
     *
     * @param registry 注册中心类型
     */
    public void setRegistry(String registry) {
        SERVICE_ESSENTIAL_MESSAGE.setRegistry(registry);
    }

    /**
     * 获取协议
     *
     * @return 协议
     */
    public String getProtocol() {
        return SERVICE_ESSENTIAL_MESSAGE.getProtocol();
    }

    /**
     * 设置协议
     *
     * @param protocol 协议
     */
    public void setProtocol(String protocol) {
        SERVICE_ESSENTIAL_MESSAGE.setProtocol(protocol);
    }
}
