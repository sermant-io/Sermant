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
import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.notification.NettyNotificationType;
import com.huaweicloud.sermant.core.notification.NotificationInfo;
import com.huaweicloud.sermant.core.notification.NotificationManager;
import com.huaweicloud.sermant.core.service.ServiceConfig;
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.core.service.send.config.GatewayConfig;
import com.huaweicloud.sermant.core.service.visibility.api.VisibilityService;
import com.huaweicloud.sermant.core.utils.ThreadFactoryUtils;
import com.huaweicloud.sermant.implement.service.send.netty.pojo.Message;
import com.huaweicloud.sermant.implement.utils.GzipUtils;

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

import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * 网关客户端
 *
 * @author lilai
 * @version 0.0.1
 * @since 2022-03-26
 */
public class NettyClient {
    // 运行日志
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 指数退避因子
     */
    private static final int BACKOFF_FACTOR = 2;

    /**
     * 消息发送间隔
     */
    private final int sendInternalTime;

    /**
     * 初始重连时间
     */
    private final int initReconnectInternalTime;

    /**
     * 最大重连时间
     */
    private final int maxReconnectInternalTime;

    /**
     * 比较数
     */
    private final int compareTime;

    // 阻塞队列，用于缓存消息，对于非即时消息，减少消息发送的频率，设置值为100条消息
    private final BlockingQueue<Message.ServiceData> queue = new ArrayBlockingQueue<>(100);

    private final String ip;

    private final int port;

    private Bootstrap bootstrap;

    private EventLoopGroup eventLoopGroup;

    private Channel channel;

    private ScheduledExecutorService executorService;

    private VisibilityService visibilityService;

    private boolean connectionAvailable = false;

    private int reconnectInternalTime;

    /**
     * 状态标识：false链接失败 true链接成功。null 未建立链接
     */
    private Boolean isConnected = null;

    /**
     * 构造函数
     *
     * @param serverIp serverIp
     * @param serverPort serverPort
     */
    public NettyClient(String serverIp, int serverPort) {
        GatewayConfig gatewayConfig = ConfigManager.getConfig(GatewayConfig.class);
        sendInternalTime = gatewayConfig.getSendInternalTime();
        initReconnectInternalTime = gatewayConfig.getInitReconnectInternalTime();
        maxReconnectInternalTime = gatewayConfig.getMaxReconnectInternalTime();
        compareTime = maxReconnectInternalTime / BACKOFF_FACTOR;
        ip = serverIp;
        port = serverPort;
        reconnectInternalTime = initReconnectInternalTime;

        bind();
    }

    /**
     * 优雅关闭Netty
     */
    public void stop() {
        eventLoopGroup.shutdownGracefully();
    }

    private void bind() {
        eventLoopGroup = new NioEventLoopGroup(new ThreadFactoryUtils("netty-nio-event-loop-group"));
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
                        ConfigManager.getConfig(GatewayConfig.class).getNettyConnectTimeout())
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel newChannel) {
                        ChannelPipeline pipeline = newChannel.pipeline();
                        pipeline.addLast(new IdleStateHandler(0, 0,
                                ConfigManager.getConfig(GatewayConfig.class).getNettyWriteAndReadWaitTime(),
                                TimeUnit.MILLISECONDS));
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
        LOGGER.info("Netty do connect.");
        if (channel != null && channel.isActive()) {
            return;
        }
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
        ChannelFuture connect = bootstrap.connect(ip, port);

        // 添加连接监听
        connect.addListener((ChannelFutureListener) channelFuture -> {
            this.connectionAvailable = channelFuture.isSuccess();

            // 如果连接成功，启动发送线程，循环发送消息队列中的内容
            if (this.connectionAvailable) {
                reconnectInternalTime = initReconnectInternalTime;
                channel = channelFuture.channel();
                if (channel.isActive()) {
                    isConnected = true;
                    Sender sender = new Sender(channel, queue);
                    LOGGER.info("Successfully Connected to server");
                    executorService = Executors.newScheduledThreadPool(1, new ThreadFactoryUtils("netty-send-thread"));
                    executorService.scheduleAtFixedRate(sender, 0, sendInternalTime, TimeUnit.SECONDS);
                    if (NotificationManager.isEnable()) {
                        NotificationManager.doNotify(new NotificationInfo(NettyNotificationType.CONNECTED, null));
                    }
                }
                if (ConfigManager.getConfig(ServiceConfig.class).isVisibilityEnable()) {
                    if (visibilityService == null) {
                        visibilityService = ServiceManager.getService(VisibilityService.class);
                    }
                    visibilityService.reconnectHandler();
                }
            } else {
                // 判断之前是否已链接 防止链接失败一直发通知，只有链接断开或者首次链接失败才发通知。
                if ((isConnected == null || isConnected) && NotificationManager.isEnable()) {
                    NotificationManager.doNotify(new NotificationInfo(NettyNotificationType.DISCONNECTED, null));
                    isConnected = false;
                }

                // 若失败则指数退避重连，初始时间为5秒，最大重连时间为180秒
                LOGGER.info(String.format(Locale.ROOT, "Failed to connect,try reconnecting after %s seconds ",
                        reconnectInternalTime));
                channelFuture.channel().eventLoop()
                        .schedule(this::doConnect, reconnectInternalTime, TimeUnit.SECONDS);
                if (reconnectInternalTime > compareTime) {
                    reconnectInternalTime = maxReconnectInternalTime;
                } else {
                    reconnectInternalTime = reconnectInternalTime * BACKOFF_FACTOR;
                }
            }
        });
    }

    /**
     * 发送数据至服务端
     *
     * @param msg 传输数据
     * @param dataType 数据类型
     */
    public void sendData(byte[] msg, Message.ServiceData.DataType dataType) {
        if (msg == null) {
            LOGGER.warning("Message is null.");
            return;
        }
        byte[] compressMsg = GzipUtils.compress(msg);
        Message.ServiceData serviceData =
                Message.ServiceData.newBuilder().setDataType(dataType).setData(ByteString.copyFrom(compressMsg))
                        .build();
        if (!queue.offer(serviceData)) {
            LOGGER.info(String.format(Locale.ROOT, "Message queue is full, add %s failed.", serviceData.getDataType()));
        }
    }

    /**
     * 发送即时数据到服务端
     *
     * @param msg 传输数据
     * @param dataType 数据类型
     * @return boolean 发送成功 ｜ 失败
     */
    public boolean sendInstantData(byte[] msg, Message.ServiceData.DataType dataType) {
        if (!this.connectionAvailable) {
            LOGGER.warning("Netty connection is not available.");
            return false;
        }
        byte[] compressMsg = GzipUtils.compress(msg);
        Message.ServiceData serviceData =
                Message.ServiceData.newBuilder().setDataType(dataType).setData(ByteString.copyFrom(compressMsg))
                        .build();
        Message.NettyMessage message = Message.NettyMessage.newBuilder()
                .setMessageType(Message.NettyMessage.MessageType.SERVICE_DATA).addServiceData(serviceData).build();
        if (channel == null) {
            LOGGER.warning("Netty channel is null, send instant data failure.");
            return false;
        } else {
            channel.writeAndFlush(message);
            LOGGER.info("Sent instant data successfully by netty.");
            return true;
        }
    }
}
