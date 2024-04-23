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

package io.sermant.discovery.service.lb.discovery.zk;

import io.sermant.discovery.entity.ServiceInstance;
import io.sermant.discovery.service.ex.QueryInstanceException;
import io.sermant.discovery.service.lb.discovery.ServiceDiscoveryClient;

import java.io.IOException;
import java.util.Collection;

/**
 * ZK client proxy
 *
 * @author zhouss
 * @since 2022-10-08
 */
public class ZkDiscoveryClientProxy implements ServiceDiscoveryClient {
    private final ZkServiceManager zkServiceManager = new ZkServiceManager();

    @Override
    public void init() {
        zkServiceManager.chooseService().init();
    }

    @Override
    public boolean registry(ServiceInstance serviceInstance) {
        return zkServiceManager.chooseService().registry(serviceInstance);
    }

    @Override
    public Collection<ServiceInstance> getInstances(String serviceId) throws QueryInstanceException {
        return zkServiceManager.chooseService().getInstances(serviceId);
    }

    @Override
    public Collection<String> getServices() {
        return zkServiceManager.chooseService().getServices();
    }

    @Override
    public boolean unRegistry() {
        return zkServiceManager.chooseService().unRegistry();
    }

    @Override
    public String name() {
        return "Zookeeper";
    }

    @Override
    public void close() throws IOException {
        zkServiceManager.chooseService().close();
    }
}
