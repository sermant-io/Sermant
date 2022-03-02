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

package com.huawei.register.interceptors;

import com.huawei.register.context.RegisterContext;
import com.huawei.register.entity.MicroServiceInstance;
import com.huawei.register.services.RegisterCenterService;
import com.huawei.register.support.InstanceInterceptorSupport;
import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huawei.sermant.core.service.ServiceManager;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.composite.CompositeDiscoveryClient;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * 拦截获取服务列表
 *
 * @author zhouss
 * @since 2021-12-13
 */
public class DiscoveryClientInterceptor extends InstanceInterceptorSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        if (isMarked()) {
            return context;
        }
        try {
            mark();
            String serviceId = (String) context.getArguments()[0];
            final RegisterCenterService service = ServiceManager.getService(RegisterCenterService.class);
            final List<MicroServiceInstance> microServiceInstances = service.getServerList(serviceId);
            if (!microServiceInstances.isEmpty()) {
                context.skip(convertAndMerge(microServiceInstances, serviceId));
            }
        } finally {
            unMark();
        }
        return context;
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private List<ServiceInstance> convertAndMerge(List<MicroServiceInstance> microServiceInstances, String serviceId) {
        List<ServiceInstance> result = new ArrayList<>(microServiceInstances.size());
        if (isOpenMigration() && RegisterContext.INSTANCE.isAvailable()) {
            result.addAll(queryOriginInstances(serviceId));
        }
        for (MicroServiceInstance microServiceInstance : microServiceInstances) {
            if (isOpenMigration()) {
                result.removeIf(originServiceInstance ->
                    Objects.equals(originServiceInstance.getHost(), microServiceInstance.getHost())
                        && originServiceInstance.getPort() == microServiceInstance.getPort());
            }
            result.add((ServiceInstance) buildInstance(microServiceInstance));
        }
        return result.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private List<ServiceInstance> queryOriginInstances(String serviceId) {
        final CompositeDiscoveryClient discoveryClient = (CompositeDiscoveryClient) RegisterContext.INSTANCE
            .getDiscoveryClient();
        try {
            return discoveryClient.getInstances(serviceId);
        } catch (Exception exception) {
            LOGGER.warning(String.format(Locale.ENGLISH,
                "Query Instances from origin register center failed, may be it is not available! reason: %s",
                exception.getMessage()));
            return Collections.emptyList();
        }
    }

    /**
     * 获取实例类与宿主关联 {@link DiscoveryServiceInstance}
     *
     * @return 类权限定名
     */
    @Override
    protected String getInstanceClassName() {
        return "com.huawei.register.interceptors.DiscoveryClientInterceptor$DiscoveryServiceInstance";
    }

    public static class DiscoveryServiceInstance implements ServiceInstance {
        private final MicroServiceInstance microServiceInstance;

        public DiscoveryServiceInstance(final MicroServiceInstance microServiceInstance) {
            this.microServiceInstance = microServiceInstance;
        }

        @Override
        public String getServiceId() {
            return microServiceInstance.getServiceId();
        }

        @Override
        public String getHost() {
            return microServiceInstance.getHost();
        }

        @Override
        public int getPort() {
            return microServiceInstance.getPort();
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
            return microServiceInstance.getMeta();
        }
    }
}
