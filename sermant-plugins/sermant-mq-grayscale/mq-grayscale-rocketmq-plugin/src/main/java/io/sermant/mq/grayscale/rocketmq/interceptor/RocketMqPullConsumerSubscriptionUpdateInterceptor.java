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

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.utils.ReflectUtils;
import io.sermant.core.utils.StringUtils;
import io.sermant.mq.grayscale.rocketmq.service.RocketMqConsumerGroupAutoCheck;
import io.sermant.mq.grayscale.rocketmq.utils.RocketMqGrayscaleConfigUtils;
import io.sermant.mq.grayscale.rocketmq.utils.RocketMqSubscriptionDataUtils;

import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.impl.consumer.DefaultMQPullConsumerImpl;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.common.protocol.heartbeat.SubscriptionData;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * update pull consumer subscription SQL92 query statement interceptor
 *
 * @author chengyouling
 * @since 2024-07-27
 **/
public class RocketMqPullConsumerSubscriptionUpdateInterceptor extends RocketMqAbstractInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    public ExecuteContext doAfter(ExecuteContext context) throws Exception {
        SubscriptionData subscriptionData = (SubscriptionData) context.getResult();
        if (RocketMqSubscriptionDataUtils.isExpressionTypeInaccurate(subscriptionData.getExpressionType())) {
            return context;
        }
        Optional<Object> fieldValue = ReflectUtils.getFieldValue(context.getObject(), "mQClientFactory");
        if (!fieldValue.isPresent()) {
            LOGGER.log(Level.SEVERE, "field mQClientFactory is not exist!");
            return context;
        }
        MQClientInstance instance = (MQClientInstance) fieldValue.get();
        buildSql92SubscriptionData(context, subscriptionData, instance);
        return context;
    }

    private void buildSql92SubscriptionData(ExecuteContext context, SubscriptionData subscriptionData,
            MQClientInstance instance) {
        DefaultMQPullConsumer pullConsumer
                = ((DefaultMQPullConsumerImpl) context.getObject()).getDefaultMQPullConsumer();
        String consumerGroup = pullConsumer.getConsumerGroup();
        if (StringUtils.isEmpty(RocketMqGrayscaleConfigUtils.getGrayGroupTag())) {
            RocketMqConsumerGroupAutoCheck.setMqClientInstance(subscriptionData.getTopic(), consumerGroup, instance);
            RocketMqConsumerGroupAutoCheck.syncUpdateCacheGrayTags();
            RocketMqConsumerGroupAutoCheck.startSchedulerCheckGroupTask();
        }
        String namesrvAddr = instance.getClientConfig().getNamesrvAddr();
        String subscribeScope = RocketMqSubscriptionDataUtils.buildSubscribeScope(subscriptionData.getTopic(),
                consumerGroup, namesrvAddr);
        RocketMqSubscriptionDataUtils.resetsSql92SubscriptionData(subscriptionData, subscribeScope);
    }
}
