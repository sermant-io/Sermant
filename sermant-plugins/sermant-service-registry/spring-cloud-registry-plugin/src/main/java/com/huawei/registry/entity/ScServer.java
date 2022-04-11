/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

import com.netflix.loadbalancer.Server;

/**
 * server 信息定义
 *
 * @since 2022-02-11
 */
public class ScServer extends Server {
    private final MicroServiceInstance microServiceInstance;
    private final String serviceName;
    private MetaInfo metaInfo;

    /**
     * 构造器
     *
     * @param microServiceInstance 实例信息
     * @param serviceName          服务名
     */
    public ScServer(final MicroServiceInstance microServiceInstance, String serviceName) {
        super(microServiceInstance.getHost(), microServiceInstance.getPort());
        this.microServiceInstance = microServiceInstance;
        this.serviceName = serviceName;
    }

    @Override
    public MetaInfo getMetaInfo() {
        if (metaInfo == null) {
            this.metaInfo = new MetaInfo() {
                @Override
                public String getAppName() {
                    return serviceName;
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
                    return microServiceInstance.getInstanceId();
                }
            };
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
}
