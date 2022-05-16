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
import com.huawei.registry.utils.CommonUtils;
import com.huawei.registry.utils.MarkUtils;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.core.utils.StringUtils;

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

    /**
     * 下游字段名,当前由于ServerList实现复杂，基于修饰者模式进行层层调用，且实现形式多，这里罗列可获取下游服务名的字段，待后续优化
     */
    private static final String[] DOWN_STREAM_FIELD = {"serviceId", "clientName", "ribbon", "clientConfig", "config"};

    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        // ServerList存在层层调用场景，此处使用外部共享线程变量保证仅在其最外层ServerList实现拦截
        if (MarkUtils.isMarked()) {
            // 此处针对当前线程， 如果是拦截器内部调用直接pass
            return context;
        }
        MarkUtils.mark();
        try {
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
            MarkUtils.unMark();
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
            final Optional<Object> serviceName = tryGetServiceName(context.getObject());
            return serviceName.map(service -> (String) service);
        } catch (ClassCastException ex) {
            LOGGER.warning("Can not find down stream service name! "
                + "The service name that has been found is not right name");
        }
        return Optional.empty();
    }

    private Optional<Object> tryGetServiceName(Object enhanceTarget) {
        for (String fieldName : DOWN_STREAM_FIELD) {
            final Optional<Object> targetOptional = CommonUtils.getFieldValue(enhanceTarget, fieldName);
            if (targetOptional.isPresent()) {
                final Object target = targetOptional.get();
                if (target instanceof String) {
                    // 服务名
                    return Optional.of(target);
                } else if (StringUtils.equals("org.springframework.cloud.netflix.ribbon.RibbonProperties",
                    target.getClass().getName())) {
                    return tryGetServiceName(target);
                } else if (target instanceof IClientConfig) {
                    return Optional.ofNullable(((IClientConfig) target).getClientName());
                }
            }
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
