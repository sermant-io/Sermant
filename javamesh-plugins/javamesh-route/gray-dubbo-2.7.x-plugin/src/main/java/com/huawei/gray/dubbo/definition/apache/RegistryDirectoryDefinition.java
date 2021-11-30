/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.gray.dubbo.definition.apache;

import com.huawei.gray.dubbo.definition.AbstractInstDefinition;

/**
 * 增强RegistryDirectory类的notify方法，获取应用缓存的路由信息
 *
 * @author pengyuyi
 * @since 2021年6月28日
 */
public class RegistryDirectoryDefinition extends AbstractInstDefinition {
    private static final String ENHANCE_CLASS = "org.apache.dubbo.registry.integration.RegistryDirectory";

    private static final String INTERCEPT_CLASS
            = "com.huawei.gray.dubbo.interceptor.apache.RegistryDirectoryInterceptor";

    private static final String METHOD_NAME = "notify";

    public RegistryDirectoryDefinition() {
        super(ENHANCE_CLASS, INTERCEPT_CLASS, METHOD_NAME);
    }
}