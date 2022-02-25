/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

import com.huawei.flowcontrol.common.adapte.cse.entity.CseServiceMeta;
import com.huawei.flowcontrol.common.config.FlowControlConfig;
import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.config.ConfigManager;
import com.huawei.sermant.core.plugin.config.PluginConfigManager;
import com.huawei.sermant.core.service.ServiceManager;
import com.huawei.sermant.core.service.dynamicconfig.DynamicConfigService;
import com.huawei.sermant.core.service.dynamicconfig.kie.KieDynamicConfigService;
import com.huawei.sermant.core.service.dynamicconfig.utils.LabelGroupUtils;
import com.huawei.sermant.core.service.meta.ServiceMeta;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * kie同步
 *
 * @author zhouss
 * @since 2021-11-18
 */
public class CseRuleSyncer implements RuleSyncer {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 检测时间
     */
    private static final int WAIT_INTERVAL_MS = 10 * 1000;

    /**
     * 最大等待5分钟
     */
    private static final int MAX_WAIT_INTERVAL_MS = 60 * 5 * 1000;

    private final Map<String, RuleDynamicConfigListener> listenerCache = new LinkedHashMap<>();

    private DynamicConfigService dynamicConfigurationFactoryService;

    /**
     * 初始化通知
     */
    @Override
    public void start() {
        initDynamicConfigService();

        // 此块只会适配KIE配置中心
        long waitTimeMs = 0L;
        while (!CseServiceMeta.getInstance().isReady()) {
            try {
                Thread.sleep(WAIT_INTERVAL_MS);
                waitTimeMs += WAIT_INTERVAL_MS;
                if (waitTimeMs >= MAX_WAIT_INTERVAL_MS) {
                    // 大于最大等待时间，放弃同步
                    LOGGER.warning("Can not acquire required service meta, "
                            + "it won't read configuration from config center!");
                    return;
                }
            } catch (InterruptedException ignored) {
                // ignored
            }
        }
        initRequests();
    }

    private void initDynamicConfigService() {
        final FlowControlConfig pluginConfig = PluginConfigManager.getPluginConfig(FlowControlConfig.class);

        // 根据使用需求选择是否使用自身配置中心
        if (pluginConfig.isUseAgentConfigCenter()) {
            dynamicConfigurationFactoryService = ServiceManager.getService(DynamicConfigService.class);
        } else {
            dynamicConfigurationFactoryService = new KieDynamicConfigService(pluginConfig.getConfigKieAddress(),
                    pluginConfig.getProject());
        }
        fillServiceMeta(pluginConfig);
    }

    private void fillServiceMeta(FlowControlConfig pluginConfig) {
        final ServiceMeta serviceMeta = ConfigManager.getConfig(ServiceMeta.class);
        CseServiceMeta.getInstance().setVersion(serviceMeta.getVersion());
        CseServiceMeta.getInstance().setEnvironment(serviceMeta.getEnvironment());
        CseServiceMeta.getInstance().setApp(serviceMeta.getApplication());
        CseServiceMeta.getInstance().setProject(serviceMeta.getProject());
        CseServiceMeta.getInstance().setCustomLabelValue(pluginConfig.getCustomLabelValue());
        CseServiceMeta.getInstance().setCustomLabel(pluginConfig.getCustomLabel());
    }

    public void initRequests() {
        buildAppRequest();
        buildServiceRequest();
        buildCustomRequest();
        for (Map.Entry<String, RuleDynamicConfigListener> entry : listenerCache.entrySet()) {
            dynamicConfigurationFactoryService.addGroupListener(entry.getKey(), entry.getValue(), true);
        }
    }

    /**
     * 停止方法
     */
    @Override
    public void stop() {
        for (Map.Entry<String, RuleDynamicConfigListener> entry : listenerCache.entrySet()) {
            dynamicConfigurationFactoryService.removeGroupListener(entry.getKey());
        }
    }

    private void buildServiceRequest() {
        final HashMap<String, String> map = new HashMap<>();
        map.put("app", CseServiceMeta.getInstance().getApp());
        map.put("service", CseServiceMeta.getInstance().getServiceName());
        map.put("environment", CseServiceMeta.getInstance().getEnvironment());
        listenerCache.put(LabelGroupUtils.createLabelGroup(map), new RuleDynamicConfigListener());
    }

    private void buildAppRequest() {
        final HashMap<String, String> map = new HashMap<>();
        map.put("app", CseServiceMeta.getInstance().getApp());
        map.put("environment", CseServiceMeta.getInstance().getEnvironment());
        listenerCache.put(LabelGroupUtils.createLabelGroup(map), new RuleDynamicConfigListener());
    }

    private void buildCustomRequest() {
        final HashMap<String, String> map = new HashMap<>();
        map.put(CseServiceMeta.getInstance().getCustomLabel(), CseServiceMeta.getInstance().getCustomLabelValue());
        listenerCache.put(LabelGroupUtils.createLabelGroup(map), new RuleDynamicConfigListener());
    }
}
