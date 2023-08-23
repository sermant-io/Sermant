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

import com.huawei.registry.config.RegisterDynamicConfig;
import com.huawei.registry.context.RegisterContext;
import com.huawei.registry.services.RegisterCenterService;
import com.huawei.registry.support.InstanceInterceptorSupport;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;

import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 拦截获取服务名列表
 *
 * @author zhouss
 * @since 2021-12-13
 */
public class DiscoveryClientServiceInterceptor extends InstanceInterceptorSupport {
    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        if (RegisterContext.INSTANCE.isAvailable()
                && !RegisterDynamicConfig.INSTANCE.isNeedCloseOriginRegisterCenter()) {
            return context;
        }
        final Object target = context.getObject();
        context.skip(isWebfLux(target) ? Flux.fromIterable(Collections.emptyList()) : Collections.emptyList());
        return context;
    }

    @Override
    public ExecuteContext doAfter(ExecuteContext context) {
        final RegisterCenterService service = PluginServiceManager.getPluginService(RegisterCenterService.class);
        final List<String> services = new ArrayList<>(service.getServices());
        final Object target = context.getObject();
        final Object contextResult = context.getResult();
        context.changeResult(
                isWebfLux(target) ? getServicesWithFlux(services, target, contextResult) : getServices(services,
                        target, contextResult));
        return context;
    }

    private Flux<String> getServicesWithFlux(List<String> services, Object target, Object contextResult) {
        return Flux.fromIterable(getServices(services, target, contextResult));
    }

    private List<String> getServices(List<String> services, Object target, Object contextResult) {
        // 合并两个注册中心
        if (isWebfLux(target)) {
            final Flux<String> originServicesFlux = (Flux<String>) contextResult;
            final List<String> originServices = new ArrayList<>();
            originServicesFlux.collectList().subscribe(originServices::addAll);
            if (originServices.size() == 0) {
                return services;
            }
            services.addAll(originServices);
        } else {
            services.addAll((List<String>) contextResult);
        }
        return services.stream().distinct().collect(Collectors.toList());
    }

    @Override
    protected String getInstanceClassName() {
        return "";
    }
}
