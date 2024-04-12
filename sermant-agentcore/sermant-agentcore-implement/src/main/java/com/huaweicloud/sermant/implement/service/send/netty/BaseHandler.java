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

package com.huaweicloud.sermant.implement.service.send.netty;

import com.huaweicloud.sermant.implement.service.send.netty.pojo.Message;
import com.huaweicloud.sermant.implement.service.send.netty.pojo.Message.NettyMessage.MessageType;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler base class. Different data types select processing methods
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-08-07
 */
public abstract class BaseHandler extends SimpleChannelInboundHandler<Message.NettyMessage> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseHandler.class);

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Message.NettyMessage msg) {
        // Get the received message type
        int type = msg.getMessageTypeValue();

        if (type == MessageType.SERVICE_DATA_VALUE) {
            handlerData(ctx, msg);
        }
    }

    /**
     * Data processing method
     *
     * @param ctx Context object
     * @param msg Message received
     */
    protected abstract void handlerData(ChannelHandlerContext ctx, Message.NettyMessage msg);

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
     * This method is triggered when no data is read over time
     *
     * @param ctx Context object
     */
    protected void handlerReaderIdle(ChannelHandlerContext ctx) {
        LOGGER.debug("Read idle...");
    }

    /**
     * This method is triggered when no data is written over time
     *
     * @param ctx Context object
     */
    protected void handlerWriterIdle(ChannelHandlerContext ctx) {
        LOGGER.debug("Write idle...");
    }

    /**
     * his method is triggered when no data is written or read over time
     *
     * @param ctx Context object
     */
    protected void handlerAllIdle(ChannelHandlerContext ctx) {
        LOGGER.debug("Read and write idle...");
    }
}
