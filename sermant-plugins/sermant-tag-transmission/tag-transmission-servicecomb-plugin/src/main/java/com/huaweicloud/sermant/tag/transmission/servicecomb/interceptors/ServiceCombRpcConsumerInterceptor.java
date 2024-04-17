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

package com.huaweicloud.sermant.tag.transmission.servicecomb.interceptors;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.CollectionUtils;
import com.huaweicloud.sermant.core.utils.tag.TrafficUtils;
import com.huaweicloud.sermant.tag.transmission.config.strategy.TagKeyMatcher;
import com.huaweicloud.sermant.tag.transmission.interceptors.AbstractClientInterceptor;

import org.apache.servicecomb.core.Invocation;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * servicecombRPC consumer interceptor, supports servicecomb2.x
 *
 * @author daizhenyu
 * @since 2023-08-26
 **/
public class ServiceCombRpcConsumerInterceptor extends AbstractClientInterceptor<Invocation> {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    protected ExecuteContext doBefore(ExecuteContext context) {
        Object[] arguments = context.getArguments();
        if (arguments == null || arguments.length == 0) {
            return context;
        }
        Object invocationObject = arguments[0];
        if (invocationObject instanceof Invocation) {
            injectTrafficTag2Carrier((Invocation) invocationObject);
        }
        return context;
    }

    @Override
    protected ExecuteContext doAfter(ExecuteContext context) {
        return context;
    }

    /**
     * Add traffic tags to Invocation
     *
     * @param invocation servicecomb rpc consumer tag delivery carrier
     */
    @Override
    protected void injectTrafficTag2Carrier(Invocation invocation) {
        for (Map.Entry<String, List<String>> entry : TrafficUtils.getTrafficTag().getTag().entrySet()) {
            String key = entry.getKey();
            if (!TagKeyMatcher.isMatch(key)) {
                continue;
            }
            List<String> values = entry.getValue();

            // If the tag value is not null on the provider side, it will be converted to list storage. If it is null,
            // it will be put directly to null. Therefore, if the values on the consumer side are empty,
            // they must be null.
            if (CollectionUtils.isEmpty(values)) {
                invocation.getContext().put(key, null);
                LOGGER.log(Level.FINE, "Traffic tag {0} have been injected to servicecomb rpc.", entry);
                continue;
            }
            invocation.getContext().put(key, values.get(0));
            LOGGER.log(Level.FINE, "Traffic tag {0}={1} have been injected to servicecomb rpc.",
                    new Object[]{key, values.get(0)});
        }
    }
}
