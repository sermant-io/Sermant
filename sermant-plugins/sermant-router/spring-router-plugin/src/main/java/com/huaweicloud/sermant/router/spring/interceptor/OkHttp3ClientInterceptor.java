/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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
import com.huaweicloud.sermant.core.utils.LogUtils;
import com.huaweicloud.sermant.core.utils.ReflectUtils;
import com.huaweicloud.sermant.core.utils.StringUtils;
import com.huaweicloud.sermant.router.common.request.RequestData;
import com.huaweicloud.sermant.router.common.utils.FlowContextUtils;
import com.huaweicloud.sermant.router.common.utils.ThreadLocalUtils;

import okhttp3.Headers;
import okhttp3.Request;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Blocking for okHttp3.x and above versions
 *
 * @author yangrh
 * @since 2022-10-25
 */
public class OkHttp3ClientInterceptor extends MarkInterceptor {
    private static final String FIELD_NAME = "originalRequest";

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
        final Optional<Request> rawRequest = getRequest(context);
        if (!rawRequest.isPresent() || StringUtils.isBlank(FlowContextUtils.getTagName())) {
            return context;
        }
        Request request = rawRequest.get();
        URI uri = request.url().uri();
        Headers headers = request.headers();
        String str = headers.get(FlowContextUtils.getTagName());
        Map<String, List<String>> decodeTags = FlowContextUtils.decodeTags(str);
        if (decodeTags.size() > 0) {
            ThreadLocalUtils.setRequestData(new RequestData(decodeTags, uri.getPath(), request.method()));
        }
        return context;
    }

    /**
     * Rear trigger point
     *
     * @param context Execution context
     * @return Execution context
     * @throws Exception Exceptions that were executed
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

    private Optional<Request> getRequest(ExecuteContext context) {
        final Optional<Object> originalRequest = ReflectUtils.getFieldValue(context.getObject(), FIELD_NAME);
        if (originalRequest.isPresent() && originalRequest.get() instanceof Request) {
            return Optional.of((Request) originalRequest.get());
        }
        return Optional.empty();
    }
}
