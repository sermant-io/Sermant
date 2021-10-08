/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.flowreplay.mockclient.define.mysql.v8;

import com.huawei.apm.bootstrap.definition.MethodInterceptPoint;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.skywalking.apm.plugin.jdbc.JDBCPluginConfig;
import org.apache.skywalking.apm.plugin.jdbc.define.Constants;

import java.util.Set;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.none;
import static org.apache.skywalking.apm.plugin.jdbc.define.Constants.PS_IGNORABLE_SETTERS;

/**
 * 引用jdbc-common插件 用于获取sql参数
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-06-03
 */
public class PreparedStatementIgnoredSetterInstrumentation extends PreparedStatementInstrumentation {

    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{
                MethodInterceptPoint.newInstMethodInterceptPoint(Constants.PREPARED_STATEMENT_IGNORABLE_SETTER_METHODS_INTERCEPTOR,
                        getMethodsMatcher())
        };
    }

    public ElementMatcher<MethodDescription> getMethodsMatcher() {
        ElementMatcher.Junction<MethodDescription> matcher = none();

        if (JDBCPluginConfig.Plugin.MySQL.TRACE_SQL_PARAMETERS || JDBCPluginConfig.Plugin.POSTGRESQL.TRACE_SQL_PARAMETERS || JDBCPluginConfig.Plugin.MARIADB.TRACE_SQL_PARAMETERS) {
            final Set<String> setters = PS_IGNORABLE_SETTERS;
            for (String setter : setters) {
                matcher = matcher.or(named(setter));
            }
        }
        return matcher;
    }
}
