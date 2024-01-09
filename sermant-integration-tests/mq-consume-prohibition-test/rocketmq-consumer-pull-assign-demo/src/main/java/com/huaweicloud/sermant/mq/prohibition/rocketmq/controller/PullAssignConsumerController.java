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
import org.apache.rocketmq.client.impl.consumer.AssignedMessageQueue;
import org.apache.rocketmq.client.impl.consumer.DefaultLitePullConsumerImpl;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
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
    private static DefaultLitePullConsumer consumer =
            new DefaultLitePullConsumer(RocketMqConstant.PULL_ASSIGN_CONSUME_GROUP);

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
        consumer.setAutoCommit(false);
        consumer.start();
        consumer.assign(consumer.fetchMessageQueues(RocketMqConstant.PULL_ASSIGN_CONSUME_TOPIC));
        while (true) {
            List<MessageExt> messageExts = consumer.poll();
            for (MessageExt messageExt : messageExts) {
                // 模拟处理消息
                messageExt.getBody();
                consumer.commitSync();
            }
        }
    }

    /**
     * 获取消息队列
     *
     * @return 消息队列
     */
    @RequestMapping(value = "messageQueue", method = RequestMethod.GET)
    public Set<MessageQueue> getMessageQueue() {
        DefaultLitePullConsumerImpl pullConsumerImpl =
                (DefaultLitePullConsumerImpl) ReflectUtils.getField(consumer.getClass(), consumer,
                        "defaultLitePullConsumerImpl");
        AssignedMessageQueue assignedMessageQueue =
                (AssignedMessageQueue) ReflectUtils.getField(pullConsumerImpl.getClass(), pullConsumerImpl,
                        "assignedMessageQueue");
        return assignedMessageQueue.messageQueues();
    }
}
