/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.discovery.interceptors;

import com.huawei.discovery.entity.JettyClientWrapper;
import com.huawei.discovery.entity.ServiceInstance;
import com.huawei.discovery.retry.InvokerContext;
import com.huawei.discovery.service.InvokerService;
import com.huawei.discovery.utils.HttpConstants;
import com.huawei.discovery.utils.RequestInterceptorUtils;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;
import com.huaweicloud.sermant.core.utils.LogUtils;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

import org.eclipse.jetty.client.api.Request;

import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * webclient拦截点
 *
 * @author provenceee
 * @since 2023-04-25
 */
public class JettyRequestInterceptor extends MarkInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final InvokerService invokerService;

    /**
     * 构造方法
     */
    public JettyRequestInterceptor() {
        invokerService = PluginServiceManager.getPluginService(InvokerService.class);
    }

    @Override
    protected ExecuteContext doBefore(ExecuteContext context) {
        LogUtils.printHttpRequestBeforePoint(context);
        Request request = (Request) context.getObject();
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Request''s classloader is {0}, jettyClientWrapper''s classloader is {1}.",
                    new Object[]{Request.class.getClassLoader().getClass().getName(),
                            JettyClientWrapper.class.getClassLoader().getClass().getName()});
        }
        if (!(request instanceof JettyClientWrapper)) {
            return context;
        }
        String url = request.getScheme() + HttpConstants.HTTP_URL_DOUBLE_SLASH + request.getHost() + request.getPath();
        Map<String, String> urlInfo = RequestInterceptorUtils.recoverUrl(url);
        RequestInterceptorUtils.printRequestLog("webClient(jetty)", urlInfo);
        Optional<Object> result = invokerService.invoke(
                invokerContext -> buildInvokerFunc(context, invokerContext, request,
                        urlInfo.get(HttpConstants.HTTP_URI_PATH)),
                ex -> ex,
                urlInfo.get(HttpConstants.HTTP_URI_SERVICE));
        if (result.isPresent()) {
            Object obj = result.get();
            if (obj instanceof Exception) {
                LOGGER.log(Level.SEVERE, "Webclient(jetty) request is error, url is " + url, (Exception) obj);
                context.setThrowableOut((Exception) obj);
                return context;
            }
        }

        // 方法返回值为void
        context.skip(null);
        return context;
    }

    private Object buildInvokerFunc(ExecuteContext context, InvokerContext invokerContext, Request request,
            String path) {
        ServiceInstance instance = invokerContext.getServiceInstance();
        ReflectUtils.setFieldValue(request, HttpConstants.HTTP_URI_HOST, instance.getIp());
        ReflectUtils.setFieldValue(request, HttpConstants.HTTP_URI_PORT, instance.getPort());
        ReflectUtils.setFieldValue(request, HttpConstants.HTTP_URI_PATH, path);
        return RequestInterceptorUtils.buildFunc(context, invokerContext).get();
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        LogUtils.printHttpRequestAfterPoint(context);
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        LogUtils.printHttpRequestOnThrowPoint(context);
        return context;
    }
}