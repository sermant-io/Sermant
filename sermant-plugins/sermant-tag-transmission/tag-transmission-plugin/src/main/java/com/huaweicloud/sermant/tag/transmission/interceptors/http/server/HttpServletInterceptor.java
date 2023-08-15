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

package com.huaweicloud.sermant.tag.transmission.interceptors.http.server;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.tag.TrafficUtils;
import com.huaweicloud.sermant.tag.transmission.interceptors.AbstractServerInterceptor;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * HttpServlet 流量标签透传的拦截器,支持servlet3.0+
 *
 * @author tangle
 * @since 2023-07-18
 */
public class HttpServletInterceptor extends AbstractServerInterceptor<HttpServletRequest> {
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
     * 从HttpServletRequest中解析流量标签
     *
     * @param httpServletRequest servlet服务端的流量标签载体
     * @return 流量标签
     */
    @Override
    protected Map<String, List<String>> extractTrafficTagFromCarrier(HttpServletRequest httpServletRequest) {
        Map<String, List<String>> tagMap = new HashMap<>();
        for (String key : tagTransmissionConfig.getTagKeys()) {
            Enumeration<String> valuesEnumeration = httpServletRequest.getHeaders(key);
            if (valuesEnumeration == null) {
                continue;
            }
            if (valuesEnumeration.hasMoreElements()) {
                List<String> values = Collections.list(valuesEnumeration);
                tagMap.put(key, values);
            }
        }
        return tagMap;
    }
}
