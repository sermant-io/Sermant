/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.gray.dubbo.export;

import com.huawei.apm.bootstrap.definition.EnhanceDefinition;
import com.huawei.apm.bootstrap.definition.MethodInterceptPoint;
import com.huawei.apm.bootstrap.matcher.ClassMatcher;
import com.huawei.apm.bootstrap.matcher.ClassMatchers;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * 增强ServiceConfig类的export方法，用来获取消费者对外暴露的接口
 *
 * @author l30008180
 * @since 2021年7月7日
 */
public class ApacheDubboExportEnhanceDefinition implements EnhanceDefinition {
    private static final String EXPORT_ENHANCE_CLASS = "org.apache.dubbo.config.ServiceConfig";

    private static final String EXPORT_INTERCEPT_CLASS
            = "com.huawei.gray.dubbo.export.ApacheDubboExportInstanceMethodInterceptor";

    private static final String ENHANCE_METHOD_NAME = "export";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.named(EXPORT_ENHANCE_CLASS);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{
                MethodInterceptPoint.newInstMethodInterceptPoint(EXPORT_INTERCEPT_CLASS,
                        ElementMatchers.<MethodDescription>named(ENHANCE_METHOD_NAME))};
    }

}
