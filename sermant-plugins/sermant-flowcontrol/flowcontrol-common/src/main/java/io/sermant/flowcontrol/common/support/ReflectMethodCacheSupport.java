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

package io.sermant.flowcontrol.common.support;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * provides a reflection method cache
 *
 * @author zhouss
 * @since 2022-02-10
 */
public class ReflectMethodCacheSupport {
    /**
     * placeholder method
     */
    protected final Method placeHolderMethod = null;

    /**
     * method for caching reflection acquisition
     */
    private final Map<String, Method> cacheMethods = new ConcurrentHashMap<>();

    /**
     * get call method
     *
     * @param methodName method name
     * @param mappingFunction how to construct a method
     * @return method
     */
    protected final Optional<Method> getInvokerMethod(String methodName,
            Function<? super String, ? extends Method> mappingFunction) {
        if (methodName == null) {
            return Optional.empty();
        }
        if (mappingFunction == null) {
            return Optional.ofNullable(cacheMethods.get(methodName));
        }
        return Optional.ofNullable(cacheMethods.computeIfAbsent(methodName, mappingFunction));
    }
}
