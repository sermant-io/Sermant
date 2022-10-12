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

import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.Interceptor;
import com.huaweicloud.sermant.core.plugin.config.ServiceMeta;
import com.huaweicloud.sermant.core.utils.StringUtils;

import org.springframework.core.env.ConfigurableEnvironment;

import java.util.HashMap;
import java.util.Map;

/**
 * 结束阶段设置服务相关信息
 *
 * @author chengyouling
 * @since 2022-10-09
 */
public class SpringEnvironmentInfoInterceptor implements Interceptor {

    private static final int DEFAULT_PORT = 8080;

    private static final String DEFAULT_APPLICATION_NAME = "default-application";

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
        String applicationName = environment.getProperty("spring.application.name");
        RegisterContext.INSTANCE.getServiceInstance().setHost(StringUtils.isEmpty(address) ? ipAddress : address);
        RegisterContext.INSTANCE.getServiceInstance().setIp(StringUtils.isEmpty(address) ? ipAddress : address);
        RegisterContext.INSTANCE.getServiceInstance()
                .setPort(StringUtils.isEmpty(port) ? DEFAULT_PORT : Integer.parseInt(port));
        RegisterContext.INSTANCE.getServiceInstance()
                .setServiceName(StringUtils.isEmpty(applicationName) ? DEFAULT_APPLICATION_NAME : applicationName);
        RegisterContext.INSTANCE.getServiceInstance().setId(RegisterContext.INSTANCE.getServiceInstance().getIp()
                + ":" + RegisterContext.INSTANCE.getServiceInstance().getPort());
        RegisterContext.INSTANCE.getServiceInstance().setStatus(Status.UP.name());
        if (null == RegisterContext.INSTANCE.getServiceInstance().getMetadata()) {
            Map<String, String> metadata = new HashMap<String, String>(8);
            ServiceMeta serviceMeta = ConfigManager.getConfig(ServiceMeta.class);
            metadata.put("zone", serviceMeta.getZone());
            RegisterContext.INSTANCE.getServiceInstance().setMetadata(metadata);
        }
    }
}
