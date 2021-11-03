/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.apm.core.lubanops.integration.transport.netty.client;

import com.huawei.apm.core.lubanops.integration.transport.netty.pojo.Message;

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

    private int sendInterval;

    public Sender(Channel channel, BlockingQueue<Message.ServiceData> queue, int sendInterval) {
        this.queue = queue;
        this.channel = channel;
        this.sendInterval = sendInterval;
    }

    @Override
    public void run() {
        List<Message.ServiceData> list;

        // 当channel不为空且存活时，循环遍历消息队列
        while (channel != null && channel.isActive()) {
            // 消息队列不为空时，发送消息
            if (queue.size() > 0) {
                try {
                    list = new ArrayList<>();
                    queue.drainTo(list);
                    Message.NettyMessage message = Message.NettyMessage.newBuilder()
                            .setMessageType(Message.NettyMessage.MessageType.SERVICE_DATA)
                            .addAllServiceData(list)
                            .build();
                    channel.writeAndFlush(message);
                    Thread.sleep(sendInterval);
                } catch (InterruptedException e) {
                    LOGGER.error("Exception occurs when send message.Exception info: {}", e);
                }
            }
        }
    }
}
