/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.lubanops.stresstest.http;

import com.huawei.apm.bootstrap.definition.EnhanceDefinition;
import com.huawei.apm.bootstrap.definition.MethodInterceptPoint;
import com.huawei.apm.bootstrap.matcher.ClassMatcher;
import com.huawei.apm.bootstrap.matcher.ClassMatchers;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * httpclient 基础增强
 *
 * @author yiwei
 * @since 2021/10/25
 */
public abstract class AbstractHttpClientEnhance implements EnhanceDefinition{
    private final String enhanceClass;

    private final String interceptorClass;

    protected AbstractHttpClientEnhance(String enhanceClass, String interceptorClass) {
        this.enhanceClass = enhanceClass;
        this.interceptorClass = interceptorClass;
    }

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.named(enhanceClass);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{MethodInterceptPoint.newInstMethodInterceptPoint(interceptorClass,
                ElementMatchers.named("execute"))
        };
    }
}
