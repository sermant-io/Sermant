/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.lubanops.apm.plugin.flowrecord.plugins.mysql.v5;

import com.huawei.apm.bootstrap.common.BeforeResult;
import com.huawei.apm.bootstrap.interceptors.StaticMethodInterceptor;
import com.lubanops.apm.bootstrap.TransformAccess;
import com.lubanops.apm.plugin.flowrecord.plugins.mysql.common.ConnectionCache;
import org.apache.skywalking.apm.plugin.jdbc.connectionurl.parser.URLParser;
import org.apache.skywalking.apm.plugin.jdbc.trace.ConnectionInfo;

import java.lang.reflect.Method;

/**
 * ConnectionImpl#getInstance in mysql-5.x has 5 parameters such as getInstance(String hostToConnectTo, int
 * portToConnectTo, Properties info, String databaseToConnectTo, String url)
 *
 * @author lihongjiang
 * @version 0.1
 * @since 2021-04-14
 */
public class ConnectionCreate5xInterceptor implements StaticMethodInterceptor {

    @Override
    public void before(Class<?> clazz, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {

    }

    @Override
    public Object after(Class<?> clazz, Method method, Object[] arguments, Object result) throws Exception {
        if (result instanceof TransformAccess) {
            ConnectionInfo connectionInfo = ConnectionCache.get(arguments[0].toString(), arguments[1].toString());
            if (connectionInfo == null) {
                connectionInfo = URLParser.parser(arguments[4].toString());
            }
            ((TransformAccess) result).setLopsAttribute(connectionInfo);
        }
        return result;
    }

    @Override
    public void onThrow(Class<?> clazz, Method method, Object[] arguments, Throwable t) {

    }
}
