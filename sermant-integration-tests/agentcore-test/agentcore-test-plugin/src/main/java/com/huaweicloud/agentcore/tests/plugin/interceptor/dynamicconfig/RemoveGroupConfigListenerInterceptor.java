/*
 * Copyright (C) 2023-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.agentcore.tests.plugin.interceptor.dynamicconfig;

import com.huaweicloud.agentcore.tests.plugin.constants.TestConstants;
import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.core.service.dynamicconfig.DynamicConfigService;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 移除组配置监听拦截器
 *
 * @author tangle
 * @since 2023-08-30
 */
public class RemoveGroupConfigListenerInterceptor extends AbstractInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    DynamicConfigService dynamicConfigService = ServiceManager.getService(DynamicConfigService.class);

    @Override
    public ExecuteContext before(ExecuteContext context) {
        String group = (String) context.getArguments()[TestConstants.PARAM_INDEX_1];
        context.getArguments()[TestConstants.PARAM_INDEX_0] = dynamicConfigService.doRemoveGroupListener(group);
        LOGGER.log(Level.INFO, "Test remove group config listener, group:{0}, result:{1}",
                new String[]{group, String.valueOf(context.getArguments()[TestConstants.PARAM_INDEX_0])});
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }
}
