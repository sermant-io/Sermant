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
import com.huaweicloud.sermant.router.common.request.RequestHeader;
import com.huaweicloud.sermant.router.common.utils.CollectionUtils;
import com.huaweicloud.sermant.router.common.utils.FlowContextUtils;
import com.huaweicloud.sermant.router.common.utils.ReflectUtils;
import com.huaweicloud.sermant.router.common.utils.ThreadLocalUtils;

import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestVariableDefault;

import feign.Request;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Client增强类，发起feign请求方法
 *
 * @author provenceee
 * @since 2022-07-12
 */
public class FeignClientInterceptor extends AbstractInterceptor {
    private static final int EXPECT_LENGTH = 4;

    @Override
    public ExecuteContext before(ExecuteContext context) {
        Object argument = context.getArguments()[0];
        if (argument instanceof Request) {
            Request request = (Request) argument;
            Map<String, List<String>> headers = getHeaders(request.headers());
            setHeaders(request, headers);
            ThreadLocalUtils.setRequestData(new RequestData(decodeTags(headers), getPath(request.url()),
                request.method()));
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        ThreadLocalUtils.removeRequestData();
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        ThreadLocalUtils.removeRequestData();
        return context;
    }

    private String getPath(String url) {
        // url形如：http://www.url.com/a/b?parameter=1，其中path为/a/b，?后面的参数非必须且不属于path
        // 以/切分时，只需要切分为4份，即["http:", "", "www.url.com", "a/b?parameter=1"]，然后去掉arr[3]中参数即可
        String[] arr = url.split("/", EXPECT_LENGTH);
        if (arr.length < EXPECT_LENGTH) {
            return "";
        }
        String path = arr[EXPECT_LENGTH - 1];
        int index = path.indexOf('?');
        if (index >= 0) {
            path = path.substring(0, index);
        }
        return "/" + path;
    }

    private Map<String, List<String>> getHeaders(Map<String, Collection<String>> headers) {
        // 传入的headers是一个unmodifiableMap，所以这里要new一个
        Map<String, List<String>> newHeaders = new HashMap<>();
        if (headers != null) {
            headers.forEach((key, value) -> newHeaders.put(key, new ArrayList<>(value)));
        }
        getRequestHeader().ifPresent(requestHeader -> {
            Map<String, List<String>> header = requestHeader.getHeader();
            for (Entry<String, List<String>> entry : header.entrySet()) {
                // 使用上游传递的header
                newHeaders.putIfAbsent(entry.getKey(), entry.getValue());
            }
        });
        return Collections.unmodifiableMap(newHeaders);
    }

    private void setHeaders(Request request, Map<String, List<String>> headers) {
        com.huaweicloud.sermant.core.utils.ReflectUtils.setFieldValue(request, "headers", headers);
    }

    private Optional<RequestHeader> getRequestHeader() {
        RequestHeader header = ThreadLocalUtils.getRequestHeader();
        if (header != null) {
            return Optional.of(header);
        }
        HystrixRequestContext context = HystrixRequestContext.getContextForCurrentThread();
        if (context == null) {
            return Optional.empty();
        }
        Map<HystrixRequestVariableDefault<?>, ?> state = ReflectUtils.getFieldValue(context, "state")
            .map(value -> (Map<HystrixRequestVariableDefault<?>, ?>) value).orElse(Collections.emptyMap());
        for (Entry<HystrixRequestVariableDefault<?>, ?> entry : state.entrySet()) {
            Object lazyInitializer = entry.getValue();
            Object obj = ReflectUtils.getFieldValue(lazyInitializer, "value").orElse(null);
            if (obj instanceof RequestHeader) {
                entry.getKey().remove();
                return Optional.of((RequestHeader) obj);
            }
        }
        return Optional.empty();
    }

    private Map<String, List<String>> decodeTags(Map<String, List<String>> headers) {
        if (StringUtils.isBlank(FlowContextUtils.getTagName()) || CollectionUtils.isEmpty(headers)) {
            return headers;
        }
        List<String> list = headers.get(FlowContextUtils.getTagName());
        if (!CollectionUtils.isEmpty(list)) {
            Map<String, List<String>> newHeaders = new HashMap<>(headers);
            newHeaders.putAll(FlowContextUtils.decodeTags(list.get(0)));
            return Collections.unmodifiableMap(newHeaders);
        }
        return headers;
    }
}