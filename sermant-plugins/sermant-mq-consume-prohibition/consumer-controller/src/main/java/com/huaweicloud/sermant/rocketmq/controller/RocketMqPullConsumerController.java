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
import com.huaweicloud.sermant.core.utils.ReflectUtils;
import com.huaweicloud.sermant.rocketmq.cache.RocketMqConsumerCache;
import com.huaweicloud.sermant.rocketmq.wrapper.DefaultLitePullConsumerWrapper;
import com.huaweicloud.sermant.utils.RocketmqWrapperUtils;

import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.client.impl.consumer.AssignedMessageQueue;
import org.apache.rocketmq.client.impl.consumer.DefaultLitePullConsumerImpl;
import org.apache.rocketmq.client.impl.consumer.RebalanceImpl;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.protocol.heartbeat.SubscriptionData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * pull消费者控制类
 *
 * @author daizhenyu
 * @since 2023-12-15
 **/
public class RocketMqPullConsumerController {
    private static final long DELAY_TIME = 1000L;

    private static final int MAXIMUM_RETRY = 5;

    private static final int THREAD_SIZE = 1;

    private static final long THREAD_KEEP_ALIVE_TIME = 60L;

    private static final int THREAD_QUEUE_CAPACITY = 20;

    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static volatile ThreadPoolExecutor executor;

    private RocketMqPullConsumerController() {
    }

    /**
     * 禁止pull消费者消费
     *
     * @param wrapper pull消费者wrapper
     * @param topics 禁止消费的topic
     */
    public static void disablePullConsumption(DefaultLitePullConsumerWrapper wrapper, Set<String> topics) {
        Set<String> subscribedTopic = wrapper.getSubscribedTopics();
        if (subscribedTopic.stream().anyMatch(topics::contains)) {
            suspendPullConsumer(wrapper);
            return;
        }
        resumePullConsumer(wrapper);
    }

    private static void suspendPullConsumer(DefaultLitePullConsumerWrapper wrapper) {
        switch (wrapper.getSubscriptionType()) {
            case SUBSCRIBE:
                suspendSubscriptiveConsumer(wrapper);
                break;
            case ASSIGN:
                suspendAssignedConsumer(wrapper);
                break;
            default:
                break;
        }
    }

    private static void suspendSubscriptiveConsumer(DefaultLitePullConsumerWrapper wrapper) {
        if (wrapper.isProhibition()) {
            LOGGER.log(Level.INFO, "Consumer has prohibited consumption, consumer instance name : {0}, "
                            + "consumer group : {1}, topic : {2}",
                    new Object[]{wrapper.getInstanceName(), wrapper.getConsumerGroup(), wrapper.getSubscribedTopics()});
            return;
        }

        // 退出消费者前主动提交消费的offset，退出消费者组后立刻触发一次重平衡，重新分配队列
        wrapper.getPullConsumerImpl().persistConsumerOffset();
        wrapper.getClientFactory().unregisterConsumer(wrapper.getConsumerGroup());
        doRebalance(wrapper);
        wrapper.setProhibition(true);
    }

    private static void suspendAssignedConsumer(DefaultLitePullConsumerWrapper wrapper) {
        DefaultLitePullConsumerImpl pullConsumerImpl = wrapper.getPullConsumerImpl();
        if (wrapper.getAssignedMessageQueue() == null) {
            Optional<AssignedMessageQueue> assignedMessageQueueOptional = RocketmqWrapperUtils
                    .getAssignedMessageQueue(pullConsumerImpl);
            if (assignedMessageQueueOptional.isPresent()) {
                wrapper.setAssignedMessageQueue(assignedMessageQueueOptional.get());
            }
        }
        AssignedMessageQueue assignedMessageQueue = wrapper.getAssignedMessageQueue();
        assignedMessageQueue.updateAssignedMessageQueue(new ArrayList<>());
        if (wrapper.getPullConsumer().isRunning()) {
            ReflectUtils.invokeMethod(pullConsumerImpl, "updateAssignPullTask",
                    new Class[]{Collection.class}, new Object[]{new ArrayList<>()});
        }
        LOGGER.log(Level.INFO, "Success to prohibit consumption, consumer instance name : {0}, consumer group : {1}, "
                        + "message queue : {2}",
                new Object[]{wrapper.getInstanceName(), wrapper.getConsumerGroup(),
                        wrapper.getMessageQueues()});
    }

    private static void doRebalance(DefaultLitePullConsumerWrapper wrapper) {
        initExecutor();

        // 退出消费者组后立刻清理消费者目前消费的消息队列
        messageQueueChanged(wrapper);

        // 延时五秒后，确认是否退出消费者组，并在此清理消费者目前消费的消息队列，确保禁消费成功
        executor.submit(() -> doDelayRebalance(wrapper));
    }

    private static void doDelayRebalance(DefaultLitePullConsumerWrapper wrapper) {
        // 获取消费者的相关参数
        RebalanceImpl rebalance = wrapper.getRebalanceImpl();
        String instanceName = wrapper.getInstanceName();
        String consumerGroup = wrapper.getConsumerGroup();
        Set<String> subscribedTopics = wrapper.getSubscribedTopics();
        MQClientInstance clientFactory = wrapper.getClientFactory();
        String clientId = clientFactory.getClientId();

        // 每间隔一秒，判断消费者是否退出消费者组，如果退出清空消费者消费的消息队列，最大重试次数为五次
        int retryCount = 0;
        boolean isConsumerGroupExited = false;
        while ((retryCount < MAXIMUM_RETRY) && !isConsumerGroupExited) {
            try {
                Thread.sleep(DELAY_TIME);
            } catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, "An InterruptedException occurs on the thread, "
                        + "details: {0}", e.getMessage());
            }
            for (String topic : subscribedTopics) {
                List<String> consumerIdList = clientFactory.findConsumerIdList(topic, consumerGroup);
                if (consumerIdList != null && consumerIdList.contains(clientId)) {
                    retryCount++;
                    break;
                }

                messageQueueChanged(rebalance, topic);
                if (!isConsumerGroupExited) {
                    isConsumerGroupExited = true;
                }
            }
        }
        if (isConsumerGroupExited) {
            LOGGER.log(Level.INFO, "Success to prohibit consumption, consumer instance name : {0}, "
                            + "consumer group : {1}, topic : {2}",
                    new Object[]{instanceName, consumerGroup, subscribedTopics});
        } else {
            LOGGER.log(Level.SEVERE, "Consumer exiting the {0} consumer group timeout may cause "
                            + "a failure to reallocate the message queue, "
                            + "consumer instance name : {0}, consumer group : {1}, topic : {2}. "
                            + "Please deliver the configuration again.",
                    new Object[]{instanceName, consumerGroup, subscribedTopics});
            wrapper.setProhibition(false);
            wrapper.getClientFactory().registerConsumer(consumerGroup, wrapper.getPullConsumerImpl());
        }
    }

    private static void messageQueueChanged(DefaultLitePullConsumerWrapper wrapper) {
        RebalanceImpl rebalance = wrapper.getRebalanceImpl();
        ConcurrentMap<String, SubscriptionData> subTable = rebalance.getSubscriptionInner();
        if (subTable != null) {
            for (Entry<String, SubscriptionData> entry : subTable.entrySet()) {
                String topic = entry.getKey();
                messageQueueChanged(rebalance, topic);
            }
        }
    }

    private static void messageQueueChanged(RebalanceImpl rebalance, String topic) {
        // 清空消费者的processQunues、assignedMessageQueues和pullTask，从而停止消费
        ReflectUtils.invokeMethod(rebalance, "updateProcessQueueTableInRebalance",
                new Class[]{String.class, Set.class, boolean.class},
                new Object[]{topic, new HashSet<MessageQueue>(), false});
        Set<MessageQueue> messageQueuesSet = rebalance.getTopicSubscribeInfoTable().get(topic);
        rebalance.messageQueueChanged(topic, messageQueuesSet, new HashSet<>());
    }

    private static void resumePullConsumer(DefaultLitePullConsumerWrapper wrapper) {
        switch (wrapper.getSubscriptionType()) {
            case SUBSCRIBE:
                resumeSubscriptiveConsumer(wrapper);
                break;
            case ASSIGN:
                resumeAssignedConsumer(wrapper);
                break;
            default:
                break;
        }
    }

    private static void resumeSubscriptiveConsumer(DefaultLitePullConsumerWrapper wrapper) {
        if (!wrapper.isProhibition()) {
            LOGGER.log(Level.INFO, "Consumer has opened consumption, consumer "
                            + "instance name : {0}, consumer group : {1}, topic : {2}",
                    new Object[]{wrapper.getInstanceName(), wrapper.getConsumerGroup(), wrapper.getSubscribedTopics()});
            return;
        }
        String consumerGroup = wrapper.getConsumerGroup();
        DefaultLitePullConsumerImpl pullConsumerImpl = wrapper.getPullConsumerImpl();

        wrapper.getClientFactory().registerConsumer(consumerGroup, pullConsumerImpl);
        pullConsumerImpl.doRebalance();
        wrapper.setProhibition(false);
        LOGGER.log(Level.INFO, "Success to open consumption, "
                        + "consumer instance name : {0}, consumer group : {1}, topic : {2}",
                new Object[]{wrapper.getInstanceName(), wrapper.getConsumerGroup(), wrapper.getSubscribedTopics()});
    }

    private static void resumeAssignedConsumer(DefaultLitePullConsumerWrapper wrapper) {
        wrapper.getPullConsumer().assign(wrapper.getMessageQueues());
        LOGGER.log(Level.INFO, "Success to open consumption, "
                        + "consumer instance name : {0}, consumer group : {1}, message queue : {2}",
                new Object[]{wrapper.getInstanceName(), wrapper.getConsumerGroup(),
                        wrapper.getMessageQueues()});
    }

    /**
     * 添加PullConsumer包装类实例
     *
     * @param pullConsumer pullConsumer实例
     */
    public static void cachePullConsumer(DefaultLitePullConsumer pullConsumer) {
        Optional<DefaultLitePullConsumerWrapper> pullConsumerWrapperOptional = RocketmqWrapperUtils
                .wrapPullConsumer(pullConsumer);
        if (pullConsumerWrapperOptional.isPresent()) {
            RocketMqConsumerCache.PULL_CONSUMERS_CACHE.put(pullConsumer.hashCode(), pullConsumerWrapperOptional.get());
            LOGGER.log(Level.INFO, "Success to cache consumer, "
                            + "consumer instance name : {0}, consumer group : {1}, topic : {2}",
                    new Object[]{pullConsumer.getInstanceName(), pullConsumer.getConsumerGroup(),
                            pullConsumerWrapperOptional.get().getSubscribedTopics()});
            return;
        }
        LOGGER.log(Level.SEVERE, "Fail to cache consumer, consumer instance name : {0}, consumer group : {1}",
                new Object[]{pullConsumer.getInstanceName(), pullConsumer.getConsumerGroup()});
    }

    /**
     * 移除PullConsumer包装类实例
     *
     * @param pullConsumer pullConsumer实例
     */
    public static void removePullConsumer(DefaultLitePullConsumer pullConsumer) {
        int hashCode = pullConsumer.hashCode();
        DefaultLitePullConsumerWrapper wrapper = RocketMqConsumerCache.PULL_CONSUMERS_CACHE
                .get(hashCode);
        if (wrapper != null) {
            RocketMqConsumerCache.PULL_CONSUMERS_CACHE.remove(hashCode);
            LOGGER.log(Level.INFO, "Success to remove consumer, consumer instance name : {0}, consumer group "
                            + ": {1}, topic : {2}",
                    new Object[]{wrapper.getInstanceName(), wrapper.getConsumerGroup(),
                            wrapper.getSubscribedTopics()});
        }
    }

    /**
     * 获取PullConsumer的包装类实例缓存
     *
     * @param pullConsumer pull消费者实例
     * @return PullConsumer包装类实例
     */
    public static DefaultLitePullConsumerWrapper getPullConsumerWrapper(
            Object pullConsumer) {
        return RocketMqConsumerCache.PULL_CONSUMERS_CACHE.get(pullConsumer.hashCode());
    }

    /**
     * 获取PullConsumer缓存
     *
     * @return PullConsumer缓存
     */
    public static Map<Integer, DefaultLitePullConsumerWrapper> getPullConsumerCache() {
        return RocketMqConsumerCache.PULL_CONSUMERS_CACHE;
    }

    private static void initExecutor() {
        if (executor == null) {
            synchronized (RocketMqPushConsumerController.class) {
                if (executor == null) {
                    executor = new ThreadPoolExecutor(THREAD_SIZE, THREAD_SIZE, THREAD_KEEP_ALIVE_TIME,
                            TimeUnit.SECONDS, new ArrayBlockingQueue<>(THREAD_QUEUE_CAPACITY));
                    executor.allowCoreThreadTimeOut(true);
                }
            }
        }
    }
}
