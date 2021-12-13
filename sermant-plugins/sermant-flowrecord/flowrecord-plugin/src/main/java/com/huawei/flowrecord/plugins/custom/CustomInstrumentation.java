/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.flowrecord.plugins.custom;

import static net.bytebuddy.matcher.ElementMatchers.named;

import com.huawei.sermant.core.agent.definition.EnhanceDefinition;
import com.huawei.sermant.core.agent.definition.MethodInterceptPoint;
import com.huawei.sermant.core.agent.matcher.ClassMatcher;
import com.huawei.sermant.core.agent.matcher.ClassMatchers;
import com.huawei.flowrecord.config.CommonConst;
import com.huawei.flowrecord.config.FlowRecordConfig;
import com.huawei.flowrecord.utils.PluginConfigUtil;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.NameMatcher;
import net.bytebuddy.matcher.StringMatcher;

/**
 * 自定义应用拦截点
 */
public class CustomInstrumentation implements EnhanceDefinition {
    private static final FlowRecordConfig flowRecordConfig = PluginConfigUtil.getFlowRecordConfig();
    private static final String ENHANCE_CLASS = flowRecordConfig.getCustomEnhanceClass();
    private static final String INSTANCE_METHOD_INTERCEPT_CLASS =
            "com.huawei.flowrecord.plugins.custom.CustomInstanceMethodInterceptor";
    private static final String STATIC_METHOD_INTERCEPT_CLASS =
            "com.huawei.flowrecord.plugins.custom.CustomStaticMethodInterceptor";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.named(ENHANCE_CLASS);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{
                MethodInterceptPoint.newInstMethodInterceptPoint(INSTANCE_METHOD_INTERCEPT_CLASS,
                        setMethodMatcher(flowRecordConfig.getCustomEnhanceInstanceMethod())),
                MethodInterceptPoint.newStaticMethodInterceptPoint(STATIC_METHOD_INTERCEPT_CLASS,
                        setMethodMatcher(flowRecordConfig.getCustomEnhanceStaticMethod()))
        };
    }

    public ElementMatcher.Junction<MethodDescription> setMethodMatcher(String methods) {
        String[] methodArray = methods.split(CommonConst.COMMA_SIGN);
        if (methodArray.length == 0) {
            return new NameMatcher<MethodDescription>(new StringMatcher("",
                    StringMatcher.Mode.EQUALS_FULLY));
        }
        ElementMatcher.Junction<MethodDescription> nameMatcher =
                new NameMatcher<MethodDescription>(new StringMatcher(methodArray[0].trim(),
                        StringMatcher.Mode.EQUALS_FULLY));
        for (int i = 1; i < methodArray.length; i++) {
            nameMatcher = nameMatcher.or(named(methodArray[i].trim()));
        }
        return nameMatcher;
    }
}
