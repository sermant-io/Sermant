/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.demo.tagtransmission.rocketmq.producer.controller;

import com.huaweicloud.demo.tagtransmission.midware.common.MessageConstant;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 消息中间件生产者controller, 用于生产消息
 *
 * @author daizhenyu
 * @since 2023-09-28
 **/
@RestController
@RequestMapping(value = "rocketMqProducer")
public class RocketMqProducerController {
    @Value("${rocketmq.address}")
    private String rocketMqAddress;

    /**
     * 用于检查生产者进程是否正常启动
     *
     * @return string
     */
    @RequestMapping(value = "checkRocketMqProducerStatus", method = RequestMethod.GET,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public String checkRocketMqProducerStatus() {
        return "ok";
    }

    /**
     * roketmq生成一条消息
     *
     * @return string
     * @throws InterruptedException
     * @throws RemotingException
     * @throws MQClientException
     * @throws MQBrokerException
     */
    @RequestMapping(value = "testRocketMqProducer", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public String testRocketMqProducer()
            throws InterruptedException, RemotingException, MQClientException, MQBrokerException {
        produceRocketMqData();
        return "rocketmq-produce-message-success";
    }

    private void produceRocketMqData()
            throws RemotingException, InterruptedException, MQBrokerException, MQClientException {
        DefaultMQProducer producer = new DefaultMQProducer(MessageConstant.ROCKETMQ_PRODUCE_GROUP);
        producer.setNamesrvAddr(rocketMqAddress);
        producer.start();

        String messageBody = buildMessageBody(MessageConstant.MESSAGE_BODY_ROCKET);

        Message message = new Message(MessageConstant.TOPIC, MessageConstant.TAG,
                messageBody.getBytes(StandardCharsets.UTF_8));
        producer.send(message);
        producer.shutdown();
    }

    private String buildMessageBody(String body) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(MessageConstant.TIME_FORMAT);
        String messageBody = body + dtf.format(LocalDateTime.now());
        return messageBody;
    }
}