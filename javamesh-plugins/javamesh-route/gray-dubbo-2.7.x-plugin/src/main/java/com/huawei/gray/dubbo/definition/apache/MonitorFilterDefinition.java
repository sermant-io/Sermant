/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.gray.dubbo.definition.apache;

import com.huawei.gray.dubbo.definition.AbstractInstDefinition;

/**
 * 增强MonitorFilter类的invoke方法，更改路由信息
 *
 * @author pengyuyi
 * @since 2021年6月28日
 */
public class MonitorFilterDefinition extends AbstractInstDefinition {
    private static final String ENHANCE_CLASS = "org.apache.dubbo.monitor.support.MonitorFilter";

    private static final String INTERCEPT_CLASS = "com.huawei.gray.dubbo.interceptor.apache.MonitorFilterInterceptor";

    private static final String METHOD_NAME = "invoke";

    public MonitorFilterDefinition() {
        super(ENHANCE_CLASS, INTERCEPT_CLASS, METHOD_NAME);
    }
}
