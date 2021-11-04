/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.core.service.heartbeat;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

import com.huawei.apm.core.lubanops.bootstrap.config.AgentConfigManager;
import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.apm.core.lubanops.core.transfer.dto.heartbeat.HeartbeatMessage;
import com.huawei.apm.core.lubanops.integration.transport.ClientManager;
import com.huawei.apm.core.lubanops.integration.transport.netty.client.NettyClient;
import com.huawei.apm.core.lubanops.integration.transport.netty.pojo.Message;

/**
 * {@link HeartbeatService}的实现
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/10/25
 */
public class HeartbeatServiceImpl implements HeartbeatService {
    /**
     * 日志
     */
    private static final Logger LOGGER = LogFactory.getLogger();

    /**
     * 心跳信息的集合，键为心跳名称，值为心跳信息的提供方式
     */
    private static final Map<String, MessageBuilder> HEARTBEAT_MAP = new ConcurrentHashMap<String, MessageBuilder>();

    /**
     * 心跳名称的存储集合，键为心跳触发帧数，值为心跳名称的存储者
     */
    private static final Map<Integer, NameHolder> NAME_HOLDER_MAP = new ConcurrentHashMap<Integer, NameHolder>();

    /**
     * 执行线程池，单例即可
     */
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable runnable) {
                    final Thread daemonThread = new Thread(runnable);
                    daemonThread.setDaemon(true);
                    return daemonThread;
                }
            }
    );

    /**
     * 运行标记
     */
    private static volatile boolean runFlag = false;

    @Override
    public synchronized void start() {
        runFlag = true;
        EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                doRun();
            }
        });
    }

    /**
     * 线程内容
     */
    private void doRun() {
        // 创建NettyClient
        final NettyClient nettyClient = ClientManager.getNettyClientFactory().getNettyClient(
                AgentConfigManager.getNettyServerIp(),
                Integer.parseInt(AgentConfigManager.getNettyServerPort()));
        // 包装心跳信息发送者
        final MessageSender messageSender = new MessageSender() {
            @Override
            public void send(String msg) {
                if (msg == null || "".equals(msg)) {
                    return;
                }
                nettyClient.sendData(msg.getBytes(Charset.forName("UTF-8")),
                        Message.ServiceData.DataType.SERVICE_HEARTBEAT);
            }
        };
        // 包装心跳名称消费者，用于查找心跳消息构建者，并发送心跳
        final NameConsumer nameConsumer = new NameConsumer() {
            @Override
            public boolean consume(String name) {
                final MessageBuilder provider = HEARTBEAT_MAP.get(name);
                if (provider == null) {
                    return false;
                }
                messageSender.send(provider.build());
                return true;
            }
        };
        // 运行中循环
        while (runFlag) {
            final long now = System.currentTimeMillis();
            final Iterator<NameHolder> itr = NAME_HOLDER_MAP.values().iterator();
            while (itr.hasNext()) {
                final NameHolder nameHolder = itr.next();
                // 检查时间，并执行心跳名称消费
                if (nameHolder.canExecute(now) && nameHolder.foreachName(nameConsumer)) {
                    itr.remove();
                }
            }
            // 休眠1帧
            HeartbeatInterval.sleepMinimalInterval();
        }
    }

    @Override
    public synchronized void stop() {
        if (!runFlag) {
            LOGGER.warning("HeartbeatService has not started yet. ");
            return;
        }
        runFlag = false;
        EXECUTOR.shutdown();
        HEARTBEAT_MAP.clear();
        NAME_HOLDER_MAP.clear();
    }

    @Override
    public void heartbeat(String heartbeatName) {
        heartbeat(heartbeatName, Collections.emptyMap());
    }

    @Override
    public void heartbeat(String heartbeatName, Map<String, String> infoMap) {
        heartbeat(heartbeatName, () -> infoMap, HeartbeatInterval.SOMETIMES);
    }

    @Override
    public void heartbeat(String heartbeatName, InfoMapProvider infoMapProvider, HeartbeatInterval heartbeatInterval) {
        heartbeat(heartbeatName, infoMapProvider, heartbeatInterval.getFrames());
    }

    @Override
    public synchronized void heartbeat(String heartbeatName, InfoMapProvider infoMapProvider, int frames) {
        if (!runFlag) {
            LOGGER.warning("HeartbeatService has not started yet. ");
            return;
        }
        // 不允许重复的心跳名称
        if (HEARTBEAT_MAP.containsKey(heartbeatName)) {
            LOGGER.warning(String.format(Locale.ROOT,
                    "Duplicate heartbeat name [%s] is not supported. ", heartbeatName));
            return;
        }
        // 构建心跳信息构建器并记录
        final MessageBuilder messageBuilder = new MessageBuilder() {
            @Override
            public String build() {
                HeartbeatMessage message = new HeartbeatMessage().registerInformation("name", heartbeatName);
                for (Map.Entry<String, String> entry : infoMapProvider.provide().entrySet()) {
                    message = message.registerInformation(entry.getKey(), entry.getValue());
                }
                return message.generateCurrentMessage();
            }
        };
        HEARTBEAT_MAP.put(heartbeatName, messageBuilder);
        // 添加心跳名称
        NameHolder nameHolder = NAME_HOLDER_MAP.get(frames);
        if (nameHolder == null) {
            nameHolder = new NameHolder(frames);
            NAME_HOLDER_MAP.put(frames, nameHolder);
        }
        nameHolder.addHeartbeat(heartbeatName);
    }

    @Override
    public synchronized void stopHeartbeat(String heartbeatName) {
        if (!runFlag) {
            LOGGER.warning("HeartbeatService has not started yet. ");
            return;
        }
        HEARTBEAT_MAP.remove(heartbeatName);
    }

    /**
     * 心跳信息构建者
     */
    private interface MessageBuilder {
        /**
         * 构建心跳信息
         *
         * @return 心跳信息
         */
        String build();
    }

    /**
     * 心跳信息发送者
     */
    private interface MessageSender {
        /**
         * 发送心跳信息
         *
         * @param message 心跳信息
         */
        void send(String message);
    }

    /**
     * 心跳名称消费者
     */
    private interface NameConsumer {
        /**
         * 消费心跳名称
         *
         * @param name 心跳名称
         * @return 心跳名称是否还有意义
         */
        boolean consume(String name);
    }

    /**
     * 心跳名称的存储者，内存储心跳的名称集和心跳触发帧数
     */
    private static class NameHolder {
        /**
         * 心跳名称
         */
        private final Set<String> heartbeatNames;
        /**
         * 帧数
         */
        private final int frames;
        /**
         * 下次执行时间，注册后第一帧必执行
         */
        private long executeTime;

        private NameHolder(int frames) {
            this.heartbeatNames = new HashSet<>();
            this.frames = frames;
        }

        /**
         * 检查时间是否可执行，当当前时间超过执行时间时，返回真，并更新执行时间，否则返回假
         *
         * @param now 当前时间
         * @return 是否执行
         */
        private boolean canExecute(long now) {
            if (now >= executeTime) {
                executeTime = now + HeartbeatInterval.getInterval(frames);
                return true;
            }
            return false;
        }

        /**
         * 遍历所有心跳名称，心跳名称无意义时移除
         *
         * @param nameConsumer 心跳名称消费者
         * @return 剩余的心跳名称集合是否为空
         */
        private boolean foreachName(NameConsumer nameConsumer) {
            final Iterator<String> itr = heartbeatNames.iterator();
            while (itr.hasNext()) {
                if (!nameConsumer.consume(itr.next())) {
                    itr.remove();
                }
            }
            return heartbeatNames.isEmpty();
        }

        /**
         * 添加心跳名称
         *
         * @param heartbeatName 心跳名称
         */
        private void addHeartbeat(String heartbeatName) {
            heartbeatNames.add(heartbeatName);
        }
    }
}
