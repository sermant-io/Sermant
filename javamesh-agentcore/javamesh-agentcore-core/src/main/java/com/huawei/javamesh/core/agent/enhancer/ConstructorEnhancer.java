/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.javamesh.core.agent.enhancer;

import com.huawei.javamesh.core.agent.interceptor.ConstructorInterceptor;
import com.huawei.javamesh.core.common.LoggerFactory;
import com.huawei.javamesh.core.lubanops.bootstrap.Interceptor;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * 多Interceptor构造方法增强委派类
 */
public final class ConstructorEnhancer extends OriginEnhancer {

    private final static Logger LOGGER = LoggerFactory.getLogger();

    private final List<ConstructorInterceptor> interceptors;

    public ConstructorEnhancer(Interceptor originInterceptor, List<ConstructorInterceptor> interceptors) {
        super(originInterceptor);
        this.interceptors = Collections.unmodifiableList(interceptors);
    }

    /**
     * 增强委派方法
     *
     * @param obj       增强实例
     * @param arguments 原构造方法参数
     */
    @RuntimeType
    public void intercept(@This Object obj, @AllArguments Object[] arguments) {
        onFinally(obj, arguments, null, null);
        for (ConstructorInterceptor interceptor : interceptors) {
            try {
                interceptor.onConstruct(obj, arguments);
            } catch (Throwable t) {
                LOGGER.severe(String.format("An error occurred on construct [{%s}] in interceptor [{%s}]: [{%s}]",
                        obj.getClass().getName(), interceptor.getClass().getName(), t.getMessage()));
            }
        }
    }
}
