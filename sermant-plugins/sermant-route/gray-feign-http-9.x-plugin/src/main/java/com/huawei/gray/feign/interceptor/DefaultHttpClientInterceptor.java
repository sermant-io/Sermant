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
 * Based on org/apache/skywalking/apm/plugin/feign/http/v9/DefaultHttpClientInterceptor.java
 * from the Apache Skywalking project.
 */

package com.huawei.gray.feign.interceptor;

import com.huawei.gray.feign.service.DefaultHttpClientService;
import com.huawei.sermant.core.agent.common.BeforeResult;
import com.huawei.sermant.core.agent.interceptor.InstanceMethodInterceptor;
import com.huawei.sermant.core.service.ServiceManager;

import java.lang.reflect.Method;

/**
 * 拦截feign执行http请求的execute方法，匹配标签规则进行灰度路由
 *
 * @author lilai
 * @since 2021-11-03
 */
public class DefaultHttpClientInterceptor implements InstanceMethodInterceptor {
    private DefaultHttpClientService defaultHttpClientService;

    /**
     * 获取当前服务信息
     *
     * @param obj 拦截对象
     * @param method 拦截方法
     * @param arguments 方法参数
     * @param beforeResult change this result, if you want to truncate the method.
     */
    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
        defaultHttpClientService = ServiceManager.getService(DefaultHttpClientService.class);
        defaultHttpClientService.before(obj, method, arguments, beforeResult);
    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) throws Exception {
        defaultHttpClientService.after(obj, method, arguments, result);
        return result;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable th) {
        defaultHttpClientService.onThrow(obj, method, arguments, th);
    }
}
