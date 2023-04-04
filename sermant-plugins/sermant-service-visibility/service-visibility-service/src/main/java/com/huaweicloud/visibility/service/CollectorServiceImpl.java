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
import com.huaweicloud.sermant.core.plugin.config.ServiceMeta;
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.core.service.send.api.GatewayClient;
import com.huaweicloud.sermant.core.service.visibility.common.OperateType;
import com.huaweicloud.sermant.core.service.visibility.entity.ServerInfo;

import com.alibaba.fastjson.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/**
 * 采集信息处理服务
 *
 * @author zhp
 * @since 2022-12-05
 */
public class CollectorServiceImpl implements CollectorService {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final int VISIBILITY_MESSAGE = 3;

    private final ServiceMeta serviceMeta = ConfigManager.getConfig(ServiceMeta.class);

    private GatewayClient client;

    /**
     * 构造函数
     */
    public CollectorServiceImpl() {
        try {
            this.client = ServiceManager.getService(GatewayClient.class);
        } catch (IllegalArgumentException e) {
            LOGGER.warning("GatewayClient is not found");
        }
    }

    @Override
    public void sendServerInfo(ServerInfo serverInfo) {
        sendMessage(OperateType.ADD.getType(), serverInfo);
    }

    /**
     * 发送采集信息
     *
     * @param operateType 操作类型
     * @param serverInfo  采集信息
     */
    private void sendMessage(String operateType, ServerInfo serverInfo) {
        if (client != null) {
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
    }
}
