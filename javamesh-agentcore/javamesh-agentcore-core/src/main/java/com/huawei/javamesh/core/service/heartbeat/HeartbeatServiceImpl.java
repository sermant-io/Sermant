/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.core.service.heartbeat;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import com.huawei.javamesh.core.common.BootArgsIndexer;
import com.huawei.javamesh.core.common.LoggerFactory;
import com.huawei.javamesh.core.config.ConfigManager;
import com.huawei.javamesh.core.lubanops.bootstrap.config.AgentConfigManager;
import com.huawei.javamesh.core.lubanops.core.transfer.dto.heartbeat.HeartbeatMessage;
import com.huawei.javamesh.core.lubanops.integration.transport.ClientManager;
import com.huawei.javamesh.core.lubanops.integration.transport.netty.client.NettyClient;
import com.huawei.javamesh.core.lubanops.integration.transport.netty.pojo.Message;
import com.huawei.javamesh.core.plugin.PluginManager;
import com.huawei.javamesh.core.plugin.common.PluginConstant;
import com.huawei.javamesh.core.util.JarFileUtil;

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
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 心跳最小发送间隔
     */
    private static final long HEARTBEAT_MINIMAL_INTERVAL = 1000;

    /**
     * 心跳额外参数
     */
    private static final Map<String, ExtInfoProvider> EXT_INFO_MAP = new ConcurrentHashMap<String, ExtInfoProvider>();

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
                execute();
            }
        });
    }

    /**
     * 执行循环
     */
    private void execute() {
        // 创建NettyClient
        final NettyClient nettyClient = ClientManager.getNettyClientFactory().getNettyClient(
                AgentConfigManager.getNettyServerIp(),
                Integer.parseInt(AgentConfigManager.getNettyServerPort()));
        // 获取插件名和版本集合
        final Map<String, String> pluginVersionMap = PluginManager.getPluginVersionMap();
        // 循环运行
        while (runFlag) {
            try {
                for (Map.Entry<String, String> entry : pluginVersionMap.entrySet()) {
                    heartbeat(nettyClient, entry.getKey(), entry.getValue());
                }
            } catch (Exception e) {
                LOGGER.warning(String.format(Locale.ROOT,
                        "Exception [%s] occurs for [%s] when sending heartbeat message, retry next time. ",
                        e.getClass(), e.getMessage()));
            }
            sleep();
        }
    }

    /**
     * 休眠
     */
    private void sleep() {
        try {
            final long interval = Math.max(
                    ConfigManager.getConfig(HeartbeatConfig.class).getInterval(),
                    HEARTBEAT_MINIMAL_INTERVAL);
            Thread.sleep(interval);
        } catch (InterruptedException ignored) {
            LOGGER.warning("Unexpected interrupt heartbeat waiting. ");
        }
    }

    /**
     * 发送心跳
     *
     * @param nettyClient   netty客户端
     * @param pluginName    插件名称
     * @param pluginVersion 插件版本
     */
    private void heartbeat(NettyClient nettyClient, String pluginName, String pluginVersion) {
        final HeartbeatMessage message = new HeartbeatMessage()
                .registerInformation(PLUGIN_NAME_KEY, pluginName)
                .registerInformation(PLUGIN_VERSION_KEY, pluginVersion);
        addExtInfo(pluginName, message);
        final String msg = message.generateCurrentMessage();
        nettyClient.sendData(msg.getBytes(Charset.forName("UTF-8")), Message.ServiceData.DataType.SERVICE_HEARTBEAT);
    }

    /**
     * 添加心跳额外信息
     *
     * @param pluginName 插件名称
     * @param message    心跳信息
     */
    private void addExtInfo(String pluginName, HeartbeatMessage message) {
        final ExtInfoProvider provider = EXT_INFO_MAP.get(pluginName);
        if (provider == null) {
            return;
        }
        final Map<String, String> extInfo = provider.getExtInfo();
        if (extInfo == null) {
            return;
        }
        for (Map.Entry<String, String> entry : extInfo.entrySet()) {
            message.registerInformation(entry.getKey(), entry.getValue());
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
        EXT_INFO_MAP.clear();
    }

    @Override
    public void setExtInfo(ExtInfoProvider extInfo) {
        final String pluginJar = extInfo.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        try {
            final Object nameAttr = JarFileUtil.getManifestAttr(new JarFile(pluginJar), PluginConstant.PLUGIN_NAME_KEY);
            if (nameAttr != null) {
                EXT_INFO_MAP.put(nameAttr.toString(), extInfo);
            } else {
                LOGGER.warning(String.format(Locale.ROOT, "Get plugin name of %s failed. ", pluginJar));
            }
        } catch (IOException ignored) {
            LOGGER.warning(String.format(Locale.ROOT, "Cannot find manifest file of %s. ", pluginJar));
        }
    }
}
