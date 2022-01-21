/*
 * Copyright (C) 2020-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.flowcontrol.core.datasource;

import com.huawei.flowcontrol.common.config.FlowControlConfig;
import com.huawei.flowcontrol.common.util.StringUtils;
import com.huawei.flowcontrol.core.datasource.kie.rule.RuleCenter;
import com.huawei.sermant.core.plugin.config.PluginConfigManager;
import com.huawei.sermant.core.service.ServiceManager;
import com.huawei.sermant.core.service.dynamicconfig.DynamicConfigService;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigListener;
import com.huawei.sermant.core.service.dynamicconfig.utils.LabelGroupUtils;

import com.alibaba.csp.sentinel.util.AppNameUtil;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 流控规则初始化
 * <h3>同时兼容zk配置中心</h3>
 *
 * @author hanpeng
 * @since 2020-11-12
 */
public class DefaultDataSourceManager implements DataSourceManager {
    /**
     * 数据源map
     */
    private final Map<String, DefaultDataSource<?>> sourceMap = new ConcurrentHashMap<String, DefaultDataSource<?>>();

    /**
     * 规则中心
     */
    private final RuleCenter ruleCenter = new RuleCenter();

    public DefaultDataSourceManager() {
    }

    @Override
    public void start() {
        // 初始化数据源
        initDataSources();

        // 注册规则管理器
        registerRuleManager();

        // 初始化监听
        initConfigListener();
    }

    @Override
    public void stop() {
    }

    private void initDataSources() {
        for (String ruleType : ruleCenter.getRuleTypes()) {
            Class<?> ruleClass = ruleCenter.getRuleClass(ruleType);
            DefaultDataSource<?> defaultDataSource = getDataSource(ruleType, ruleClass);
            sourceMap.put(ruleType, defaultDataSource);
        }
    }

    private <T> DefaultDataSource<T> getDataSource(String ruleKey, Class<T> ruleClass) {
        return new DefaultDataSource<T>(ruleClass, ruleKey);
    }

    private void registerRuleManager() {
        for (Map.Entry<String, DefaultDataSource<?>> entry : sourceMap.entrySet()) {
            ruleCenter.registerRuleManager(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 初始化配置监听
     */
    private void initConfigListener() {
        final FlowControlConfig pluginConfig = PluginConfigManager.getPluginConfig(FlowControlConfig.class);
        String serviceName = AppNameUtil.getAppName();
        if (!StringUtils.isEmpty(pluginConfig.getConfigServiceName())) {
            serviceName = pluginConfig.getConfigServiceName();
        }
        final String groupLabel = LabelGroupUtils.createLabelGroup(Collections.singletonMap("service", serviceName));
        final DynamicConfigService service = ServiceManager.getService(DynamicConfigService.class);
        service.addGroupListener(groupLabel, new DynamicConfigListener() {
            @Override
            public void process(DynamicConfigEvent event) {
                for (DefaultDataSource<?> defaultDataSource : sourceMap.values()) {
                    defaultDataSource.update(event);
                }
            }
        }, true);
    }
}
