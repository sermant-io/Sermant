/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.registry.entity;

import com.huawei.registry.auto.sc.ServiceCombServerMetaInfo;

import com.netflix.loadbalancer.Server;

import java.util.Map;

/**
 * server Information Definitions {@link com.netflix.loadbalancer.ServerList}
 *
 * @author zhouss
 * @since 2022-04-11
 */
public class ScServer extends Server {
    private final MicroServiceInstance microServiceInstance;

    private final String serviceName;

    private MetaInfo metaInfo;

    /**
     * Constructor
     *
     * @param microServiceInstance Instance information
     * @param serviceName Service name
     */
    public ScServer(final MicroServiceInstance microServiceInstance, String serviceName) {
        super(microServiceInstance.isSecure() ? "https" : "http", microServiceInstance.getIp(),
                microServiceInstance.getPort());
        this.microServiceInstance = microServiceInstance;
        this.serviceName = serviceName;
    }

    @Override
    public MetaInfo getMetaInfo() {
        if (metaInfo == null) {
            this.metaInfo = new ServiceCombServerMetaInfo(microServiceInstance.getInstanceId(), serviceName);
        }
        return this.metaInfo;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Obtain the service meta-information
     *
     * @return Service meta information
     */
    public Map<String, String> getMetadata() {
        return microServiceInstance.getMetadata();
    }
}
