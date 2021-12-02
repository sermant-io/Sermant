/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.javamesh.core.lubanops.integration.transport.netty.client;

import com.huawei.javamesh.core.lubanops.integration.transport.netty.pojo.Message;
import com.huawei.javamesh.core.lubanops.integration.utils.GzipUtils;

import com.google.protobuf.ByteString;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 网关客户端
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-08-07
 */
public class NettyClient {
    // 运行日志
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyClient.class);

    // 消息队列用于缓存来自用户的消息
    private static BlockingQueue<Message.ServiceData> queue = new LinkedBlockingQueue<>();

    private static final int CONNECT_TIMEOUT = 9000;

    private static final int WAIT_TIME = 30;

    private static final int SEND_INTERNAL_MILLISECOND = 10000;

    private static final int RECONNECT_INTERVAL_SECOND = 10;

    // 客户端读写闲置时间
    private int writeOrReadWaitTime;

    // 服务端ip
    private String ip;

    // 服务端端口
    private int port;

    // 发送消息间隔时间
    private int sendInterval;

    // 尝试重连服务器间隔时间
    private int reconectInterval;

    private Bootstrap bootstrap;

    private Channel channel;

    private ScheduledExecutorService pool;

    public NettyClient(String serverIp, int serverPort) {
        ip = serverIp;
        port = serverPort;
        writeOrReadWaitTime = WAIT_TIME;
        sendInterval = SEND_INTERNAL_MILLISECOND;
        reconectInterval = RECONNECT_INTERVAL_SECOND;
        bind();
    }

    private void bind() {
        EventLoopGroup eventExecutors = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventExecutors)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast(new IdleStateHandler(0, 0, writeOrReadWaitTime));
                        pipeline.addLast(new ProtobufVarint32FrameDecoder());
                        pipeline.addLast(new ProtobufDecoder(Message.NettyMessage.getDefaultInstance()));
                        pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                        pipeline.addLast(new ProtobufEncoder());
                        pipeline.addLast(new ClientHandler(NettyClient.this));
                    }
                });
        doConnect();
    }

    /**
     * 连接服务器
     */
    public synchronized void doConnect() {
        LOGGER.info("do connect");
        if (channel != null && channel.isActive()) {
            return;
        }
        if (pool != null && !pool.isShutdown()) {
            pool.shutdownNow();
        }
        ChannelFuture connect = bootstrap.connect(ip, port);

        // 添加连接监听
        connect.addListener((ChannelFutureListener) channelFuture -> {
            // 如果连接成功，启动发送线程，循环发送消息队列中的内容
            if (channelFuture.isSuccess()) {
                channel = channelFuture.channel();
                if (channel.isActive()) {
                    Sender sender = new Sender(channel, queue);
                    LOGGER.info("Successfully Connected to server");
                    pool = Executors.newScheduledThreadPool(1);
                    pool.scheduleAtFixedRate(sender, 0, sendInterval, TimeUnit.MILLISECONDS);
                }
            } else {
                // 失败则在X秒后重试连接
                LOGGER.info("Failed to connect,try reconnecting after {} seconds...", reconectInterval);
                channelFuture.channel().eventLoop().schedule(this::doConnect, reconectInterval, TimeUnit.SECONDS);
            }
        });
    }

    /**
     * 发送数据至服务端
     *
     * @param msg      传输数据
     * @param dataType 数据类型
     */
    public void sendData(byte[] msg, Message.ServiceData.DataType dataType) {
        if (msg == null) {
            LOGGER.warn("Message is null.");
            return;
        }
        byte[] compressMsg = GzipUtils.compress(msg);
        Message.ServiceData serviceData = Message.ServiceData.newBuilder()
                .setDataType(dataType)
                .setData(ByteString.copyFrom(compressMsg))
                .build();
        ThreadPools.getExecutor().execute(() -> {
            try {
                queue.put(serviceData);
            } catch (InterruptedException e) {
                LOGGER.error("Exception occurs when put data to message queue. Exception info: {}", e);
            }
        });
    }
}
