/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.example.demo.interceptor;

import com.huawei.apm.core.agent.interceptor.ConstructorInterceptor;
import com.huawei.example.demo.common.DemoLogger;

/**
 * 构造函数的拦截器示例，本示例将展示如何对构造函数进行增强
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/10/25
 */
public class DemoConstInterceptor implements ConstructorInterceptor {
    @Override
    public void onConstruct(Object obj, Object[] allArguments) {
        DemoLogger.println(obj + ": [DemoConstInterceptor]-onConstruct");
    }
}
