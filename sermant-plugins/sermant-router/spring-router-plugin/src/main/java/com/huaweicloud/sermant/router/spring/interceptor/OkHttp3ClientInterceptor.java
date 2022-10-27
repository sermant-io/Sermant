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
import com.huaweicloud.sermant.core.utils.ReflectUtils;
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
 * 针对okHttp3.x版本以上的拦截
 *
 * @author yangrh
 * @since 2022-10-25
 */
public class OkHttp3ClientInterceptor extends AbstractInterceptor {
    private static final String FIELD_NAME = "originalRequest";

    /**
     * 前置触发点
     *
     * @param context 执行上下文
     * @return 执行上下文
     * @throws Exception 执行异常
     */
    @Override
    public ExecuteContext before(ExecuteContext context) throws Exception {
        final Optional<Request> rawRequest = getRequest(context);
        if (!rawRequest.isPresent()) {
            return context;
        }
        Request request = rawRequest.get();
        URI uri = request.url().uri();
        Headers headers = request.headers();
        String str = headers.get("sw8-correlation");
        Map<String, List<String>> decodeTags = FlowContextUtils.decodeTags(str);
        if (decodeTags.size() > 0) {
            ThreadLocalUtils.setRequestData(new RequestData(decodeTags, uri.getPath(), request.method()));
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

    private Optional<Request> getRequest(ExecuteContext context) {
        final Optional<Object> originalRequest = ReflectUtils.getFieldValue(context.getObject(), FIELD_NAME);
        if (originalRequest.isPresent() && originalRequest.get() instanceof Request) {
            return Optional.of((Request) originalRequest.get());
        }
        return Optional.empty();
    }
}
