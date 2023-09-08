/*
 *   Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.tag.transmission.interceptors.rpc.servicecomb;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.tag.TrafficUtils;
import com.huaweicloud.sermant.tag.transmission.config.strategy.TagKeyMatcher;
import com.huaweicloud.sermant.tag.transmission.interceptors.AbstractServerInterceptor;

import org.apache.servicecomb.core.Invocation;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * servicecombRPC provider端interceptor，支持servicecomb2.x版本
 *
 * @author daizhenyu
 * @since 2023-08-26
 **/
public class ServiceCombRpcProviderInterceptor extends AbstractServerInterceptor<Invocation> {
    @Override
    protected ExecuteContext doBefore(ExecuteContext context) {
        Object[] arguments = context.getArguments();
        if (arguments == null || arguments.length == 0) {
            return context;
        }
        Object invocationObject = arguments[0];
        if (invocationObject instanceof Invocation) {
            TrafficUtils.updateTrafficTag(extractTrafficTagFromCarrier((Invocation) invocationObject));
        }
        return context;
    }

    @Override
    protected ExecuteContext doAfter(ExecuteContext context) {
        TrafficUtils.removeTrafficTag();
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        TrafficUtils.removeTrafficTag();
        return context;
    }

    /**
     * 从Invocation中解析流量标签
     *
     * @param invocation servicecomb rpc服务端的流量标签载体
     * @return 流量标签
     */
    @Override
    protected Map<String, List<String>> extractTrafficTagFromCarrier(Invocation invocation) {
        Map<String, List<String>> tag = new HashMap<>();
        extractFromContext(invocation, tag);
        extractFromRequestEx(invocation, tag);
        return tag;
    }

    private void extractFromContext(Invocation invocation, Map<String, List<String>> tag) {
        if (invocation.getContext() == null) {
            return;
        }
        Set<String> keySet = invocation.getContext().keySet();
        for (String key : keySet) {
            if (!TagKeyMatcher.isMatch(key)) {
                continue;
            }
            String value = invocation.getContext().get(key);
            if (value != null) {
                // consumer端使用servicecombrpc方式调用provider端
                tag.put(key, Collections.singletonList(value));
                continue;
            }

            // 流量标签的value为null时，也需存入本地变量，覆盖原来的value，以防误用旧流量标签
            tag.put(key, null);
        }
    }

    private void extractFromRequestEx(Invocation invocation, Map<String, List<String>> tag) {
        if (invocation.getRequestEx() == null) {
            return;
        }
        Enumeration<String> headerNames = invocation.getRequestEx().getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            if (!TagKeyMatcher.isMatch(key)) {
                continue;
            }

            // consumer端使用非servicecombrpc方式调用provider端，比如httpclient，okhttp等
            Enumeration<String> valuesEnumeration = invocation.getRequestEx().getHeaders(key);
            if (valuesEnumeration != null && valuesEnumeration.hasMoreElements()) {
                List<String> values = Collections.list(valuesEnumeration);
                tag.put(key, values);
                continue;
            }

            // 流量标签的value为null时，也需存入本地变量，覆盖原来的value，以防误用旧流量标签
            tag.put(key, null);
        }
    }
}
