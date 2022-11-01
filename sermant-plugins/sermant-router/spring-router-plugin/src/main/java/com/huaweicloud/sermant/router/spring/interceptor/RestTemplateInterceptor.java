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

package com.huaweicloud.sermant.router.spring.interceptor;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.utils.StringUtils;
import com.huaweicloud.sermant.router.common.request.RequestData;
import com.huaweicloud.sermant.router.common.utils.FlowContextUtils;
import com.huaweicloud.sermant.router.common.utils.ThreadLocalUtils;
import com.huaweicloud.sermant.router.spring.wrapper.RequestCallbackWrapper;

import org.springframework.http.HttpMethod;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * org.springframework.web.client.RestTemplate的增强拦截器<br>
 *
 * @author yuzl 俞真龙
 * @since 2022-10-27
 */
public class RestTemplateInterceptor extends AbstractInterceptor {
    private static final int CALLBACK_ARG_LENGTH = 3;

    private static final int CALLBACK_ARG_POSITION = 2;

    @Override
    public ExecuteContext before(ExecuteContext context) {
        Object[] arguments = context.getArguments();

        if (arguments != null && arguments.length > CALLBACK_ARG_LENGTH) {
            Object argument = arguments[CALLBACK_ARG_POSITION];
            if (argument instanceof RequestCallbackWrapper) {
                RequestCallbackWrapper callback = (RequestCallbackWrapper)argument;
                parseTags(callback, arguments[0], arguments[1]);
            }
        }
        return context;
    }

    private void parseTags(RequestCallbackWrapper callback, Object url, Object method) {
        Map<String, String> header = callback.getHeader();
        if (StringUtils.isBlank(FlowContextUtils.getTagName())) {
            return;
        }
        String encodeTag = header.get(FlowContextUtils.getTagName());
        if (StringUtils.isBlank(encodeTag)) {
            return;
        }
        Map<String, List<String>> tags = FlowContextUtils.decodeTags(encodeTag);
        if (!tags.isEmpty()) {
            ThreadLocalUtils.setRequestData(getRequestData(tags, url, method));
        }
    }

    private RequestData getRequestData(Map<String, List<String>> tags, Object url, Object method) {
        String path = "";
        if (url instanceof URI) {
            path = ((URI) url).getPath();
        }
        String httpMethod = "";
        if (method instanceof HttpMethod) {
            httpMethod = ((HttpMethod) method).name();
        }
        return new RequestData(tags, path, httpMethod);
    }

    @Override
    public ExecuteContext after(ExecuteContext context) throws Exception {
        ThreadLocalUtils.removeRequestData();
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) throws Exception {
        ThreadLocalUtils.removeRequestData();
        return super.onThrow(context);
    }
}
