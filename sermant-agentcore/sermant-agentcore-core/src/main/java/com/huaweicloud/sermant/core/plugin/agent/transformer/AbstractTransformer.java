/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.core.plugin.agent.transformer;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.Interceptor;
import com.huaweicloud.sermant.core.plugin.agent.template.MethodKeyCreator;
import com.huaweicloud.sermant.core.plugin.agent.template.TemplateForCtor;
import com.huaweicloud.sermant.core.plugin.agent.template.TemplateForMember;
import com.huaweicloud.sermant.core.plugin.agent.template.TemplateForStatic;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.MethodDescription.InDefinedShape;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.utility.JavaModule;

import java.lang.reflect.InvocationTargetException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Abstract transformer
 *
 * @author luanwenfei
 * @since 2023-09-08
 */
public abstract class AbstractTransformer implements AgentBuilder.Transformer {
    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * intercept declarer set
     */
    private final InterceptDeclarer[] interceptDeclarers;

    /**
     * constructor
     *
     * @param interceptDeclarers intercept declarer set
     */
    public AbstractTransformer(InterceptDeclarer[] interceptDeclarers) {
        this.interceptDeclarers = interceptDeclarers;
    }

    @Override
    public Builder<?> transform(Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader,
            JavaModule javaModule, ProtectionDomain protectionDomain) {
        if (interceptDeclarers == null || interceptDeclarers.length == 0) {
            return builder;
        }
        return enhanceMethods(builder, typeDescription, classLoader);
    }

    /**
     * Check all methods defined by the class, and try to enhance，see{@link #enhanceMethod}. Native methods, abstract
     * methods, and methods defined by parent classes, will not be checked
     *
     * @param builder builder
     * @param typeDesc class definition
     * @param classLoader classloader of the enhanced class
     * @return DynamicType.Builder
     */
    private DynamicType.Builder<?> enhanceMethods(DynamicType.Builder<?> builder, TypeDescription typeDesc,
            ClassLoader classLoader) {
        final MethodList<InDefinedShape> declaredMethods = typeDesc.getDeclaredMethods();
        DynamicType.Builder<?> newBuilder = builder;
        for (MethodDescription.InDefinedShape methodDesc : declaredMethods) {
            if (methodDesc.isNative() || methodDesc.isAbstract()) {
                continue;
            }
            newBuilder = enhanceMethod(newBuilder, methodDesc, classLoader);
        }
        return newBuilder;
    }

    /**
     * Enhance a single method
     *
     * @param builder builder
     * @param methodDesc method definition
     * @param classLoader classloader of the enhanced class
     * @return DynamicType.Builder
     */
    private DynamicType.Builder<?> enhanceMethod(DynamicType.Builder<?> builder,
            MethodDescription.InDefinedShape methodDesc, ClassLoader classLoader) {
        final List<Interceptor> interceptors = getInterceptors(methodDesc, classLoader);
        if (interceptors.isEmpty()) {
            return builder;
        }
        try {
            if (methodDesc.isStatic()) {
                return resolve(builder, methodDesc, interceptors, TemplateForStatic.class, classLoader);
            } else if (methodDesc.isConstructor()) {
                return resolve(builder, methodDesc, interceptors, TemplateForCtor.class, classLoader);
            } else {
                return resolve(builder, methodDesc, interceptors, TemplateForMember.class, classLoader);
            }
        } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            LOGGER.warning(String.format(Locale.ROOT, "Enhance [%s] failed for [%s], caused by [%s]. ",
                    MethodKeyCreator.getMethodDescKey(methodDesc), e.getClass().getName(), e.getMessage()));
        }
        return builder;
    }

    /**
     * Gets interceptor list related to a single method
     *
     * @param methodDesc method definition
     * @param classLoader classLoader
     * @return interceptor list
     */
    private List<Interceptor> getInterceptors(MethodDescription.InDefinedShape methodDesc, ClassLoader classLoader) {
        final List<Interceptor> interceptors = new ArrayList<>();
        for (InterceptDeclarer declarer : interceptDeclarers) {
            if (!declarer.getMethodMatcher().matches(methodDesc)) {
                continue;
            }
            if (classLoader == null) {
                interceptors.addAll(Arrays.asList(declarer.getInterceptors(ClassLoader.getSystemClassLoader())));
            } else {
                interceptors.addAll(Arrays.asList(declarer.getInterceptors(classLoader)));
            }
        }
        return interceptors;
    }

    /**
     * Process method enhancement
     * <pre>
     *     1.Create an enhancement Adviser based on the template class
     *     2.Define the Adviser using the classloader of the enhanced class
     *     3.Add interceptors to the Adviser
     *     4.Define the enhancement logic in the builder
     * </pre>
     *
     * @param builder builder
     * @param methodDesc method definition
     * @param interceptors interceptor list
     * @param templateCls template class
     * @param classLoader classLoader
     * @return DynamicType.Builder
     * @throws InvocationTargetException invoke method error
     * @throws IllegalAccessException Unable to access a filed or method, normally will not be thrown
     * @throws NoSuchMethodException Unable to find a method, normally will not be thrown
     * @throws NoSuchFieldException Filed not found
     */
    abstract DynamicType.Builder<?> resolve(DynamicType.Builder<?> builder, MethodDescription.InDefinedShape methodDesc,
            List<Interceptor> interceptors, Class<?> templateCls, ClassLoader classLoader)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, NoSuchFieldException;

    /**
     * The format of the AdviceKey is [standard class name of template class_hash value of meta information of the
     * enhanced method_classloader of the enhanced class]. Meta information of the enhanced method，see{@link
     * MethodKeyCreator#getMethodDescKey}
     *
     * @param templateCls template class
     * @param classLoader classloader of the enhanced class
     * @param methodDesc method definition
     * @return adviceKey
     */
    protected String getAdviceKey(Class<?> templateCls, ClassLoader classLoader,
            MethodDescription.InDefinedShape methodDesc) {
        return templateCls.getSimpleName() + "_" + Integer.toHexString(
                MethodKeyCreator.getMethodDescKey(methodDesc).hashCode()) + "_" + classLoader;
    }
}
