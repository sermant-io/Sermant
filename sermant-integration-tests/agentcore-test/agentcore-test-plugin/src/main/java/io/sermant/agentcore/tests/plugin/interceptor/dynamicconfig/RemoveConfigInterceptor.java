/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.agentcore.tests.plugin.interceptor.dynamicconfig;

import io.sermant.agentcore.tests.plugin.constants.TestConstants;
import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import io.sermant.core.service.ServiceManager;
import io.sermant.core.service.dynamicconfig.DynamicConfigService;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 配置移除拦截器
 *
 * @author tangle
 * @since 2023-08-30
 */
public class RemoveConfigInterceptor extends AbstractInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    DynamicConfigService dynamicConfigService = ServiceManager.getService(DynamicConfigService.class);

    @Override
    public ExecuteContext before(ExecuteContext context) {
        String key = (String) context.getArguments()[TestConstants.PARAM_INDEX_1];
        String group = (String) context.getArguments()[TestConstants.PARAM_INDEX_2];
        context.getArguments()[TestConstants.PARAM_INDEX_0] = dynamicConfigService.removeConfig(key, group);
        LOGGER.log(Level.INFO, "Test remove config, key:{0}, group:{1}, result:{2}",
                new String[]{key, group, String.valueOf(context.getArguments()[TestConstants.PARAM_INDEX_0])});
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }
}
