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
import com.huaweicloud.sermant.router.spring.utils.RequestInterceptorUtils;

import org.apache.http.Header;
import org.apache.http.HttpRequest;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 仅针对4.x版本得http拦截
 *
 * @author yangrh
 * @since 2022-10-25
 */
public class HttpClient4xInterceptor extends AbstractInterceptor {
    /**
     * 前置触发点
     *
     * @param context 执行上下文
     * @return 执行上下文
     * @throws Exception 执行异常
     */
    @Override
    public ExecuteContext before(ExecuteContext context) throws Exception {
        Object httpRequestObject = context.getArguments()[1];
        if (httpRequestObject instanceof HttpRequest) {
            final HttpRequest httpRequest = (HttpRequest) httpRequestObject;
            final Optional<URI> optionalUri = RequestInterceptorUtils.formatUri(httpRequest.getRequestLine().getUri());
            if (!optionalUri.isPresent()) {
                return context;
            }
            URI uri = optionalUri.get();
            if (StringUtils.isBlank(FlowContextUtils.getTagName())) {
                return context;
            }
            Header[] headers = httpRequest.getHeaders(FlowContextUtils.getTagName());
            Map<String, List<String>> flowTags = new HashMap<>();
            if (headers != null && headers.length > 0) {
                for (Header header : headers) {
                    String headerValue = header.getValue();
                    Map<String, List<String>> stringListMap = FlowContextUtils.decodeTags(headerValue);
                    flowTags.putAll(stringListMap);
                }
            }
            if (flowTags.size() > 0) {
                ThreadLocalUtils.setRequestData(new RequestData(
                        flowTags, uri.getPath(), httpRequest.getRequestLine().getMethod()));
            }
        }
        return context;
    }

    /**
     * 后置触发点
     *
     * @param context 执行上下文
     * @return 执行上下文
     * @throws Exception 执行异常
     */
    @Override
    public ExecuteContext after(ExecuteContext context) throws Exception {
        ThreadLocalUtils.removeRequestData();
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        ThreadLocalUtils.removeRequestData();
        return context;
    }
}
