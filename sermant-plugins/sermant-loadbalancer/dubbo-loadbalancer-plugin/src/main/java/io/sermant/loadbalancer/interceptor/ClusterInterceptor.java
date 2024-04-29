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

package io.sermant.loadbalancer.interceptor;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import io.sermant.core.utils.ReflectUtils;
import io.sermant.loadbalancer.cache.DubboApplicationCache;
import io.sermant.loadbalancer.constants.DubboUrlParamsConstants;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Intercept ClusterUtils#mergeUrl. Whenever a downstream service is found, this method will be called. Here need to
 * get the mapping relationship between the interface and the downstream.
 *
 * @author zhouss
 * @since 2022-09-13
 */
public class ClusterInterceptor extends AbstractInterceptor {
    @Override
    public ExecuteContext before(ExecuteContext context) {
        final Object url = context.getArguments()[0];
        final Map<String, String> parameters = getParameters(url);
        final String application = parameters.get(DubboUrlParamsConstants.DUBBO_APPLICATION);
        final String interfaceName = parameters.get(DubboUrlParamsConstants.DUBBO_INTERFACE);
        if (Objects.nonNull(application) && Objects.nonNull(interfaceName)) {
            DubboApplicationCache.INSTANCE.cache(interfaceName, application);
        }
        return context;
    }

    private Map<String, String> getParameters(Object url) {
        final Optional<Object> parameters = ReflectUtils.invokeMethod(url, "getParameters", null, null);
        if (!parameters.isPresent()) {
            return Collections.emptyMap();
        }
        final Object paramMap = parameters.get();
        if (paramMap instanceof Map) {
            return (Map<String, String>) paramMap;
        }
        return Collections.emptyMap();
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }
}
