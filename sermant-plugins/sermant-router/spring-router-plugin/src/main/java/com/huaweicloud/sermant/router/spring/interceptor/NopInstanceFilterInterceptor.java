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
import com.huaweicloud.sermant.router.common.request.RequestData;
import com.huaweicloud.sermant.router.common.utils.ThreadLocalUtils;
import com.huaweicloud.sermant.router.spring.service.LoadBalancerService;

import java.util.List;

/**
 * 注册插件拦截点
 *
 * @author provenceee
 * @since 2022-10-08
 */
public class NopInstanceFilterInterceptor extends AbstractInterceptor {
    private final LoadBalancerService loadBalancerService;

    /**
     * 构造方法
     */
    public NopInstanceFilterInterceptor() {
        loadBalancerService = ServiceManager.getService(LoadBalancerService.class);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        Object[] arguments = context.getArguments();
        String serviceName = (String) arguments[0];
        List<Object> instances = (List<Object>) arguments[1];
        RequestData requestData = ThreadLocalUtils.getRequestData();
        List<Object> targetInstances = loadBalancerService.getTargetInstances(serviceName, instances, requestData);
        context.skip(targetInstances);
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        ThreadLocalUtils.removeRequestData();
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        ThreadLocalUtils.removeRequestData();
        return context;
    }
}