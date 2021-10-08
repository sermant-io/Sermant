/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.flowrecord.plugins.mysql.v8;

import com.huawei.apm.bootstrap.definition.MethodInterceptPoint;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.plugin.jdbc.JDBCPreparedStatementNullSetterInstanceMethodsInterceptPoint;

/**
 * 使用JDBC插件获取sql参数
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-06-03
 */
public class PreparedStatementNullSetterInstrumentation extends PreparedStatementInstrumentation {

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{
                MethodInterceptPoint.newInstMethodInterceptPoint("org.apache.skywalking.apm.plugin.jdbc.JDBCPreparedStatementNullSetterInterceptor",
                        ElementMatchers.<MethodDescription>named("setNull"))
        };
    }
}
