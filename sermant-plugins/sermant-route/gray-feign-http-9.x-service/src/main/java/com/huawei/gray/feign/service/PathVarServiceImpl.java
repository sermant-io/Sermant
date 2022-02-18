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

package com.huawei.gray.feign.service;

import com.huawei.gray.feign.context.FeignResolvedUrl;
import com.huawei.sermant.core.agent.common.BeforeResult;

import feign.RequestTemplate;

import java.lang.reflect.Method;

/**
 * PathVarInterceptor的service
 *
 * @author provenceee
 * @since 2021/11/26
 */
public class PathVarServiceImpl implements PathVarService {
    static final ThreadLocal<FeignResolvedUrl> URL_CONTEXT = new ThreadLocal<FeignResolvedUrl>();

    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
        RequestTemplate template = (RequestTemplate) arguments[1];
        URL_CONTEXT.set(new FeignResolvedUrl(template.url()));
    }

    @Override
    public void after(Object obj, Method method, Object[] arguments, Object result) throws Exception {
        RequestTemplate resolvedTemplate = (RequestTemplate) result;
        URL_CONTEXT.get().setUrl(resolvedTemplate.url());
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable throwable) {
        if (URL_CONTEXT.get() != null) {
            URL_CONTEXT.remove();
        }
    }
}
