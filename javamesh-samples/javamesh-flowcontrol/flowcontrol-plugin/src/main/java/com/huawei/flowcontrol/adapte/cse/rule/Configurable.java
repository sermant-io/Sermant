/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adapte.cse.rule;

/**
 * 可配置的规则
 * 默认都含有名称与目标服务
 *
 * @author zhouss
 * @since 2021-11-15
 */
public abstract class Configurable {
    /**
     * 配置名
     */
    protected String name;

    /**
     * 目标服务
     */
    protected String services;

    /**
     * 是否合法
     *
     * @return 是否合法
     */
    public abstract boolean isValid();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getServices() {
        return services;
    }

    public void setServices(String services) {
        this.services = services;
    }
}
