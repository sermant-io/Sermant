/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.stresstest.redis.lettuce;

import com.huawei.sermant.core.agent.definition.EnhanceDefinition;
import com.huawei.sermant.core.agent.definition.MethodInterceptPoint;
import com.huawei.sermant.core.agent.matcher.ClassMatcher;
import com.huawei.sermant.core.agent.matcher.ClassMatchers;

import net.bytebuddy.matcher.ElementMatchers;

/**
 * Jedis pool增强
 *
 * @author yiwei
 * @since 2021-11-03
 */
public class LettucePoolEnhance implements EnhanceDefinition {
    /**
     * ConnectionPoolSupport 方法 createGenericObjectPool
     */
    public static final String GENERIC_OBJECT_POOL_METHOD = "createGenericObjectPool";
    /**
     * ConnectionPoolSupport 方法 createSoftReferenceObjectPool
     */
    public static final String SOFT_OBJECT_POOL_METHOD = "createSoftReferenceObjectPool";
    private static final String ENHANCE_CLASS = "io.lettuce.core.support.ConnectionPoolSupport";
    private static final String INTERCEPT_CLASS = "com.huawei.sermant.stresstest.redis.lettuce.LettucePoolInterceptor";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.named(ENHANCE_CLASS);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[] {MethodInterceptPoint.newStaticMethodInterceptPoint(INTERCEPT_CLASS,
            ElementMatchers.namedOneOf(GENERIC_OBJECT_POOL_METHOD, SOFT_OBJECT_POOL_METHOD))};
    }
}
