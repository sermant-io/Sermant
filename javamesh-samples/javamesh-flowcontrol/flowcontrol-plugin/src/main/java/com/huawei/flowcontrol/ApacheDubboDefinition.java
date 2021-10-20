/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol;

/**
 * 拦截 org.apache.dubbo.monitor.support.MonitorFilter 的invoke方法
 *
 * @author liyi
 * @since 2020-08-26
 */
public class ApacheDubboDefinition extends DubboDefinition {
    private static final String ENHANCE_CLASS = "org.apache.dubbo.monitor.support.MonitorFilter";
    private static final String INTERCEPT_CLASS = "com.lubanops.apm.plugin.flowcontrol.ApacheDubboInterceptor";

    public ApacheDubboDefinition() {
        super(ENHANCE_CLASS, INTERCEPT_CLASS);
    }
}
