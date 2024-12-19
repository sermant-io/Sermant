/*
 * Copyright (C) 2021-2024 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.implement.service.heartbeat;

import com.alibaba.fastjson.JSONObject;

import io.sermant.core.common.CommonConstant;
import io.sermant.core.common.LoggerFactory;
import io.sermant.core.config.ConfigManager;
import io.sermant.core.ext.ExternalAgentManager;
import io.sermant.core.plugin.common.PluginConstant;
import io.sermant.core.plugin.common.PluginSchemaValidator;
import io.sermant.core.service.heartbeat.api.ExtInfoProvider;
import io.sermant.core.service.heartbeat.api.HeartbeatService;
import io.sermant.core.service.heartbeat.common.ExternalAgentInfo;
import io.sermant.core.service.heartbeat.common.HeartbeatConstant;
import io.sermant.core.service.heartbeat.common.HeartbeatMessage;
import io.sermant.core.service.heartbeat.common.PluginInfo;
import io.sermant.core.service.heartbeat.config.HeartbeatConfig;
import io.sermant.core.utils.JarFileUtils;
import io.sermant.core.utils.ThreadFactoryUtils;
import io.sermant.implement.service.send.netty.NettyClient;
import io.sermant.implement.service.send.netty.NettyClientFactory;
import io.sermant.implement.service.send.netty.pojo.Message;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * Implementation class of {@link HeartbeatService}
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-10-25
 */
public class HeartbeatServiceImpl implements HeartbeatService {
    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * Heartbeat extra parameter
     */
    private static final Map<String, ExtInfoProvider> EXT_INFO_MAP = new ConcurrentHashMap<>();

    /**
     * Heartbeat timing task
     */
    private ScheduledExecutorService executorService;

    /**
     * NettyClient
     */
    private NettyClient nettyClient;

    // Initialize the heartbeat data message
    private final HeartbeatMessage heartbeatMessage = new HeartbeatMessage();

    @Override
    public void start() {
        executorService =
                Executors.newScheduledThreadPool(1, new ThreadFactoryUtils("heartbeat-task"));
        nettyClient = NettyClientFactory.getInstance().getDefaultNettyClient();
        executorService.scheduleAtFixedRate(this::execute, 0,
                Math.max(ConfigManager.getConfig(HeartbeatConfig.class).getInterval(),
                        HeartbeatConstant.HEARTBEAT_MINIMAL_INTERVAL),
                TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        if (executorService != null) {
            executorService.shutdownNow();
        }
        NettyClientFactory.stop();
        EXT_INFO_MAP.clear();
    }

    /**
     * execute task
     */
    private void execute() {
        // Gets the plugin name and version Map
        final Map<String, String> pluginVersionMap = PluginSchemaValidator.getPluginVersionMap();

        // Clear the plugin information cache
        heartbeatMessage.getPluginInfoMap().clear();
        for (Map.Entry<String, String> entry : pluginVersionMap.entrySet()) {
            heartbeatMessage.getPluginInfoMap().putIfAbsent(entry.getKey(),
                    new PluginInfo(entry.getKey(), entry.getValue()));
            addExtInfo(entry.getKey(), heartbeatMessage.getPluginInfoMap().get(entry.getKey()));
        }

        // add external agent installation information
        for (String agentName : ExternalAgentManager.getExternalAgentInstallationStatus().keySet()) {
            if (ExternalAgentManager.getInstallationStatus(agentName)) {
                heartbeatMessage.getExternalAgentInfoMap().putIfAbsent(agentName,
                        new ExternalAgentInfo(agentName, ExternalAgentManager.getAgentVersion(agentName)));
            }
        }

        heartbeatMessage.updateHeartbeatVersion();
        if (nettyClient == null) {
            LOGGER.warning("Netty client is null when send heartbeat message.");
            return;
        }
        nettyClient.sendInstantData(JSONObject.toJSONString(heartbeatMessage).getBytes(CommonConstant.DEFAULT_CHARSET),
                Message.ServiceData.DataType.HEARTBEAT_DATA);
    }

    /**
     * Add additional heartbeat information
     *
     * @param pluginName plugin name
     * @param pluginInfo plugin information
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
