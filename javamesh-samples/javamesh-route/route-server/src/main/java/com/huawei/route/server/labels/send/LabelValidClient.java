/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.labels.send;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * 使用udp传输标签生效的netty客户端
 *
 * @author zhanghu
 * @since 2021-05-21
 */
public class LabelValidClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(LabelValidClient.class);
    private static final int MINIMUM = 64;
    private static final int INITIAL = 65535;
    private static final int MAXIMUM = 65535;
    private final String host;
    private final int port;

    public LabelValidClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * netty客户端启动入口
     *
     * @param data 需发送的数据
     * @return String
     * @throws InterruptedException 线程中断抛出
     */
    public String start(String data) throws InterruptedException {
        EventLoopGroup clientGroup = new NioEventLoopGroup();
        Bootstrap clientBootStrap = new Bootstrap();
        ClientChannelInitializer channelInitializer = new ClientChannelInitializer(data);
        clientBootStrap.group(clientGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(MINIMUM, INITIAL, MAXIMUM))
                .remoteAddress(new InetSocketAddress(host, port))
                .handler(channelInitializer);
        try {
            ChannelFuture future = clientBootStrap.connect().sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            LOGGER.error("connect interrupted.", e);
        } finally {
            clientGroup.shutdownGracefully();
        }
        return channelInitializer.getResponse();
    }
}
