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

import org.apache.rocketmq.client.ClientConfig;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;

/**
 * DefaultMQPushConsumer包装类
 *
 * @author daizhenyu
 * @since 2023-12-04
 **/
public class DefaultMqPushConsumerWrapper extends AbstractConsumerWrapper {
    private final DefaultMQPushConsumer pushConsumer;

    private final DefaultMQPushConsumerImpl pushConsumerImpl;

    /**
     * 有参构造方法
     *
     * @param consumer push消费者
     * @param pushConsumerImpl 内部push消费者
     * @param clientFactory rocketmq客户端工厂类
     */
    public DefaultMqPushConsumerWrapper(DefaultMQPushConsumer consumer, DefaultMQPushConsumerImpl pushConsumerImpl,
            MQClientInstance clientFactory) {
        super(clientFactory);
        this.pushConsumer = consumer;
        this.pushConsumerImpl = pushConsumerImpl;
        initClientInfo();
    }

    @Override
    protected void initClientInfo() {
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