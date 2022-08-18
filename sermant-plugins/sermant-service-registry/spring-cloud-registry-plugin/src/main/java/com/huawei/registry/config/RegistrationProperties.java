/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.registry.config;

import com.huawei.registry.context.RegisterContext;
import com.huawei.registry.utils.HostUtils;

import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtils.HostInfo;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

import java.util.HashMap;

import javax.annotation.PostConstruct;

/**
 * 注册信息
 *
 * @author zhouss
 * @since 2022-06-28
 */
@Order(value = Ordered.HIGHEST_PRECEDENCE)
public class RegistrationProperties {
    @Value("${dubbo.application.name:${spring.application.name:application}}")
    private String serviceName;

    @Value("${server.port}")
    private int port;

    @Autowired
    private Environment environment;

    @Autowired
    private InetUtils inetUtils;

    /**
     * 初始化
     */
    @PostConstruct
    public void init() {
        RegisterContext.INSTANCE.getClientInfo().setServiceName(serviceName);
        RegisterContext.INSTANCE.getClientInfo().setServiceId(serviceName);
        RegisterContext.INSTANCE.getClientInfo().setPort(port);
        RegisterContext.INSTANCE.getClientInfo().setMeta(new HashMap<>());
        final HostInfo hostInfo = inetUtils.findFirstNonLoopbackHostInfo();
        RegisterContext.INSTANCE.getClientInfo().setHost(hostInfo.getHostname());
        RegisterContext.INSTANCE.getClientInfo().setIp(hostInfo.getIpAddress());
        RegisterContext.INSTANCE.getClientInfo().setZone(
                environment.getProperty(SpringRegistryConstants.SPRING_LOAD_BALANCER_ZONE));

        // 开始订阅配置
        PluginServiceManager.getPluginService(RegistryConfigSubscribeServiceImpl.class)
                .subscribeRegistryConfig(RegisterContext.INSTANCE.getClientInfo().getServiceName());
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
