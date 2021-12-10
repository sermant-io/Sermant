/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 */

package com.lubanops.stresstest.db.mybatis;

import com.huawei.javamesh.core.agent.common.BeforeResult;
import com.huawei.javamesh.core.agent.interceptor.InstanceMethodInterceptor;

import javax.sql.DataSource;
import java.lang.reflect.Method;

/**
 * Mybatis 增强实现
 *
 * @author yiwei
 * @since 2021/10/21
 */
public class MybatisInterceptor implements InstanceMethodInterceptor {
    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) {
        if (arguments.length == 1 && arguments[0] instanceof DataSource) {
            arguments[0] = new ShadowDataSource((DataSource) arguments[0]);
        }
    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) {
        return result;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable throwable) {
    }
}
