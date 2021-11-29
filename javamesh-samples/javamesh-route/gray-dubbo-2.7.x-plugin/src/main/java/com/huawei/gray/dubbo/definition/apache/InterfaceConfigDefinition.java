/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.gray.dubbo.definition.apache;

import com.huawei.gray.dubbo.definition.AbstractInstDefinition;

/**
 * 增强AbstractInterfaceConfig类的getApplication方法，用来获取应用名
 *
 * @author pengyuyi
 * @since 2021年11月8日
 */
public class InterfaceConfigDefinition extends AbstractInstDefinition {
    private static final String ENHANCE_CLASS = "org.apache.dubbo.config.AbstractInterfaceConfig";

    private static final String INTERCEPT_CLASS = "com.huawei.gray.dubbo.interceptor.apache.InterfaceConfigInterceptor";

    private static final String METHOD_NAME = "getApplication";

    public InterfaceConfigDefinition() {
        super(ENHANCE_CLASS, INTERCEPT_CLASS, METHOD_NAME);
    }
}