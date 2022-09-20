/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huaweicloud.loadbalancer.inject;

import com.huaweicloud.loadbalancer.config.LbContext;
import com.huaweicloud.loadbalancer.rule.LoadbalancerRuleResolver;
import com.huaweicloud.loadbalancer.rule.RuleManager;
import com.huaweicloud.loadbalancer.service.LoadbalancerConfigServiceImpl;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;

/**
 * 负载均衡配置类
 *
 * @author zhouss
 * @since 2022-08-04
 */
@Component
public class LoadbalancerProperties {
    @Autowired
    private Environment environment;

    /**
     * 从spring获取服务名
     *
     * @param serviceName 服务名
     */
    public LoadbalancerProperties(@Value("${dubbo.application.name:${spring.application.name:application}}")
            String serviceName) {
        LbContext.INSTANCE.setServiceName(serviceName);
        final LoadbalancerConfigServiceImpl pluginService = PluginServiceManager
                .getPluginService(LoadbalancerConfigServiceImpl.class);
        pluginService.subscribe();
    }

    /**
     * 基于配置文件加载符合要求的负载均衡规则
     */
    @PostConstruct
    public void loadLbRule() {
        loadRuleFromEnvironment();
    }

    /**
     * 从spring环境变量读取流控策略
     */
    private void loadRuleFromEnvironment() {
        if (!(environment instanceof ConfigurableEnvironment)) {
            return;
        }
        ConfigurableEnvironment configurableEnvironment = (ConfigurableEnvironment) environment;
        for (PropertySource<?> next : getPropertySources(configurableEnvironment.getPropertySources())) {
            loadRuleFromSource((EnumerablePropertySource<?>) next);
        }
    }

    /**
     * 这里对配置源倒序排序，保证获取的配置优先级与spring一致
     *
     * @param propertySources 配置源
     * @return 倒序后的配置源
     */
    private List<PropertySource<?>> getPropertySources(MutablePropertySources propertySources) {
        final LinkedList<PropertySource<?>> result = new LinkedList<>();
        for (PropertySource<?> next : propertySources) {
            if (next instanceof EnumerablePropertySource) {
                result.addFirst(next);
            }
        }
        return result;
    }

    private void loadRuleFromSource(EnumerablePropertySource<?> source) {
        final String[] propertyNames = source.getPropertyNames();
        for (String propertyName : propertyNames) {
            if (!isLbConfig(propertyName)) {
                continue;
            }
            final DynamicConfigEvent event = DynamicConfigEvent
                    .createEvent(propertyName, null, String.valueOf(source.getProperty(propertyName)));
            RuleManager.INSTANCE.resolve(event);
        }
    }

    private boolean isLbConfig(String propertyName) {
        if (propertyName == null) {
            return false;
        }
        return propertyName.startsWith(LoadbalancerRuleResolver.LOAD_BALANCER_PREFIX)
                || propertyName.startsWith(LoadbalancerRuleResolver.MATCH_GROUP_PREFIX);
    }
}
