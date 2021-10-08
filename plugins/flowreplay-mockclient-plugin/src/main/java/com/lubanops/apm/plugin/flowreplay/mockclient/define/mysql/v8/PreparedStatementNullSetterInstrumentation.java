/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.flowreplay.mockclient.define.mysql.v8;

import com.huawei.apm.bootstrap.definition.MethodInterceptPoint;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.skywalking.apm.plugin.jdbc.define.Constants;

/**
 * 引用jdbc-common插件 用于获取sql参数
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-06-03
 */
public class PreparedStatementNullSetterInstrumentation extends PreparedStatementInstrumentation {

    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{
                MethodInterceptPoint.newInstMethodInterceptPoint(Constants.PREPARED_STATEMENT_NULL_SETTER_METHODS_INTERCEPTOR,
                        ElementMatchers.<MethodDescription>named("setNull"))
        };
    }
}
