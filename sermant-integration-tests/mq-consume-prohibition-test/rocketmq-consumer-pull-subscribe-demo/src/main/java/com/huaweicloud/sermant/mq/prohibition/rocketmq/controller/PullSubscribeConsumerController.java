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
import com.huaweicloud.sermant.mq.prohibition.common.utils.ReflectUtils;

import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.impl.consumer.DefaultLitePullConsumerImpl;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
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
public class PullSubscribeConsumerController {
    /**
     * pull消费者
     */
    private static DefaultLitePullConsumer consumer =
            new DefaultLitePullConsumer(RocketMqConstant.PULL_SUBSCRIBE_CONSUME_GROUP);

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
     * 消费者启动
     *
     * @throws MQClientException
     */
    @RequestMapping(value = "start", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public void start() throws MQClientException {
        consumer.setNamesrvAddr(rocketMqAddress);
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        consumer.subscribe(RocketMqConstant.PULL_SUBSCRIBE_CONSUME_TOPIC, RocketMqConstant.TAG_SCOPE);
        consumer.start();
        while (true) {
            List<MessageExt> messageExts = consumer.poll();
            for (MessageExt messageExt : messageExts) {
                // 模拟处理消息
                messageExt.getBody();
            }
        }
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
    @RequestMapping(value = "consumerIdList", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public String getConsumerIdList(String topic, String group) {
        DefaultLitePullConsumerImpl pullConsumerImpl = (DefaultLitePullConsumerImpl) ReflectUtils
                .getField(consumer.getClass(), consumer, "defaultLitePullConsumerImpl");
        MQClientInstance clientFactory = (MQClientInstance) ReflectUtils
                .getField(pullConsumerImpl.getClass(), pullConsumerImpl, "mQClientFactory");
        List<String> consumerIdList = clientFactory.findConsumerIdList(topic, group);
        if (consumerIdList == null) {
            return "[]";
        }
        return consumerIdList.toString();
    }
}
