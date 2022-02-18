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

package com.huawei.gray.feign.interceptor;

import com.huawei.gray.feign.service.ServiceInstanceListSupplierService;
import com.huawei.sermant.core.agent.common.BeforeResult;
import com.huawei.sermant.core.agent.interceptor.InstanceMethodInterceptor;
import com.huawei.sermant.core.service.ServiceManager;

import java.lang.reflect.Method;

/**
 * 获取服务下游列表拦截器
 *
 * @author fuziye
 * @since 2021-12-29
 */
public class ServiceInstanceListSupplierInterceptor implements InstanceMethodInterceptor {
    private ServiceInstanceListSupplierService serviceInstanceListSupplierService;

    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
        serviceInstanceListSupplierService = ServiceManager.getService(ServiceInstanceListSupplierService.class);
        serviceInstanceListSupplierService.before(obj, method, arguments, beforeResult);
    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) throws Exception {
        serviceInstanceListSupplierService.after(obj, method, arguments, result);
        return result;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable th) {
    }
}
