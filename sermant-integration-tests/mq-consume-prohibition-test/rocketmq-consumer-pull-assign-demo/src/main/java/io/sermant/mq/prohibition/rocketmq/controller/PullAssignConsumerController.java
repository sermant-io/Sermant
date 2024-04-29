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

package io.sermant.mq.prohibition.rocketmq.controller;

import io.sermant.mq.prohibition.common.constant.RocketMqConstant;
import io.sermant.mq.prohibition.common.utils.ReflectUtils;

import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.impl.consumer.AssignedMessageQueue;
import org.apache.rocketmq.client.impl.consumer.DefaultLitePullConsumerImpl;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/**
 * controller接口类
 *
 * @author daizhenyu
 * @since 2024-01-08
 **/
@RestController
public class PullAssignConsumerController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PullAssignConsumerController.class);

    private static DefaultLitePullConsumer consumer =
            new DefaultLitePullConsumer(RocketMqConstant.PULL_ASSIGN_CONSUME_GROUP);

    @Value("${rocketmq.address}")
    private String rocketMqAddress;

    /**
     * 用于检查消费者进程是否正常启动
     *
     * @return string
     */
    @RequestMapping(value = "checkStatus", method = RequestMethod.GET,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public String checkStatus() {
        return "ok";
    }

    /**
     * 消费者初始化参数并启动
     */
    @RequestMapping(value = "initAndStart", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public void initAndStart() {
        Thread thread = new Thread(() -> {
            consumer.setNamesrvAddr(rocketMqAddress);
            consumer.setAutoCommit(false);
            try {
                consumer.start();
                consumer.assign(consumer.fetchMessageQueues(RocketMqConstant.PULL_ASSIGN_CONSUME_TOPIC));
            } catch (MQClientException e) {
                LOGGER.error("Consumer run failed, message : {}", e.getMessage());
                return;
            }
            while (true) {
                List<MessageExt> messageExts = consumer.poll();
                for (MessageExt messageExt : messageExts) {
                    // 模拟处理消息
                    messageExt.getBody();
                    consumer.commitSync();
                }
            }
        });
        thread.start();
    }

    /**
     * 获取消息队列数量
     *
     * @return 消息队列数量
     */
    @RequestMapping(value = "messageQueue", method = RequestMethod.GET)
    public String getMessageQueue() {
        DefaultLitePullConsumerImpl pullConsumerImpl =
                (DefaultLitePullConsumerImpl) ReflectUtils.getField(consumer.getClass(), consumer,
                        "defaultLitePullConsumerImpl");
        AssignedMessageQueue assignedMessageQueue =
                (AssignedMessageQueue) ReflectUtils.getField(pullConsumerImpl.getClass(), pullConsumerImpl,
                        "assignedMessageQueue");
        Class<?> clazz = assignedMessageQueue.getClass();
        Object messageQueues;
        if (ReflectUtils.isHasMethod(clazz, "messageQueues")) {
            messageQueues = ReflectUtils
                    .invokeMethod(assignedMessageQueue.getClass(), assignedMessageQueue, "messageQueues");
            return Integer.toString(((Set<MessageQueue>) messageQueues).size());
        }
        messageQueues = ReflectUtils
                .invokeMethod(clazz, assignedMessageQueue, "getAssignedMessageQueues");
        return Integer.toString(((Set<MessageQueue>) messageQueues).size());
    }
}