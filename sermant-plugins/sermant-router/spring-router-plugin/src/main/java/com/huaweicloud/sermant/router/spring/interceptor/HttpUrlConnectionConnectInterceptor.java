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
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.utils.LogUtils;
import com.huaweicloud.sermant.core.utils.ReflectUtils;
import com.huaweicloud.sermant.core.utils.StringUtils;
import com.huaweicloud.sermant.router.common.request.RequestData;
import com.huaweicloud.sermant.router.common.utils.CollectionUtils;
import com.huaweicloud.sermant.router.common.utils.FlowContextUtils;
import com.huaweicloud.sermant.router.common.utils.ThreadLocalUtils;

import sun.net.www.MessageHeader;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * An enhanced interceptor for java.net.HttpURLConnection in JDK version 1.8<br>
 *
 * @author yuzl Yu Zhenlong
 * @since 2022-10-25
 */
public class HttpUrlConnectionConnectInterceptor extends AbstractInterceptor {
    @Override
    public ExecuteContext before(ExecuteContext context) {
        LogUtils.printHttpRequestBeforePoint(context);
        if (context.getObject() instanceof HttpURLConnection) {
            HttpURLConnection connection = (HttpURLConnection) context.getObject();
            Optional<Object> requests = ReflectUtils.getFieldValue(connection, "requests");
            if (!requests.isPresent()) {
                return context;
            }
            Map<String, List<String>> headers = ((MessageHeader) requests.get()).getHeaders(null);
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
        }
        return context;
    }

    private String getPath(HttpURLConnection connection) {
        return Optional.ofNullable(connection.getURL()).map(URL::getPath).orElse("/");
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        ThreadLocalUtils.removeRequestData();
        LogUtils.printHttpRequestAfterPoint(context);
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) throws Exception {
        ThreadLocalUtils.removeRequestData();
        LogUtils.printHttpRequestOnThrowPoint(context);
        return super.onThrow(context);
    }
}
