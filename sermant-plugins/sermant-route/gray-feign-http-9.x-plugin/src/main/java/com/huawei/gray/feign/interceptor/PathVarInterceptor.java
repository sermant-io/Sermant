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
 */

/*
 * Based on org/apache/skywalking/apm/plugin/feign/http/v9/PathVarInterceptor.java
 * from the Apache Skywalking project.
 */

package com.huawei.gray.feign.interceptor;

import com.huawei.gray.feign.service.PathVarService;
import com.huawei.sermant.core.agent.common.BeforeResult;
import com.huawei.sermant.core.agent.interceptor.InstanceMethodInterceptor;
import com.huawei.sermant.core.service.ServiceManager;

import java.lang.reflect.Method;

/**
 * 拦截feign.ReflectiveFeign.BuildTemplateByResolvingArgs获得url解析前后的结果
 *
 * @author lilai
 * @since 2021-11-03
 */
public class PathVarInterceptor implements InstanceMethodInterceptor {
    private PathVarService pathVarService;

    /**
     * feign.ReflectiveFeign.BuildTemplateByResolvingArgs.resolve解析url路径参数前将原始url放入线程变量
     *
     * @param obj 拦截对象
     * @param method 拦截方法
     * @param arguments 方法参数
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
     * @param obj 拦截对象
     * @param method 拦截方法
     * @param arguments 方法参数
     * @param result 返回结果
     */
    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) throws Exception {
        pathVarService.after(obj, method, arguments, result);
        return result;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable th) {
        pathVarService.onThrow(obj, method, arguments, th);
    }
}
