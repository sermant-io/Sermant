/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
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

package io.sermant.router.dubbo.interceptor;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import io.sermant.router.common.cache.DubboCache;
import io.sermant.router.common.constants.RouterConstant;
import io.sermant.router.common.metric.MetricsManager;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invoker;

import java.util.HashMap;
import java.util.Map;

/**
 * Interceptor of the MonitorFilter, Collecting Metric Information
 *
 * @author zhp
 * @since 2024-10-23
 */
public class ApacheDubboMonitorFilterInterceptor extends AbstractInterceptor {
    @Override
    public ExecuteContext before(ExecuteContext context) throws Exception {
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) throws Exception {
        Object[] arguments = context.getArguments();
        if (arguments == null || arguments.length == 0) {
            return context;
        }
        if (!(arguments[0] instanceof Invoker)) {
            return context;
        }
        Invoker<?> invoker = (Invoker<?>) arguments[0];
        URL url = invoker.getUrl();
        if (isConsumer(url)) {
            String address = url.getHost() + RouterConstant.URL_CONNECTOR + url.getPort();
            Map<String, String> tagsMap = new HashMap<>();
            tagsMap.put(RouterConstant.SERVER_ADDRESS, address);
            tagsMap.put(RouterConstant.CLIENT_SERVICE_NAME, DubboCache.INSTANCE.getAppName());
            tagsMap.put(RouterConstant.PROTOCOL, RouterConstant.DUBBO_PROTOCOL);
            MetricsManager.addOrUpdateCounterMetricValue(RouterConstant.ROUTER_REQUEST_COUNT, tagsMap, 1);
        }
        return context;
    }

    private boolean isConsumer(URL url) {
        return RouterConstant.DUBBO_CONSUMER.equals(url.getParameter(RouterConstant.DUBBO_SIDE,
                RouterConstant.DUBBO_PROVIDER));
    }
}
