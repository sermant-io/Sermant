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

import com.huawei.discovery.entity.DefaultServiceInstance;
import com.huawei.discovery.entity.RegisterContext;
import com.huawei.discovery.entity.ServiceInstance.Status;
import com.huawei.discovery.utils.HostIpAddressUtils;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.Interceptor;
import com.huaweicloud.sermant.core.plugin.config.ServiceMeta;
import com.huaweicloud.sermant.core.utils.StringUtils;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Set up service-related information at the end stage
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
        Object[] arguments = context.getArguments();
        if (arguments != null && arguments.length > 0) {
            Object argument = arguments[0];
            if (argument instanceof ConfigurableApplicationContext) {
                ConfigurableApplicationContext applicationContext = (ConfigurableApplicationContext) argument;

                // There may be multiple entries here, and when entering multiple times, the latter will take precedence
                // over the front, so just override the update
                this.setClientInfo(applicationContext.getEnvironment(), HostIpAddressUtils.getHostAddress());
            }
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
        DefaultServiceInstance instance = RegisterContext.INSTANCE.getServiceInstance();
        instance.setHost(StringUtils.isEmpty(address) ? ipAddress : address);
        instance.setIp(StringUtils.isEmpty(address) ? ipAddress : address);
        instance.setPort(getProperty(instance.getPort(), value -> value == 0, port, Integer::parseInt, DEFAULT_PORT));
        instance.setServiceName(getProperty(instance.getServiceName(), Objects::isNull, serviceName, value -> value,
                DEFAULT_SERVICE_NAME));
        instance.setId(instance.getIp() + ":" + instance.getPort());
        instance.setStatus(Status.UP.name());
        if (instance.getMetadata() == null) {
            Map<String, String> metadata = new HashMap<String, String>();
            ServiceMeta serviceMeta = ConfigManager.getConfig(ServiceMeta.class);
            if (StringUtils.isExist(serviceMeta.getZone())) {
                metadata.put("zone", serviceMeta.getZone());
            }
            LOGGER.log(Level.INFO, "Instance''s metadata is {0}.", metadata);
            instance.setMetadata(metadata);
        }
        LOGGER.log(Level.INFO, "Instance''s msg is {0}.", instance);
    }

    private <T> T getProperty(T currentProperty, Function<T, Boolean> judgmentMapper, String env,
            Function<String, T> envMapper, T defaultValue) {
        // environment.getProperty not empty and overwritten
        if (!StringUtils.isBlank(env)) {
            T property = envMapper.apply(env);
            LOGGER.log(Level.INFO, "Env is not null, current property is {0}, will return {1}.",
                    new Object[]{currentProperty, property});
            return property;
        }

        // environment.getPropertyis is empty, and the current value is null, the default value is stored
        if (judgmentMapper.apply(currentProperty)) {
            LOGGER.log(Level.INFO, "Env is null, current property is invalid, will return {0}.", defaultValue);
            return defaultValue;
        }

        // environment.getPropertyis is empty and the current value exists, returns the current value
        LOGGER.log(Level.INFO, "Env is null, current property is valid, will return {0}.", currentProperty);
        return currentProperty;
    }
}
