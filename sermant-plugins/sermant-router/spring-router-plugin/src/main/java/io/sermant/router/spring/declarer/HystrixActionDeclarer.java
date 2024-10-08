/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.router.spring.declarer;

import io.sermant.core.plugin.agent.matcher.ClassMatcher;
import io.sermant.core.plugin.agent.matcher.MethodMatcher;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.router.common.config.TransmitConfig;

import net.bytebuddy.matcher.ElementMatchers;

/**
 * HystrixContexSchedulerAction enhancement class and set the thread parameters
 *
 * @author provenceee
 * @since 2022-07-12
 */
public class HystrixActionDeclarer extends AbstractDeclarer {
    private static final String ENHANCE_CLASS = "com.netflix.hystrix.strategy.concurrency.HystrixContexSchedulerAction";

    private static final String INTERCEPT_CLASS
            = "io.sermant.router.spring.interceptor.HystrixActionInterceptor";

    private static final int ARGS_LENGTH = 2;

    /**
     * Constructor
     */
    public HystrixActionDeclarer() {
        super(ENHANCE_CLASS, INTERCEPT_CLASS, "");
    }

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameEquals(ENHANCE_CLASS);
    }

    @Override
    public MethodMatcher getMethodMatcher() {
        return MethodMatcher.isConstructor().and(ElementMatchers.takesArguments(ARGS_LENGTH));
    }

    @Override
    public boolean isEnabled() {
        // This is only required if thread pool asynchronous routing is not enabled
        return !PluginConfigManager.getPluginConfig(TransmitConfig.class).isEnabledThreadPool();
    }
}
