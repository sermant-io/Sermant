/*
 * Copyright (C) 2021-2024 Sermant Authors. All rights reserved.
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

package com.huawei.flowcontrol;

import com.huaweicloud.sermant.core.plugin.agent.declarer.AbstractPluginDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;

/**
 * Enhanced the mergeUrl method of the ClusterUtils class. The interception point is used to obtain the relationship
 * between the downstream dubbo interface and the downstream service name
 *
 * @author provenceee
 * @since 2021-06-28
 */
public class ClusterDeclarer extends AbstractPluginDeclarer {
    private static final String[] ENHANCE_CLASS = {"org.apache.dubbo.rpc.cluster.support.ClusterUtils",
            "com.alibaba.dubbo.rpc.cluster.support.ClusterUtils"};

    private static final String INTERCEPT_CLASS = ClusterInterceptor.class.getCanonicalName();

    private static final String METHOD_NAME = "mergeUrl";

    private static final int PARAMS_LEN = 2;

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameContains(ENHANCE_CLASS);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
                InterceptDeclarer.build(MethodMatcher.nameEquals(METHOD_NAME)
                                .and(MethodMatcher.isStaticMethod()
                                        .and(MethodMatcher.paramCountEquals(PARAMS_LEN))),
                        INTERCEPT_CLASS)
        };
    }
}
