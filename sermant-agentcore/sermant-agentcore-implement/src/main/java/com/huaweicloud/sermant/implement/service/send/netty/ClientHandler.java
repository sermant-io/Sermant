/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.implement.service.send.netty.pojo.Message;

import io.netty.channel.ChannelHandlerContext;

import java.util.Locale;
import java.util.logging.Logger;

/**
 * Client message handler
 *
 * @author lilai
 * @version 0.0.1
 * @since 2022-03-26
 */
public class ClientHandler extends BaseHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final NettyClient client;

    /**
     * Constructor
     *
     * @param client client
     */
    public ClientHandler(NettyClient client) {
        this.client = client;
    }

    @Override
    protected void handlerData(ChannelHandlerContext ctx, Message.NettyMessage msg) {
    }

    @Override
    protected void handlerAllIdle(ChannelHandlerContext ctx) {
        super.handlerAllIdle(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        LOGGER.severe("Server channel is inaction");
        client.doConnect();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.severe(String.format(Locale.ROOT, "Exception occurs. Exception info: %s", cause.getMessage()));
    }
}
