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

package com.huawei.javamesh.core.lubanops.integration.transport.netty.common;

import com.huawei.javamesh.core.lubanops.integration.transport.netty.pojo.Message;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * handler的基类
 * 不同数据类型选择处理方式，心跳触发等功能
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-08-07
 */
public abstract class BaseHandler extends SimpleChannelInboundHandler<Message.NettyMessage> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseHandler.class);

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Message.NettyMessage msg) {
        // 获取收到的消息类型
        int type = msg.getMessageTypeValue();
        switch (type) {
            // 如果收到消息类型为心跳PING，直接发送心跳PONG
            case Message.NettyMessage.MessageType.HEARTBEAT_PING_VALUE:
                sendPongMsg(ctx, msg);
                break;

            // 如果收到消息为PONG，证明对方状态正常，连接正常
            case Message.NettyMessage.MessageType.HEARTBEAT_PONG_VALUE:
                break;

            // 如果为业务数据进行各自的处理
            case Message.NettyMessage.MessageType.SERVICE_DATA_VALUE:
                handlerData(ctx, msg);
                break;
            default:
                break;
        }
    }

    /**
     * 业务数据处理方法
     *
     * @param ctx 上下文对象
     * @param msg 收到的数据
     */
    protected abstract void handlerData(ChannelHandlerContext ctx, Message.NettyMessage msg);

    /**
     * 发送PING心跳
     *
     * @param ctx 上下文对象
     */
    protected void sendPingMsg(ChannelHandlerContext ctx) {
        Message.NettyMessage msg = Message.NettyMessage.newBuilder()
                .setMessageType(Message.NettyMessage.MessageType.HEARTBEAT_PING)
                .setHeartBeat(Message.HeartBeat.newBuilder().build())
                .build();
        Channel channel = ctx.channel();
        channel.writeAndFlush(msg);
    }

    private void sendPongMsg(ChannelHandlerContext ctx, Message.NettyMessage msg) {
        Message.NettyMessage message = msg.newBuilderForType()
                .setMessageType(Message.NettyMessage.MessageType.HEARTBEAT_PONG)
                .setHeartBeat(Message.HeartBeat.newBuilder().build())
                .build();
        Channel channel = ctx.channel();
        channel.writeAndFlush(message);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        IdleStateEvent stateEvent = (IdleStateEvent) evt;
        switch (stateEvent.state()) {
            case READER_IDLE:
                handlerReaderIdle(ctx);
                break;
            case WRITER_IDLE:
                handlerWriterIdle(ctx);
                break;
            case ALL_IDLE:
                handlerAllIdle(ctx);
                break;
            default:
                break;
        }
    }

    /**
     * 超过时间未读到数据触发方法
     *
     * @param ctx 上下文对象
     */
    protected void handlerReaderIdle(ChannelHandlerContext ctx) {
        LOGGER.debug("Read idle...");
    }

    /**
     * 超过时间未写出数据触发方法
     *
     * @param ctx 上下文对象
     */
    protected void handlerWriterIdle(ChannelHandlerContext ctx) {
        LOGGER.debug("Read idle...");
    }

    /**
     * 超过时间未读写数据触发方法
     *
     * @param ctx 上下文对象
     */
    protected void handlerAllIdle(ChannelHandlerContext ctx) {
        LOGGER.debug("Read and write idle...");
    }
}
