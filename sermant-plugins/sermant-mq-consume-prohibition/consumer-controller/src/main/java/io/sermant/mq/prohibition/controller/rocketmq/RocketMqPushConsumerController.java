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

package io.sermant.mq.prohibition.controller.rocketmq;

import io.sermant.core.common.LoggerFactory;
import io.sermant.mq.prohibition.controller.rocketmq.cache.RocketMqConsumerCache;
import io.sermant.mq.prohibition.controller.rocketmq.wrapper.DefaultMqPushConsumerWrapper;
import io.sermant.mq.prohibition.controller.utils.RocketMqWrapperUtils;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Push consumer control class
 *
 * @author daizhenyu
 * @since 2023-12-04
 **/
public class RocketMqPushConsumerController {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private RocketMqPushConsumerController() {
    }

    /**
     * Prohibit pushing consumer consumption
     *
     * @param wrapper Push consumer wrapper
     * @param topics Prohibited consumption topic
     */
    public static void disablePushConsumption(DefaultMqPushConsumerWrapper wrapper, Set<String> topics) {
        Set<String> subscribedTopic = wrapper.getSubscribedTopics();
        if (subscribedTopic.stream().anyMatch(topics::contains)) {
            suspendPushConsumer(wrapper);
            return;
        }
        resumePushConsumer(wrapper);
    }

    private static void suspendPushConsumer(DefaultMqPushConsumerWrapper wrapper) {
        if (wrapper.isProhibition()) {
            LOGGER.log(Level.INFO, "Consumer has prohibited consumption, consumer instance name : {0}, "
                            + "consumer group : {1}, topic : {2}",
                    new Object[]{wrapper.getInstanceName(), wrapper.getConsumerGroup(), wrapper.getSubscribedTopics()});
            return;
        }

        DefaultMQPushConsumerImpl pushConsumerImpl = wrapper.getPushConsumerImpl();
        String consumerGroup = wrapper.getConsumerGroup();

        // Before exiting the consumer group, actively submit the offset for consumption, and immediately trigger
        // a rebalancing and reassign the queue after exiting the consumer group
        pushConsumerImpl.persistConsumerOffset();
        wrapper.getClientFactory().unregisterConsumer(consumerGroup);
        pushConsumerImpl.doRebalance();
        wrapper.setProhibition(true);

        LOGGER.log(Level.INFO, "Success to prohibit consumption, consumer instance name : {0}, "
                        + "consumer group : {1}, topic : {2}",
                new Object[]{wrapper.getInstanceName(), consumerGroup, wrapper.getSubscribedTopics()});
    }

    private static void resumePushConsumer(DefaultMqPushConsumerWrapper wrapper) {
        String instanceName = wrapper.getInstanceName();
        String consumerGroup = wrapper.getConsumerGroup();
        Set<String> subscribedTopics = wrapper.getSubscribedTopics();
        if (!wrapper.isProhibition()) {
            LOGGER.log(Level.INFO, "Consumer has opened consumption, consumer "
                            + "instance name : {0}, consumer group : {1}, topic : {2}",
                    new Object[]{instanceName, consumerGroup, subscribedTopics});
            return;
        }

        DefaultMQPushConsumerImpl pushConsumerImpl = wrapper.getPushConsumerImpl();
        wrapper.getClientFactory().registerConsumer(consumerGroup, pushConsumerImpl);
        pushConsumerImpl.doRebalance();
        wrapper.setProhibition(false);
        LOGGER.log(Level.INFO, "Success to open consumption, consumer "
                        + "instance name : {0}, consumer group : {1}, topic : {2}",
                new Object[]{instanceName, consumerGroup, subscribedTopics});
    }

    /**
     * Add PushConsumer wrapper class instance
     *
     * @param pushConsumer PushConsumer instance
     */
    public static void cachePushConsumer(DefaultMQPushConsumer pushConsumer) {
        Optional<DefaultMqPushConsumerWrapper> pushConsumerWrapperOptional = RocketMqWrapperUtils
                .wrapPushConsumer(pushConsumer);
        if (pushConsumerWrapperOptional.isPresent()) {
            RocketMqConsumerCache.PUSH_CONSUMERS_CACHE.put(pushConsumer.hashCode(), pushConsumerWrapperOptional.get());
            LOGGER.log(Level.INFO, "Success to cache consumer, "
                            + "consumer instance name : {0}, consumer group : {1}, topic : {2}",
                    new Object[]{pushConsumer.getInstanceName(), pushConsumer.getConsumerGroup(),
                            pushConsumerWrapperOptional.get().getSubscribedTopics()});
            return;
        }
        LOGGER.log(Level.SEVERE, "Fail to cache consumer, consumer instance name : {0}, consumer group : {1}",
                new Object[]{pushConsumer.getInstanceName(), pushConsumer.getConsumerGroup()});
    }

    /**
     * Remove PushConsumer wrapper class instance
     *
     * @param pushConsumer PushConsumer instance
     */
    public static void removePushConsumer(DefaultMQPushConsumer pushConsumer) {
        int hashCode = pushConsumer.hashCode();
        DefaultMqPushConsumerWrapper wrapper = RocketMqConsumerCache.PUSH_CONSUMERS_CACHE.get(hashCode);
        if (wrapper != null) {
            RocketMqConsumerCache.PUSH_CONSUMERS_CACHE.remove(hashCode);
            LOGGER.log(Level.INFO, "Success to remove consumer, consumer instance name : {0}, consumer group "
                            + ": {1}, topic : {2}",
                    new Object[]{wrapper.getInstanceName(), wrapper.getConsumerGroup(),
                            wrapper.getSubscribedTopics()});
        }
    }

    /**
     * Get the packaging class instance of PushConsumer
     *
     * @param pushConsumer Consumer instance
     * @return PushConsumer packaging class instance
     */
    public static DefaultMqPushConsumerWrapper getPushConsumerWrapper(DefaultMQPushConsumer pushConsumer) {
        return RocketMqConsumerCache.PUSH_CONSUMERS_CACHE.get(pushConsumer.hashCode());
    }

    /**
     * Get PushConsumer cache
     *
     * @return PushConsumer cache
     */
    public static Map<Integer, DefaultMqPushConsumerWrapper> getPushConsumerCache() {
        return RocketMqConsumerCache.PUSH_CONSUMERS_CACHE;
    }
}
