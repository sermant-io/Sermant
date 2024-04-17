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

package com.huaweicloud.sermant.implement.service.send.netty;

import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.service.send.config.GatewayConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Netty client factory
 *
 * @author lilai
 * @version 0.0.1
 * @since 2022-03-26
 */
public class NettyClientFactory {
    private static final NettyClientFactory FACTORY = new NettyClientFactory();

    private static final Map<String, NettyClient> CLIENT_MAP = new HashMap<>();

    /**
     * Get the netty client factory singleton
     *
     * @return singleton
     */
    public static NettyClientFactory getInstance() {
        return FACTORY;
    }

    private static void refreshClientMap(String address, NettyClient client) {
        CLIENT_MAP.put(address, client);
    }

    /**
     * Get default netty client with default port and IP through the core configuration
     *
     * @return NettyClient
     */
    public synchronized NettyClient getDefaultNettyClient() {
        GatewayConfig gatewayConfig = ConfigManager.getConfig(GatewayConfig.class);
        return getNettyClient(gatewayConfig.getNettyIp(), gatewayConfig.getNettyPort());
    }

    /**
     * getNettyClient
     *
     * @param serverIp serverIp
     * @param serverPort serverPort
     * @return NettyClient
     */
    public synchronized NettyClient getNettyClient(String serverIp, int serverPort) {
        String address = serverIp + ":" + serverPort;
        if (CLIENT_MAP.containsKey(address)) {
            return CLIENT_MAP.get(address);
        }

        NettyClient client = new NettyClient(serverIp, serverPort);
        refreshClientMap(address, client);
        return client;
    }

    /**
     * Close the factory and clear the client
     */
    public static void stop() {
        for (Map.Entry<String, NettyClient> entry : CLIENT_MAP.entrySet()) {
            entry.getValue().stop();
        }
        CLIENT_MAP.clear();
    }
}
