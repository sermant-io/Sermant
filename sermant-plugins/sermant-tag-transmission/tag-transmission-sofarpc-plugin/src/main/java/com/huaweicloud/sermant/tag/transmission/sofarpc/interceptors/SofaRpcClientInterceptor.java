/*
 *   Copyright (C) 2023-2024 Sermant Authors. All rights reserved.
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
import com.huaweicloud.sermant.core.utils.CollectionUtils;
import com.huaweicloud.sermant.core.utils.tag.TrafficUtils;
import com.huaweicloud.sermant.tag.transmission.config.strategy.TagKeyMatcher;
import com.huaweicloud.sermant.tag.transmission.interceptors.AbstractClientInterceptor;

import com.alipay.sofa.rpc.core.request.SofaRequest;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * sofarpc client interceptor, supports 5.0+
 *
 * @author daizhenyu
 * @since 2023-08-22
 **/
public class SofaRpcClientInterceptor extends AbstractClientInterceptor<SofaRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    protected ExecuteContext doBefore(ExecuteContext context) {
        Object[] arguments = context.getArguments();
        if (arguments == null || arguments.length == 0) {
            return context;
        }
        Object argument = arguments[0];
        if (argument instanceof SofaRequest) {
            injectTrafficTag2Carrier((SofaRequest) argument);
        }
        return context;
    }

    @Override
    protected ExecuteContext doAfter(ExecuteContext context) {
        return context;
    }

    /**
     * Add traffic tags to SofaRequest
     *
     * @param sofaRequest sofarpc client tag delivery carrier
     */
    @Override
    protected void injectTrafficTag2Carrier(SofaRequest sofaRequest) {
        for (Map.Entry<String, List<String>> entry : TrafficUtils.getTrafficTag().getTag().entrySet()) {
            String key = entry.getKey();
            if (!TagKeyMatcher.isMatch(key)) {
                continue;
            }
            List<String> values = entry.getValue();

            // The server side converts the label value to list storage when it is not null. If it is null, it directly
            // puts null. Therefore, if the client side values are empty, they must be null.
            if (CollectionUtils.isEmpty(values)) {
                // sofa cannot add a key-value pair with null value
                sofaRequest.addRequestProp(key, "null");
                LOGGER.log(Level.FINE, "Traffic tag {0} have been injected to sofarpc.", entry);
                continue;
            }
            sofaRequest.addRequestProp(key, values.get(0));
            LOGGER.log(Level.FINE, "Traffic tag {0}={1} have been injected to sofarpc.", new Object[]{key,
                    values.get(0)});
        }
    }
}