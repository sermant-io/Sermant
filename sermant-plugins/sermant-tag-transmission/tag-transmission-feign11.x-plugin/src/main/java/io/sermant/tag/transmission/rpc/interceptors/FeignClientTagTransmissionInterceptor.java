/*
 * Copyright (C) 2025-2025 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.tag.transmission.rpc.interceptors;

import feign.Request;
import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.utils.CollectionUtils;
import io.sermant.core.utils.ReflectUtils;
import io.sermant.core.utils.tag.TrafficTag;
import io.sermant.core.utils.tag.TrafficUtils;
import io.sermant.tag.transmission.config.strategy.TagKeyMatcher;
import io.sermant.tag.transmission.interceptors.AbstractClientInterceptor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The client enhancement class, initiates the feign request method
 *
 * @author chengyouling
 * @since 2025-02-25
 */
public class FeignClientTagTransmissionInterceptor extends AbstractClientInterceptor<Map<String, Collection<String>>> {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    protected ExecuteContext doBefore(ExecuteContext context) {
        Object argument = context.getArguments()[0];
        if (argument instanceof Request) {
            Request request = (Request) argument;
            LOGGER.log(Level.FINE, "feign client request url: {0}", request.url());
            Map<String, Collection<String>> headers = new HashMap<>(request.headers());
            injectTrafficTag2Carrier(headers);
            ReflectUtils.setFieldValue(request, "headers", Collections.unmodifiableMap(headers));
            LOGGER.log(Level.FINE, "after refactor feign client request headers: {0}", request.headers());
        }
        return context;
    }

    @Override
    protected ExecuteContext doAfter(ExecuteContext context) {
        return context;
    }

    @Override
    protected void injectTrafficTag2Carrier(Map<String, Collection<String>> headers) {
        TrafficTag trafficTag = TrafficUtils.getTrafficTag();
        for (Map.Entry<String, List<String>> entry : trafficTag.getTag().entrySet()) {
            String key = entry.getKey();
            if (!TagKeyMatcher.isMatch(key)) {
                continue;
            }
            List<String> values = entry.getValue();
            Collection<String> originValues = headers.get(key);
            if (originValues != null) {
                continue;
            }

            // The server side converts the label value to list storage when it is not null. If it is null,
            // it directly puts null. Therefore, if the client side values are empty, they must be null.
            if (CollectionUtils.isEmpty(values)) {
                setHeaders(headers, key, Collections.emptyList());
                LOGGER.log(Level.FINE, "Traffic tag {0} has no values.", entry);
                continue;
            }
            setHeaders(headers, key, values);
            LOGGER.log(Level.FINE, "Traffic tag {0}={1} have been injected to feign client.",
                    new Object[]{key, values});
        }
    }

    private void setHeaders(Map<String, Collection<String>> headers, String key, List<String> values) {
        headers.put(key, values);
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        return context;
    }
}
