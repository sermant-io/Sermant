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

package com.huaweicloud.sermant.core.service.send;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.service.send.api.GatewayClient;
import com.huaweicloud.sermant.core.service.send.config.BackendConfig;
import com.huaweicloud.sermant.core.service.send.netty.pojo.Message;

import java.util.logging.Logger;

/**
 * 基于Netty Client的网关发送服务
 *
 * @since 2022-03-26
 */
public class NettyGatewayClient implements GatewayClient {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private NettyClient nettyClient;

    @Override
    public void start() {
        BackendConfig backendConfig = ConfigManager.getConfig(BackendConfig.class);
        nettyClient = ClientManager.getNettyClientFactory().getNettyClient(backendConfig.getNettyIp(),
            backendConfig.getNettyPort());
    }

    @Override
    public void stop() {
        // close nettyClient
    }

    @Override
    public void send(byte[] data, int typeNum) {
        Message.ServiceData.DataType dataType = Message.ServiceData.DataType.forNumber(typeNum);
        if (dataType == null) {
            LOGGER.severe("Wrong type of data.");
            return;
        }
        nettyClient.sendData(data, dataType);
    }
}
