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

package io.sermant.backend.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import io.sermant.backend.common.conf.VisibilityConfig;
import io.sermant.backend.pojo.Message;
import io.sermant.backend.timer.DeleteTimeoutDataTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Timer;

import javax.annotation.PostConstruct;

/**
 * Netty server
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-08-07
 */
@Component
public class NettyServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyServer.class);

    // Maximum connection wait size
    private static final int CONNECTION_SIZE = 1024;

    // Delay time of clearing cached data
    private static final int DELETE_TIMEOUT_DATA_DELAY_TIME = 5000;

    // Interval for clearing cached data
    private static final int DELETE_TIMEOUT_DATA_PERIOD_TIME = 3000;

    private static final Timer TIMER = new Timer();

    // Read wait time
    @Value("${netty.wait.time}")
    private int readWaitTime;

    // Netty port
    @Value("${netty.port}")
    private int port;

    // Effective heartbeat time
    @Value("${max.effective.time:60000}")
    private long maxEffectiveTime;

    // Heartbeat cache time
    @Value("${max.cache.time:600000}")
    private long maxCacheTime;

    @Value("${netty.thread.num:20}")
    private int threadNum;

    @Autowired
    private VisibilityConfig visibilityConfig;

    /**
     * Server-side core method Is pulled up with tomcat startup to handle client connections and data
     */
    @PostConstruct
    public void start() {
        LOGGER.info("Start netty server...");

        // Clear expired data
        TIMER.schedule(new DeleteTimeoutDataTask(maxEffectiveTime, maxCacheTime, visibilityConfig),
                DELETE_TIMEOUT_DATA_DELAY_TIME, DELETE_TIMEOUT_DATA_PERIOD_TIME);

        // Thread group that handles connections
        EventLoopGroup bossGroup = new NioEventLoopGroup(threadNum);

        // Thread group that handles data
        EventLoopGroup workerGroup = new NioEventLoopGroup(threadNum);
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, CONNECTION_SIZE).childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel channel) {
                            ChannelPipeline pipeline = channel.pipeline();

                            // If no client is received within the read wait period, the read wait event is triggered
                            pipeline.addLast(new IdleStateHandler(readWaitTime, 0, 0));
                            pipeline.addLast(new ProtobufVarint32FrameDecoder());
                            pipeline.addLast(new ProtobufDecoder(Message.NettyMessage.getDefaultInstance()));
                            pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                            pipeline.addLast(new ProtobufEncoder());
                            pipeline.addLast(new ServerHandler());
                        }
                    });

            // Synchronous blocking waiting for the server to start
            serverBootstrap.bind(port).sync();
            LOGGER.info("Netty server started, port is {}", port);
        } catch (InterruptedException e) {
            LOGGER.error("Exception occurs when start netty server, exception message : {}", e.getMessage());
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
