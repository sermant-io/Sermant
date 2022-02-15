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

package com.huawei.flowcontrol.common.adapte.cse.rule.syncer;

import com.huawei.flowcontrol.common.config.FlowControlConfig;
import com.huawei.sermant.core.plugin.config.PluginConfigManager;
import com.huawei.sermant.core.service.ServiceManager;
import com.huawei.sermant.core.service.dynamicconfig.DynamicConfigService;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigListener;
import com.huawei.sermant.core.service.dynamicconfig.utils.LabelGroupUtils;

import java.util.Collections;

/**
 * 默认规则同步，非CSE场景
 *
 * @author zhouss
 * @since 2022-01-25
 */
public class DefaultRuleSyncer implements RuleSyncer {
    private final DynamicConfigListener listener = new RuleDynamicConfigListener();

    @Override
    public void start() {
        final String groupLabel = LabelGroupUtils.createLabelGroup(
            Collections.singletonMap("service", getServiceName()));
        final DynamicConfigService service = ServiceManager.getService(DynamicConfigService.class);
        service.addGroupListener(groupLabel, listener, true);
    }

    @Override
    public void stop() {
        final String groupLabel = LabelGroupUtils
            .createLabelGroup(Collections.singletonMap("service", getServiceName()));
        final DynamicConfigService service = ServiceManager.getService(DynamicConfigService.class);
        service.removeGroupListener(groupLabel);
    }

    private String getServiceName() {
        String serviceName = System.getProperty("project.name");
        if (serviceName == null) {
            serviceName = System.getenv("project.name");
        }
        if (serviceName != null) {
            return serviceName;
        }
        return PluginConfigManager.getPluginConfig(FlowControlConfig.class).getConfigServiceName();
    }
}
