/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.discovery.service.lb.cache;

import com.huawei.discovery.entity.ServiceInstance;

import java.util.List;

/**
 * 实例缓存类
 *
 * @author zhouss
 * @since 2022-09-26
 */
public class InstanceCache {
    private String serviceName;

    private List<ServiceInstance> instances;

    /**
     * 实例缓存
     *
     * @param serviceName 目标服务名
     * @param instances 缓存
     */
    public InstanceCache(String serviceName, List<ServiceInstance> instances) {
        this.serviceName = serviceName;
        this.instances = instances;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public List<ServiceInstance> getInstances() {
        return instances;
    }

    public void setInstances(List<ServiceInstance> instances) {
        this.instances = instances;
    }
}
