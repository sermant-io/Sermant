/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huaweicloud.loadbalancer.interceptor;

import com.huaweicloud.loadbalancer.cache.DubboApplicationCache;
import com.huaweicloud.loadbalancer.constants.DubboUrlParamsConstants;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

import java.util.Optional;

/**
 * 拦截ClusterUtils#mergeUrl,每当发现下游服务时, 该方法则会被调用. 此处需取出接口与下游的映射关系
 *
 * @author zhouss
 * @since 2022-09-13
 */
public class ClusterInterceptor extends AbstractInterceptor {
    @Override
    public ExecuteContext before(ExecuteContext context) {
        final Object url = context.getArguments()[0];
        final Optional<Object> application = ReflectUtils.invokeMethod(url, "getParameter", new Class[]{String.class},
                new Object[]{DubboUrlParamsConstants.DUBBO_APPLICATION});
        final Optional<Object> interfaceName = ReflectUtils.invokeMethod(url, "getParameter", new Class[]{String.class},
                new Object[]{DubboUrlParamsConstants.DUBBO_INTERFACE});
        if (application.isPresent() && interfaceName.isPresent()) {
            DubboApplicationCache.INSTANCE.cache(String.valueOf(interfaceName.get()),
                    String.valueOf(application.get()));
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }
}
