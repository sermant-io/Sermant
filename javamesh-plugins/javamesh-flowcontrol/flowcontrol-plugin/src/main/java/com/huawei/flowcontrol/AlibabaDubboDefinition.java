/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol;

/**
 * 拦截点
 * 拦截 alibaba MonitorFilter invoke
 *
 * @author liyi
 * @since 2020-08-26
 */
public class AlibabaDubboDefinition extends DubboDefinition {
    /**
     * 增强类的全限定名
     */
    private static final String ENHANCE_CLASS = "com.alibaba.dubbo.monitor.support.MonitorFilter";

    /**
     * 拦截类的全限定名
     */
    private static final String INTERCEPT_CLASS = "com.huawei.flowcontrol.AlibabaDubboInterceptor";

    public AlibabaDubboDefinition() {
        super(ENHANCE_CLASS, INTERCEPT_CLASS);
    }
}
