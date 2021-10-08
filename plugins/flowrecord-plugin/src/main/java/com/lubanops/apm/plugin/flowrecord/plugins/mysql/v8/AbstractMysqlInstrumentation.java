/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.lubanops.apm.plugin.flowrecord.plugins.mysql.v8;

import com.lubanops.apm.plugin.flowrecord.plugins.mysql.common.MysqlCommonConstants;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.ConstructorInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.StaticMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassEnhancePluginDefine;

/**
 * Mysql 增强类的抽象类
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-06-03
 */
public abstract class AbstractMysqlInstrumentation extends ClassEnhancePluginDefine {
    @Override
    public ConstructorInterceptPoint[] getConstructorsInterceptPoints() {
        return new ConstructorInterceptPoint[0];
    }

    @Override
    public StaticMethodsInterceptPoint[] getStaticMethodsInterceptPoints() {
        return new StaticMethodsInterceptPoint[0];
    }

    @Override
    public InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new InstanceMethodsInterceptPoint[0];
    }

    @Override
    protected String[] witnessClasses() {
        return new String[]{MysqlCommonConstants.WITNESS_MYSQL_8X_CLASS};
    }
}
