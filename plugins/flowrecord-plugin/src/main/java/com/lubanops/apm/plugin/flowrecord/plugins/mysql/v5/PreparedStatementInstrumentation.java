/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.lubanops.apm.plugin.flowrecord.plugins.mysql.v5;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static org.apache.skywalking.apm.agent.core.plugin.match.NameMatch.byName;

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
import org.apache.skywalking.apm.agent.core.plugin.match.ClassMatch;

/**
 * {@link PreparedStatementInstrumentation} define that the mysql-2.x plugin intercepts the following methods in the
 * com.mysql.jdbc.JDBC42PreparedStatement, com.mysql.jdbc.PreparedStatement and com.mysql.cj.jdbc.PreparedStatement
 * class: 1. execute 2. executeQuery 3. executeUpdate 4. executeLargeUpdate 5. addBatch
 *
 * @author lihongjiang
 * @version 0.1
 * @since 2021-04-14
 */
public class PreparedStatementInstrumentation implements EnhanceDefinition {

    private static final String SERVICE_METHOD_INTERCEPTOR = MysqlCommonConstants.PREPARED_STATEMENT_INTERCEPTOR;
    public static final String MYSQL_PREPARED_STATEMENT_CLASS_NAME = "com.mysql.jdbc.PreparedStatement";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.named(MYSQL_PREPARED_STATEMENT_CLASS_NAME);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{
                MethodInterceptPoint.newInstMethodInterceptPoint(SERVICE_METHOD_INTERCEPTOR,
                        ElementMatchers.namedOneOf("execute", "executeQuery", "executeUpdate", "executeLargeUpdate", "getResultSet"))
        };
    }
}
