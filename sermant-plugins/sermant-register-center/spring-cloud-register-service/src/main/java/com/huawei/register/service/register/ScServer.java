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

package com.huawei.register.service.register;

import com.huawei.register.service.utils.CommonUtils;
import com.netflix.loadbalancer.Server;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.springframework.cloud.client.ServiceInstance;

/**
 * SC服务
 *
 * @author zhouss
 * @since 2021-12-29
 */
public class ScServer extends Server {
    private final MetaInfo metaInfo;

    public ScServer(final MicroserviceInstance microserviceInstance) {
        super(microserviceInstance.getHostName(),
                CommonUtils.getPortByEndpoint(microserviceInstance.getEndpoints().get(0)));
        this.metaInfo = new Server.MetaInfo() {
            @Override
            public String getAppName() {
                return microserviceInstance.getServiceId();
            }

            @Override
            public String getServerGroup() {
                return null;
            }

            @Override
            public String getServiceIdForDiscovery() {
                return null;
            }

            @Override
            public String getInstanceId() {
                return microserviceInstance.getInstanceId();
            }
        };
    }

    public ScServer(final ServiceInstance serviceInstance) {
        super(serviceInstance.getHost(), serviceInstance.getPort());
        this.metaInfo = new Server.MetaInfo() {
            @Override
            public String getAppName() {
                return serviceInstance.getServiceId();
            }

            @Override
            public String getServerGroup() {
                return null;
            }

            @Override
            public String getServiceIdForDiscovery() {
                return null;
            }

            @Override
            public String getInstanceId() {
                return serviceInstance.getInstanceId();
            }
        };
    }

    @Override
    public MetaInfo getMetaInfo() {
        return this.metaInfo;
    }

}
