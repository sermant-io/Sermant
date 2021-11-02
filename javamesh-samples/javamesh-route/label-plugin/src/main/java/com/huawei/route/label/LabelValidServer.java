/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.label;

import com.huawei.apm.bootstrap.lubanops.log.LogFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

import java.net.InetSocketAddress;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * 标签生效服务端
 *
 * @author zhanghu
 * @since 2021-05-21
 */
public class LabelValidServer {
    private static final Logger LOGGER = LogFactory.getLogger();

    private static final int MINIMUM = 64;

    private static final int INITIAL = 65535;

    private static final int MAXIMUM = 65535;

    private static final int MAX_LENGTH = 1024;

    public LabelValidServer() {
    }

    public void start(int port) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(group)
            .channel(NioServerSocketChannel.class)
            .childOption(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(MINIMUM, INITIAL, MAXIMUM))
            .localAddress(new InetSocketAddress(port))
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) {
                    ch.pipeline().addLast(new LabelValidServerHandler());
                    ch.pipeline().addLast(new LineBasedFrameDecoder(MAX_LENGTH));
                    ch.pipeline().addLast(new StringDecoder());
                }
            });

        try {
            ChannelFuture channelFuture = serverBootstrap.bind().sync();
            LOGGER.info(String.format(Locale.ENGLISH, "thread: {%s} is running. {%s} started and listen on {%s}.",
                    Thread.currentThread().getName(),
                    LabelValidServer.class.getName(),
                    channelFuture.channel().localAddress()));
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            LOGGER.severe(String.format(Locale.ENGLISH, "Netty was interrupted by %s", e.getMessage()));
        } finally {
            group.shutdownGracefully();
        }
    }
}
