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

package io.sermant.mq.prohibition.rocketmq.interceptor;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.mq.prohibition.controller.rocketmq.RocketMqPullConsumerController;
import io.sermant.mq.prohibition.controller.rocketmq.constant.SubscriptionType;
import io.sermant.mq.prohibition.controller.rocketmq.extension.RocketMqConsumerHandler;
import io.sermant.mq.prohibition.controller.rocketmq.wrapper.DefaultLitePullConsumerWrapper;
import io.sermant.mq.prohibition.rocketmq.utils.PullConsumerLocalInfoUtils;

import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;

/**
 * RocketMq pullConsumer subscription interceptor
 *
 * @author daizhenyu
 * @since 2023-12-15
 **/
public class RocketMqPullConsumerSubscribeInterceptor extends AbstractPullConsumerInterceptor {
    /**
     * Non-parametric construction method
     */
    public RocketMqPullConsumerSubscribeInterceptor() {
    }

    /**
     * Parameterized construction method
     *
     * @param handler handler
     */
    public RocketMqPullConsumerSubscribeInterceptor(RocketMqConsumerHandler handler) {
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
        DefaultLitePullConsumerWrapper wrapper = RocketMqPullConsumerController
                .getPullConsumerWrapper((DefaultLitePullConsumer) context.getObject());
        if (wrapper == null) {
            PullConsumerLocalInfoUtils.setSubscriptionType(SubscriptionType.SUBSCRIBE);
        } else {
            wrapper.setSubscriptionType(SubscriptionType.SUBSCRIBE);
        }

        if (handler != null) {
            handler.doAfter(context);
            return context;
        }

        // After adding topic subscriptions, consumer subscription information changes, and consumers need to be enabled
        // or prohibited from consuming according to the prohibited topic configuration
        disablePullConsumption(wrapper);
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        if (handler != null) {
            handler.doOnThrow(context);
        }
        return context;
    }
}
