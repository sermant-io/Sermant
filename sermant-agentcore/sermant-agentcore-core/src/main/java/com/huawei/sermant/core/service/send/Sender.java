/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.sermant.core.service.send;

import com.huawei.sermant.core.lubanops.integration.transport.netty.pojo.Message;

import io.netty.channel.Channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * 发送数据类
 *
 * @author lilai
 * @version 0.0.1
 * @since 2022-03-26
 */
public class Sender implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Sender.class);

    private final BlockingQueue<Message.ServiceData> queue;

    private final Channel channel;

    /**
     * 构造函数
     *
     * @param channel channel
     * @param queue queue
     */
    public Sender(Channel channel, BlockingQueue<Message.ServiceData> queue) {
        this.queue = queue;
        this.channel = channel;
    }

    @Override
    public void run() {
        List<Message.ServiceData> list;

        // 消息队列不为空时，发送消息
        if (queue.size() > 0) {
            list = new ArrayList<>();
            queue.drainTo(list);
            Message.NettyMessage message = Message.NettyMessage.newBuilder()
                .setMessageType(Message.NettyMessage.MessageType.SERVICE_DATA).addAllServiceData(list).build();
            if (channel == null) {
                LOGGER.info("channel is null");
            }

            channel.writeAndFlush(message);
            LOGGER.info("The message is sent to the gateway successfully. Number of messages: {}", list.size());
        }
    }
}
