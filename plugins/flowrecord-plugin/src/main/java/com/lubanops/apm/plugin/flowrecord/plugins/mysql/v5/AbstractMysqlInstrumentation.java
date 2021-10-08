/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.lubanops.apm.plugin.flowrecord.plugins.mysql.v5;

import com.lubanops.apm.plugin.flowrecord.plugins.mysql.common.MysqlCommonConstants;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.ConstructorInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.StaticMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassEnhancePluginDefine;

/**
 * mysql Abstract instrumentation
 *
 * @author lihongjiang
 * @version 0.1
 * @since 2021-04-14
 */
public abstract class AbstractMysqlInstrumentation extends ClassEnhancePluginDefine {

    @Override
    public ConstructorInterceptPoint[] getConstructorsInterceptPoints() {
        return null;
    }

    @Override
    public StaticMethodsInterceptPoint[] getStaticMethodsInterceptPoints() {
        return null;
    }

    @Override
    public InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return null;
    }

    @Override
    protected String[] witnessClasses() {
        return new String[]{MysqlCommonConstants.WITNESS_MYSQL_5X_CLASS};
    }
}
