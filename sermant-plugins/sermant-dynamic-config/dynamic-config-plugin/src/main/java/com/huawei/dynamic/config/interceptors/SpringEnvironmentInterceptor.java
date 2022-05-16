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

package com.huawei.dynamic.config.interceptors;

import com.huawei.dynamic.config.entity.ClientMeta;
import com.huawei.dynamic.config.init.DynamicConfigInitializer;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.service.ServiceManager;

import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Set;

/**
 * 获取服务名称同时启动配置订阅
 *
 * @author zhouss
 * @since 2022-04-13
 */
public class SpringEnvironmentInterceptor extends DynamicConfigSwitchSupport {
    /**
     * 标记为bootstrap.run, 该方法当前环境变量未初始化完成, 若有该标记则跳过
     */
    private static final String BOOTSTRAP_MARK_CLASS =
        "org.springframework.cloud.bootstrap.BootstrapImportSelectorConfiguration";

    @Override
    public ExecuteContext before(ExecuteContext context) {
        return context;
    }

    @Override
    public ExecuteContext doAfter(ExecuteContext context) {
        if (canSubscribe(context)) {
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
        ClientMeta.INSTANCE.setServiceName(getServiceName((ConfigurableEnvironment) configurableEnvironment));
        final DynamicConfigInitializer service = ServiceManager.getService(DynamicConfigInitializer.class);
        service.doStart();
    }

    private String getServiceName(ConfigurableEnvironment environment) {
        return environment.getProperty("spring.application.name",
            environment.getProperty("project.name", "application"));
    }
}
