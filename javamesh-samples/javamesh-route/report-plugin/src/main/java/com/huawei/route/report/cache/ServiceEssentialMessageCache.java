/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.report.cache;

import com.huawei.route.report.common.entity.ServiceEssentialMessage;

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

    public static ServiceEssentialMessageCache getInstance() {
        return ESSENTIAL_MESSAGE_CACHE;
    }

    public void setServiceName(String serviceName) {
        SERVICE_ESSENTIAL_MESSAGE.setServiceName(serviceName);
    }

    public void setRoot(String root) {
        SERVICE_ESSENTIAL_MESSAGE.setRoot(root);
    }

    public String getServiceName() {
        return SERVICE_ESSENTIAL_MESSAGE.getServiceName();
    }

    public String getRoot() {
        return SERVICE_ESSENTIAL_MESSAGE.getRoot();
    }

    public String getRegistrarServiceName() {
        return SERVICE_ESSENTIAL_MESSAGE.getRegistrarServiceName();
    }

    public void setRegistrarServiceName(String registrarServiceName) {
        SERVICE_ESSENTIAL_MESSAGE.setRegistrarServiceName(registrarServiceName);
    }

    public String getClusterName() {
        return SERVICE_ESSENTIAL_MESSAGE.getClusterName();
    }

    public void setClusterName(String clusterName) {
        SERVICE_ESSENTIAL_MESSAGE.setClusterName(clusterName);
    }

    public String getRegistry() {
        return SERVICE_ESSENTIAL_MESSAGE.getRegistry();
    }

    public void setRegistry(String registry) {
        SERVICE_ESSENTIAL_MESSAGE.setRegistry(registry);
    }

    public String getProtocol() {
        return SERVICE_ESSENTIAL_MESSAGE.getProtocol();
    }

    public void setProtocol(String protocol) {
        SERVICE_ESSENTIAL_MESSAGE.setProtocol(protocol);
    }
}
