/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.javamesh.core.lubanops.integration.transport.netty.client;

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
