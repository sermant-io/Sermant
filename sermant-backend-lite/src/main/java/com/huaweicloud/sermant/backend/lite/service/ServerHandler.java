/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.backend.lite.service;

import com.huaweicloud.sermant.backend.lite.cache.HeartbeatCache;
import com.huaweicloud.sermant.backend.lite.entity.HeartbeatMessage;
import com.huaweicloud.sermant.backend.lite.pojo.Message;
import com.huaweicloud.sermant.backend.lite.utils.GzipUtils;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.ByteString;

import io.netty.channel.ChannelHandlerContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
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

    private final Map<String, HeartbeatMessage> hbMessages = HeartbeatCache.getHeartbeatMessageMap();

    /**
     * 构造方法
     */
    public ServerHandler() {
    }

    @Override
    protected void handlerData(ChannelHandlerContext ctx, Message.NettyMessage msg) {
        List<Message.ServiceData> serviceDataList = msg.getServiceDataList();
        for (Message.ServiceData serviceData : serviceDataList) {
            // 获取NettyMessage中的业务数据
            ByteString data = serviceData.getData();
            if (serviceData.getDataType().equals(Message.ServiceData.DataType.SERVICE_HEARTBEAT)) {
                // 解压业务数据
                byte[] message = GzipUtils.decompress(data.toByteArray());
                writeHeartBeatCacheCache(message);
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

    private void writeHeartBeatCacheCache(byte[] message) {
        // 缓存心跳数据
        HeartbeatMessage heartbeatMessage =
            JSON.parseObject(new String(message, StandardCharsets.UTF_8), HeartbeatMessage.class);
        List<String> ips = heartbeatMessage.getIp();
        if (ips != null && ips.size() != 0) {
            heartbeatMessage.setReceiveTime(System.currentTimeMillis());
            heartbeatMessage.setHealth(true);
            hbMessages.put(heartbeatMessage.getAppName() + heartbeatMessage.getInstanceId(), heartbeatMessage);
        }
    }
}
