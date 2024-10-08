/*
 * Copyright (C) 2024-2024 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.router.transmit.declarer;

import io.sermant.core.plugin.agent.declarer.AbstractPluginDeclarer;
import io.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import io.sermant.core.plugin.agent.matcher.ClassMatcher;
import io.sermant.core.plugin.agent.matcher.MethodMatcher;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.router.common.config.TransmitConfig;
import io.sermant.router.transmit.interceptor.ScheduledExecutorServiceInterceptor;

/**
 * SchedulingExecutorService interception point
 *
 * @author provenceee
 * @since 2024-01-16
 */
public class ScheduledExecutorServiceDeclarer extends AbstractPluginDeclarer {
    private static final String ENHANCE_CLASS = "java.util.concurrent.ScheduledExecutorService";

    private static final String[] METHOD_NAME = {"schedule", "scheduleAtFixedRate", "scheduleWithFixedDelay"};

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.isExtendedFrom(ENHANCE_CLASS);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
                InterceptDeclarer.build(MethodMatcher.nameContains(METHOD_NAME).and(MethodMatcher.isPublicMethod()),
                        new ScheduledExecutorServiceInterceptor())
        };
    }

    @Override
    public boolean isEnabled() {
        TransmitConfig pluginConfig = PluginConfigManager.getPluginConfig(TransmitConfig.class);
        return pluginConfig.isEnabledThread() || pluginConfig.isEnabledScheduler();
    }
}
