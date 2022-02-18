/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.route.common.gray.addr.entity;

import com.huawei.route.common.utils.CollectionUtils;

import java.util.List;

/**
 * 服务地址
 *
 * @author provenceee
 * @since 2021/10/15
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