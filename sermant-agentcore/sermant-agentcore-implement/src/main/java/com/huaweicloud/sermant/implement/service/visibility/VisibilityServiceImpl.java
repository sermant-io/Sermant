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

package com.huaweicloud.sermant.implement.service.visibility;

import com.huaweicloud.sermant.core.common.BootArgsIndexer;
import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.plugin.config.ServiceMeta;
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.core.service.send.api.GatewayClient;
import com.huaweicloud.sermant.core.service.visibility.api.VisibilityService;
import com.huaweicloud.sermant.core.service.visibility.common.CollectorCache;
import com.huaweicloud.sermant.core.service.visibility.common.OperateType;
import com.huaweicloud.sermant.core.service.visibility.config.VisibilityServiceConfig;
import com.huaweicloud.sermant.core.service.visibility.entity.ServerInfo;
import com.huaweicloud.sermant.implement.service.send.netty.pojo.Message;

import com.alibaba.fastjson.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * 服务可见性服务
 *
 * @author zhp
 * @since 2022-12-05
 */
public class VisibilityServiceImpl implements VisibilityService {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final VisibilityServiceConfig config = ConfigManager.getConfig(VisibilityServiceConfig.class);

    private final ServiceMeta serviceMeta = ConfigManager.getConfig(ServiceMeta.class);

    private GatewayClient client;

    @Override
    public void reconnectHandler() {
        if (!config.isEnableStart()) {
            return;
        }
        initClient();
        if (client != null) {
            ServerInfo serverInfo = new ServerInfo();
            serverInfo.setContractList(new ArrayList<>(CollectorCache.CONTRACT_MAP.values()));
            serverInfo.setConsanguinityList(new ArrayList<>(CollectorCache.CONSANGUINITY_MAP.values()));
            serverInfo.setRegistryInfo(CollectorCache.REGISTRY_MAP);
            fillBaseInfo(serverInfo);
            serverInfo.setOperateType(OperateType.ADD.getType());
            serverInfo.setInstanceId(BootArgsIndexer.getInstanceId());
            client.send(JSONObject.toJSONString(serverInfo).getBytes(StandardCharsets.UTF_8),
                    Message.ServiceData.DataType.VISIBILITY_DATA_VALUE);
        }
    }

    @Override
    public void stop() {
        if (!config.isEnableStart()) {
            return;
        }
        initClient();
        if (client != null) {
            ServerInfo serverInfo = new ServerInfo();
            serverInfo.setContractList(new ArrayList<>());
            serverInfo.setConsanguinityList(new ArrayList<>());
            fillBaseInfo(serverInfo);
            serverInfo.setOperateType(OperateType.DELETE.getType());
            serverInfo.setInstanceId(BootArgsIndexer.getInstanceId());
            client.send(JSONObject.toJSONString(serverInfo).getBytes(StandardCharsets.UTF_8),
                    Message.ServiceData.DataType.VISIBILITY_DATA_VALUE);
        }
    }

    /**
     * 填充基本信息
     *
     * @param serverInfo 服务信息
     */
    private void fillBaseInfo(ServerInfo serverInfo) {
        serverInfo.setApplicationName(BootArgsIndexer.getAppName());
        serverInfo.setGroupName(serviceMeta.getApplication());
        serverInfo.setVersion(serviceMeta.getVersion());
        serverInfo.setEnvironment(serviceMeta.getEnvironment());
        serverInfo.setZone(serviceMeta.getZone());
        serverInfo.setProject(serviceMeta.getProject());
    }

    /**
     * 初始化client
     */
    private void initClient() {
        if (client == null) {
            try {
                this.client = ServiceManager.getService(GatewayClient.class);
            } catch (IllegalArgumentException e) {
                LOGGER.warning("GatewayClient is not found");
            }
        }
    }
}
