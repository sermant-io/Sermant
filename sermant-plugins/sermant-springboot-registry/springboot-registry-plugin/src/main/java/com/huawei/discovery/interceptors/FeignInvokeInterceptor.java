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

package com.huawei.discovery.interceptors;

import com.huawei.discovery.retry.InvokerContext;
import com.huawei.discovery.service.InvokerService;
import com.huawei.discovery.utils.HttpConstants;
import com.huawei.discovery.utils.PlugEffectWhiteBlackUtils;
import com.huawei.discovery.utils.RequestInterceptorUtils;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;

import feign.Request;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 拦截获取服务列表
 *
 * @author chengyouling
 * @since 2022-09-27
 */
public class FeignInvokeInterceptor extends MarkInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    protected ExecuteContext doBefore(ExecuteContext context) throws Exception {
        final InvokerService invokerService = PluginServiceManager.getPluginService(InvokerService.class);
        Request request = (Request) context.getArguments()[0];
        Map<String, String> urlInfo = RequestInterceptorUtils.recoverUrl(request.url());
        if (!PlugEffectWhiteBlackUtils.isAllowRun(urlInfo.get(HttpConstants.HTTP_URI_HOST),
                urlInfo.get(HttpConstants.HTTP_URI_SERVICE))) {
            return context;
        }
        RequestInterceptorUtils.printRequestLog("feign", urlInfo);
        Optional<Object> result = invokerService.invoke(
            buildInvokerFunc(context, request, urlInfo),
            ex -> ex,
            urlInfo.get(HttpConstants.HTTP_URI_SERVICE));
        if (result.isPresent()) {
            Object obj = result.get();
            if (obj instanceof Exception) {
                LOGGER.log(Level.SEVERE, "request is error, uri is " + request.url(), (Exception) obj);
                context.setThrowableOut((Exception) obj);
                return context;
            }
            context.skip(obj);
        }
        return context;
    }

    private Function<InvokerContext, Object> buildInvokerFunc(ExecuteContext context, Request request,
        Map<String, String> urlInfo) {
        return invokerContext -> {
            context.getArguments()[0] = Request.create(request.method(),
                RequestInterceptorUtils.buildUrl(urlInfo, invokerContext.getServiceInstance()),
                request.headers(), request.body(), request.charset());
            return RequestInterceptorUtils.buildFunc(context, invokerContext).get();
        };
    }

    @Override
    public ExecuteContext after(ExecuteContext context) throws Exception {
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) throws Exception {
        return context;
    }
}
