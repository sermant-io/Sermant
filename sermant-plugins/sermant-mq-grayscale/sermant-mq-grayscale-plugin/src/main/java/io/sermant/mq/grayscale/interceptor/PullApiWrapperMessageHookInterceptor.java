/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
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

package io.sermant.mq.grayscale.interceptor;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import io.sermant.core.utils.ReflectUtils;
import io.sermant.core.utils.StringUtils;
import io.sermant.mq.grayscale.service.MqConsumerGroupAutoCheck;
import io.sermant.mq.grayscale.service.MqGrayMessageFilter;
import io.sermant.mq.grayscale.utils.MqGrayscaleConfigUtils;

import org.apache.rocketmq.client.hook.FilterMessageHook;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;

import java.util.ArrayList;
import java.util.Optional;

/**
 * MessageFilter builder interceptor
 *
 * @author chengyouling
 * @since 2024-05-27
 **/
public class PullApiWrapperMessageHookInterceptor extends AbstractInterceptor {
    private static final String MESSAGE_FILTER_NAME = "MqGrayMessageFilter";

    @Override
    public ExecuteContext before(ExecuteContext context) throws Exception {
        return context;
    }

    /**
     * add message filter
     * 1、if using server filter message, not need add filter
     * 2、if already add filter, not need add filter again
     *
     * @param context Execution context
     * @return executeContext
     * @throws Exception exception
     */
    @Override
    public ExecuteContext after(ExecuteContext context) throws Exception {
        if (MqGrayscaleConfigUtils.isPlugEnabled()) {
            String grayEnv = MqGrayscaleConfigUtils.getGrayEnvTag();
            if (StringUtils.isBlank(grayEnv)) {
                Optional<Object> fieldValue = ReflectUtils.getFieldValue(context.getObject(), "mQClientFactory");
                fieldValue.ifPresent(o -> MqConsumerGroupAutoCheck.setMqClientInstance((MQClientInstance) o));
            }
            if (!MqGrayscaleConfigUtils.isMqServerGrayEnabled()) {
                ArrayList<FilterMessageHook> messageHooks = (ArrayList<FilterMessageHook>) context.getArguments()[0];
                for (FilterMessageHook filterMessageHook : messageHooks) {
                    if (MESSAGE_FILTER_NAME.equals(filterMessageHook.hookName())) {
                        return context;
                    }
                }
                messageHooks.add(new MqGrayMessageFilter());
            }
        }
        return context;
    }
}
