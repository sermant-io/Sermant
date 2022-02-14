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

package com.huawei.dubbo.register.interceptor;

import com.huawei.dubbo.register.service.RegistryService;
import com.huawei.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huawei.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huawei.sermant.core.service.ServiceManager;

/**
 * 增强SpringBootApplication类的main方法
 *
 * @author provenceee
 * @since 2022年1月24日
 */
public class SpringBootInterceptor extends AbstractInterceptor {
    private final RegistryService registryService;

    public SpringBootInterceptor() {
        registryService = ServiceManager.getService(RegistryService.class);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        registryService.startRegistration();
        return context;
    }
}