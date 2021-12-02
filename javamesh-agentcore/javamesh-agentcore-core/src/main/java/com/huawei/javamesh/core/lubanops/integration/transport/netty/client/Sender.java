/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.javamesh.core.lubanops.integration.transport.netty.client;

import com.huawei.javamesh.core.lubanops.integration.transport.netty.pojo.Message;

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
 * @since 2021-08-07
 */
public class Sender implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Sender.class);

    private BlockingQueue<Message.ServiceData> queue;

    private Channel channel;

    public Sender(Channel channel, BlockingQueue<Message.ServiceData> queue) {
        this.queue = queue;
        this.channel = channel;
    }

    @Override
    public void run() {
        List<Message.ServiceData> list;

        //消息队列不为空时，发送消息
        if (queue.size() > 0) {
            list = new ArrayList<>();
            queue.drainTo(list);
            Message.NettyMessage message = Message.NettyMessage.newBuilder()
                    .setMessageType(Message.NettyMessage.MessageType.SERVICE_DATA)
                    .addAllServiceData(list)
                    .build();
            if(channel==null){
                LOGGER.info("channel is null");
            }

            channel.writeAndFlush(message);
            LOGGER.info("The message is sent to the gateway successfully. Number of messages: {}", list.size());
        }
    }
}
