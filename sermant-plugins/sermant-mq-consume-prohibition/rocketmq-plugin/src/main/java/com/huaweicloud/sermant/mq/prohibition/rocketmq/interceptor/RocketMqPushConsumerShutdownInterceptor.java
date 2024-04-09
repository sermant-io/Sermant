/*
 *  Copyright (C) 2023-2024 Sermant Authors. All rights reserved.
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

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;

/**
 * RocketMq pushConsumer shutdown interceptor
 *
 * @author daizhenyu
 * @since 2023-12-04
 **/
public class RocketMqPushConsumerShutdownInterceptor extends AbstractPushConsumerInterceptor {
    /**
     * Non-parametric construction method
     */
    public RocketMqPushConsumerShutdownInterceptor() {
    }

    /**
     * Parameterized construction method
     *
     * @param handler Interception point handler
     */
    public RocketMqPushConsumerShutdownInterceptor(RocketMqConsumerHandler handler) {
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
        RocketMqPushConsumerController.removePushConsumer((DefaultMQPushConsumer) context.getObject());

        if (handler != null) {
            handler.doAfter(context);
        }
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