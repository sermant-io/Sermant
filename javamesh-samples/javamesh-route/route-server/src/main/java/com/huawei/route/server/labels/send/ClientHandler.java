/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.labels.send;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * 客户端执行处理类
 *
 * @author zhanghu
 * @since 2021-05-21
 */
public class ClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientHandler.class);
    private String data;
    private ClientChannelInitializer channelInitializer;

    public ClientHandler(String data, ClientChannelInitializer channelInitializer) {
        this.data = data;
        this.channelInitializer = channelInitializer;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.channel().writeAndFlush(Unpooled.copiedBuffer(data, StandardCharsets.UTF_8));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf o) {
        channelInitializer.setResponse(o.toString(StandardCharsets.UTF_8));
        channelHandlerContext.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error("error", cause);
        ctx.close();
    }
}
