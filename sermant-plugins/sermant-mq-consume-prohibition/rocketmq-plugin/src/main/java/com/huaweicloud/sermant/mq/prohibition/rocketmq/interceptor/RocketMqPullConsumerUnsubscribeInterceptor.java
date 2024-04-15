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
import com.huaweicloud.sermant.rocketmq.controller.RocketMqPullConsumerController;
import com.huaweicloud.sermant.rocketmq.extension.RocketMqConsumerHandler;
import com.huaweicloud.sermant.rocketmq.wrapper.DefaultLitePullConsumerWrapper;

import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;

/**
 * RocketMq pullConsumer subscription interceptor
 *
 * @author daizhenyu
 * @since 2023-12-15
 **/
public class RocketMqPullConsumerUnsubscribeInterceptor extends AbstractPullConsumerInterceptor {
    /**
     * Non-parametric construction method
     */
    public RocketMqPullConsumerUnsubscribeInterceptor() {
    }

    /**
     * Parameterized construction method
     *
     * @param handler handler
     */
    public RocketMqPullConsumerUnsubscribeInterceptor(RocketMqConsumerHandler handler) {
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
        if (handler != null) {
            handler.doAfter(context);
            return context;
        }

        DefaultLitePullConsumerWrapper wrapper = RocketMqPullConsumerController
                .getPullConsumerWrapper((DefaultLitePullConsumer) context.getObject());

        // After canceling the subscription, the consumer's subscription information will change, and they need to be
        // enabled or prohibited from consuming according to the prohibited topic configuration
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