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

package com.huaweicloud.sermant.tag.transmission.sofarpc.interceptors;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.tag.TrafficUtils;
import com.huaweicloud.sermant.tag.transmission.config.strategy.TagKeyMatcher;
import com.huaweicloud.sermant.tag.transmission.interceptors.AbstractServerInterceptor;

import com.alipay.sofa.rpc.core.request.SofaRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * sofarpc server端interceptor，支持5.0+版本
 *
 * @author daizhenyu
 * @since 2023-08-22
 **/
public class SofaRpcServerInterceptor extends AbstractServerInterceptor<SofaRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    protected ExecuteContext doBefore(ExecuteContext context) {
        Object[] arguments = context.getArguments();
        if (arguments == null || arguments.length == 0) {
            return context;
        }
        Object argument = arguments[0];
        if (argument instanceof SofaRequest) {
            TrafficUtils.updateTrafficTag(extractTrafficTagFromCarrier((SofaRequest) argument));
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
     * 从SofaRequest中解析流量标签
     *
     * @param sofaRequest sofarpc服务端的流量标签载体
     * @return 流量标签
     */
    @Override
    protected Map<String, List<String>> extractTrafficTagFromCarrier(SofaRequest sofaRequest) {
        Map<String, List<String>> tag = new HashMap<>();
        if (sofaRequest.getRequestProps() == null) {
            return tag;
        }
        Set<String> keySet = sofaRequest.getRequestProps().keySet();
        for (Map.Entry<String, Object> entry : sofaRequest.getRequestProps().entrySet()) {
            String key = entry.getKey();
            if (!TagKeyMatcher.isMatch(key)) {
                continue;
            }
            Object value = entry.getValue();
            if (value instanceof String && !"null".equals(value)) {
                tag.put(key, Collections.singletonList((String) value));
                LOGGER.log(Level.FINE, "Traffic tag {0} have been extracted from sofarpc.", entry);
                continue;
            }

            // 流量标签的value为null时，也需存入本地变量，覆盖原来的value，以防误用旧流量标签
            tag.put(key, null);
            LOGGER.log(Level.FINE, "Traffic tag {0} have been extracted from sofarpc.", entry);
        }
        return tag;
    }
}