/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.route.common.gray.addr.entity;

import com.huawei.route.common.utils.CollectionUtils;

import java.util.List;

/**
 * 服务地址
 *
 * @author pengyuyi
 * @date 2021/10/15
 */
public class Addr {
    /**
     * 服务名
     */
    private String serviceName;

    /**
     * 实例列表
     */
    private List<Instances> instances;

    /**
     * 注册中心类型
     */
    private String registerType;

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setInstances(List<Instances> instances) {
        this.instances = instances;
    }

    public List<Instances> getInstances() {
        return instances;
    }

    public String getRegisterType() {
        return registerType;
    }

    public void setRegisterType(String registerType) {
        this.registerType = registerType;
    }

    /**
     * 地址是否为空
     *
     * @param addr 地址
     * @return 是否为空
     */
    public static boolean isEmpty(Addr addr) {
        return addr == null || CollectionUtils.isEmpty(addr.instances);
    }
}