/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.flowrecord.plugins.mysql.v8;

import com.huawei.apm.bootstrap.common.BeforeResult;
import com.huawei.apm.bootstrap.interceptors.StaticMethodInterceptor;
import com.lubanops.apm.bootstrap.TransformAccess;
import com.mysql.cj.conf.HostInfo;

import org.apache.skywalking.apm.plugin.jdbc.connectionurl.parser.URLParser;
import org.apache.skywalking.apm.plugin.jdbc.trace.ConnectionInfo;

import java.lang.reflect.Method;

/**
 * 获取创建连接时的URL 可以用来解析database name
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-06-03
 */
public class ConnectionCreateInterceptor implements StaticMethodInterceptor {

    @Override
    public void before(Class<?> clazz, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {

    }

    @Override
    public Object after(Class<?> clazz, Method method, Object[] arguments, Object result) throws Exception {
        if (result instanceof TransformAccess) {
            final HostInfo hostInfo = (HostInfo) arguments[0];
            ConnectionInfo connectionInfo = URLParser.parser(hostInfo.getDatabaseUrl());
            ((TransformAccess) result).setLopsAttribute(connectionInfo);
        }
        return result;
    }

    @Override
    public void onThrow(Class<?> clazz, Method method, Object[] arguments, Throwable t) {

    }
}
