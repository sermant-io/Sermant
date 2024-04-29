/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.spring.beans.deal.declarers;

import io.sermant.core.plugin.agent.declarer.AbstractPluginDeclarer;
import io.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import io.sermant.core.plugin.agent.matcher.ClassMatcher;
import io.sermant.core.plugin.agent.matcher.MethodMatcher;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.spring.beans.deal.config.SpringBeansDealConfig;
import io.sermant.spring.beans.deal.interceptors.SpringComponentBeanInterceptor;

/**
 * Component assembly bean interception definition
 *
 * @author chengyouling
 * @since 2023-03-27
 */
public class SpringComponentBeanDeclarer extends AbstractPluginDeclarer {
    private static final String ENHANCE_CLASS = "org.springframework.context.annotation.ClassPathBeanDefinitionScanner";

    private static final String INTERCEPTOR_CLASS = SpringComponentBeanInterceptor.class.getCanonicalName();

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameEquals(ENHANCE_CLASS);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[] {
                InterceptDeclarer.build(MethodMatcher.nameEquals("doScan"), INTERCEPTOR_CLASS)
        };
    }

    @Override
    public boolean isEnabled() {
        SpringBeansDealConfig config = PluginConfigManager.getPluginConfig(SpringBeansDealConfig.class);
        return config.isEnabled();
    }
}
