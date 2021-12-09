/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.javamesh.backend.server;

import com.google.protobuf.ByteString;
import com.huawei.javamesh.backend.common.conf.DataTypeTopicMapping;
import com.huawei.javamesh.backend.common.handler.BaseHandler;
import com.huawei.javamesh.backend.common.util.GzipUtils;
import com.huawei.javamesh.backend.pojo.Message;

import io.netty.channel.ChannelHandlerContext;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 网关服务端handler
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-08-07
 */
public class ServerHandler extends BaseHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerHandler.class);

    private final KafkaProducer<String, byte[]> producer;
    private KafkaConsumer<String, String> consumer;

    private final DataTypeTopicMapping topicMapping;

    public ServerHandler(KafkaProducer<String, byte[]> producer, KafkaConsumer<String, String> consumer, DataTypeTopicMapping topicMapping) {
        this.producer = producer;
        this.consumer = consumer;
        this.topicMapping = topicMapping;
    }

    @Override
    protected void handlerData(ChannelHandlerContext ctx, Message.NettyMessage msg) {
        List<Message.ServiceData> serviceDataList = msg.getServiceDataList();
        for (Message.ServiceData serviceData : serviceDataList) {
            // 获取NettyMessage中的业务数据
            ByteString data = serviceData.getData();

            // 解压业务数据
            byte[] message = GzipUtils.decompress(data.toByteArray());
            int dataType = serviceData.getDataTypeValue();
            String topic = topicMapping.getTopicOfType(dataType);
            if (StringUtils.hasText(topic)) {
                producer.send(new ProducerRecord<>(topic, message));
            } else {
                LOGGER.warn("Can not find the corresponding topic of type {}.", dataType);
            }
        }
    }

    @Override
    protected void handlerReaderIdle(ChannelHandlerContext ctx) {
        super.handlerReaderIdle(ctx);
        LOGGER.info("Client timeOut, close it");
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        LOGGER.info("Close channelHandlerContext");
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error("Exception occurs. Exception info: {}", cause);
    }
}
