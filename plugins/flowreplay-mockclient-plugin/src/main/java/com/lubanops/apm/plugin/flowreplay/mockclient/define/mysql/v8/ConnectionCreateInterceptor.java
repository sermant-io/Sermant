/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.flowreplay.mockclient.define.mysql.v8;

import com.huawei.apm.bootstrap.common.BeforeResult;
import com.huawei.apm.bootstrap.interceptors.StaticMethodInterceptor;
import com.lubanops.apm.bootstrap.TransformAccess;
import com.mysql.cj.conf.HostInfo;
import org.apache.skywalking.apm.plugin.jdbc.connectionurl.parser.URLParser;
import org.apache.skywalking.apm.plugin.jdbc.trace.ConnectionInfo;

import java.lang.reflect.Method;

/**
 * 拦截mysql创建连接后获取连接信息
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
