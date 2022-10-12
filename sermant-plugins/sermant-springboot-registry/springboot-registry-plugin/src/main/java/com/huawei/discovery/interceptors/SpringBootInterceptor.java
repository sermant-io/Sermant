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
import com.huawei.discovery.service.ConfigCenterService;
import com.huawei.discovery.service.RegistryService;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.service.ServiceManager;

/**
 * 结束阶段开始注册微服务
 *
 * @author chengyouling
 * @since 2022-10-10
 */
public class SpringBootInterceptor extends AbstractInterceptor {

    private final RegistryService registryService;

    private final ConfigCenterService configCenterService;

    public SpringBootInterceptor() {
        registryService = ServiceManager.getService(RegistryService.class);
        configCenterService = ServiceManager.getService(ConfigCenterService.class);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) throws Exception {
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        configCenterService.init(RegisterContext.INSTANCE.getServiceInstance().getServiceName());
        registryService.registry(RegisterContext.INSTANCE.getServiceInstance());
        return context;
    }
}
