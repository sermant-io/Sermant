/*
 * Copyright (C) 2021-2024 Sermant Authors. All rights reserved.
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
import com.huaweicloud.sermant.backend.common.handler.BaseHandler;
import com.huaweicloud.sermant.backend.entity.InstanceMeta;
import com.huaweicloud.sermant.backend.entity.NodeEntity;
import com.huaweicloud.sermant.backend.entity.event.EventMessage;
import com.huaweicloud.sermant.backend.entity.heartbeat.HeartbeatMessage;
import com.huaweicloud.sermant.backend.entity.visibility.OperateType;
import com.huaweicloud.sermant.backend.entity.visibility.ServerInfo;
import com.huaweicloud.sermant.backend.handler.EventPushHandler;
import com.huaweicloud.sermant.backend.pojo.Message;
import com.huaweicloud.sermant.backend.util.GzipUtils;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.ByteString;

import io.netty.channel.ChannelHandlerContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

/**
 * Netty Server Handler
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-08-07
 */
@Component
public class ServerHandler extends BaseHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerHandler.class);

    /**
     * Netty Server Handler
     */
    private static ServerHandler serverHandler;

    private final Map<String, HeartbeatMessage> hbMessages = HeartbeatCache.getHeartbeatMessageMap();

    @Autowired
    private EventPushHandler eventPushHandler;

    @Autowired
    private EventServer eventServer;

    /**
     * Initializes the event notify server
     */
    @PostConstruct
    public void init() {
        serverHandler = this;
        serverHandler.eventPushHandler = this.eventPushHandler;
        serverHandler.eventServer = this.eventServer;
    }

    @Override
    protected void handlerData(ChannelHandlerContext ctx, Message.NettyMessage msg) {
        List<Message.ServiceData> serviceDataList = msg.getServiceDataList();
        for (Message.ServiceData serviceData : serviceDataList) {
            ByteString data = serviceData.getData();
            byte[] message = GzipUtils.decompress(data.toByteArray());
            int dataType = serviceData.getDataTypeValue();
            switch (dataType) {
                case Message.ServiceData.DataType.HEARTBEAT_DATA_VALUE:
                    handleHeartBeat(message);
                    break;
                case Message.ServiceData.DataType.EVENT_DATA_VALUE:
                    handleEvent(message);
                    break;
                case Message.ServiceData.DataType.VISIBILITY_DATA_VALUE:
                    handleServiceVisibility(message);
                    break;
                default:
                    LOGGER.warn("Can not find the corresponding data type {}.", dataType);
            }
        }
    }

    @Override
    protected void handlerReaderIdle(ChannelHandlerContext ctx) {
        super.handlerReaderIdle(ctx);
        LOGGER.info("Client timeout, close it");
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        LOGGER.info("Close channelHandlerContext");
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error("Exception occurs. Exception info: {}", cause.getMessage());
    }

    private void handleHeartBeat(byte[] message) {
        // Cache heartbeat data
        HeartbeatMessage heartbeatMessage =
                JSON.parseObject(new String(message, StandardCharsets.UTF_8), HeartbeatMessage.class);

        // agent information is written to the database
        writeInstanceMeta(heartbeatMessage);

        List<String> ips = heartbeatMessage.getIp();
        if (ips != null && ips.size() != 0) {
            heartbeatMessage.setReceiveTime(System.currentTimeMillis());
            heartbeatMessage.setHealth(true);
            hbMessages.put(heartbeatMessage.getService() + heartbeatMessage.getInstanceId(), heartbeatMessage);
        }
        setServiceValidityPeriod(heartbeatMessage.getInstanceId());
    }

    /**
     * Store instance metadata
     *
     * @param heartbeatMessage heartbeat message
     */
    private void writeInstanceMeta(HeartbeatMessage heartbeatMessage) {
        InstanceMeta instanceMeta = new InstanceMeta();
        instanceMeta.setInstanceId(heartbeatMessage.getInstanceId());
        instanceMeta.setMetaHash(heartbeatMessage.getInstanceId());
        instanceMeta.setService(heartbeatMessage.getService());
        List<String> ips = heartbeatMessage.getIp();
        if (ips != null && ips.size() != 0) {
            NodeEntity nodeEntity = new NodeEntity();
            nodeEntity.setIp(ips.get(0));
            instanceMeta.setNode(nodeEntity);
        }
        serverHandler.eventServer.addEvent(instanceMeta);
    }

    /**
     * Configure the validity period of service visibility data
     *
     * @param instanceId instance id
     */
    private void setServiceValidityPeriod(String instanceId) {
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setValidateDate(new Date());
        CollectorCache.SERVER_VALIDITY_PERIOD_MAP.put(instanceId, serverInfo);
    }

    /**
     * Process service visibility information
     *
     * @param message message content
     */
    private void handleServiceVisibility(byte[] message) {
        String messageStr = new String(message, StandardCharsets.UTF_8);
        ServerInfo visibilityInfo = JSON.parseObject(messageStr, ServerInfo.class);
        if (OperateType.ADD.getType().equals(visibilityInfo.getOperateType())) {
            CollectorCache.saveInfo(visibilityInfo);
        } else {
            CollectorCache.removeServer(visibilityInfo);
        }
    }

    /**
     * Process event data
     *
     * @param message message content
     */
    private void handleEvent(byte[] message) {
        String messageStr = new String(message, StandardCharsets.UTF_8);
        EventMessage eventMessage = JSON.parseObject(messageStr, EventMessage.class);
        serverHandler.eventPushHandler.pushEvent(eventMessage.getEvents());
    }
}
