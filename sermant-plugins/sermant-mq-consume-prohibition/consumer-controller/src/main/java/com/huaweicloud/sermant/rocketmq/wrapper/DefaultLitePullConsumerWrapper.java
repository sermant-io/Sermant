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

package com.huaweicloud.sermant.rocketmq.wrapper;

import com.huaweicloud.sermant.rocketmq.constant.SubscriptionType;

import org.apache.rocketmq.client.ClientConfig;
import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.client.impl.consumer.AssignedMessageQueue;
import org.apache.rocketmq.client.impl.consumer.DefaultLitePullConsumerImpl;
import org.apache.rocketmq.client.impl.consumer.RebalanceImpl;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.common.message.MessageQueue;

import java.util.Collection;

/**
 * DefaultLitePullConsumer包装类
 *
 * @author daizhenyu
 * @since 2023-12-04
 **/
public class DefaultLitePullConsumerWrapper extends AbstractConsumerWrapper {
    private final DefaultLitePullConsumer pullConsumer;

    private final DefaultLitePullConsumerImpl pullConsumerImpl;

    private final RebalanceImpl rebalanceImpl;

    private Collection<MessageQueue> messageQueues;

    private SubscriptionType subscriptionType = SubscriptionType.NONE;

    private AssignedMessageQueue assignedMessageQueue;

    /**
     * 有参构造方法
     *
     * @param pullConsumer pull消费者
     * @param pullConsumerImpl 内部pull消费者
     * @param rebalanceImpl 重平衡实现类
     * @param clientFactory rocketmq客户端工厂类
     */
    public DefaultLitePullConsumerWrapper(DefaultLitePullConsumer pullConsumer,
            DefaultLitePullConsumerImpl pullConsumerImpl, RebalanceImpl rebalanceImpl,
            MQClientInstance clientFactory) {
        super(clientFactory);
        this.pullConsumer = pullConsumer;
        this.pullConsumerImpl = pullConsumerImpl;
        this.rebalanceImpl = rebalanceImpl;
        initClientInfo();
    }

    @Override
    protected void initClientInfo() {
        ClientConfig clientConfig = clientFactory.getClientConfig();
        nameServerAddress = clientConfig.getClientIP();
        clientIp = clientConfig.getClientIP();
        instanceName = clientConfig.getInstanceName();
        consumerGroup = pullConsumer.getConsumerGroup();
    }

    public void setMessageQueues(
            Collection<MessageQueue> messageQueues) {
        this.messageQueues = messageQueues;
    }

    public void setSubscriptionType(SubscriptionType subscriptionType) {
        this.subscriptionType = subscriptionType;
    }

    public DefaultLitePullConsumer getPullConsumer() {
        return pullConsumer;
    }

    public DefaultLitePullConsumerImpl getPullConsumerImpl() {
        return pullConsumerImpl;
    }

    public RebalanceImpl getRebalanceImpl() {
        return rebalanceImpl;
    }

    public Collection<MessageQueue> getMessageQueues() {
        return messageQueues;
    }

    public SubscriptionType getSubscriptionType() {
        return subscriptionType;
    }

    public AssignedMessageQueue getAssignedMessageQueue() {
        return assignedMessageQueue;
    }

    public void setAssignedMessageQueue(AssignedMessageQueue assignedMessageQueue) {
        this.assignedMessageQueue = assignedMessageQueue;
    }
}
