/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.huaweicloud.demo.tagtransmission.sofarpc.provider.server;

import com.huaweicloud.demo.tagtransmission.rpc.api.sofarpc.SofaRpcTagTransmissionService;
import com.huaweicloud.demo.tagtransmission.sofarpc.provider.serviceimpl.SofaRpcTagTransmissionServiceImpl;

import com.alipay.sofa.rpc.config.ProviderConfig;
import com.alipay.sofa.rpc.config.ServerConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * sofarpc 服务端启动
 *
 * @author daizhenyu
 * @since 2023-09-28
 **/
@Component
public class SofarpcServer implements CommandLineRunner {
    @Value("${sofarpc.server.port}")
    private int sofaRpcPort;

    @Autowired
    private SofaRpcTagTransmissionServiceImpl sofaRpcTagTransmissionService;

    @Override
    public void run(String[] args) {
        ServerConfig serverConfig = new ServerConfig()
                // 设置一个协议，默认bolt
                .setProtocol("bolt")
                // 设置一个端口
                .setPort(sofaRpcPort)
                // 非守护线程
                .setDaemon(false);

        ProviderConfig<SofaRpcTagTransmissionService> providerConfig =
                new ProviderConfig<SofaRpcTagTransmissionService>()
                        // 指定接口
                        .setInterfaceId(SofaRpcTagTransmissionService.class.getName())
                        // 指定实现
                        .setRef(sofaRpcTagTransmissionService)
                        // 指定服务端
                        .setServer(serverConfig);

        providerConfig.export();
    }
}
