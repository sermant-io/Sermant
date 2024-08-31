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

package io.sermant.mq.grayscale.rocketmq.interceptor;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import io.sermant.core.utils.StringUtils;
import io.sermant.mq.grayscale.config.MqGrayConfigCache;
import io.sermant.mq.grayscale.config.rocketmq.RocketMqConfigUtils;
import io.sermant.mq.grayscale.rocketmq.service.RocketMqConsumerGroupAutoCheck;
import io.sermant.mq.grayscale.rocketmq.utils.RocketMqGrayscaleConfigUtils;
import io.sermant.mq.grayscale.rocketmq.utils.RocketMqSubscriptionDataUtils;

/**
 * mq consumer abstract interceptor
 *
 * @author chengyouling
 * @since 2024-09-04
 **/
public abstract class RocketMqAbstractInterceptor extends AbstractInterceptor {
    @Override
    public ExecuteContext before(ExecuteContext context) throws Exception {
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) throws Exception {
        if (!MqGrayConfigCache.getCacheConfig().isEnabled()) {
            return context;
        }
        return doAfter(context);
    }

    /**
     * Handling after the intercept point
     *
     * @param context context
     * @return context
     * @throws Exception Exception
     */
    protected abstract ExecuteContext doAfter(ExecuteContext context) throws Exception;

    /**
     * build gray group name and set consumer client configs
     *
     * @param namesrvAddr namesrvAddr
     * @param topic topic
     * @param consumerGroup consumerGroup
     */
    protected void buildGroupAndClientConfig(String namesrvAddr, String topic, String consumerGroup) {
        String grayGroupTag = RocketMqGrayscaleConfigUtils.getGrayGroupTag();
        String subscribeScope = RocketMqSubscriptionDataUtils.buildSubscribeScope(topic, consumerGroup, namesrvAddr);
        if (StringUtils.isEmpty(grayGroupTag)) {
            RocketMqConsumerGroupAutoCheck.setConsumerClientConfig(namesrvAddr, topic, consumerGroup);
            RocketMqConfigUtils.setBaseGroupTagChangeMap(subscribeScope, true);
            return;
        }
        RocketMqConfigUtils.setGrayGroupTagChangeMap(subscribeScope, true);
    }
}
