/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.apm.core.ext.lubanops.transport.netty.client;

import com.huawei.apm.core.ext.lubanops.transport.netty.common.BaseHandler;
import com.huawei.apm.core.ext.lubanops.transport.netty.pojo.Message;

import io.netty.channel.ChannelHandlerContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * client消息处理类
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-08-07
 */
public class ClientHandler extends BaseHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientHandler.class);

    private NettyClient client;

    public ClientHandler(NettyClient client) {
        this.client = client;
    }

    @Override
    protected void handlerData(ChannelHandlerContext ctx, Message.NettyMessage msg) {
    }

    @Override
    protected void handlerAllIdle(ChannelHandlerContext ctx) {
        super.handlerAllIdle(ctx);
        sendPingMsg(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        LOGGER.error("Server channel is inaction");
        client.doConnect();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error("Exception occurs. Exception info: {}", cause);
    }
}
