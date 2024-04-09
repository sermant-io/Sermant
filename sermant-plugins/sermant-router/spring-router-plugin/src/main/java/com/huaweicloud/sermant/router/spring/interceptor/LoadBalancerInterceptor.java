/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
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
import com.huaweicloud.sermant.core.utils.StringUtils;
import com.huaweicloud.sermant.router.common.request.RequestData;
import com.huaweicloud.sermant.router.common.utils.CollectionUtils;
import com.huaweicloud.sermant.router.common.utils.ReflectUtils;
import com.huaweicloud.sermant.router.common.utils.ThreadLocalUtils;
import com.huaweicloud.sermant.router.spring.service.LoadBalancerService;

import java.util.List;
import java.util.Optional;

/**
 * spring cloud loadbalancer Interception points
 *
 * @author provenceee
 * @since 2024-01-16
 */
public class LoadBalancerInterceptor extends AbstractInterceptor {
    private final LoadBalancerService loadBalancerService;

    /**
     * Constructor
     */
    public LoadBalancerInterceptor() {
        loadBalancerService = ServiceManager.getService(LoadBalancerService.class);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        Object object = context.getObject();
        String serviceId = getServiceId(object).orElse(null);
        if (StringUtils.isBlank(serviceId)) {
            return context;
        }
        Object[] arguments = context.getArguments();
        List<Object> instances = (List<Object>) arguments[0];
        if (CollectionUtils.isEmpty(instances)) {
            return context;
        }
        RequestData requestData = ThreadLocalUtils.getRequestData();
        List<Object> targetInstances = loadBalancerService.getTargetInstances(serviceId, instances, requestData);
        arguments[0] = targetInstances;
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }

    private Optional<String> getServiceId(Object object) {
        return ReflectUtils.getFieldValue(object, "serviceId").map(obj -> (String) obj);
    }
}