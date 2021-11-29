/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.apm.backend.server;

import com.google.protobuf.ByteString;
import com.huawei.apm.backend.common.conf.KafkaConf;
import com.huawei.apm.backend.common.handler.BaseHandler;
import com.huawei.apm.backend.common.util.GzipUtils;
import com.huawei.apm.backend.pojo.Message;

import com.huawei.apm.backend.service.SendService;
import com.huawei.apm.backend.service.SendServiceManager;
import io.netty.channel.ChannelHandlerContext;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * 网关服务端handler
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-08-07
 */
public class ServerHandler extends BaseHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerHandler.class);

    // 随机数最大值
    private static final int MAX_RANDOM = 100;

    // 随机数最小值
    private static final int MIN_RANDOM = 0;

    private KafkaProducer<String, String> producer;
    private KafkaConsumer<String, String> consumer;

    Map<Integer, SendService> sendServiceMap = SendServiceManager.INSTANCE.getSendServices();

    private KafkaConf conf;

    public ServerHandler(KafkaProducer<String, String> producer, KafkaConsumer<String, String> consumer, KafkaConf conf) {
        this.producer = producer;
        this.consumer = consumer;
        this.conf = conf;
    }

    @Override
    protected void handlerData(ChannelHandlerContext ctx, Message.NettyMessage msg) {
        List<Message.ServiceData> serviceDataList = msg.getServiceDataList();
        for (Message.ServiceData serviceData : serviceDataList) {
            // 获取NettyMessage中的业务数据
            ByteString data = serviceData.getData();

            // 解压业务数据
            byte[] message = GzipUtils.decompress(data.toByteArray());
            String msgStr = new String(message);
            int dataType = serviceData.getDataTypeValue();

            SendService sendService = sendServiceMap.get(dataType);
            if (sendService != null) {
                sendService.send(conf, msgStr);
            } else {
                LOGGER.error("send service is null");
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
