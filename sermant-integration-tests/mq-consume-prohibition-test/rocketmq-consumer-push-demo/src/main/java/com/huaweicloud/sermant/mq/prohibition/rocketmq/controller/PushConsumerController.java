/*
 *  Copyright (C) 2024-2024 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.mq.prohibition.rocketmq.controller;

import com.huaweicloud.sermant.mq.prohibition.common.constant.RocketMqConstant;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * controller接口类
 *
 * @author daizhenyu
 * @since 2024-01-08
 **/
@RestController
public class PushConsumerController {
    /**
     * push消费者
     */
    private static DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(RocketMqConstant.PUSH_CONSUME_GROUP);

    @Value("${rocketmq.address}")
    private String rocketMqAddress;

    /**
     * 用于检查消费者进程是否正常启动
     *
     * @return string
     */
    @RequestMapping(value = "checkRocketMqStatus", method = RequestMethod.GET,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public String checkRocketMqStatus() {
        return "ok";
    }

    /**
     * 消费者初始化参数并启动
     *
     * @throws MQClientException
     */
    @RequestMapping(value = "initAndStart", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public void initAndStart() throws MQClientException {
        consumer.setNamesrvAddr(rocketMqAddress);
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        consumer.subscribe(RocketMqConstant.PUSH_CONSUME_TOPIC, RocketMqConstant.TAG_SCOPE);
        consumer.registerMessageListener(new MessageListenerOrderly() {
            @Override
            public ConsumeOrderlyStatus consumeMessage(List<MessageExt> messageExts,
                    ConsumeOrderlyContext context) {
                if (messageExts != null) {
                    for (MessageExt ext : messageExts) {
                        // 模拟处理消息
                        ext.getBody();
                    }
                }
                return ConsumeOrderlyStatus.SUCCESS;
            }
        });
        consumer.start();
    }

    /**
     * 消费者订阅topic
     *
     * @param topic 订阅的topic
     * @throws MQClientException
     */
    @RequestMapping(value = "subscribe", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public void subscribe(String topic) throws MQClientException {
        consumer.subscribe(topic, RocketMqConstant.TAG_SCOPE);
    }

    /**
     * 消费者取消订阅topic
     *
     * @param topic 取消订阅的topic
     * @throws MQClientException
     */
    @RequestMapping(value = "unsubscribe", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public void unsubscribe(String topic) {
        consumer.unsubscribe(topic);
    }

    /**
     * 获取消费者组的消费者实例id
     *
     * @param topic 订阅的topic
     * @param group 消费者组
     * @return 消费者实例id列表
     * @throws MQClientException
     */
    @RequestMapping(value = "subscribe", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public String getConsumerIdList(String topic, String group) {
        List<String> consumerIdList = consumer.getDefaultMQPushConsumerImpl().getmQClientFactory()
                .findConsumerIdList(topic, group);
        if (consumerIdList == null) {
            return "[]";
        }
        return consumerIdList.toString();
    }
}
