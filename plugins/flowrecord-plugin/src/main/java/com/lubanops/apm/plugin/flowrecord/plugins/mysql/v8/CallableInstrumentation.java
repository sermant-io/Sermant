/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.lubanops.apm.plugin.flowrecord.plugins.mysql.v8;

import com.huawei.apm.bootstrap.definition.EnhanceDefinition;
import com.huawei.apm.bootstrap.definition.MethodInterceptPoint;
import com.huawei.apm.bootstrap.matcher.ClassMatcher;
import com.huawei.apm.bootstrap.matcher.ClassMatchers;
import com.lubanops.apm.plugin.flowrecord.plugins.mysql.common.MysqlCommonConstants;

import net.bytebuddy.matcher.ElementMatchers;

/**
 * CallableStatement 拦截器
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-06-03
 */
public class CallableInstrumentation implements EnhanceDefinition {
    private static final String ENHANCE_CLASS = "com.mysql.cj.jdbc.CallableStatement";
    private static final String SERVICE_METHOD_INTERCEPTOR = MysqlCommonConstants.PREPARED_STATEMENT_INTERCEPTOR;

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.named(ENHANCE_CLASS);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{
                MethodInterceptPoint.newInstMethodInterceptPoint(SERVICE_METHOD_INTERCEPTOR,
                        ElementMatchers.namedOneOf("execute", "executeQuery", "executeUpdate"))
        };
    }
}
