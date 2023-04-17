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

package com.huawei.discovery.interceptors;

import com.huawei.discovery.entity.RegisterContext;
import com.huawei.discovery.entity.ServiceInstance.Status;
import com.huawei.discovery.utils.HostIpAddressUtils;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.Interceptor;
import com.huaweicloud.sermant.core.plugin.config.ServiceMeta;
import com.huaweicloud.sermant.core.utils.StringUtils;

import org.springframework.core.env.ConfigurableEnvironment;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 结束阶段设置服务相关信息
 *
 * @author chengyouling
 * @since 2022-10-09
 */
public class SpringEnvironmentInfoInterceptor implements Interceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final int DEFAULT_PORT = 8080;

    private static final String DEFAULT_SERVICE_NAME = "default-service";

    @Override
    public ExecuteContext before(ExecuteContext context) throws Exception {
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) throws Exception {
        Object result = context.getResult();
        String ipAddress = HostIpAddressUtils.getHostAddress();
        if (result instanceof ConfigurableEnvironment) {
            this.setClientInfo((ConfigurableEnvironment) result, ipAddress);
        }
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) throws Exception {
        return context;
    }

    private void setClientInfo(ConfigurableEnvironment environment, String ipAddress) {
        String address = environment.getProperty("server.address");
        String port = environment.getProperty("server.port");
        String serviceName = environment.getProperty("spring.application.name");
        RegisterContext.INSTANCE.getServiceInstance().setHost(StringUtils.isEmpty(address) ? ipAddress : address);
        RegisterContext.INSTANCE.getServiceInstance().setIp(StringUtils.isEmpty(address) ? ipAddress : address);

        // 避免重复初始化port
        int currentPort = RegisterContext.INSTANCE.getServiceInstance().getPort();
        if (currentPort == DEFAULT_PORT) {
            if (!StringUtils.isEmpty(port)) {
                try {
                    RegisterContext.INSTANCE.getServiceInstance().setPort(Integer.parseInt(port));
                } catch (NumberFormatException numberFormatException) {
                    LOGGER.severe("The port value in environment server.port is not format, port is: " + port);
                }
            }
        }

        // 避免重复初始化serviceName
        String currentServiceName = RegisterContext.INSTANCE.getServiceInstance().getServiceName();
        if (StringUtils.isEmpty(currentServiceName) || DEFAULT_SERVICE_NAME.equals(currentServiceName)) {
            if (StringUtils.isEmpty(serviceName)) {
                RegisterContext.INSTANCE.getServiceInstance().setServiceName(DEFAULT_SERVICE_NAME);
            } else {
                RegisterContext.INSTANCE.getServiceInstance().setServiceName(serviceName);
            }
        }
        RegisterContext.INSTANCE.getServiceInstance().setId(RegisterContext.INSTANCE.getServiceInstance().getIp()
                + ":" + RegisterContext.INSTANCE.getServiceInstance().getPort());
        RegisterContext.INSTANCE.getServiceInstance().setStatus(Status.UP.name());
        if (RegisterContext.INSTANCE.getServiceInstance().getMetadata() == null) {
            Map<String, String> metadata = new HashMap<String, String>();
            ServiceMeta serviceMeta = ConfigManager.getConfig(ServiceMeta.class);
            if (StringUtils.isExist(serviceMeta.getZone())) {
                metadata.put("zone", serviceMeta.getZone());
            }
            RegisterContext.INSTANCE.getServiceInstance().setMetadata(metadata);
        }
    }
}
