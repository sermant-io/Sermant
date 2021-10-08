/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.lubanops.apm.plugin.flowrecord.plugins.mysql.common;

import com.huawei.apm.bootstrap.common.BeforeResult;
import com.huawei.apm.bootstrap.interceptors.InstanceMethodInterceptor;
import com.lubanops.apm.bootstrap.TransformAccess;
import org.apache.skywalking.apm.plugin.jdbc.define.StatementEnhanceInfos;
import org.apache.skywalking.apm.plugin.jdbc.trace.ConnectionInfo;

import java.lang.reflect.Method;

/**
 * mysql PreparedStatement establish
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-06-03
 */
public class CreatePreparedStatementInterceptor implements InstanceMethodInterceptor {

    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {

    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) throws Exception {
        TransformAccess objInst = (TransformAccess) obj;
        if (result instanceof TransformAccess) {
            ((TransformAccess) result).setLopsAttribute(
                    new StatementEnhanceInfos((ConnectionInfo) objInst.getLopsAttribute(),
                            (String) arguments[0], "PreparedStatement")
            );
        }
        return result;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable t) {

    }
}