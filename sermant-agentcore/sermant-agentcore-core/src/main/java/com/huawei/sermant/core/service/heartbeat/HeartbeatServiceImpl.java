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

package com.huawei.sermant.core.service.heartbeat;

import com.huawei.sermant.core.common.CommonConstant;
import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.config.ConfigManager;
import com.huawei.sermant.core.lubanops.bootstrap.config.AgentConfigManager;
import com.huawei.sermant.core.lubanops.core.transfer.dto.heartbeat.HeartbeatMessage;
import com.huawei.sermant.core.lubanops.integration.transport.ClientManager;
import com.huawei.sermant.core.lubanops.integration.transport.netty.client.NettyClient;
import com.huawei.sermant.core.lubanops.integration.transport.netty.pojo.Message;
import com.huawei.sermant.core.plugin.common.PluginConstant;
import com.huawei.sermant.core.plugin.common.PluginSchemaValidator;
import com.huawei.sermant.core.utils.JarFileUtils;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * {@link HeartbeatService}的实现
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-10-25
 */
public class HeartbeatServiceImpl implements HeartbeatService {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 心跳最小发送间隔
     */
    private static final long HEARTBEAT_MINIMAL_INTERVAL = 1000L;

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
    private static volatile boolean isRunning = false;

    @Override
    public synchronized void start() {
        isRunning = true;
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
        final Map<String, String> pluginVersionMap = PluginSchemaValidator.getPluginVersionMap();

        // 循环运行
        while (isRunning) {
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
        nettyClient.sendData(msg.getBytes(CommonConstant.DEFAULT_CHARSET),
                Message.ServiceData.DataType.SERVICE_HEARTBEAT);
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
        if (!isRunning) {
            LOGGER.warning("HeartbeatService has not started yet. ");
            return;
        }
        isRunning = false;
        EXECUTOR.shutdown();
        EXT_INFO_MAP.clear();
    }

    @Override
    public void setExtInfo(ExtInfoProvider extInfo) {
        final String pluginJar = JarFileUtils.getJarUrl(extInfo.getClass()).getPath();
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(pluginJar);
            final Object nameAttr = JarFileUtils.getManifestAttr(jarFile, PluginConstant.PLUGIN_NAME_KEY);
            if (nameAttr != null) {
                EXT_INFO_MAP.put(nameAttr.toString(), extInfo);
            } else {
                LOGGER.warning(String.format(Locale.ROOT, "Get plugin name of %s failed. ", pluginJar));
            }
        } catch (IOException ignored) {
            LOGGER.warning(String.format(Locale.ROOT, "Cannot find manifest file of %s. ", pluginJar));
        } finally {
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException ignored) {
                    LOGGER.warning("Unexpected exception occurs. ");
                }
            }
        }
    }
}
