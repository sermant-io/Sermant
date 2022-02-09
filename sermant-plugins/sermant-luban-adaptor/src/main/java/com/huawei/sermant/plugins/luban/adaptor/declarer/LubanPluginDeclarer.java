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

import com.huawei.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huawei.sermant.core.plugin.agent.declarer.PluginDeclarer;
import com.huawei.sermant.core.plugin.agent.declarer.SuperTypeDeclarer;
import com.huawei.sermant.core.plugin.agent.interceptor.Interceptor;
import com.huawei.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huawei.sermant.core.plugin.agent.matcher.MethodMatcher;
import com.huawei.sermant.plugins.luban.adaptor.collector.AttributeAccessImplTemplate;
import com.huawei.sermant.plugins.luban.adaptor.collector.BufferedTransformAccess;
import com.huawei.sermant.plugins.luban.adaptor.matcher.GetterSetterMatcher;

import com.lubanops.apm.bootstrap.AttributeAccess;
import com.lubanops.apm.bootstrap.Listener;
import com.lubanops.apm.bootstrap.TransformerMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * luban插件声明器，主要功能为将luban的监听器转换为插件声明器
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-26
 */
public class LubanPluginDeclarer implements PluginDeclarer {
    /**
     * luban监听器
     */
    private final Listener listener;

    /**
     * 加载luban插件的类加载器
     */
    private final ClassLoader pluginClassLoader;

    public LubanPluginDeclarer(Listener listener, ClassLoader pluginClassLoader) {
        this.listener = listener;
        this.pluginClassLoader = pluginClassLoader;
    }

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameContains(listener.getClasses());
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        final List<InterceptDeclarer> declarers = new ArrayList<>();
        for (final TransformerMethod transformerMethod : listener.getTransformerMethod()) {
            declarers.add(transformerMethodToInterceptDeclarer(transformerMethod));
        }
        return declarers.toArray(new InterceptDeclarer[0]);
    }

    @Override
    public SuperTypeDeclarer[] getSuperTypeDeclarers() {
        final List<SuperTypeDeclarer> declarers = new ArrayList<>();
        if (listener.hasAttribute()) {
            declarers.add(SuperTypeDeclarer.ForBeanProperty.build(BufferedTransformAccess.class));
        }
        final List<String> fields = listener.getFields();
        if (fields != null && !fields.isEmpty()) {
            declarers.add(SuperTypeDeclarer.ForImplInstance.build(AttributeAccess.class,
                    new AttributeAccessImplTemplate(fields)));
        }
        return declarers.toArray(new SuperTypeDeclarer[0]);
    }

    /**
     * 将luban的转换器方法转换为拦截声明器
     *
     * @param transformerMethod 转换器方法
     * @return 拦截声明器
     */
    private InterceptDeclarer transformerMethodToInterceptDeclarer(final TransformerMethod transformerMethod) {
        return new InterceptDeclarer() {
            @Override
            public MethodMatcher getMethodMatcher() {
                return transformerMethodToMethodMatcher(transformerMethod);
            }

            @Override
            public Interceptor[] getInterceptors(ClassLoader classLoader) {
                final Interceptor interceptor =
                        InterceptorTransformer.createInterceptor(transformerMethod.getInterceptor(), pluginClassLoader);
                if (interceptor == null) {
                    return new Interceptor[0];
                } else {
                    return new Interceptor[]{interceptor};
                }
            }
        };
    }

    /**
     * 将luban的转换器方法转换为方法匹配器
     *
     * @param transformerMethod 转换器方法
     * @return 方法匹配器
     */
    private MethodMatcher transformerMethodToMethodMatcher(TransformerMethod transformerMethod) {
        MethodMatcher methodMatcher;
        if (transformerMethod.isConstructor()) {
            methodMatcher = MethodMatcher.isConstructor();
        } else {
            methodMatcher = MethodMatcher.isConstructor().not();
        }
        if (!transformerMethod.isInterceptorGetAndSet()) {
            methodMatcher = methodMatcher.and(new GetterSetterMatcher().not());
        }
        final Set<String> excludeMethods = transformerMethod.getExcludeMethods();
        if (excludeMethods == null || excludeMethods.isEmpty()) {
            methodMatcher = methodMatcher.and(MethodMatcher.nameEquals(transformerMethod.getMethod()));
        } else {
            methodMatcher = methodMatcher.and(MethodMatcher.nameContains(excludeMethods).not());
        }
        final List<String> params = transformerMethod.getParams();
        if (params != null && !params.isEmpty()) {
            methodMatcher = methodMatcher.and(MethodMatcher.paramTypesEqual(params.toArray(new String[0])));
        }
        return methodMatcher;
    }
}
