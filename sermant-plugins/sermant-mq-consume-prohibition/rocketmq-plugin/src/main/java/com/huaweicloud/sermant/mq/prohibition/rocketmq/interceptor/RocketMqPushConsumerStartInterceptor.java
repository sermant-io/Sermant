/*
 *  Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.mq.prohibition.rocketmq.interceptor;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.rocketmq.controller.RocketMqPushConsumerController;
import com.huaweicloud.sermant.rocketmq.extension.RocketMqConsumerHandler;
import com.huaweicloud.sermant.rocketmq.wrapper.DefaultMqPushConsumerWrapper;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;

/**
 * RocketMq pushConsumer Start Interceptor
 *
 * @author daizhenyu
 * @since 2023-12-04
 **/
public class RocketMqPushConsumerStartInterceptor extends AbstractPushConsumerInterceptor {
    /**
     * Non-parametric construction method
     */
    public RocketMqPushConsumerStartInterceptor() {
    }

    /**
     * Parameterized construction method
     *
     * @param handler Interception point handler
     */
    public RocketMqPushConsumerStartInterceptor(RocketMqConsumerHandler handler) {
        super(handler);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        if (handler != null) {
            handler.doBefore(context);
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        DefaultMQPushConsumer pushConsumer = (DefaultMQPushConsumer) context.getObject();
        RocketMqPushConsumerController.cachePushConsumer(pushConsumer);

        DefaultMqPushConsumerWrapper pushConsumerWrapper =
                RocketMqPushConsumerController.getPushConsumerWrapper(pushConsumer);
        if (pushConsumerWrapper != null) {
            pushConsumerWrapper.setSubscribedTopics(pushConsumerWrapper.getPushConsumerImpl()
                    .getSubscriptionInner().keySet());
        }

        if (handler != null) {
            handler.doAfter(context);
            return context;
        }

        // Consumer activation will execute Prohibition of consumption on consumers based on the cached prohibition of
        // consumption configuration
        disablePushConsumption(pushConsumerWrapper);
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        return context;
    }
}
