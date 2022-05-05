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

package com.huawei.flowcontrol.config;

import com.huawei.flowcontrol.common.adapte.cse.ResolverManager;
import com.huawei.flowcontrol.common.adapte.cse.constants.CseConstants;
import com.huawei.flowcontrol.common.adapte.cse.entity.CseServiceMeta;
import com.huawei.flowcontrol.common.config.ConfigConst;
import com.huawei.flowcontrol.common.config.FlowControlConfig;
import com.huawei.flowcontrol.common.util.StringUtils;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * apache dubbo配置拦截
 *
 * @author zhouss
 * @since 2022-01-28
 */
public class SpringEnvironmentInterceptor extends AbstractInterceptor {
    /**
     * 标记为bootstrap.run, 该方法当前环境变量未初始化完成, 若有该标记则跳过
     */
    private static final String BOOTSTRAP_MARK_CLASS =
        "org.springframework.cloud.bootstrap.BootstrapImportSelectorConfiguration";

    private final AtomicBoolean isLoadedRule = new AtomicBoolean();

    @Override
    public ExecuteContext before(ExecuteContext context) throws Exception {
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        final Object applicationContext = context.getArguments()[0];
        if (!(applicationContext instanceof ConfigurableApplicationContext) || !canSubscribe(context)) {
            return context;
        }
        Environment environment = ((ConfigurableApplicationContext) applicationContext).getEnvironment();
        loadRuleFromEnvironment(environment);
        if (CseServiceMeta.getInstance().isDubboService()) {
            return context;
        }
        final FlowControlConfig pluginConfig = PluginConfigManager.getPluginConfig(FlowControlConfig.class);
        if (pluginConfig.isUseCseRule() && pluginConfig.isBaseSdk()) {
            CseServiceMeta.getInstance().setProject(environment.getProperty(CseConstants.KEY_SPRING_KIE_PROJECT,
                CseConstants.DEFAULT_PROJECT));
            CseServiceMeta.getInstance().setServiceName(environment.getProperty(CseConstants.KEY_SPRING_SERVICE_NAME));
            CseServiceMeta.getInstance().setEnvironment(environment.getProperty(CseConstants.KEY_SPRING_ENVIRONMENT));
            CseServiceMeta.getInstance().setApp(environment.getProperty(CseConstants.KEY_SPRING_APP_NAME));
            CseServiceMeta.getInstance().setCustomLabel(environment.getProperty(CseConstants.KEY_SPRING_CUSTOM_LABEL,
                CseConstants.DEFAULT_CUSTOM_LABEL));
            CseServiceMeta.getInstance().setCustomLabelValue(environment.getProperty(
                CseConstants.KEY_SPRING_CUSTOM_LABEL_VALUE, CseConstants.DEFAULT_CUSTOM_LABEL_VALUE));
            CseServiceMeta.getInstance().setVersion(environment.getProperty(CseConstants.KEY_SPRING_VERSION));
        } else {
            String serviceName = environment.getProperty(ConfigConst.SPRING_APPLICATION_NAME);
            if (StringUtils.isEmpty(serviceName)) {
                serviceName = environment
                    .getProperty(ConfigConst.PROJECT_NAME, CseConstants.DEFAULT_DUBBO_SERVICE_NAME);
            }
            CseServiceMeta.getInstance().setServiceName(serviceName);
        }
        return context;
    }

    /**
     * 从spring环境变量读取流控策略
     *
     * @param environment 环境变量
     */
    private void loadRuleFromEnvironment(Environment environment) {
        if (!(environment instanceof ConfigurableEnvironment)) {
            return;
        }
        ConfigurableEnvironment configurableEnvironment = (ConfigurableEnvironment) environment;
        if (isLoadedRule.compareAndSet(false, true)) {
            for (PropertySource<?> next : getPropertySources(configurableEnvironment.getPropertySources())) {
                loadRuleFromSource((EnumerablePropertySource<?>) next);
            }
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
        propertySources.stream().filter(propertySource -> propertySource instanceof EnumerablePropertySource)
                .forEach(result::addFirst);
        return result;
    }

    private void loadRuleFromSource(EnumerablePropertySource<?> source) {
        final String[] propertyNames = source.getPropertyNames();
        for (String propertyName : propertyNames) {
            if (!ResolverManager.INSTANCE.isTarget(propertyName)) {
                continue;
            }
            ResolverManager.INSTANCE.resolve(propertyName, String.valueOf(source.getProperty(propertyName)), false);
        }
    }

    private boolean canSubscribe(ExecuteContext context) {
        final Object primarySources = context.getMemberFieldValue("primarySources");
        if (!(primarySources instanceof Set)) {
            return false;
        }
        Set<Class<?>> primaryClasses = (Set<Class<?>>) primarySources;
        for (Class<?> clazz : primaryClasses) {
            if (clazz.getName().equals(BOOTSTRAP_MARK_CLASS)) {
                return false;
            }
        }
        return true;
    }
}
