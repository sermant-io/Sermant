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

package com.huaweicloud.sermant.implement.service.heartbeat;

import com.huaweicloud.sermant.core.common.CommonConstant;
import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.plugin.common.PluginConstant;
import com.huaweicloud.sermant.core.plugin.common.PluginSchemaValidator;
import com.huaweicloud.sermant.core.service.heartbeat.api.ExtInfoProvider;
import com.huaweicloud.sermant.core.service.heartbeat.api.HeartbeatService;
import com.huaweicloud.sermant.core.service.heartbeat.common.HeartbeatConstant;
import com.huaweicloud.sermant.core.service.heartbeat.common.HeartbeatMessage;
import com.huaweicloud.sermant.core.service.heartbeat.common.PluginInfo;
import com.huaweicloud.sermant.core.service.heartbeat.config.HeartbeatConfig;
import com.huaweicloud.sermant.core.utils.JarFileUtils;
import com.huaweicloud.sermant.implement.service.send.netty.NettyClient;
import com.huaweicloud.sermant.implement.service.send.netty.NettyClientFactory;
import com.huaweicloud.sermant.implement.service.send.netty.pojo.Message;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
     * 心跳额外参数
     */
    private static final Map<String, ExtInfoProvider> EXT_INFO_MAP = new ConcurrentHashMap<String, ExtInfoProvider>();

    /**
     * 执行线程池，单例即可
     */
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        final Thread daemonThread = new Thread(runnable);
        daemonThread.setDaemon(true);
        return daemonThread;
    });

    /**
     * 运行标记
     */
    private static volatile boolean isRunning = false;

    private static final Object LOCK = new Object();

    @Override
    public void start() {
        synchronized (LOCK) {
            isRunning = true;
            EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    execute();
                }
            });
        }
    }

    @Override
    public void stop() {
        synchronized (LOCK) {
            if (!isRunning) {
                LOGGER.warning("HeartbeatService has not started yet. ");
                return;
            }
            isRunning = false;
            EXECUTOR.shutdown();
            EXT_INFO_MAP.clear();
        }
    }

    /**
     * 执行循环
     */
    private void execute() {
        // 创建NettyClient
        final NettyClient nettyClient = NettyClientFactory.getInstance().getDefaultNettyClient();

        // 初始化心跳数据常量
        HeartbeatMessage heartbeatMessage = new HeartbeatMessage();

        Map<String, PluginInfo> pluginInfoMap = heartbeatMessage.getPluginInfoMap();

        // 循环运行
        while (isRunning) {
            // 获取插件名和版本集合
            final Map<String, String> pluginVersionMap = PluginSchemaValidator.getPluginVersionMap();
            for (Map.Entry<String, String> entry : pluginVersionMap.entrySet()) {
                pluginInfoMap.putIfAbsent(entry.getKey(), new PluginInfo(entry.getKey(), entry.getValue()));
                addExtInfo(entry.getKey(), pluginInfoMap.get(entry.getKey()));
            }
            heartbeatMessage.updateHeartbeatVersion();
            nettyClient.sendInstantData(
                    JSONObject.toJSONString(heartbeatMessage).getBytes(CommonConstant.DEFAULT_CHARSET),
                    Message.ServiceData.DataType.HEARTBEAT_DATA);
            sleep();
        }
    }

    /**
     * 休眠
     */
    private void sleep() {
        try {
            final long interval = Math.max(ConfigManager.getConfig(HeartbeatConfig.class).getInterval(),
                    HeartbeatConstant.HEARTBEAT_MINIMAL_INTERVAL);
            Thread.sleep(interval);
        } catch (InterruptedException ignored) {
            LOGGER.warning("Unexpected interrupt heartbeat waiting. ");
        }
    }

    /**
     * 添加心跳额外信息
     *
     * @param pluginName 插件名称
     * @param pluginInfo 插件信息
     */
    private void addExtInfo(String pluginName, PluginInfo pluginInfo) {
        final ExtInfoProvider provider = EXT_INFO_MAP.get(pluginName);
        if (provider == null) {
            return;
        }
        final Map<String, String> extInfo = provider.getExtInfo();
        if (extInfo != null) {
            pluginInfo.setExtInfo(extInfo);
        }
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