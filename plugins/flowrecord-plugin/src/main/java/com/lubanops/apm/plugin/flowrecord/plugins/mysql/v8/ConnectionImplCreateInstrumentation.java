/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.flowrecord.plugins.mysql.v8;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static org.apache.skywalking.apm.agent.core.plugin.match.NameMatch.byName;

import com.huawei.apm.bootstrap.definition.EnhanceDefinition;
import com.huawei.apm.bootstrap.definition.MethodInterceptPoint;
import com.huawei.apm.bootstrap.matcher.ClassMatcher;
import com.huawei.apm.bootstrap.matcher.ClassMatchers;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import net.bytebuddy.matcher.ElementMatchers;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.StaticMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.match.ClassMatch;

/**
 * ConnectionImpl 拦截器
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-06-03
 */
public class ConnectionImplCreateInstrumentation implements EnhanceDefinition {
    private static final String JDBC_ENHANCE_CLASS = "com.mysql.cj.jdbc.ConnectionImpl";
    private static final String CONNECT_METHOD = "getInstance";
    private static final String INTERCEPTOR = "com.lubanops.apm.plugin.flowrecord.plugins.mysql.v8.ConnectionCreateInterceptor";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.named(JDBC_ENHANCE_CLASS);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{
                MethodInterceptPoint.newStaticMethodInterceptPoint(INTERCEPTOR, ElementMatchers.<MethodDescription>named(CONNECT_METHOD))
        };
    }
}
