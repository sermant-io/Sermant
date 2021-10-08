/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.flowreplay.mockclient.define.mysql.common;

import com.huawei.apm.bootstrap.common.BeforeResult;
import com.huawei.apm.bootstrap.interceptors.InstanceMethodInterceptor;
import com.lubanops.apm.bootstrap.TransformAccess;
import org.apache.skywalking.apm.plugin.jdbc.define.StatementEnhanceInfos;
import org.apache.skywalking.apm.plugin.jdbc.trace.ConnectionInfo;

import java.lang.reflect.Method;

/**
 * 创建CallableStatement时拦截处理 获取sql
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-06-03
 */
public class CreateCallableStatementInterceptor implements InstanceMethodInterceptor {
    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {

    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) throws Exception {
        if (result instanceof TransformAccess) {
            ((TransformAccess) result).setLopsAttribute(
                    new StatementEnhanceInfos((ConnectionInfo) ((TransformAccess) obj).getLopsAttribute(),
                            (String) arguments[0], "CallableStatement")
            );
        }
        return result;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable t) {

    }
}
