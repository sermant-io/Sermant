/*
 * Copyright (C) 2023-2024 Sermant Authors. All rights reserved.
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

import com.huawei.discovery.retry.InvokerContext;
import com.huawei.discovery.service.InvokerService;
import com.huawei.discovery.utils.HttpConstants;
import com.huawei.discovery.utils.PlugEffectWhiteBlackUtils;
import com.huawei.discovery.utils.RequestInterceptorUtils;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;
import com.huaweicloud.sermant.core.utils.LogUtils;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Webclient interception point
 *
 * @author provenceee
 * @since 2023-04-25
 */
public class MonoHttpConnectInterceptor extends MarkInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final String ORIGIN_URI_FIELD_NAME = "originUri";

    private static final String ORIGIN_CONFIG_FIELD_NAME = "originConfig";

    private static String configFieldName;

    private static String uriFieldName;

    private final InvokerService invokerService;

    /**
     * Constructor
     */
    public MonoHttpConnectInterceptor() {
        invokerService = PluginServiceManager.getPluginService(InvokerService.class);
    }

    @Override
    protected ExecuteContext doBefore(ExecuteContext context) {
        LogUtils.printHttpRequestBeforePoint(context);
        init(context.getObject());
        Optional<Object> configOptional = ReflectUtils.getFieldValue(context.getObject(), configFieldName);
        if (!configOptional.isPresent()) {
            return context;
        }
        Object config = configOptional.get();
        Optional<Object> uriOptional = ReflectUtils.getFieldValue(config, uriFieldName);
        if (!uriOptional.isPresent()) {
            return context;
        }
        String uri = (String) uriOptional.get();
        Map<String, String> uriInfo = RequestInterceptorUtils.recoverUrl(uri);
        if (!PlugEffectWhiteBlackUtils.isAllowRun(uriInfo.get(HttpConstants.HTTP_URI_HOST),
                uriInfo.get(HttpConstants.HTTP_URI_SERVICE))) {
            return context;
        }
        RequestInterceptorUtils.printRequestLog("webClient(reactor)", uriInfo);
        Optional<Object> result = invokerService.invoke(
                invokerContext -> buildInvokerFunc(context, invokerContext, config, uri, uriInfo),
                ex -> ex,
                uriInfo.get(HttpConstants.HTTP_URI_SERVICE));
        if (result.isPresent()) {
            Object obj = result.get();
            if (obj instanceof Exception) {
                LOGGER.log(Level.SEVERE, "Webclient(reactor) request is error, uri is " + uri, (Exception) obj);
                context.setThrowableOut((Exception) obj);
                return context;
            }
        }

        // The method returns void
        context.skip(null);
        return context;
    }

    @Override
    protected void ready() {
    }

    private Object buildInvokerFunc(ExecuteContext context, InvokerContext invokerContext, Object config, String uri,
            Map<String, String> uriInfo) {
        context.setLocalFieldValue(ORIGIN_URI_FIELD_NAME, uri);
        context.setLocalFieldValue(ORIGIN_CONFIG_FIELD_NAME, config);
        ReflectUtils.setFieldValue(config, uriFieldName,
                RequestInterceptorUtils.buildUrl(uriInfo, invokerContext.getServiceInstance()));
        return RequestInterceptorUtils.buildFunc(context, invokerContext).get();
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        LogUtils.printHttpRequestAfterPoint(context);
        resetUri(context);
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        LogUtils.printHttpRequestOnThrowPoint(context);
        resetUri(context);
        return context;
    }

    // The prefix method replaces the URI of the original object with the actual IP, so the URI needs to be restored in
    // the suffix method and the exception method, otherwise, if the same object is used to request again, the load
    // balancing will not be performed
    private void resetUri(ExecuteContext context) {
        Object originUri = context.getLocalFieldValue(ORIGIN_URI_FIELD_NAME);
        if (originUri == null) {
            return;
        }
        Object config = context.getLocalFieldValue(ORIGIN_CONFIG_FIELD_NAME);
        if (config == null) {
            return;
        }
        ReflectUtils.setFieldValue(config, uriFieldName, originUri);
    }

    private void init(Object obj) {
        if (uriFieldName != null) {
            return;
        }
        try {
            // spring boot 2.1.0.RELEASE - 2.3.x.RELEASE
            obj.getClass().getDeclaredField("configuration");
            configFieldName = "configuration";
            uriFieldName = "uri";
        } catch (NoClassDefFoundError | NoSuchFieldException throwable) {
            // spring boot 2.4.0+
            configFieldName = "config";
            uriFieldName = "uriStr";
        }
    }
}