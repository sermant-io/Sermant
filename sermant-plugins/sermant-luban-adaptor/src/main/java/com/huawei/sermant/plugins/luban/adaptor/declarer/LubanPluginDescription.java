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
import com.huawei.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huawei.sermant.core.plugin.agent.declarer.PluginDescription;
import com.huawei.sermant.core.plugin.agent.declarer.SuperTypeDeclarer;
import com.huawei.sermant.core.plugin.agent.interceptor.Interceptor;
import com.huawei.sermant.core.plugin.agent.matcher.MethodMatcher;
import com.huawei.sermant.core.plugin.agent.transformer.AdviceTransformer;
import com.huawei.sermant.core.plugin.agent.transformer.BootstrapTransformer;
import com.huawei.sermant.plugins.luban.adaptor.collector.BufferedTransformAccess;
import com.huawei.sermant.plugins.luban.adaptor.matcher.GetterSetterMatcher;

import com.lubanops.apm.bootstrap.NoneNamedListener;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.io.IOException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * luban插件描述器，主要功能为将luban的非命名监听器转换为插件描述器
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-26
 */
public class LubanPluginDescription implements PluginDescription {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * luban非命名的监听器
     */
    private final NoneNamedListener noneNamedListener;

    /**
     * 加载luban插件的类加载器
     */
    private final ClassLoader pluginClassLoader;

    /**
     * 匹配成功的方法样式集，现为方法名集
     */
    private List<String> methodPatterns;

    public LubanPluginDescription(NoneNamedListener noneNamedListener, ClassLoader pluginClassLoader) {
        this.noneNamedListener = noneNamedListener;
        this.pluginClassLoader = pluginClassLoader;
    }

    @Override
    public boolean matches(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module,
            Class<?> classBeingRedefined, ProtectionDomain protectionDomain) {
        try {
            final byte[] classfileBuffer = ClassFileLocator.ForClassLoader.of(classLoader)
                    .locate(typeDescription.getActualName()).resolve();
            final List<String> matchedMethods =
                    noneNamedListener.matchClass(typeDescription.getActualName(), classfileBuffer);
            if (matchedMethods != null) {
                this.methodPatterns = matchedMethods;
                return true;
            }
        } catch (IOException ignored) {
            LOGGER.warning("Unexpected exception occurs. ");
        }
        return false;
    }

    @Override
    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription,
            ClassLoader classLoader, JavaModule module) {
        final Interceptor interceptor =
                InterceptorTransformer.createInterceptor(noneNamedListener.getInterceptor(), pluginClassLoader);
        final List<InterceptDeclarer> interceptDeclarers = new ArrayList<>();
        for (MethodDescription.InDefinedShape methodDesc : typeDescription.getDeclaredMethods()) {
            if (methodDesc.isNative() || methodDesc.isAbstract() || methodDesc.isBridge()
                    || MethodMatcher.isDeclaredByObject().matches(methodDesc)) {
                continue;
            }
            if (new GetterSetterMatcher().matches(methodDesc)) {
                continue;
            }
            if (!methodPatterns.contains(methodDesc.getActualName())) {
                continue;
            }
            interceptDeclarers.add(InterceptDeclarer.build(
                    MethodMatcher.build(ElementMatchers.is(methodDesc)), interceptor));
        }
        if (classLoader == null) {
            return new BootstrapTransformer(interceptDeclarers.toArray(new InterceptDeclarer[0]))
                    .transform(builder, typeDescription, null, module);
        } else {
            final List<SuperTypeDeclarer> superTypeDeclarers;
            if (noneNamedListener.hasAttribute()) {
                superTypeDeclarers = Collections.singletonList(
                        SuperTypeDeclarer.ForBeanProperty.build(BufferedTransformAccess.class));
            } else {
                superTypeDeclarers = Collections.emptyList();
            }
            return new AdviceTransformer(
                    interceptDeclarers.toArray(new InterceptDeclarer[0]),
                    superTypeDeclarers.toArray(new SuperTypeDeclarer[0])
            ).transform(builder, typeDescription, classLoader, module);
        }
    }
}
