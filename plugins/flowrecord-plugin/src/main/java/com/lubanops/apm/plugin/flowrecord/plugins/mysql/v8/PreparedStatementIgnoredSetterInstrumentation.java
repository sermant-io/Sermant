/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.flowrecord.plugins.mysql.v8;

import com.huawei.apm.bootstrap.definition.MethodInterceptPoint;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.skywalking.apm.plugin.jdbc.JDBCPluginConfig;
import org.apache.skywalking.apm.plugin.jdbc.define.Constants;

import java.util.Iterator;
import java.util.Set;

/**
 * 使用JDBC插件获取sql参数
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-06-03
 */
public class PreparedStatementIgnoredSetterInstrumentation extends PreparedStatementInstrumentation {

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{
                MethodInterceptPoint.newInstMethodInterceptPoint("org.apache.skywalking.apm.plugin.jdbc.JDBCPreparedStatementIgnorableSetterInterceptor",
                        getMethodsMatcher())
        };
    }

    public ElementMatcher<MethodDescription> getMethodsMatcher() {
        ElementMatcher.Junction<MethodDescription> matcher = ElementMatchers.none();
        if (JDBCPluginConfig.Plugin.MySQL.TRACE_SQL_PARAMETERS || JDBCPluginConfig.Plugin.POSTGRESQL.TRACE_SQL_PARAMETERS || JDBCPluginConfig.Plugin.MARIADB.TRACE_SQL_PARAMETERS) {
            Set<String> setters = Constants.PS_IGNORABLE_SETTERS;

            String setter;
            for (Iterator var3 = setters.iterator(); var3.hasNext(); matcher = matcher.or(ElementMatchers.named(setter))) {
                setter = (String) var3.next();
            }
        }

        return matcher;
    }
}
