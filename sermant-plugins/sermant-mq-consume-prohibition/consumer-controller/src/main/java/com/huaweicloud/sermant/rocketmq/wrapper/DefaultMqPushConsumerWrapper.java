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

package com.huaweicloud.sermant.rocketmq.wrapper;

import org.apache.rocketmq.client.ClientConfig;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;

/**
 * DefaultMQPushConsumer packaging class
 *
 * @author daizhenyu
 * @since 2023-12-04
 **/
public class DefaultMqPushConsumerWrapper extends AbstractConsumerWrapper {
    private final DefaultMQPushConsumer pushConsumer;

    private final DefaultMQPushConsumerImpl pushConsumerImpl;

    /**
     * parameter construction method
     *
     * @param consumer push consumers
     * @param pushConsumerImpl Push consumers internally
     * @param clientFactory rocketmq client factory class
     */
    public DefaultMqPushConsumerWrapper(DefaultMQPushConsumer consumer, DefaultMQPushConsumerImpl pushConsumerImpl,
            MQClientInstance clientFactory) {
        super(clientFactory);
        this.pushConsumer = consumer;
        this.pushConsumerImpl = pushConsumerImpl;
        initPushClientInfo();
    }

    private void initPushClientInfo() {
        ClientConfig clientConfig = clientFactory.getClientConfig();
        nameServerAddress = clientConfig.getClientIP();
        clientIp = clientConfig.getClientIP();
        instanceName = clientConfig.getInstanceName();
        consumerGroup = pushConsumer.getConsumerGroup();
    }

    public DefaultMQPushConsumer getPushConsumer() {
        return pushConsumer;
    }

    public DefaultMQPushConsumerImpl getPushConsumerImpl() {
        return pushConsumerImpl;
    }
}