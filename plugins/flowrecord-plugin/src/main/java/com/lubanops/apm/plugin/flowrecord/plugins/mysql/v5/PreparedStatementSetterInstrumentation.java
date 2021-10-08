/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.lubanops.apm.plugin.flowrecord.plugins.mysql.v5;

import com.huawei.apm.bootstrap.definition.MethodInterceptPoint;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.plugin.jdbc.JDBCPluginConfig;
import org.apache.skywalking.apm.plugin.jdbc.PSSetterDefinitionOfJDBCInstrumentation;
import org.apache.skywalking.apm.plugin.jdbc.define.Constants;

import java.util.Iterator;
import java.util.Set;

/**
 * mysql PreparedStatement with real meaning
 *
 * @author lihongjiang
 * @version 0.1
 * @since 2021-04-14
 */
public class PreparedStatementSetterInstrumentation extends PreparedStatementInstrumentation {

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{
                MethodInterceptPoint.newInstMethodInterceptPoint("org.apache.skywalking.apm.plugin.jdbc.JDBCPreparedStatementSetterInterceptor",
                        getMethodsMatcher())
        };
    }

    public ElementMatcher<MethodDescription> getMethodsMatcher() {
        ElementMatcher.Junction<MethodDescription> matcher = ElementMatchers.none();
        if (JDBCPluginConfig.Plugin.MySQL.TRACE_SQL_PARAMETERS || JDBCPluginConfig.Plugin.POSTGRESQL.TRACE_SQL_PARAMETERS || JDBCPluginConfig.Plugin.MARIADB.TRACE_SQL_PARAMETERS) {
            Set<String> setters = Constants.PS_SETTERS;
            String setter;
            for (Iterator var3 = setters.iterator(); var3.hasNext(); matcher = matcher.or(ElementMatchers.named(setter))) {
                setter = (String) var3.next();
            }
        }
        return matcher;
    }
}
