/*
 *   Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.tag.transmission.alibabadubbo.declarers;

import io.sermant.core.plugin.agent.declarer.AbstractPluginDeclarer;
import io.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import io.sermant.core.plugin.agent.matcher.ClassMatcher;
import io.sermant.core.plugin.agent.matcher.MethodMatcher;
import io.sermant.tag.transmission.alibabadubbo.interceptors.AlibabaDubboConsumerInterceptor;

/**
 * Enhanced Declarer on the consumer side of dubbo traffic tag transparent transmission,
 * supporting alibaba dubbo version 2.6.x
 *
 * @author daizhenyu
 * @since 2023-08-12
 **/
public class AlibabaDubboConsumerDeclarer extends AbstractPluginDeclarer {
    private static final String ENHANCE_CLASS_ALIBABA_DUBBO = "com.alibaba.dubbo.rpc.filter.ConsumerContextFilter";

    private static final String METHOD_NAME = "invoke";

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameEquals(ENHANCE_CLASS_ALIBABA_DUBBO);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
                InterceptDeclarer.build(MethodMatcher.nameEquals(METHOD_NAME),
                        new AlibabaDubboConsumerInterceptor())
        };
    }
}
