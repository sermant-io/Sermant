/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huaweicloud.visibility.service;

import com.huaweicloud.sermant.core.common.BootArgsIndexer;
import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.notification.NettyNotificationType;
import com.huaweicloud.sermant.core.notification.NotificationListener;
import com.huaweicloud.sermant.core.notification.NotificationManager;
import com.huaweicloud.sermant.core.plugin.config.ServiceMeta;
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.core.service.send.api.GatewayClient;
import com.huaweicloud.visibility.common.CollectorCache;
import com.huaweicloud.visibility.common.OperateType;
import com.huaweicloud.visibility.entity.ServerInfo;

import com.alibaba.fastjson.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Collection information processing service
 *
 * @author zhp
 * @since 2022-12-05
 */
public class CollectorServiceImpl implements CollectorService {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final int VISIBILITY_MESSAGE = 3;

    private final ServiceMeta serviceMeta = ConfigManager.getConfig(ServiceMeta.class);

    private GatewayClient client;

    private NotificationListener listener;

    @Override
    public void start() {
        try {
            this.client = ServiceManager.getService(GatewayClient.class);
        } catch (IllegalArgumentException e) {
            LOGGER.warning("Exception occurred while obtaining GatewayClient");
        }
        if (client != null) {
            listener = notificationInfo -> reconnectHandler();
            NotificationManager.registry(listener, NettyNotificationType.CONNECTED.getClass());
        } else {
            LOGGER.warning("GatewayClient is null and cannot register Netty connection listening.");
        }
    }

    @Override
    public void sendServerInfo(ServerInfo serverInfo) {
        if (client == null) {
            return;
        }
        sendMessage(OperateType.ADD.getType(), serverInfo);
    }

    @Override
    public void reconnectHandler() {
        if (client == null) {
            return;
        }
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setContractList(new ArrayList<>(CollectorCache.CONTRACT_MAP.values()));
        serverInfo.setConsanguinityList(new ArrayList<>(CollectorCache.CONSANGUINITY_MAP.values()));
        serverInfo.setRegistryInfo(CollectorCache.REGISTRY_MAP);
        sendMessage(OperateType.ADD.getType(), serverInfo);
    }

    /**
     * Send collection information
     *
     * @param operateType The type of operation
     * @param serverInfo Collect information
     */
    private void sendMessage(String operateType, ServerInfo serverInfo) {
        serverInfo.setApplicationName(BootArgsIndexer.getAppName());
        serverInfo.setGroupName(serviceMeta.getApplication());
        serverInfo.setVersion(serviceMeta.getVersion());
        serverInfo.setEnvironment(serviceMeta.getEnvironment());
        serverInfo.setZone(serviceMeta.getZone());
        serverInfo.setProject(serviceMeta.getProject());
        serverInfo.setOperateType(operateType);
        serverInfo.setInstanceId(BootArgsIndexer.getInstanceId());
        client.send(JSONObject.toJSONString(serverInfo).getBytes(StandardCharsets.UTF_8), VISIBILITY_MESSAGE);
    }

    @Override
    public void stop() {
        if (client == null) {
            return;
        }
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setContractList(new ArrayList<>());
        serverInfo.setConsanguinityList(new ArrayList<>());
        sendMessage(OperateType.DELETE.getType(), serverInfo);
        if (listener != null) {
            NotificationManager.unRegistry(listener, NettyNotificationType.CONNECTED.getClass());
        } else {
            LOGGER.warning("NotificationListener is null and cannot unregister Netty connection listening.");
        }
    }
}
