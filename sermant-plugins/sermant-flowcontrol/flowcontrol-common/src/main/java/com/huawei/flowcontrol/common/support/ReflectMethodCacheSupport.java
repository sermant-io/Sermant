/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.flowcontrol.common.support;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 提供反射方法缓存
 *
 * @author zhouss
 * @since 2022-02-10
 */
public class ReflectMethodCacheSupport {
    /**
     * 用于缓存反射获取的方法
     */
    private final Map<String, Method> cacheMethods = new ConcurrentHashMap<>();

    protected final Method getInvokerMethod(String methodName,
            Function<? super String, ? extends Method> mappingFunction) {
        if (methodName == null || mappingFunction == null) {
            return null;
        }
        return cacheMethods.computeIfAbsent(methodName, mappingFunction);
    }
}
