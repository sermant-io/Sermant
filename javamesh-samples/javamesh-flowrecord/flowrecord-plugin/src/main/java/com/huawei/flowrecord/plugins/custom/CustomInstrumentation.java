/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowrecord.plugins.custom;

import static net.bytebuddy.matcher.ElementMatchers.named;

import com.huawei.apm.bootstrap.definition.EnhanceDefinition;
import com.huawei.apm.bootstrap.definition.MethodInterceptPoint;
import com.huawei.apm.bootstrap.matcher.ClassMatcher;
import com.huawei.apm.bootstrap.matcher.ClassMatchers;
import com.huawei.flowrecord.config.CommonConst;
import com.huawei.flowrecord.config.ConfigConst;
import com.huawei.flowrecord.utils.PluginConfigUtil;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.NameMatcher;
import net.bytebuddy.matcher.StringMatcher;

/**
 * 自定义应用拦截点
 *
 */
public class CustomInstrumentation implements EnhanceDefinition {
    private static final String ENHANCE_CLASS = PluginConfigUtil.getValueByKey(ConfigConst.CUSTOM_ENHANCE_CLASS);
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
                        setMethodMatcher(PluginConfigUtil.getValueByKey(ConfigConst.CUSTOM_ENHANCE_INSTANCE_METHOD))),
                MethodInterceptPoint.newStaticMethodInterceptPoint(STATIC_METHOD_INTERCEPT_CLASS,
                        setMethodMatcher(PluginConfigUtil.getValueByKey(ConfigConst.CUSTOM_ENHANCE_STATIC_METHOD)))
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
