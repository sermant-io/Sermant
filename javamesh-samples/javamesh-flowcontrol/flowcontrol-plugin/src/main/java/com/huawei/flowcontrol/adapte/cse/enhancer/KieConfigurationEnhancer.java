/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adapte.cse.enhancer;

import com.huawei.apm.core.agent.definition.EnhanceDefinition;
import com.huawei.apm.core.agent.definition.MethodInterceptPoint;
import com.huawei.apm.core.agent.matcher.ClassMatcher;
import com.huawei.apm.core.agent.matcher.ClassMatchers;
import com.huawei.flowcontrol.adapte.cse.constants.CseConstants;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * org.apache.servicecomb.config.kie.client.model.KieConfiguration增强
 * 获取环境与自定义标签
 *
 * @author zhouss
 * @since 2021-11-18
 */
public class KieConfigurationEnhancer implements EnhanceDefinition {
    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.named("org.apache.servicecomb.config.kie.client.model.KieConfiguration");
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[] {
                MethodInterceptPoint.newInstMethodInterceptPoint(
                        "com.huawei.flowcontrol.adapte.cse.interceptors.KieConfigurationInterceptor",
                        ElementMatchers.<MethodDescription>namedOneOf(
                                CseConstants.SERVICE_NAME_METHOD,
                                CseConstants.APP_NAME_METHOD,
                                CseConstants.ENVIRONMENT_METHOD,
                                CseConstants.PROJECT_METHOD,
                                CseConstants.CUSTOM_LABEL_METHOD,
                                CseConstants.CUSTOM_LABEL_VALUE_METHOD))
        };
    }
}
