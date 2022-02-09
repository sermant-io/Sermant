/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Based on org/apache/skywalking/apm/agent/core/plugin/interceptor/enhance/ConstructorInter.java
 * from the Apache Skywalking project.
 */

package com.huawei.sermant.core.agent.enhancer;

import com.huawei.sermant.core.agent.annotations.AboutDelete;
import com.huawei.sermant.core.agent.interceptor.ConstructorInterceptor;
import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.lubanops.bootstrap.Interceptor;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * 多Interceptor构造方法增强委派类
 * <p> Copyright 2021
 *
 * @since 2021
 */
@AboutDelete
@Deprecated
public final class ConstructorEnhancer extends OriginEnhancer {

    @SuppressWarnings("checkstyle:ModifierOrder")
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
    @SuppressWarnings("checkstyle:IllegalCatch")
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
