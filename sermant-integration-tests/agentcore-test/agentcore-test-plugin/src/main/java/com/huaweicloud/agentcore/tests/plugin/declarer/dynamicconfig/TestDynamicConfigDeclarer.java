/*
 * Copyright (C) 2023-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.agentcore.tests.plugin.declarer.dynamicconfig;

import com.huaweicloud.agentcore.tests.plugin.interceptor.dynamicconfig.AddConfigListenerInterceptor;
import com.huaweicloud.agentcore.tests.plugin.interceptor.dynamicconfig.AddGroupConfigListenerInterceptor;
import com.huaweicloud.agentcore.tests.plugin.interceptor.dynamicconfig.GetConfigInterceptor;
import com.huaweicloud.agentcore.tests.plugin.interceptor.dynamicconfig.PublishConfigInterceptor;
import com.huaweicloud.agentcore.tests.plugin.interceptor.dynamicconfig.RemoveConfigInterceptor;
import com.huaweicloud.agentcore.tests.plugin.interceptor.dynamicconfig.RemoveConfigListenerInterceptor;
import com.huaweicloud.agentcore.tests.plugin.interceptor.dynamicconfig.RemoveGroupConfigListenerInterceptor;
import com.huaweicloud.sermant.core.plugin.agent.declarer.AbstractPluginDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;

/**
 * 测试动态配置核心
 *
 * @author tangle
 * @since 2023-09-07
 */
public class TestDynamicConfigDeclarer extends AbstractPluginDeclarer {
    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameEquals(
                "com.huaweicloud.agentcore.test.application.tests.dynamicconfig.DynamicConfigTest");
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
                InterceptDeclarer.build(MethodMatcher.nameEquals("publishConfig"), new PublishConfigInterceptor()),
                InterceptDeclarer.build(MethodMatcher.nameEquals("getConfig"), new GetConfigInterceptor()),
                InterceptDeclarer.build(MethodMatcher.nameEquals("removeConfig"), new RemoveConfigInterceptor()),
                InterceptDeclarer.build(MethodMatcher.nameEquals("addConfigListener"),
                        new AddConfigListenerInterceptor()),
                InterceptDeclarer.build(MethodMatcher.nameEquals("removeConfigListener"),
                        new RemoveConfigListenerInterceptor()),
                InterceptDeclarer.build(MethodMatcher.nameEquals("addGroupConfigListener"),
                        new AddGroupConfigListenerInterceptor()),
                InterceptDeclarer.build(MethodMatcher.nameEquals("removeGroupConfigListener"),
                        new RemoveGroupConfigListenerInterceptor())};
    }
}
