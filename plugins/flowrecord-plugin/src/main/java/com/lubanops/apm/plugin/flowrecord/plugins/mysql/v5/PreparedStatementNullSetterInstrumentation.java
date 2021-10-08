/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.lubanops.apm.plugin.flowrecord.plugins.mysql.v5;

import com.huawei.apm.bootstrap.definition.MethodInterceptPoint;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.plugin.jdbc.JDBCPreparedStatementNullSetterInstanceMethodsInterceptPoint;

/**
 * mysql PreparedStatement with arguments that set to null
 *
 * @author lihongjiang
 * @version 0.1
 * @since 2021-04-14
 */
public class PreparedStatementNullSetterInstrumentation extends PreparedStatementInstrumentation {
    private static final String INTERCEPT_CLASS = "org.apache.skywalking.apm.plugin.jdbc.JDBCPreparedStatementNullSetterInterceptor";

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{
                MethodInterceptPoint.newInstMethodInterceptPoint(INTERCEPT_CLASS, ElementMatchers.<MethodDescription>named("setNull"))
        };
    }
}
