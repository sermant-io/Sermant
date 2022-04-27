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

package com.huawei.lubanops.apm.plugin.threadlocal;

import static com.huawei.lubanops.apm.plugin.common.Constant.INTERCEPT_CLASS;

import com.huaweicloud.sermant.core.agent.definition.EnhanceDefinition;
import com.huaweicloud.sermant.core.agent.definition.MethodInterceptPoint;
import com.huaweicloud.sermant.core.agent.matcher.ClassMatcher;
import com.huaweicloud.sermant.core.agent.matcher.ClassMatchers;
import com.huaweicloud.sermant.dependencies.net.bytebuddy.matcher.ElementMatchers;

/**
 * ScheduledThreadpool 增强
 *
 * @author yiwei
 * @since 2021-10-21
 */
public class ScheduledThreadPoolEnhance implements EnhanceDefinition {
    /**
     * 增强类的全限定名
     */
    private static final String ENHANCE_CLASS = "java.util.concurrent.ScheduledThreadPoolExecutor";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.named(ENHANCE_CLASS);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{MethodInterceptPoint.newInstMethodInterceptPoint(INTERCEPT_CLASS,
                ElementMatchers.namedOneOf("schedule", "scheduleAtFixedRate","scheduleWithFixedDelay",
                        "submit", "execute"))};
    }
}
