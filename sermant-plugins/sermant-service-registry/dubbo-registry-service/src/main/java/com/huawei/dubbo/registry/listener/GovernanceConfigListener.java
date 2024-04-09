/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huawei.dubbo.registry.listener;

import com.huawei.dubbo.registry.entity.GovernanceCache;
import com.huawei.dubbo.registry.entity.GovernanceData;
import com.huawei.dubbo.registry.service.RegistryService;

import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigListener;
import com.huaweicloud.sermant.core.utils.StringUtils;

import com.alibaba.fastjson.JSONObject;

/**
 * dubbo governance Listener
 *
 * @author provenceee
 * @since 2022-04-21
 */
public class GovernanceConfigListener implements DynamicConfigListener {
    private static final String GOVERNANCE_KEY = "dubbo.servicecomb.governance";

    private final RegistryService registryService;

    /**
     * Constructor
     */
    public GovernanceConfigListener() {
        registryService = PluginServiceManager.getPluginService(RegistryService.class);
    }

    @Override
    public void process(DynamicConfigEvent event) {
        if (!GOVERNANCE_KEY.equals(event.getKey())) {
            return;
        }
        GovernanceData governanceData = null;
        if (event.getEventType() != DynamicConfigEventType.DELETE) {
            String content = event.getContent();
            if (StringUtils.isBlank(content)) {
                return;
            }
            governanceData = JSONObject.parseObject(content, GovernanceData.class);
        }
        GovernanceCache.INSTANCE.setGovernanceData(governanceData);
        registryService.notifyGovernanceUrl();
    }
}