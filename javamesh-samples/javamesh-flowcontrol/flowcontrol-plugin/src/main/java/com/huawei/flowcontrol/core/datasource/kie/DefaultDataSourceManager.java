/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol.core.datasource.kie;

import com.alibaba.csp.sentinel.util.AppNameUtil;
import com.huawei.apm.core.plugin.config.PluginConfigManager;
import com.huawei.apm.core.service.ServiceManager;
import com.huawei.apm.core.service.dynamicconfig.kie.utils.LabelGroupUtils;
import com.huawei.apm.core.service.dynamicconfig.service.ConfigChangedEvent;
import com.huawei.apm.core.service.dynamicconfig.service.ConfigurationListener;
import com.huawei.apm.core.service.dynamicconfig.service.DynamicConfigurationFactoryService;
import com.huawei.flowcontrol.core.config.FlowControlConfig;
import com.huawei.flowcontrol.core.datasource.DataSourceManager;
import com.huawei.flowcontrol.core.datasource.kie.rule.RuleCenter;
import com.huawei.flowcontrol.util.StringUtils;

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
    private final Map<String, KieDataSource<?>> sourceMap = new ConcurrentHashMap<String, KieDataSource<?>>();

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
            KieDataSource<?> kieDataSource = getKieDataSource(ruleType, ruleClass);
            sourceMap.put(ruleType, kieDataSource);
        }
    }

    private <T> KieDataSource<T> getKieDataSource(String ruleKey, Class<T> ruleClass) {
        return new KieDataSource<T>(ruleClass, ruleKey);
    }

    private void registerRuleManager() {
        for (Map.Entry<String, KieDataSource<?>> entry : sourceMap.entrySet()) {
            ruleCenter.registerRuleManager(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 初始化配置监听
     */
    private void initConfigListener() {
        final FlowControlConfig pluginConfig = PluginConfigManager.getPluginConfig(FlowControlConfig.class);
        String serviceName = AppNameUtil.getAppName();
        if (!StringUtils.isEmpty(pluginConfig.getKieConfigServiceName())) {
            serviceName = pluginConfig.getKieConfigServiceName();
        }
        final String groupLabel = LabelGroupUtils.createLabelGroup(Collections.singletonMap("service", serviceName));
        final DynamicConfigurationFactoryService service = ServiceManager.getService(DynamicConfigurationFactoryService.class);
        service.getDynamicConfigurationService().addGroupListener(groupLabel, new ConfigurationListener() {
            @Override
            public void process(ConfigChangedEvent event) {
                for (KieDataSource<?> kieDataSource : sourceMap.values()) {
                    kieDataSource.update(event);
                }
            }
        });
    }
}
