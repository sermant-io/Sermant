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

package io.sermant.implement.service.send.netty;

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
import io.sermant.core.common.LoggerFactory;
import io.sermant.core.config.ConfigManager;
import io.sermant.core.notification.NettyNotificationType;
import io.sermant.core.notification.NotificationInfo;
import io.sermant.core.notification.NotificationManager;
import io.sermant.core.service.send.config.GatewayConfig;
import io.sermant.core.utils.ThreadFactoryUtils;
import io.sermant.implement.service.send.netty.pojo.Message;
import io.sermant.implement.service.send.netty.pojo.Message.ServiceData;
import io.sermant.implement.utils.GzipUtils;

import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * NettyClient
 *
 * @author lilai
 * @version 0.0.1
 * @since 2022-03-26
 */
public class NettyClient {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * Backoff factor foe reconnection
     */
    private static final int BACKOFF_FACTOR = 2;

    /**
     * Message sending interval
     */
    private final int sendInternalTime;

    /**
     * Initial reconnection time
     */
    private final int initReconnectInternalTime;

    /**
     * Maximum reconnection time
     */
    private final int maxReconnectInternalTime;

    /**
     * Compare time
     */
    private final int compareTime;

    // Block queue, used to cache messages, for non-instant messages, reduce the frequency of message sending, set
    // the capacity to 100 messages
    private final BlockingQueue<ServiceData> queue = new ArrayBlockingQueue<>(100);

    private final String ip;

    private final int port;

    private Bootstrap bootstrap;

    private EventLoopGroup eventLoopGroup;

    private Channel channel;

    private ScheduledExecutorService executorService;

    private boolean connectionAvailable = false;

    private int reconnectInternalTime;

    /**
     * Status. False: connection failed, true: connection succeeded, null: no connection
     */
    private Boolean isConnected = null;

    /**
     * Constructor
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
     * Gracefully close Netty
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
     * Connect to server
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

        // Add connection listener
        connect.addListener((ChannelFutureListener) channelFuture -> {
            this.connectionAvailable = channelFuture.isSuccess();

            // If the connection is successful, the sending thread is started and the contents of the message queue
            // are sent over and over again
            if (this.connectionAvailable) {
                createSendTask(channelFuture);
            } else {
                reconnect(channelFuture);
            }
        });
    }

    private void reconnect(ChannelFuture channelFuture) {
        // Check whether the connection has been connected before, prevent the connection failure to send a
        // notification, only the connection is disconnected or the first connection fails to send a
        // notification
        if ((isConnected == null || isConnected) && NotificationManager.isEnable()) {
            NotificationManager.doNotify(new NotificationInfo(NettyNotificationType.DISCONNECTED, null));
            isConnected = false;
        }

        // If the system fails, the system reconnects by backoff way. The initial reconnection time is 5
        // seconds and the maximum reconnection time is 180 seconds
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

    private void createSendTask(ChannelFuture channelFuture) {
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
    }

    /**
     * Send data to the server
     *
     * @param msg message
     * @param dataType data type
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
     * Send instant time data to the server
     *
     * @param msg message
     * @param dataType data type
     * @return send result
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
