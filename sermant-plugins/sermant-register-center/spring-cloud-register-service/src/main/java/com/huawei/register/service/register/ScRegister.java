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
import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstanceStatus;
import org.apache.servicecomb.service.center.client.model.MicroservicesResponse;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.serviceregistry.Registration;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
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
                    return microserviceInstance.getProperties();
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

    private List<MicroserviceInstance> getScInstances(String serviceName) {
        if (serviceName == null) {
            return Collections.emptyList();
        }
        String serviceId = getScServiceId(serviceName);
        final List<MicroserviceInstance> microserviceInstances = client.queryInstancesByServiceId(serviceId);
        if (microserviceInstances == null) {
            return Collections.emptyList();
        }
        final Iterator<MicroserviceInstance> iterator = microserviceInstances.iterator();
        while (iterator.hasNext()) {
            final MicroserviceInstance next = iterator.next();
            if (next.getStatus() != MicroserviceInstanceStatus.UP) {
                iterator.remove();
            }
        }
        return microserviceInstances;
    }

    /**
     * 获取Service Center的service Id
     *
     * @param serviceName 服务名, 从其他注册中心获取的servieId均为服务名
     * @return serviceId
     */
    private String getScServiceId(String serviceName) {
        final MicroservicesResponse response = client.getRawClient().getMicroserviceList();
        if (response == null || response.getServices() == null) {
            return null;
        }
        final List<Microservice> services = response.getServices();
        Collections.sort(services, new Comparator<Microservice>() {
            @Override
            public int compare(Microservice o1, Microservice o2) {
                return (int) (Long.parseLong(o2.getModTimestamp()) - Long.parseLong(o1.getModTimestamp()));
            }
        });
        for (Microservice microservice : response.getServices()) {
            if (serviceName.equals(microservice.getServiceName())) {
                return microservice.getServiceId();
            }
        }
        return null;
    }
}
