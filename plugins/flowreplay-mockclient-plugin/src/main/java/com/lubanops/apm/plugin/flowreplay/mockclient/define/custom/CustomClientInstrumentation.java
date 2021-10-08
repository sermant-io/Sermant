/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.flowreplay.mockclient.define.custom;

import com.huawei.apm.bootstrap.definition.EnhanceDefinition;
import com.huawei.apm.bootstrap.definition.MethodInterceptPoint;
import com.huawei.apm.bootstrap.matcher.ClassMatcher;
import com.huawei.apm.bootstrap.matcher.ClassMatchers;
import com.lubanops.apm.plugin.flowreplay.mockclient.config.PluginConfig;

import net.bytebuddy.description.method.MethodDescription;

import net.bytebuddy.matcher.ElementMatchers;

/**
 * Custom Client Instrumentation 拦截用户自定义接口和方法
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-04-22
 */
public class CustomClientInstrumentation implements EnhanceDefinition {
    private static final String INTERCEPT_CLASS =
            "com.lubanops.apm.plugin.flowreplay.mockclient.define.custom.CustomClientInterceptor";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.named(PluginConfig.customEnhanceClass);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{
                MethodInterceptPoint.newInstMethodInterceptPoint(INTERCEPT_CLASS, ElementMatchers.<MethodDescription>named(PluginConfig.customEnhanceMethod))
        };
    }
}
