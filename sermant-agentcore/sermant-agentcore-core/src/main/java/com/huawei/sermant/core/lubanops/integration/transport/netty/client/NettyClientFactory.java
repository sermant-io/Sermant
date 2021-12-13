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

package com.huawei.sermant.core.lubanops.integration.transport.netty.client;

import java.util.HashMap;
import java.util.Map;

/**
 * netty客户端工厂
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-08-07
 */
public class NettyClientFactory {
    private static NettyClientFactory factory = new NettyClientFactory();

    private static Map<String, NettyClient> clientMap = new HashMap<>();

    public static NettyClientFactory getInstance() {
        return factory;
    }

    private static void refreshClientMap(String address, NettyClient client) {
        clientMap.put(address, client);
    }

    public synchronized NettyClient getNettyClient(String serverIp, int serverPort) {
        String address = serverIp + ":" + String.valueOf(serverPort);
        if (clientMap.containsKey(address)) {
            return clientMap.get(address);
        }

        NettyClient client = new NettyClient(serverIp, serverPort);
        refreshClientMap(address, client);
        return client;
    }
}
