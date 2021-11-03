/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.nacos;

import com.huawei.apm.bootstrap.common.BeforeResult;
import com.huawei.apm.bootstrap.interceptors.StaticMethodInterceptor;
import com.huawei.route.common.threadlocal.ThreadLocalContext;

import java.lang.reflect.Method;

/**
 * nacos命名空间拦截器, 将命名空间进行传递
 * {@link com.alibaba.nacos.client.naming.NacosNamingService} initNamespaceForNaming方法
 *
 * @author zhouss
 * @since 2021-11-03
 */
public class NacosNamespaceInterceptor implements StaticMethodInterceptor {

    @Override
    public void before(Class<?> clazz, Method method, Object[] arguments, BeforeResult beforeResult) {

    }

    @Override
    public Object after(Class<?> clazz, Method method, Object[] arguments, Object result) {
        // 存放命名空间
        ThreadLocalContext.INSTANCE.put(NacosConstants.NAMESPACE_KEY, result);
        return result;
    }

    @Override
    public void onThrow(Class<?> clazz, Method method, Object[] arguments, Throwable t) {

    }
}
