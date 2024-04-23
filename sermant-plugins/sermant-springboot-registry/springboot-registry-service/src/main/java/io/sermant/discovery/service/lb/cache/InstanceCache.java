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

package io.sermant.discovery.service.lb.cache;

import io.sermant.discovery.entity.ServiceInstance;

import java.util.List;

/**
 * Instance cache class
 *
 * @author zhouss
 * @since 2022-09-26
 */
public class InstanceCache {
    private String serviceName;

    private List<ServiceInstance> instances;

    /**
     * Update time
     */
    private long updateTimestamp;

    /**
     * Instance caching
     *
     * @param serviceName Target service name
     * @param instances Cache
     */
    public InstanceCache(String serviceName, List<ServiceInstance> instances) {
        this.serviceName = serviceName;
        this.instances = instances;
        this.updateTimestamp = System.currentTimeMillis();
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

    public long getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setUpdateTimestamp(long updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }
}
