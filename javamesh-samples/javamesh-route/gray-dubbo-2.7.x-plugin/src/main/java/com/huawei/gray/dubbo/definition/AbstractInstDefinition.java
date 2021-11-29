/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.gray.dubbo.definition;

import com.huawei.apm.core.agent.definition.EnhanceDefinition;
import com.huawei.apm.core.agent.definition.MethodInterceptPoint;
import com.huawei.apm.core.agent.matcher.ClassMatcher;
import com.huawei.apm.core.agent.matcher.ClassMatchers;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * 实例增强基类
 *
 * @author pengyuyi
 * @date 2021/11/24
 */
public abstract class AbstractInstDefinition implements EnhanceDefinition {
    private final String enhanceClass;

    private final String interceptClass;

    private final String methodName;

    /**
     * 构造方法
     *
     * @param enhanceClass 增加类
     * @param interceptClass 拦截类
     * @param methodName 拦截方法
     */
    public AbstractInstDefinition(String enhanceClass, String interceptClass, String methodName) {
        this.enhanceClass = enhanceClass;
        this.interceptClass = interceptClass;
        this.methodName = methodName;
    }

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.named(enhanceClass);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{
                MethodInterceptPoint.newInstMethodInterceptPoint(interceptClass,
                        ElementMatchers.<MethodDescription>named(methodName))};
    }
}
