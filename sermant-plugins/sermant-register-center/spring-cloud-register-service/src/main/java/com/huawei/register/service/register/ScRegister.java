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

import com.huawei.register.service.client.ScClient;
import com.huawei.register.service.utils.CommonUtils;
import com.netflix.loadbalancer.Server;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.serviceregistry.Registration;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * SC注册实现
 *
 * @author zhouss
 * @since 2021-12-17
 */
public class ScRegister implements Register {
    private ScClient client;

    @Override
    public void start() {
        client = new ScClient();
        client.start();
    }

    @Override
    public void stop() {
        client.stop();
    }

    @Override
    public void register(Object rawRegistration) {
        if (rawRegistration instanceof Registration) {
            Registration registration = (Registration) rawRegistration;
            client.register(registration);
        }
    }

    @Override
    public List<ServiceInstance> getDiscoveryServerList(String serviceId) {
        final List<MicroserviceInstance> microserviceInstances = getScInstances(serviceId);
        final List<ServiceInstance> serviceInstances = new ArrayList<ServiceInstance>();
        for (final MicroserviceInstance microserviceInstance : microserviceInstances) {
            serviceInstances.add(new ServiceInstance() {
                @Override
                public String getServiceId() {
                    return microserviceInstance.getServiceId();
                }

                @Override
                public String getHost() {
                    return microserviceInstance.getHostName();
                }

                @Override
                public int getPort() {
                    return CommonUtils.getPortByEndpoint(microserviceInstance.getEndpoints().get(0));
                }

                @Override
                public boolean isSecure() {
                    return false;
                }

                @Override
                public URI getUri() {
                    return null;
                }

                @Override
                public Map<String, String> getMetadata() {
                    return null;
                }
            });
        }
        return serviceInstances;
    }

    @Override
    public List<ScServer> getServerList(String serviceId) {
        final List<MicroserviceInstance> microserviceInstances = getScInstances(serviceId);
        final List<ScServer> servers = new ArrayList<ScServer>();
        for (final MicroserviceInstance microserviceInstance : microserviceInstances) {
            servers.add(new ScServer(microserviceInstance));
        }
        return servers;
    }

    @Override
    public RegisterType registerType() {
        return RegisterType.SERVICE_COMB;
    }

    private List<MicroserviceInstance> getScInstances(String serviceId) {
        if (serviceId == null) {
            return Collections.emptyList();
        }
        return client.queryInstancesByServiceId(serviceId);
    }

    static class ScServer extends Server {
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

        @Override
        public MetaInfo getMetaInfo() {
            return this.metaInfo;
        }
    }
}
