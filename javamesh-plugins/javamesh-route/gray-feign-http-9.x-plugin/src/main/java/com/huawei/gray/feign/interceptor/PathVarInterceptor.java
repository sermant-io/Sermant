/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.gray.feign.interceptor;

import com.huawei.javamesh.core.agent.common.BeforeResult;
import com.huawei.javamesh.core.agent.interceptor.InstanceMethodInterceptor;
import com.huawei.javamesh.core.service.ServiceManager;
import com.huawei.gray.feign.service.PathVarService;

import java.lang.reflect.Method;

public class PathVarInterceptor implements InstanceMethodInterceptor {
    private PathVarService pathVarService;

    /**
     * feign.ReflectiveFeign.BuildTemplateByResolvingArgs.resolve解析url路径参数前将原始url放入线程变量
     *
     * @param obj          拦截对象
     * @param method       拦截方法
     * @param arguments    方法参数
     * @param beforeResult 返回结果
     */
    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
        pathVarService = ServiceManager.getService(PathVarService.class);
        pathVarService.before(obj, method, arguments, beforeResult);
    }

    /**
     * url路径参数解析后的结果放入线程变量
     *
     * @param obj       拦截对象
     * @param method    拦截方法
     * @param arguments 方法参数
     * @param result    返回结果
     */
    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) throws Exception {
        pathVarService.after(obj, method, arguments, result);
        return result;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable t) {
        pathVarService.onThrow(obj, method, arguments, t);
    }
}
