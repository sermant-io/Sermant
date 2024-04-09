/*
 *   Copyright (C) 2023-2024 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.huaweicloud.sermant.tag.transmission.crossthread.declarers;

import com.huaweicloud.sermant.core.plugin.agent.declarer.AbstractPluginDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.utils.tag.TrafficUtils;
import com.huaweicloud.sermant.tag.transmission.config.CrossThreadConfig;
import com.huaweicloud.sermant.tag.transmission.crossthread.interceptors.ExecutorInterceptor;

/**
 * ExecutorDeclarer
 *
 * @author provenceee
 * @since 2023-04-20
 */
public class ExecutorDeclarer extends AbstractPluginDeclarer {
    private static final String ENHANCE_CLASS = "java.util.concurrent.Executor";

    private static final String[] METHOD_NAME = {"execute", "submit"};

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.isExtendedFrom(ENHANCE_CLASS);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
                InterceptDeclarer.build(MethodMatcher.nameContains(METHOD_NAME).and(MethodMatcher.isPublicMethod()),
                        new ExecutorInterceptor())
        };
    }

    @Override
    public boolean isEnabled() {
        CrossThreadConfig config = PluginConfigManager.getPluginConfig(CrossThreadConfig.class);
        if (config.isEnabledThread()) {
            TrafficUtils.setInheritableThreadLocal();
            return true;
        }
        return config.isEnabledThreadPool();
    }
}