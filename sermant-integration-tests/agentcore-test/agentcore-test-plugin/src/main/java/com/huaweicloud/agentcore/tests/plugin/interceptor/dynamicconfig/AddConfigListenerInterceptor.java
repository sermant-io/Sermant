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

package com.huaweicloud.agentcore.tests.plugin.interceptor.dynamicconfig;

import com.huaweicloud.agentcore.tests.plugin.constants.TestConstants;
import com.huaweicloud.agentcore.tests.plugin.listener.TestListener;
import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.core.service.dynamicconfig.DynamicConfigService;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 添加单一配置监听拦截器
 *
 * @author tangle
 * @since 2023-08-30
 */
public class AddConfigListenerInterceptor extends AbstractInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    DynamicConfigService dynamicConfigService = ServiceManager.getService(DynamicConfigService.class);

    @Override
    public ExecuteContext before(ExecuteContext context) {
        String key = (String) context.getArguments()[TestConstants.PARAM_INDEX_1];
        String group = (String) context.getArguments()[TestConstants.PARAM_INDEX_2];
        context.getArguments()[TestConstants.PARAM_INDEX_0] = dynamicConfigService.doAddConfigListener(key, group,
                new TestListener());
        LOGGER.log(Level.INFO, "Test agentcore add config listener, key:{0}, group:{1}, result:{2}",
                new String[]{key, group, String.valueOf(context.getArguments()[TestConstants.PARAM_INDEX_0])});
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }
}
