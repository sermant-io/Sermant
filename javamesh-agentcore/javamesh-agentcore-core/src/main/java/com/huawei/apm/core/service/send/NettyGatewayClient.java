/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.core.service.send;

import com.huawei.apm.bootstrap.boot.CoreService;
import com.huawei.apm.bootstrap.lubanops.config.AgentConfigManager;
import com.huawei.apm.bootstrap.lubanops.log.LogFactory;
import com.huawei.apm.bootstrap.service.send.GatewayClient;
import com.huawei.apm.core.ext.lubanops.transport.ClientManager;
import com.huawei.apm.core.ext.lubanops.transport.netty.client.NettyClient;
import com.huawei.apm.core.ext.lubanops.transport.netty.pojo.Message;

import java.util.logging.Logger;

/**
 * 基于Netty Client的网关发送服务
 */
public class NettyGatewayClient implements GatewayClient, CoreService {

    private static final Logger LOGGER = LogFactory.getLogger();

    private NettyClient nettyClient;

    @Override
    public void start() {
        nettyClient = ClientManager.getNettyClientFactory().getNettyClient(
            AgentConfigManager.getNettyServerIp(),
            Integer.parseInt(AgentConfigManager.getNettyServerPort()));
        nettyClient.doConnect();
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