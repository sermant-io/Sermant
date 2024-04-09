/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.registry.auto.sc;

import com.huawei.registry.entity.MicroServiceInstance;

import com.netflix.loadbalancer.Server;

import java.util.Map;

/**
 * ServiceComb service instance
 *
 * @author zhouss
 * @since 2022-05-19
 */
public class ServiceCombServer extends Server {
    private final MicroServiceInstance microServiceInstance;

    private MetaInfo metaInfo;

    /**
     * Constructor
     *
     * @param microServiceInstance Instance information
     */
    public ServiceCombServer(MicroServiceInstance microServiceInstance) {
        super(microServiceInstance.isSecure() ? "https" : "http", microServiceInstance.getIp(),
                microServiceInstance.getPort());
        this.microServiceInstance = microServiceInstance;
    }

    @Override
    public MetaInfo getMetaInfo() {
        if (metaInfo == null) {
            this.metaInfo = new ServiceCombServerMetaInfo(microServiceInstance.getInstanceId(),
                    microServiceInstance.getServiceName());
        }
        return metaInfo;
    }

    /**
     * Get service metadata
     *
     * @return Metadata
     */
    public Map<String, String> getMetadata() {
        return microServiceInstance.getMetadata();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
