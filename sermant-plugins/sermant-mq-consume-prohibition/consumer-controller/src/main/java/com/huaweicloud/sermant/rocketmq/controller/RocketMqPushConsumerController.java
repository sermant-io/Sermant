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

package com.huaweicloud.sermant.rocketmq.controller;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.rocketmq.cache.RocketMqConsumerCache;
import com.huaweicloud.sermant.rocketmq.wrapper.DefaultMqPushConsumerWrapper;
import com.huaweicloud.sermant.utils.RocketmqWrapperUtils;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * push消费者控制类
 *
 * @author daizhenyu
 * @since 2023-12-04
 **/
public class RocketMqPushConsumerController {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private RocketMqPushConsumerController() {
    }

    /**
     * 禁止push消费者消费
     *
     * @param wrapper push消费者wrapper
     * @param topics 禁止消费的topic
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

        // 退出消费者前主动提交消费的offset，退出消费者组后立刻触发一次重平衡，重新分配队列
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
     * 添加PushConsumer包装类实例
     *
     * @param pushConsumer pushConsumer实例
     */
    public static void cachePushConsumer(DefaultMQPushConsumer pushConsumer) {
        Optional<DefaultMqPushConsumerWrapper> pushConsumerWrapperOptional = RocketmqWrapperUtils
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
     * 移除PushConsumer包装类实例
     *
     * @param pushConsumer pushConsumer实例
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
     * 获取PushConsumer的包装类实例
     *
     * @param pushConsumer 消费者实例
     * @return PushConsumer包装类实例
     */
    public static DefaultMqPushConsumerWrapper getPushConsumerWrapper(Object pushConsumer) {
        return RocketMqConsumerCache.PUSH_CONSUMERS_CACHE.get(pushConsumer.hashCode());
    }

    /**
     * 获取PushConsumer缓存
     *
     * @return PushConsumer缓存
     */
    public static Map<Integer, DefaultMqPushConsumerWrapper> getPushConsumerCache() {
        return RocketMqConsumerCache.PUSH_CONSUMERS_CACHE;
    }
}
