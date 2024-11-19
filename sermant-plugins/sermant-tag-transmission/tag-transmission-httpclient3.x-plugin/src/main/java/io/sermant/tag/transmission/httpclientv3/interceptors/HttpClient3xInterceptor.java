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

package io.sermant.tag.transmission.httpclientv3.interceptors;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.utils.CollectionUtils;
import io.sermant.core.utils.tag.TrafficUtils;
import io.sermant.tag.transmission.config.strategy.TagKeyMatcher;
import io.sermant.tag.transmission.interceptors.AbstractClientInterceptor;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * HttpClient Interceptor for transparent transmission of traffic tags, for 3.x version only
 *
 * @author lilai
 * @since 2023-08-08
 */
public class HttpClient3xInterceptor extends AbstractClientInterceptor<HttpMethod> {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        Object httpMethodObject = context.getArguments()[1];
        if (!(httpMethodObject instanceof HttpMethod)) {
            return context;
        }
        injectTrafficTag2Carrier((HttpMethod) httpMethodObject);
        return context;
    }

    @Override
    public ExecuteContext doAfter(ExecuteContext context) {
        return context;
    }

    /**
     * add traffic tag to HttpMethod
     *
     * @param httpMethod httpclient 3.x traffic tag carrier
     */
    @Override
    protected void injectTrafficTag2Carrier(HttpMethod httpMethod) {
        for (Map.Entry<String, List<String>> entry : TrafficUtils.getTrafficTag().getTag().entrySet()) {
            String key = entry.getKey();
            if (!TagKeyMatcher.isMatch(key)) {
                continue;
            }

            // if original headers contains the specific key, then ignore
            Header originalRequestHeader = httpMethod.getRequestHeader(key);
            if (originalRequestHeader != null) {
                continue;
            }

            List<String> values = entry.getValue();

            // The server side converts the label value to list storage when it is not null. If it is null, it directly
            // puts null. Therefore, if the client side values are empty, they must be null.
            if (CollectionUtils.isEmpty(values)) {
                httpMethod.setRequestHeader(key, null);
                LOGGER.log(Level.FINE, "Traffic tag {0} have been injected to httpclient.", entry);
                continue;
            }

            // HttpClient 3.x does not support duplicate key
            httpMethod.setRequestHeader(key, values.get(0));
            LOGGER.log(Level.FINE, "Traffic tag {0}={1} have been injected to httpclient.", new Object[]{key,
                    values.get(0)});
        }
    }
}
