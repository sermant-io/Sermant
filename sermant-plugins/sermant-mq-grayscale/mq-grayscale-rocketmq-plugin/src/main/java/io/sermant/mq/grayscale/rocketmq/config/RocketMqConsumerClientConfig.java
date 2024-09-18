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

package io.sermant.mq.grayscale.rocketmq.config;

import org.apache.rocketmq.client.impl.factory.MQClientInstance;

/**
 * consumer client config entity
 *
 * @author chengyouling
 * @since 2024-05-27
 **/
public class RocketMqConsumerClientConfig {
    private String topic;

    private String address;

    private String consumerGroup;

    private MQClientInstance mqClientInstance;

    /**
     * construction
     *
     * @param address address
     * @param topic topic
     * @param consumerGroup consumerGroup
     */
    public RocketMqConsumerClientConfig(String address, String topic, String consumerGroup) {
        this.address = address;
        this.topic = topic;
        this.consumerGroup = consumerGroup;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public MQClientInstance getMqClientInstance() {
        return mqClientInstance;
    }

    public void setMqClientInstance(MQClientInstance mqClientInstance) {
        this.mqClientInstance = mqClientInstance;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }
}
