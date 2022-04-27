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

import com.huawei.gray.feign.service.LoadBalancerClientService;

import com.huaweicloud.sermant.core.agent.common.BeforeResult;
import com.huaweicloud.sermant.core.agent.interceptor.InstanceMethodInterceptor;
import com.huaweicloud.sermant.core.service.ServiceManager;

import java.lang.reflect.Method;

/**
 * 拦截LoadBalancerFeignClientInstrumentation的execute方法，获取request的域名host（服务名称）
 *
 * @author lilai
 * @since 2021-11-03
 */
public class LoadBalancerClientInterceptor implements InstanceMethodInterceptor {
    private LoadBalancerClientService loadBalancerClientService;

    /**
     * 拦截获取下游服务名称，并存放到线程变量中
     *
     * @param obj 拦截对象
     * @param method 拦截方法
     * @param arguments 方法参数
     * @param beforeResult change this result, if you want to truncate the method.
     */
    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
        loadBalancerClientService = ServiceManager.getService(LoadBalancerClientService.class);
        loadBalancerClientService.before(obj, method, arguments, beforeResult);
    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) throws Exception {
        return result;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable th) {
        loadBalancerClientService.onThrow(obj, method, arguments, th);
    }
}
