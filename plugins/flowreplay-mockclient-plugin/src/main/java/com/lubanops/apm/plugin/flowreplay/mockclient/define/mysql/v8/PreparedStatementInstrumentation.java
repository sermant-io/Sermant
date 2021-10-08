/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.flowreplay.mockclient.define.mysql.v8;

import com.huawei.apm.bootstrap.definition.EnhanceDefinition;
import com.huawei.apm.bootstrap.definition.MethodInterceptPoint;
import com.huawei.apm.bootstrap.matcher.ClassMatcher;
import com.huawei.apm.bootstrap.matcher.ClassMatchers;
import net.bytebuddy.description.method.MethodDescription;

import net.bytebuddy.matcher.ElementMatchers;

/**
 * Mysql 拦截器
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-05-10
 */
public class PreparedStatementInstrumentation implements EnhanceDefinition {
    private static final String ENHANCE_CLASS = "com.mysql.cj.jdbc.ClientPreparedStatement";

    private static final String INTERCEPT_CLASS =
            "com.lubanops.apm.plugin.flowreplay.mockclient.define.mysql.common.PreparedStatementInterceptor";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.named(ENHANCE_CLASS);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{
                MethodInterceptPoint.newInstMethodInterceptPoint(INTERCEPT_CLASS,
                        ElementMatchers.<MethodDescription>namedOneOf("execute", "executeQuery", "executeUpdate", "executeLargeUpdate", "getResultSet"))
        };
    }
}
