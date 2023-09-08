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

package com.huaweicloud.agentcore.tests.plugin.declarer.methodmatch;

import com.huaweicloud.agentcore.tests.plugin.interceptor.SetEnhanceFlagInterceptor;
import com.huaweicloud.sermant.core.plugin.agent.declarer.AbstractPluginDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;

/**
 * 测试方法匹配
 *
 * @author luanwenfei
 * @since 2023-09-07
 */
public class TestMethodMatcherDeclarer extends AbstractPluginDeclarer {
    /**
     * 测试方法参数为3时的方法匹配
     */
    private static final int TEST_METHOD_PARAMS_COUNT = 3;

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameEquals(
                "com.huaweicloud.agentcore.test.application.tests.methodmatch.MethodMatchersTest");
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
                InterceptDeclarer.build(MethodMatcher.isConstructor(), new SetEnhanceFlagInterceptor()),
                InterceptDeclarer.build(MethodMatcher.isStaticMethod(), new SetEnhanceFlagInterceptor()),
                InterceptDeclarer.build(MethodMatcher.nameEquals("exactNameMethod"), new SetEnhanceFlagInterceptor()),
                InterceptDeclarer.build(MethodMatcher.nameEquals("exactNameMethod"), new SetEnhanceFlagInterceptor()),
                InterceptDeclarer.build(MethodMatcher.namePrefixedWith("prefix"), new SetEnhanceFlagInterceptor()),
                InterceptDeclarer.build(MethodMatcher.nameInfixedWith("Infix"), new SetEnhanceFlagInterceptor()),
                InterceptDeclarer.build(MethodMatcher.nameSuffixedWith("Suffix"), new SetEnhanceFlagInterceptor()),
                InterceptDeclarer.build(MethodMatcher.resultTypeEquals(boolean.class), new SetEnhanceFlagInterceptor()),
                InterceptDeclarer.build(MethodMatcher.paramCountEquals(TEST_METHOD_PARAMS_COUNT),
                        new SetEnhanceFlagInterceptor()),
                InterceptDeclarer.build(MethodMatcher.paramTypesEqual(boolean.class, boolean.class),
                        new SetEnhanceFlagInterceptor()),
                InterceptDeclarer.build(
                        MethodMatcher.isAnnotatedWith(
                                "com.huaweicloud.agentcore.test.application.common.TestAnnotationA"),
                        new SetEnhanceFlagInterceptor()),
                InterceptDeclarer.build(
                        MethodMatcher.isAnnotatedWith(
                                "com.huaweicloud.agentcore.test.application.common.TestAnnotationA",
                                "com.huaweicloud.agentcore.test.application.common.TestAnnotationB"),
                        new SetEnhanceFlagInterceptor())};
    }
}
