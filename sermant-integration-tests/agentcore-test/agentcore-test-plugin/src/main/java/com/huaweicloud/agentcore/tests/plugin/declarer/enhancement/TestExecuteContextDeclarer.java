/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.agentcore.tests.plugin.declarer.enhancement;

import com.huaweicloud.sermant.core.plugin.agent.declarer.AbstractPluginDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.Interceptor;
import com.huaweicloud.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;

/**
 * 测试ExecuteContext增强能力
 *
 * @author luanwenfei
 * @since 2023-09-07
 */
public class TestExecuteContextDeclarer extends AbstractPluginDeclarer {
    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameEquals("com.huaweicloud.agentcore.test.application.tests.enhancement.EnhancementTest");
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
                InterceptDeclarer.build(MethodMatcher.nameEquals("testSkipFunction"), new Interceptor() {
                    @Override
                    public ExecuteContext before(ExecuteContext context) {
                        context.skip(true);
                        return context;
                    }

                    @Override
                    public ExecuteContext after(ExecuteContext context) {
                        return context;
                    }

                    @Override
                    public ExecuteContext onThrow(ExecuteContext context) {
                        return context;
                    }
                }),
                InterceptDeclarer.build(MethodMatcher.nameEquals("testSetFiledFunction"), new Interceptor() {
                    @Override
                    public ExecuteContext before(ExecuteContext context) {
                        context.setStaticFieldValue("staticField", "staticFieldSetBySermant");
                        context.setMemberFieldValue("memberField", "memberFieldSetBySermant");
                        return context;
                    }

                    @Override
                    public ExecuteContext after(ExecuteContext context) {
                        return context;
                    }

                    @Override
                    public ExecuteContext onThrow(ExecuteContext context) {
                        return context;
                    }
                }),
                InterceptDeclarer.build(MethodMatcher.nameEquals("testSetArguments"), new Interceptor() {
                    @Override
                    public ExecuteContext before(ExecuteContext context) {
                        context.getArguments()[0] = "argSetBySermant";
                        return context;
                    }

                    @Override
                    public ExecuteContext after(ExecuteContext context) {
                        return context;
                    }

                    @Override
                    public ExecuteContext onThrow(ExecuteContext context) {
                        return context;
                    }
                })
        };
    }
}
