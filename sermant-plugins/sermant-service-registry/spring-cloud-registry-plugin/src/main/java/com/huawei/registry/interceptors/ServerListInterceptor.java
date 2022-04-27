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

package com.huawei.registry.interceptors;

import com.huawei.registry.context.RegisterContext;
import com.huawei.registry.entity.MicroServiceInstance;
import com.huawei.registry.services.RegisterCenterService;
import com.huawei.registry.support.InstanceInterceptorSupport;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * 拦截替换服务列表
 *
 * @author zhouss
 * @since 2021-12-13
 */
public class ServerListInterceptor extends InstanceInterceptorSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        if (isMarked()) {
            // 此处针对当前线程， 如果是拦截器内部调用直接pass
            return context;
        }
        try {
            mark();
            Optional<String> serviceIdOption = getServiceName(context);
            if (!serviceIdOption.isPresent()) {
                return context;
            }
            final RegisterCenterService service = ServiceManager.getService(RegisterCenterService.class);
            String serviceName = serviceIdOption.get();
            final List<MicroServiceInstance> serverList = service.getServerList(serviceName);
            if (!serverList.isEmpty()) {
                // 单注册中心场景无需合并
                context.skip(convertAndMerge(context.getObject(), serverList, serviceName));
            }
        } finally {
            unMark();
        }
        return context;
    }

    /**
     * 获取下游服务名
     *
     * @return 下游服务名称
     */
    private Optional<String> getServiceName(ExecuteContext context) {
        try {
            Object serviceId = context.getMemberFieldValue("serviceId");
            if (serviceId == null) {
                final Object clientConfig = context.getMemberFieldValue("clientConfig");
                if (clientConfig instanceof IClientConfig) {
                    serviceId = ((IClientConfig) clientConfig).getClientName();
                }
            }
            return Optional.ofNullable((String) serviceId);
        } catch (ClassCastException ex) {
            LOGGER.warning("Can not find down stream service name! "
                + "The service name that has been found is not right name");
        }
        return Optional.empty();
    }

    /**
     * 合并原注册中心的服务
     *
     * @param serviceInstances 从迁移后的注册中心查询的实例列表
     * @return 服务列表
     */
    private List<Server> convertAndMerge(Object obj, List<MicroServiceInstance> serviceInstances, String serviceName) {
        List<Server> result = new ArrayList<>(serviceInstances.size());
        if (isOpenMigration() && RegisterContext.INSTANCE.isAvailable()) {
            result.addAll(queryInstances(obj));
        }
        for (MicroServiceInstance microServiceInstance : serviceInstances) {
            if (isOpenMigration()) {
                result.removeIf(originServiceInstance ->
                    Objects.equals(originServiceInstance.getHost(), microServiceInstance.getHost())
                        && originServiceInstance.getPort() == microServiceInstance.getPort());
            }
            buildInstance(microServiceInstance, serviceName)
                .ifPresent(instance -> result.add((Server) instance));
        }
        return result.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    private List<Server> queryInstances(Object obj) {
        ServerList<Server> serverList = (ServerList<Server>) obj;
        try {
            return serverList.getUpdatedListOfServers();
        } catch (Exception exception) {
            LOGGER.warning(String.format(Locale.ENGLISH,
                "Query Instances from origin register center failed, may be it is not available! reason: %s",
                exception.getMessage()));
            return Collections.emptyList();
        }
    }

    @Override
    protected String getInstanceClassName() {
        return "com.huawei.registry.entity.ScServer";
    }
}
