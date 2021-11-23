/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.apm.backend.server;

import com.google.protobuf.ByteString;
import com.huawei.apm.backend.common.conf.KafkaConf;
import com.huawei.apm.backend.common.handler.BaseHandler;
import com.huawei.apm.backend.common.util.GzipUtils;
import com.huawei.apm.backend.pojo.Message;

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
 * @since 2021-08-07
 */
public class ServerHandler extends BaseHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerHandler.class);

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
            byte[] message = GzipUtils.decompress(data.toByteArray());
            int dataType = serviceData.getDataTypeValue();

            // 此处可扩展消息类型，针对不同消息类型执行相应操作
            switch (dataType) {
                // 心跳数据类型
                case Message.ServiceData.DataType.SERVICE_HEARTBEAT_VALUE:
                    producer.send(new ProducerRecord<>(conf.getTopicHeartBeat(), Bytes.wrap(message)));
                    break;
                case Message.ServiceData.DataType.LOG_VALUE:
                    producer.send(new ProducerRecord<>(conf.getTopicLog(), Bytes.wrap(message)));
                    break;
                case Message.ServiceData.DataType.PLUGIN_FLOW_CONTROL_DATA_VALUE:
                    producer.send(new ProducerRecord<>(conf.getTopicFlowControl(), Bytes.wrap(message)));
                    break;
                case Message.ServiceData.DataType.PLUGIN_FLOW_RECORD_DATA_VALUE:
                    producer.send(new ProducerRecord<>(conf.getTopicFlowRecord(), Bytes.wrap(message)));
                    break;
                case Message.ServiceData.DataType.SERVER_MONITOR_VALUE:
                    producer.send(new ProducerRecord<>(conf.getTopicServerMonitor(), Bytes.wrap(message)));
                    break;
                case Message.ServiceData.DataType.ORACLE_JVM_MONITOR_VALUE:
                    producer.send(new ProducerRecord<>(conf.getTopicOracleJvmMonitor(), Bytes.wrap(message)));
                    break;
                case Message.ServiceData.DataType.IBM_JVM_MONITOR_VALUE:
                    producer.send(new ProducerRecord<>(conf.getTopicIbmJvmMonitor(), Bytes.wrap(message)));
                    break;
                case Message.ServiceData.DataType.AGENT_REGISTRATION_VALUE:
                    producer.send(new ProducerRecord<>(conf.getTopicAgentRegistration(), Bytes.wrap(message)));
                    break;
                case Message.ServiceData.DataType.AGENT_MONITOR_VALUE:
                    producer.send(new ProducerRecord<>(conf.getTopicAgentMonitor(), Bytes.wrap(message)));
                    break;
                case Message.ServiceData.DataType.AGENT_SPAN_EVENT_VALUE:
                    producer.send(new ProducerRecord<>(conf.getTopicAgentSpanEvent(), Bytes.wrap(message)));
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
