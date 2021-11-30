/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.gray.dubbo.definition.servicecomb;

import com.huawei.gray.dubbo.definition.AbstractInstDefinition;

/**
 * 增强RegistrationListener类的notify方法
 *
 * @author pengyuyi
 * @since 2021年11月8日
 */
public class RegistrationDefinition extends AbstractInstDefinition {
    private static final String ENHANCE_CLASS = "com.huaweicloud.dubbo.discovery.RegistrationListener";

    private static final String INTERCEPT_CLASS
            = "com.huawei.gray.dubbo.interceptor.servicecomb.RegistrationInterceptor";

    private static final String METHOD_NAME = "notify";

    public RegistrationDefinition() {
        super(ENHANCE_CLASS, INTERCEPT_CLASS, METHOD_NAME);
    }
}
