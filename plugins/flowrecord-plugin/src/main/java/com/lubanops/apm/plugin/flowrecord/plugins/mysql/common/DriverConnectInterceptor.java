/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.lubanops.apm.plugin.flowrecord.plugins.mysql.common;

import com.huawei.apm.bootstrap.common.BeforeResult;
import com.huawei.apm.bootstrap.interceptors.InstanceMethodInterceptor;
import org.apache.skywalking.apm.plugin.jdbc.connectionurl.parser.URLParser;

import java.lang.reflect.Method;

/**
 * mysql url parse
 *
 * @author lihongjiang
 * @version 0.1
 * @since 2021-04-14
 */
public class DriverConnectInterceptor implements InstanceMethodInterceptor {

    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
        ConnectionCache.save(URLParser.parser(arguments[0].toString()));
    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) throws Exception {
        return result;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable t) {

    }
}
