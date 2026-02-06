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

package io.sermant.tag.transmission.okhttp34.interceptors;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.utils.ReflectUtils;
import io.sermant.core.utils.tag.TrafficUtils;
import io.sermant.tag.transmission.config.strategy.TagKeyMatcher;
import io.sermant.tag.transmission.interceptors.AbstractClientInterceptor;
import okhttp3.Headers;
import okhttp3.Request;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * OkHttp Interceptor for transparent transmission of traffic tag, for 3.x,4.x
 *
 * @since 2025-05-30
 */
public class OkHttp34Interceptor extends AbstractClientInterceptor<Request.Builder> {
    /**
     * Filter multiple calls of interceptors during a single processing
     */
    protected static final ThreadLocal<Boolean> LOCK_MARK = new ThreadLocal<>();

    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        if (LOCK_MARK.get() != null) {
            return context;
        }
        LOCK_MARK.set(Boolean.TRUE);
        Object builderObject = context.getObject();
        if (!(builderObject instanceof Request.Builder)) {
            return context;
        }
        injectTrafficTag2Carrier((Request.Builder) builderObject);
        return context;
    }

    @Override
    public ExecuteContext doAfter(ExecuteContext context) {
        LOCK_MARK.remove();
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context){
        LOCK_MARK.remove();
        return context;
    }

    /**
     * Add traffic tags to Request.Builder
     *
     * @param builder OkHttp 3.x,4.x label transfer carrier
     */
    @Override
    protected void injectTrafficTag2Carrier(Request.Builder builder) {
        for (Map.Entry<String, List<String>> entry : TrafficUtils.getTrafficTag().getTag().entrySet()) {
            String key = entry.getKey();
            if (!TagKeyMatcher.isMatch(key)) {
                continue;
            }

            // if original headers contain the specific key, then ignore
            Optional<Object> headersBuilderObject = ReflectUtils.getFieldValue(builder, "headers");
            if (headersBuilderObject.isPresent()) {
                Headers.Builder headersBuilder = (Headers.Builder) headersBuilderObject.get();
                String value = headersBuilder.get(key);
                if (value != null) {
                    continue;
                }
            }

            List<String> values = entry.getValue();
            for (String value : values) {
                builder.addHeader(key, value);
            }
            LOGGER.log(Level.FINE, "Traffic tag {0}={1} have been injected to okhttp.", new Object[]{key, values});
        }
    }
}
