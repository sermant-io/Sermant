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

import com.huawei.dynamic.config.DynamicContext;
import com.huawei.dynamic.config.entity.ClientMeta;
import com.huawei.dynamic.config.init.DynamicConfigInitializer;
import com.huawei.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huawei.sermant.core.service.ServiceManager;

import org.springframework.core.env.ConfigurableEnvironment;

/**
 * 获取服务名称同时启动配置订阅
 *
 * @author zhouss
 * @since 2022-04-13
 */
public class SpringEnvironmentInterceptor extends DynamicConfigSwitchSupport {
    @Override
    public ExecuteContext before(ExecuteContext context) {
        return context;
    }

    @Override
    public ExecuteContext doAfter(ExecuteContext context) {
        if (!DynamicContext.INSTANCE.isEnableBootstrap()) {
            return context;
        }
        final Object configurableEnvironment = context.getResult();
        if (!(configurableEnvironment instanceof ConfigurableEnvironment)) {
            return context;
        }
        ClientMeta.INSTANCE.setServiceName(getServiceName((ConfigurableEnvironment) configurableEnvironment));
        final DynamicConfigInitializer service = ServiceManager.getService(DynamicConfigInitializer.class);
        service.doStart();
        return context;
    }

    private String getServiceName(ConfigurableEnvironment environment) {
        return environment.getProperty("spring.application.name",
            environment.getProperty("project.name", "application"));
    }
}
