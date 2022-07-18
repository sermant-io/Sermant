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

package com.huaweicloud.sermant.router.spring.interceptor;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.router.common.constants.RouterConstant;
import com.huaweicloud.sermant.router.common.utils.CollectionUtils;
import com.huaweicloud.sermant.router.spring.cache.RequestData;
import com.huaweicloud.sermant.router.spring.service.LoadBalancerService;
import com.huaweicloud.sermant.router.spring.service.SpringConfigService;
import com.huaweicloud.sermant.router.spring.utils.ThreadLocalUtils;

import reactor.core.publisher.Flux;

import org.springframework.cloud.loadbalancer.core.DelegatingServiceInstanceListSupplier;

import java.util.List;

/**
 * spring cloud loadbalancer CachingServiceInstanceListSupplier增强类，获取下游实例
 *
 * @author provenceee
 * @since 2022-07-12
 */
public class LoadBalancerInterceptor extends AbstractInterceptor {
    private final SpringConfigService configService;
    private final LoadBalancerService loadBalancerService;

    /**
     * 构造方法
     */
    public LoadBalancerInterceptor() {
        configService = ServiceManager.getService(SpringConfigService.class);
        loadBalancerService = ServiceManager.getService(LoadBalancerService.class);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        if (configService.isInValid(RouterConstant.SPRING_CACHE_NAME)) {
            return context;
        }
        RequestData requestData = ThreadLocalUtils.getRequestData();
        if (requestData == null) {
            return context;
        }
        Object object = context.getObject();
        String serviceId = "";
        if (object instanceof DelegatingServiceInstanceListSupplier) {
            serviceId = ((DelegatingServiceInstanceListSupplier) object).getServiceId();
        }
        Object obj = context.getMemberFieldValue("serviceInstances");
        if (obj instanceof Flux<?>) {
            Flux<List<Object>> flux = (Flux<List<Object>>) obj;
            List<Object> instances = flux.next().block();
            if (CollectionUtils.isEmpty(instances)) {
                return context;
            }
            List<Object> list = loadBalancerService.getTargetInstances(serviceId, instances, requestData.getPath(),
                requestData.getHeader());
            context.skip(Flux.just(list));
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }
}