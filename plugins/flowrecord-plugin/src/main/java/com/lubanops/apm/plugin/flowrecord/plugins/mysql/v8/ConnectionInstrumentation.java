/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.flowrecord.plugins.mysql.v8;

import com.huawei.apm.bootstrap.definition.EnhanceDefinition;
import com.huawei.apm.bootstrap.definition.MethodInterceptPoint;
import com.huawei.apm.bootstrap.matcher.ClassMatcher;
import com.huawei.apm.bootstrap.matcher.ClassMatchers;
import com.lubanops.apm.plugin.flowrecord.plugins.mysql.common.MysqlCommonConstants;

import net.bytebuddy.description.method.MethodDescription;

import net.bytebuddy.matcher.ElementMatchers;
import org.apache.skywalking.apm.plugin.jdbc.define.Constants;

/**
 * ConnectionImpl 拦截器
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-06-03
 */
public class ConnectionInstrumentation implements EnhanceDefinition {
    private static final String ENHANCE_CLASS = "com.mysql.cj.jdbc.ConnectionImpl";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.named(ENHANCE_CLASS);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{
                MethodInterceptPoint.newInstMethodInterceptPoint(MysqlCommonConstants.CREATE_PREPARED_STATEMENT_INTERCEPTOR,
                        ElementMatchers.<MethodDescription>named("Constants.PREPARE_STATEMENT_METHOD_NAME")),
                MethodInterceptPoint.newInstMethodInterceptPoint(Constants.SERVICE_METHOD_INTERCEPT_CLASS,
                        ElementMatchers.namedOneOf(Constants.COMMIT_METHOD_NAME, Constants.ROLLBACK_METHOD_NAME,
                                Constants.CLOSE_METHOD_NAME, Constants.RELEASE_SAVE_POINT_METHOD_NAME)),
                MethodInterceptPoint.newInstMethodInterceptPoint(MysqlCommonConstants.SET_CATALOG_INTERCEPTOR,
                        ElementMatchers.<MethodDescription>named("setCatalog"))
        };
    }
}
