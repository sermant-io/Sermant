/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.gray.dubbo.definition.apache;

import com.huawei.gray.dubbo.definition.AbstractInstDefinition;

/**
 * 增强DubboInvoker类的doInvoke方法，更改路由信息
 *
 * @author pengyuyi
 * @since 2021年6月28日
 */
public class DubboInvokerDefinition extends AbstractInstDefinition {
    private static final String ENHANCE_CLASS = "org.apache.dubbo.rpc.protocol.dubbo.DubboInvoker";

    private static final String INTERCEPT_CLASS = "com.huawei.gray.dubbo.interceptor.apache.DubboInvokerInterceptor";

    private static final String METHOD_NAME = "doInvoke";

    public DubboInvokerDefinition() {
        super(ENHANCE_CLASS, INTERCEPT_CLASS, METHOD_NAME);
    }
}
