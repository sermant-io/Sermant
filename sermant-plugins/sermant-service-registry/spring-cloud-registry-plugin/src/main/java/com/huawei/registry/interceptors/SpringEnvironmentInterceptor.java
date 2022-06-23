/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.registry.interceptors;

import com.huawei.registry.config.RegistryConfigSubscribeServiceImpl;
import com.huawei.registry.config.SpringRegistryConstants;
import com.huawei.registry.context.RegisterContext;
import com.huawei.registry.support.RegisterSwitchSupport;
import com.huawei.registry.utils.HostUtils;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;

import org.springframework.core.env.ConfigurableEnvironment;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 获取服务名称同时启动配置订阅
 *
 * @author zhouss
 * @since 2022-04-13
 */
public class SpringEnvironmentInterceptor extends RegisterSwitchSupport {
    /**
     * 标记为bootstrap.run, 该方法当前环境变量未初始化完成, 若有该标记则跳过
     */
    private static final String BOOTSTRAP_MARK_CLASS =
            "org.springframework.cloud.bootstrap.BootstrapImportSelectorConfiguration";

    private static final int DEFAULT_PORT = 8080;

    private final AtomicBoolean isStarted = new AtomicBoolean();

    @Override
    public ExecuteContext before(ExecuteContext context) {
        return context;
    }

    @Override
    public ExecuteContext doAfter(ExecuteContext context) {
        if (canSubscribe(context) && isStarted.compareAndSet(false, true)) {
            startSubscribe(context);
        }
        return context;
    }

    private boolean canSubscribe(ExecuteContext context) {
        return lowVersionCheck(context) || highVersionCheck(context);
    }

    private boolean highVersionCheck(ExecuteContext context) {
        final Object primarySources = context.getMemberFieldValue("primarySources");
        if (!(primarySources instanceof Set)) {
            return false;
        }
        Set<Class<?>> primaryClasses = (Set<Class<?>>) primarySources;
        for (Class<?> clazz : primaryClasses) {
            if (clazz.getName().equals(BOOTSTRAP_MARK_CLASS)) {
                return false;
            }
        }
        return true;
    }

    private boolean lowVersionCheck(ExecuteContext context) {
        final Object sources = context.getMemberFieldValue("sources");
        if (!(sources instanceof Set)) {
            return false;
        }

        // 仅当source为对应application时, 说明环境变量已完成初始化
        return ((Set<?>) sources).size() == 1;
    }

    private void startSubscribe(ExecuteContext context) {
        final Object configurableEnvironment = context.getResult();
        if (!(configurableEnvironment instanceof ConfigurableEnvironment)) {
            return;
        }
        fillClientInfo((ConfigurableEnvironment) configurableEnvironment);

        // 开始订阅配置
        PluginServiceManager.getPluginService(RegistryConfigSubscribeServiceImpl.class)
                .subscribeRegistryConfig(RegisterContext.INSTANCE.getClientInfo().getServiceName());
    }

    private void fillClientInfo(ConfigurableEnvironment environment) {
        RegisterContext.INSTANCE.getClientInfo().setServiceName(getServiceName(environment));
        RegisterContext.INSTANCE.getClientInfo().setPort(getPort(environment));
        RegisterContext.INSTANCE.getClientInfo().setMeta(new HashMap<>());
        RegisterContext.INSTANCE.getClientInfo().setHost(HostUtils.getHostName());
        RegisterContext.INSTANCE.getClientInfo().setIp(HostUtils.getMachineIp());
        RegisterContext.INSTANCE.getClientInfo().setZone(
                environment.getProperty(SpringRegistryConstants.SPRING_LOAD_BALANCER_ZONE));
    }

    private int getPort(ConfigurableEnvironment environment) {
        final String port = environment.getProperty("server.port");
        return port == null ? DEFAULT_PORT : Integer.parseInt(port);
    }

    private String getServiceName(ConfigurableEnvironment environment) {
        return environment.getProperty("spring.application.name",
                environment.getProperty("project.name", "application"));
    }

    @Override
    protected boolean isEnabled() {
        return true;
    }
}
