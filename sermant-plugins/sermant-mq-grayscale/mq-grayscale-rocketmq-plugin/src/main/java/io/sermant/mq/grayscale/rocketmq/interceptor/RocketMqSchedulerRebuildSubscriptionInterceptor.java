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
import io.sermant.core.utils.StringUtils;
import io.sermant.mq.grayscale.rocketmq.service.RocketMqConsumerGroupAutoCheck;
import io.sermant.mq.grayscale.rocketmq.utils.RocketMqGrayscaleConfigUtils;
import io.sermant.mq.grayscale.rocketmq.utils.RocketMqReflectUtils;
import io.sermant.mq.grayscale.rocketmq.utils.RocketMqSubscriptionDataUtils;

import org.apache.rocketmq.client.impl.consumer.RebalanceImpl;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;

import java.util.concurrent.ConcurrentMap;

/**
 * TAG/SQL92 query message statement interceptor
 *
 * @author chengyouling
 * @since 2024-05-27
 **/
public class RocketMqSchedulerRebuildSubscriptionInterceptor extends RocketMqAbstractInterceptor {
    private final Object lock = new Object();

    @Override
    public ExecuteContext doAfter(ExecuteContext context) throws Exception {
        ConcurrentMap<String, Object> map = (ConcurrentMap<String, Object>) context.getResult();
        RebalanceImpl balance = (RebalanceImpl) context.getObject();
        if (balance.getConsumerGroup() == null) {
            return context;
        }
        for (Object subscriptionData : map.values()) {
            if (RocketMqSubscriptionDataUtils
                    .isExpressionTypeInaccurate(RocketMqReflectUtils.getExpressionType(subscriptionData))) {
                continue;
            }
            buildSql92SubscriptionData(subscriptionData, balance);
        }
        return context;
    }

    private void buildSql92SubscriptionData(Object subscriptionData, RebalanceImpl balance) {
        synchronized (lock) {
            String topic = RocketMqReflectUtils.getTopic(subscriptionData);
            if (!RocketMqSubscriptionDataUtils.getGrayTagChangeFlag(topic, balance)) {
                return;
            }
            String consumerGroup = balance.getConsumerGroup();
            MQClientInstance instance = balance.getmQClientFactory();
            if (StringUtils.isEmpty(RocketMqGrayscaleConfigUtils.getGrayGroupTag())) {
                RocketMqConsumerGroupAutoCheck.setMqClientInstance(topic, consumerGroup, instance);
                RocketMqConsumerGroupAutoCheck.syncUpdateCacheGrayTags();
                RocketMqConsumerGroupAutoCheck.startSchedulerCheckGroupTask();
            }
            String namesrvAddr = balance.getmQClientFactory().getClientConfig().getNamesrvAddr();
            resetsSql92SubscriptionData(topic, consumerGroup, subscriptionData, namesrvAddr);

            // update change flag when finished build substr
            RocketMqSubscriptionDataUtils.resetTagChangeMap(namesrvAddr, topic, consumerGroup, false);
        }
    }

    private void resetsSql92SubscriptionData(String topic, String consumerGroup, Object subscriptionData,
            String namesrvAddr) {
        String subscribeScope = RocketMqSubscriptionDataUtils.buildSubscribeScope(topic, consumerGroup,
                namesrvAddr);
        RocketMqSubscriptionDataUtils.resetsSql92SubscriptionData(subscriptionData, subscribeScope);
    }
}
