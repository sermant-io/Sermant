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

package io.sermant.router.spring.interceptor;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.service.xds.entity.ServiceInstance;
import io.sermant.core.utils.LogUtils;
import io.sermant.core.utils.ReflectUtils;
import io.sermant.core.utils.StringUtils;
import io.sermant.router.common.config.RouterConfig;
import io.sermant.router.common.constants.RouterConstant;
import io.sermant.router.common.metric.MetricThreadLocal;
import io.sermant.router.common.metric.MetricsManager;
import io.sermant.router.common.request.RequestData;
import io.sermant.router.common.utils.CollectionUtils;
import io.sermant.router.common.utils.FlowContextUtils;
import io.sermant.router.common.utils.ThreadLocalUtils;
import io.sermant.router.spring.utils.BaseHttpRouterUtils;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An enhanced interceptor for java.net.HttpURLConnection in JDK version 1.8<br>
 *
 * @author yuzl Yu Zhenlong
 * @since 2022-10-25
 */
public class HttpUrlConnectionConnectInterceptor extends AbstractInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final RouterConfig routerConfig = PluginConfigManager.getPluginConfig(RouterConfig.class);

    @Override
    public ExecuteContext before(ExecuteContext context) {
        LogUtils.printHttpRequestBeforePoint(context);
        if (!(context.getObject() instanceof HttpURLConnection)) {
            return context;
        }
        HttpURLConnection connection = (HttpURLConnection) context.getObject();
        Map<String, List<String>> headers = connection.getRequestProperties();

        handleXdsRouterAndUpdateHttpRequest(connection);
        MetricThreadLocal.setFlag(true);
        String method = connection.getRequestMethod();
        if (StringUtils.isBlank(FlowContextUtils.getTagName()) || CollectionUtils
                .isEmpty(headers.get(FlowContextUtils.getTagName()))) {
            ThreadLocalUtils.setRequestData(new RequestData(headers, getPath(connection), method));
            return context;
        }
        String encodeTag = headers.get(FlowContextUtils.getTagName()).get(0);
        if (StringUtils.isBlank(encodeTag)) {
            ThreadLocalUtils.setRequestData(new RequestData(headers, getPath(connection), method));
            return context;
        }
        Map<String, List<String>> tags = FlowContextUtils.decodeTags(encodeTag);
        if (!tags.isEmpty()) {
            ThreadLocalUtils.setRequestData(new RequestData(tags, getPath(connection), method));
        } else {
            ThreadLocalUtils.setRequestData(new RequestData(headers, getPath(connection), method));
        }
        return context;
    }

    private String getPath(HttpURLConnection connection) {
        return Optional.ofNullable(connection.getURL()).map(URL::getPath).orElse("/");
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        collectRequestCountMetric(context);
        ThreadLocalUtils.removeRequestData();
        LogUtils.printHttpRequestAfterPoint(context);
        return context;
    }

    private void collectRequestCountMetric(ExecuteContext context) {
        if (routerConfig.isEnableMetric() && MetricThreadLocal.getFlag()) {
            HttpURLConnection connection = (HttpURLConnection) context.getObject();
            MetricsManager.collectRequestCountMetric(connection.getURL());
            context.setLocalFieldValue(RouterConstant.EXECUTE_FLAG, Boolean.TRUE);
        }
        MetricThreadLocal.removeFlag();
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) throws Exception {
        ThreadLocalUtils.removeRequestData();
        LogUtils.printHttpRequestOnThrowPoint(context);
        MetricThreadLocal.removeFlag();
        return super.onThrow(context);
    }

    private void handleXdsRouterAndUpdateHttpRequest(HttpURLConnection connection) {
        if (!routerConfig.isEnabledXdsRoute()) {
            return;
        }
        Map<String, List<String>> headers = connection.getRequestProperties();
        URL url = connection.getURL();
        String host = url.getHost();
        if (!BaseHttpRouterUtils.isXdsRouteRequired(host)) {
            return;
        }

        // use xds route to find a service instance, and modify url by it
        Optional<ServiceInstance> serviceInstanceOptional = BaseHttpRouterUtils
                .chooseServiceInstanceByXds(host.split(RouterConstant.ESCAPED_POINT)[0], url.getPath(),
                        BaseHttpRouterUtils.processHeaders(headers));
        if (!serviceInstanceOptional.isPresent()) {
            return;
        }
        ServiceInstance instance = serviceInstanceOptional.get();
        try {
            ReflectUtils.setFieldValue(connection, "url",
                    new URL(BaseHttpRouterUtils.rebuildUrlByXdsServiceInstance(url, instance)));
        } catch (MalformedURLException e) {
            LOGGER.log(Level.WARNING, "Create url using xds service instance failed.", e.getMessage());
        }
    }
}
