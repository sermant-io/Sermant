/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.flowcontrol.adapte.cse.enhancer;

import com.huawei.sermant.core.agent.definition.EnhanceDefinition;
import com.huawei.sermant.core.agent.definition.MethodInterceptPoint;
import com.huawei.sermant.core.agent.matcher.ClassMatcher;
import com.huawei.sermant.core.agent.matcher.ClassMatchers;
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
