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

package com.huaweicloud.sermant.tag.transmission.interceptors;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.utils.tag.TrafficUtils;
import com.huaweicloud.sermant.tag.transmission.config.TagTransmissionConfig;

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
public class HttpServletInterceptor extends AbstractInterceptor {
    /**
     * 过滤一次处理过程中拦截器的多次调用
     */
    private static final ThreadLocal<Boolean> LOCK_MARK = new ThreadLocal<>();

    private final TagTransmissionConfig tagTransmissionConfig;

    /**
     * 构造器
     */
    public HttpServletInterceptor() {
        tagTransmissionConfig = PluginConfigManager.getPluginConfig(TagTransmissionConfig.class);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        if (!tagTransmissionConfig.isEnabled() || LOCK_MARK.get() != null) {
            return context;
        }
        LOCK_MARK.set(Boolean.TRUE);

        Object httpServletRequestObject = context.getArguments()[0];
        if (httpServletRequestObject instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) httpServletRequestObject;
            Map<String, List<String>> tag = new HashMap<>();
            for (String key : tagTransmissionConfig.getTagKeys()) {
                Enumeration<String> valuesEnumeration = httpServletRequest.getHeaders(key);
                if (valuesEnumeration.hasMoreElements()) {
                    List<String> values = Collections.list(valuesEnumeration);
                    tag.put(key, values);
                }
            }
            if (!tag.isEmpty()) {
                TrafficUtils.updateTrafficTag(tag);
            }
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
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
}
