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

import com.netflix.loadbalancer.Server.MetaInfo;

/**
 * ServiceComb service meta information
 *
 * @author zhouss
 * @since 2022-06-07
 */
public class ServiceCombServerMetaInfo implements MetaInfo {
    private final String instanceId;
    private final String serviceName;

    /**
     * Constructor
     *
     * @param instanceId Instance ID
     * @param serviceName Service name
     */
    public ServiceCombServerMetaInfo(String instanceId, String serviceName) {
        this.instanceId = instanceId;
        this.serviceName = serviceName;
    }

    @Override
    public String getAppName() {
        return this.serviceName;
    }

    @Override
    public String getServerGroup() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getServiceIdForDiscovery() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getInstanceId() {
        return instanceId;
    }
}
