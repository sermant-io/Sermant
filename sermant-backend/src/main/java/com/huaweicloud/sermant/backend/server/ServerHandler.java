/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.backend.server;

import com.huaweicloud.sermant.backend.cache.CollectorCache;
import com.huaweicloud.sermant.backend.cache.HeartbeatCache;
import com.huaweicloud.sermant.backend.common.conf.DataTypeTopicMapping;
import com.huaweicloud.sermant.backend.common.handler.BaseHandler;
import com.huaweicloud.sermant.backend.common.util.GzipUtils;
import com.huaweicloud.sermant.backend.entity.HeartbeatEntity;
import com.huaweicloud.sermant.backend.entity.OperateType;
import com.huaweicloud.sermant.backend.entity.ServerInfo;
import com.huawei.sermant.backend.pojo.Message;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ByteString;

import io.netty.channel.ChannelHandlerContext;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 网关服务端handler
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-08-07
 */
public class ServerHandler extends BaseHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerHandler.class);

    private static final int HEARTBEAT_TOPIC_INDEX = 0;

    private static final int VISIBILITY_TOPIC_INDEX = 12;
    private final KafkaProducer<String, byte[]> producer;
    private KafkaConsumer<String, String> consumer;
    private boolean isHeartBeatCache;

    private final DataTypeTopicMapping topicMapping;

    private final Map<String, HeartbeatEntity> hbMessages = HeartbeatCache.getHeartbeatMessages();

    private final Map<String, ServerInfo> lastHeartBeatDate = HeartbeatCache.getHeartbeatDate();

    /**
     * ServerHandler
     *
     * @param producer         kafka producer
     * @param consumer         kafka consumer
     * @param topicMapping     kafka topic map
     * @param isHeartBeatCache is or not open heartbeat cache
     */
    public ServerHandler(KafkaProducer<String, byte[]> producer, KafkaConsumer<String, String> consumer,
                         DataTypeTopicMapping topicMapping, String isHeartBeatCache) {
        this.producer = producer;
        this.consumer = consumer;
        this.topicMapping = topicMapping;
        this.isHeartBeatCache = Boolean.parseBoolean(isHeartBeatCache);
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
                if (Objects.equals(topic, topicMapping.getTopicOfType(HEARTBEAT_TOPIC_INDEX))) {
                    writeHeartBeatCacheCache(topic, message);
                    continue;
                }
                if (Objects.equals(topic, topicMapping.getTopicOfType(VISIBILITY_TOPIC_INDEX))) {
                    handlerServiceVisibility(message);
                }
                if (!this.isHeartBeatCache) {
                    producer.send(new ProducerRecord<>(topic, message));
                }
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

    private void writeHeartBeatCacheCache(String topic, byte[] message) {
        // 缓存心跳数据
        if (Objects.equals(topic, topicMapping.getTopicOfType(HEARTBEAT_TOPIC_INDEX))) {
            String messageStr = new String(message, StandardCharsets.UTF_8);
            if (!this.isHeartBeatCache) {
                producer.send(new ProducerRecord<>(topic, message));
            }
            HeartbeatEntity heartbeatEntity = JSONObject.parseObject(messageStr, HeartbeatEntity.class);
            List<String> ips = heartbeatEntity.getIp();
            if (ips != null && ips.size() != 0 && heartbeatEntity.getPluginName() != null) {
                String instanceId = heartbeatEntity.getInstanceId();
                String pluginName = heartbeatEntity.getPluginName();
                hbMessages.put(pluginName + instanceId, heartbeatEntity);
            }
            ServerInfo serverInfo = new ServerInfo();
            serverInfo.setInstanceId(heartbeatEntity.getInstanceId());
            serverInfo.setValidateDate(new Date());
            lastHeartBeatDate.put(heartbeatEntity.getInstanceId(), serverInfo);
        }
    }

    /**
     * 处理服务可见性信息
     *
     * @param message 消息内容
     */
    private void handlerServiceVisibility(byte[] message) {
        String messageStr = new String(message, StandardCharsets.UTF_8);
        ServerInfo visibilityInfo = JSON.parseObject(messageStr, ServerInfo.class);
        if (OperateType.ADD.getType().equals(visibilityInfo.getOperateType())) {
            CollectorCache.saveInfo(visibilityInfo);
        } else {
            CollectorCache.removeServer(visibilityInfo);
        }
    }
}
