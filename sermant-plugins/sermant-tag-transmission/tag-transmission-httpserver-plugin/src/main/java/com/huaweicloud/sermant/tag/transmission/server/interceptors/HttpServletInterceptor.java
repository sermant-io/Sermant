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

package com.huaweicloud.sermant.tag.transmission.server.interceptors;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;
import com.huaweicloud.sermant.core.utils.StringUtils;
import com.huaweicloud.sermant.core.utils.tag.TrafficUtils;
import com.huaweicloud.sermant.tag.transmission.config.strategy.TagKeyMatcher;
import com.huaweicloud.sermant.tag.transmission.interceptors.AbstractServerInterceptor;
import com.huaweicloud.sermant.tag.transmission.server.service.ServiceCombHeaderParseService;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

/**
 * HttpServlet Interceptor for transparent transmission of traffic tag, supporting servlet3.0+
 *
 * @author tangle
 * @since 2023-07-18
 */
public class HttpServletInterceptor extends AbstractServerInterceptor<HttpServletRequest> {
    /**
     * Filter multiple calls of interceptors during a single processing
     */
    protected static final ThreadLocal<Boolean> LOCK_MARK = new ThreadLocal<>();

    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final String SERVICECOMB_HEADER_KEY = "x-cse-context";

    private static ServiceCombHeaderParseService parseService =
            PluginServiceManager.getPluginService(ServiceCombHeaderParseService.class);

    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        if (LOCK_MARK.get() != null) {
            return context;
        }
        LOCK_MARK.set(Boolean.TRUE);

        Object httpServletRequestObject = context.getArguments()[0];
        if (!(httpServletRequestObject instanceof HttpServletRequest)) {
            return context;
        }

        Map<String, List<String>> tagMap = extractTrafficTagFromCarrier((HttpServletRequest) httpServletRequestObject);
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
     * @return 流量标签
     */
    @Override
    protected Map<String, List<String>> extractTrafficTagFromCarrier(HttpServletRequest httpServletRequest) {
        Map<String, List<String>> tagMap = new HashMap<>();

        // In the servicecomb scenario, the consumer side is servicecomb rpc. Need to obtain the servicecomb header
        // string from the http request and parse it.
        String serviceCombHeaderValue = httpServletRequest.getHeader(SERVICECOMB_HEADER_KEY);
        if (!StringUtils.isBlank(serviceCombHeaderValue)) {
            Map<String, String> headers = parseService.parseHeaderFromJson(serviceCombHeaderValue);
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                String key = entry.getKey();
                if (!TagKeyMatcher.isMatch(key)) {
                    continue;
                }
                String value = headers.get(key);
                if (value != null) {
                    tagMap.put(key, Collections.singletonList(value));
                    LOGGER.log(Level.FINE, "Traffic tag {0}={1} have been extracted from servicecomb.",
                            new Object[]{key, value});
                    continue;
                }

                // 流量标签的value为null时，也需存入本地变量，覆盖原来的value，以防误用旧流量标签
                tagMap.put(key, null);
                LOGGER.log(Level.FINE, "Traffic tag {0}=null have been extracted from servicecomb.", key);
            }
        }

        // general http scenario
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
