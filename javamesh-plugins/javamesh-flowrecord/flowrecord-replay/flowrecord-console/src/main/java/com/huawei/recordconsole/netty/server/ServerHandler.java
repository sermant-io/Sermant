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

package com.huawei.recordconsole.netty.server;

import com.huawei.recordconsole.netty.common.conf.KafkaConf;
import com.huawei.recordconsole.netty.common.handler.BaseHandler;
import com.huawei.recordconsole.netty.common.util.GZipUtils;
import com.huawei.recordconsole.netty.pojo.Message;

import com.google.protobuf.ByteString;

import io.netty.channel.ChannelHandlerContext;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.utils.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 网关服务端handler
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-07-12
 */
public class ServerHandler extends BaseHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyServer.class);

    // 随机数最大值
    private static final int MAX_RANDOM = 100;

    // 随机数最小值
    private static final int MIN_RANDOM = 0;

    private KafkaProducer<String, Bytes> producer;

    private KafkaConf conf;

    public ServerHandler(KafkaProducer<String, Bytes> producer, KafkaConf conf) {
        this.producer = producer;
        this.conf = conf;
    }

    @Override
    protected void handlerData(ChannelHandlerContext ctx, Message.NettyMessage msg) {
        List<Message.ServiceData> serviceDataList = msg.getServiceDataList();
        for (Message.ServiceData serviceData : serviceDataList) {
            // 获取NettyMessage中的业务数据
            ByteString data = serviceData.getData();

            // 解压业务数据
            byte[] message = GZipUtils.decompress(data.toByteArray());
            int dataType = serviceData.getDataTypeValue();
            switch (dataType) {
                case Message.ServiceData.DataType.RECORD_VALUE:
                    producer.send(new ProducerRecord<>(conf.getTopicRecord(), Bytes.wrap(message)));
                    break;
                default:
                    break;
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
