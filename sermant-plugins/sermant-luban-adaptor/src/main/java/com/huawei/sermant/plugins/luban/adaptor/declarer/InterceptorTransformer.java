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

package com.huawei.sermant.plugins.luban.adaptor.declarer;

import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huawei.sermant.core.plugin.agent.interceptor.Interceptor;

import java.util.logging.Logger;

/**
 * 拦截器转换器，提供将luban拦截器转换为sermant拦截器的方法
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-26
 */
public class InterceptorTransformer {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private InterceptorTransformer() {
    }

    /**
     * 创建sermant的拦截器
     *
     * @param interceptor luban拦截器名
     * @param classLoader 加载luban插件的类加载器
     * @return sermant的拦截器
     */
    public static Interceptor createInterceptor(String interceptor, ClassLoader classLoader) {
        try {
            return interceptorTransform((com.lubanops.apm.bootstrap.Interceptor)
                    classLoader.loadClass(interceptor).newInstance());
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException ignored) {
            LOGGER.warning("Create Luban interceptor failed. ");
        }
        return null;
    }

    /**
     * 将luban的拦截器转换为sermant的拦截器
     *
     * @param interceptor luban的拦截器
     * @return sermant的拦截器
     */
    private static Interceptor interceptorTransform(final com.lubanops.apm.bootstrap.Interceptor interceptor) {
        return new Interceptor() {
            @Override
            public ExecuteContext before(ExecuteContext context) {
                return context.changeArgs(interceptor.onStart(context.getObject(), context.getArguments(),
                        context.getRawCls().getName(), context.getMethod().getName()));
            }

            @Override
            public ExecuteContext after(ExecuteContext context) {
                interceptor.onFinally(context.getObject(), context.getArguments(), context.getResult(),
                        context.getRawCls().getName(), context.getMethod().getName());
                return context;
            }

            @Override
            public ExecuteContext onThrow(ExecuteContext context) {
                interceptor.onError(context.getObject(), context.getArguments(), context.getThrowable(),
                        context.getRawCls().getName(), context.getMethod().getName());
                return context;
            }
        };
    }
}
