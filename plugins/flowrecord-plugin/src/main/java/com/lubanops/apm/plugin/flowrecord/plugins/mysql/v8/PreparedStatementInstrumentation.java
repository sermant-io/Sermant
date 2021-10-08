/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.flowrecord.plugins.mysql.v8;

import static net.bytebuddy.matcher.ElementMatchers.named;

import com.huawei.apm.bootstrap.definition.EnhanceDefinition;
import com.huawei.apm.bootstrap.definition.MethodInterceptPoint;
import com.huawei.apm.bootstrap.matcher.ClassMatcher;
import com.huawei.apm.bootstrap.matcher.ClassMatchers;
import com.lubanops.apm.plugin.flowrecord.plugins.mysql.common.MysqlCommonConstants;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import net.bytebuddy.matcher.ElementMatchers;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.ConstructorInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine;
import org.apache.skywalking.apm.agent.core.plugin.match.ClassMatch;
import org.apache.skywalking.apm.agent.core.plugin.match.NameMatch;

/**
 * Mysql 拦截器
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-05-10
 */
public class PreparedStatementInstrumentation implements EnhanceDefinition {
    private static final String ENHANCE_CLASS = "com.mysql.cj.jdbc.ClientPreparedStatement";

    private static final String INTERCEPT_CLASS = MysqlCommonConstants.PREPARED_STATEMENT_INTERCEPTOR;

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.named(ENHANCE_CLASS);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{
                MethodInterceptPoint.newInstMethodInterceptPoint(INTERCEPT_CLASS,
                        ElementMatchers.namedOneOf("execute", "executeQuery", "executeUpdate",
                                "executeLargeUpdate", "getResultSet"))
        };
    }
}
