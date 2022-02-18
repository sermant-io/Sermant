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

package com.huawei.gray.feign.interceptor;

import com.huawei.gray.feign.service.RegisterService;
import com.huawei.sermant.core.agent.common.BeforeResult;
import com.huawei.sermant.core.agent.interceptor.InstanceMethodInterceptor;
import com.huawei.sermant.core.service.ServiceManager;

import java.lang.reflect.Method;

/**
 * 获取当前服务信息
 *
 * @author provenceee
 * @since 2022/2/18
 */
public abstract class RegisterInterceptor implements InstanceMethodInterceptor {
    private RegisterService registerService;

    /**
     * 获取当前服务信息
     *
     * @param obj 拦截对象
     * @param method 拦截方法
     * @param arguments 方法参数
     * @param beforeResult change this result, if you want to truncate the method.
     */
    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) {
        registerService = ServiceManager.getService(RegisterService.class);
        registerService.before(obj, method, arguments, beforeResult);
    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) {
        registerService.after(obj, method, arguments, result);
        return result;
    }

    @Override
    public abstract void onThrow(Object obj, Method method, Object[] arguments, Throwable th);
}
