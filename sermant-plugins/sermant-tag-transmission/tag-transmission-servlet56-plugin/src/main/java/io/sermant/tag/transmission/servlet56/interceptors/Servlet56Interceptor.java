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

package io.sermant.tag.transmission.servlet56.interceptors;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.utils.tag.TrafficUtils;
import io.sermant.tag.transmission.config.strategy.TagKeyMatcher;
import io.sermant.tag.transmission.interceptors.AbstractServerInterceptor;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet 5.x, 6.x Interceptor for transparent transmission of traffic tag
 *
 * @since 2025-05-22
 */
public class Servlet56Interceptor extends AbstractServerInterceptor<HttpServletRequest> {
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

        HttpServletRequest httpServletRequest = (HttpServletRequest) context.getArguments()[0];
        Map<String, List<String>> tagMap = extractTrafficTagFromCarrier(httpServletRequest);
        TrafficUtils.updateTrafficTag(tagMap);
        return context;
    }

    @Override
    public ExecuteContext doAfter(ExecuteContext context) {
        TrafficUtils.removeTrafficTag();
        LOCK_MARK.remove();
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        TrafficUtils.removeTrafficTag();
        LOCK_MARK.remove();
        return context;
    }

    /**
     * Parse the traffic tag from the HttpServletRequest
     *
     * @param httpServletRequest servlet carrier of the traffic tag on the server
     * @return traffic tag map
     */
    @Override
    protected Map<String, List<String>> extractTrafficTagFromCarrier(HttpServletRequest httpServletRequest) {
        Map<String, List<String>> tagMap = new HashMap<>();

        Enumeration<String> keyEnumeration = httpServletRequest.getHeaderNames();
        while (keyEnumeration.hasMoreElements()) {
            String key = keyEnumeration.nextElement();
            if (!TagKeyMatcher.isMatch(key)) {
                continue;
            }
            Enumeration<String> valuesEnumeration = httpServletRequest.getHeaders(key);
            if (valuesEnumeration != null && valuesEnumeration.hasMoreElements()) {
                List<String> values = Collections.list(valuesEnumeration);
                tagMap.put(key, values);
                LOGGER.log(Level.FINE, "Traffic tag {0}={1} have been extracted from servlet.",
                        new Object[]{key, values});
                continue;
            }

            // If the value of the traffic label is null, you need to store the local variable to override the original
            // value to prevent misuse of the old traffic label
            tagMap.put(key, null);
            LOGGER.log(Level.FINE, "Traffic tag {0}=null have been extracted from servlet.", key);
        }
        return tagMap;
    }
}
