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

package io.sermant.tag.transmission.sofarpc.interceptors;

import com.alipay.sofa.rpc.core.request.SofaRequest;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.utils.tag.TrafficUtils;
import io.sermant.tag.transmission.config.strategy.TagKeyMatcher;
import io.sermant.tag.transmission.interceptors.AbstractServerInterceptor;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * sofarpc server interceptor, supports 5.0+
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
     * Parse traffic tags from SofaRequest
     *
     * @param sofaRequest sofarpc server-side traffic tag carrier
     * @return 流量标签
     */
    @Override
    protected Map<String, List<String>> extractTrafficTagFromCarrier(SofaRequest sofaRequest) {
        Map<String, List<String>> tag = new HashMap<>();
        if (sofaRequest.getRequestProps() == null) {
            return tag;
        }
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

            // If the value of the traffic label is null, you need to store the local variable to override the
            // original value to prevent misuse of the old traffic label
            tag.put(key, null);
            LOGGER.log(Level.FINE, "Traffic tag {0} have been extracted from sofarpc.", entry);
        }
        return tag;
    }
}