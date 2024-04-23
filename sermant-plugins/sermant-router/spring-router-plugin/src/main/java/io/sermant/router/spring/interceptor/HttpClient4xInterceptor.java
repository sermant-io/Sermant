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

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.utils.LogUtils;
import io.sermant.core.utils.StringUtils;
import io.sermant.router.common.request.RequestData;
import io.sermant.router.common.utils.FlowContextUtils;
import io.sermant.router.common.utils.ThreadLocalUtils;
import io.sermant.router.spring.utils.RequestInterceptorUtils;

import org.apache.http.Header;
import org.apache.http.HttpRequest;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * HTTP interception only for version 4. x
 *
 * @author yangrh
 * @since 2022-10-25
 */
public class HttpClient4xInterceptor extends MarkInterceptor {
    /**
     * Pre-trigger point
     *
     * @param context Execution context
     * @return Execution context
     * @throws Exception Execution exception
     */
    @Override
    public ExecuteContext doBefore(ExecuteContext context) throws Exception {
        LogUtils.printHttpRequestBeforePoint(context);
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
     * Rear trigger point
     *
     * @param context Execution context
     * @return Execution context
     * @throws Exception Execution exception
     */
    @Override
    public ExecuteContext after(ExecuteContext context) throws Exception {
        ThreadLocalUtils.removeRequestData();
        LogUtils.printHttpRequestAfterPoint(context);
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        ThreadLocalUtils.removeRequestData();
        LogUtils.printHttpRequestOnThrowPoint(context);
        return context;
    }
}
