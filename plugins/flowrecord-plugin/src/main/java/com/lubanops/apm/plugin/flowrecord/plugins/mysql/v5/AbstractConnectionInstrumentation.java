/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.lubanops.apm.plugin.flowrecord.plugins.mysql.v5;

import com.huawei.apm.bootstrap.definition.EnhanceDefinition;
import com.huawei.apm.bootstrap.definition.MethodInterceptPoint;
import com.huawei.apm.bootstrap.matcher.ClassMatcher;
import com.lubanops.apm.plugin.flowrecord.plugins.mysql.common.MysqlCommonConstants;

import net.bytebuddy.description.method.MethodDescription;

import net.bytebuddy.matcher.ElementMatchers;
import org.apache.skywalking.apm.plugin.jdbc.define.Constants;

/**
 * {@link AbstractConnectionInstrumentation} intercepts the following methods that the class which extend
 * com.mysql.jdbc.ConnectionImpl.
 *
 * @author lihongjiang
 * @version 0.1
 * @since 2021-04-14
 */
public abstract class AbstractConnectionInstrumentation implements EnhanceDefinition {

    @Override
    public ClassMatcher enhanceClass() {
        return null;
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{
                MethodInterceptPoint.newInstMethodInterceptPoint(MysqlCommonConstants.CREATE_PREPARED_STATEMENT_INTERCEPTOR,
                        ElementMatchers.<MethodDescription>named(Constants.PREPARE_STATEMENT_METHOD_NAME)),
                MethodInterceptPoint.newInstMethodInterceptPoint(Constants.SERVICE_METHOD_INTERCEPT_CLASS,
                        ElementMatchers.namedOneOf(Constants.COMMIT_METHOD_NAME, Constants.ROLLBACK_METHOD_NAME,
                                Constants.CLOSE_METHOD_NAME, Constants.RELEASE_SAVE_POINT_METHOD_NAME)),
                MethodInterceptPoint.newInstMethodInterceptPoint(MysqlCommonConstants.SET_CATALOG_INTERCEPTOR,
                        ElementMatchers.<MethodDescription>named("setCatalog"))
        };
    }
}
