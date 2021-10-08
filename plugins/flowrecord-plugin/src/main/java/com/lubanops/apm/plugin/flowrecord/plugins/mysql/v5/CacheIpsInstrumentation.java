/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.lubanops.apm.plugin.flowrecord.plugins.mysql.v5;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static org.apache.skywalking.apm.agent.core.plugin.match.MultiClassNameMatch.byMultiClassMatch;

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
 * mysql connectioncache implements
 *
 * @author lihongjiang
 * @version 0.1
 * @since 2021-04-14
 */
public class CacheIpsInstrumentation implements EnhanceDefinition {

    private static final String ENHANCE_CLASS_NON_REG_REP = "com.mysql.jdbc.NonRegisteringReplicationDriver";
    private static final String ENHANCE_CLASS = "com.mysql.jdbc.Driver";
    private static final String ENHANCE_CLASS_NON_REG = "com.mysql.jdbc.NonRegisteringDriver";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.multiClass(ENHANCE_CLASS, ENHANCE_CLASS_NON_REG, ENHANCE_CLASS_NON_REG_REP);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{
                MethodInterceptPoint.newInstMethodInterceptPoint(MysqlCommonConstants.DRIVER_CONNECT_INTERCEPTOR, ElementMatchers.<MethodDescription>named("connect"))
        };
    }
}

