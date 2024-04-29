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

package io.sermant.implement.service.send.netty;

import io.netty.channel.Channel;
import io.sermant.implement.service.send.netty.pojo.Message;
import io.sermant.implement.service.send.netty.pojo.Message.NettyMessage;
import io.sermant.implement.service.send.netty.pojo.Message.ServiceData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Data sender
 *
 * @author lilai
 * @version 0.0.1
 * @since 2022-03-26
 */
public class Sender implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Sender.class);

    private final BlockingQueue<ServiceData> queue;

    private final Channel channel;

    /**
     * Constructor
     *
     * @param channel channel
     * @param queue queue
     */
    public Sender(Channel channel, BlockingQueue<ServiceData> queue) {
        this.queue = queue;
        this.channel = channel;
    }

    @Override
    public void run() {
        List<ServiceData> list;

        // Send a message when the message queue is not empty
        if (queue.size() > 0) {
            list = new ArrayList<>();
            queue.drainTo(list);
            NettyMessage message = Message.NettyMessage.newBuilder()
                    .setMessageType(Message.NettyMessage.MessageType.SERVICE_DATA).addAllServiceData(list).build();
            if (channel == null) {
                LOGGER.warn("channel is null");
            } else {
                channel.writeAndFlush(message);
                LOGGER.info("The message is sent to the gateway successfully. Number of messages: {}", list.size());
            }
        }
    }
}
