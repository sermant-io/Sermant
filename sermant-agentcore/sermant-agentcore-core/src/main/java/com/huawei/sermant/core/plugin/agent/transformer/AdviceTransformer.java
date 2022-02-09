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

package com.huawei.sermant.core.plugin.agent.transformer;

import com.huawei.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huawei.sermant.core.plugin.agent.declarer.SuperTypeDeclarer;
import com.huawei.sermant.core.plugin.agent.interceptor.Interceptor;
import com.huawei.sermant.core.plugin.agent.template.AdviceConstTemplate;
import com.huawei.sermant.core.plugin.agent.template.AdviceMemberTemplate;
import com.huawei.sermant.core.plugin.agent.template.AdviceStaticTemplate;
import com.huawei.sermant.core.plugin.agent.template.MethodKeyCreator;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.modifier.Ownership;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.LoadedTypeInitializer;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 增强普通类的Transformer，advice风格
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-24
 */
public class AdviceTransformer implements AgentBuilder.Transformer {
    /**
     * 超类校验集
     */
    private static final Map<Integer, Set<Class<?>>> SUPERTYPE_VERIFY_MAP = new HashMap<>();

    /**
     * 拦截器全局集
     */
    private static final Map<Integer, Map<String, List<Interceptor>>> INTERCEPTOR_GLOBAL_MAP = new HashMap<>();

    /**
     * 拦截声明器数组
     */
    private final InterceptDeclarer[] interceptDeclarers;

    /**
     * 超类生命器数组
     */
    private final SuperTypeDeclarer[] superTypeDeclarers;

    public AdviceTransformer(InterceptDeclarer[] interceptDeclarers, SuperTypeDeclarer[] superTypeDeclarers) {
        this.interceptDeclarers = interceptDeclarers;
        this.superTypeDeclarers = superTypeDeclarers;
    }

    @Override
    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDesc,
            ClassLoader classLoader, JavaModule module) {
        final int verifiedKey = Objects.hash(classLoader, typeDesc.getActualName());
        DynamicType.Builder<?> newBuilder = builder;
        newBuilder = extendsFromSuperTypes(newBuilder, typeDesc, verifiedKey);
        newBuilder = enhanceMethods(newBuilder, typeDesc, classLoader, verifiedKey);
        return newBuilder;
    }

    /**
     * 让被增强类实现超类接口集
     * <p>注意，指定超类本身为被增强类超类时，不实现；若通过超类尝试多次被实现时，仅第一次生效
     *
     * @param builder     构建器
     * @param typeDesc    类定义
     * @param verifiedKey 校验键
     * @return 构建器
     */
    private DynamicType.Builder<?> extendsFromSuperTypes(DynamicType.Builder<?> builder, TypeDescription typeDesc,
            int verifiedKey) {
        if (superTypeDeclarers == null || superTypeDeclarers.length <= 0) {
            return builder;
        }
        Set<Class<?>> superTypeSet = SUPERTYPE_VERIFY_MAP.get(verifiedKey);
        if (superTypeSet == null) {
            superTypeSet = new HashSet<>();
            SUPERTYPE_VERIFY_MAP.put(verifiedKey, superTypeSet);
        }
        DynamicType.Builder<?> newBuilder = builder;
        for (SuperTypeDeclarer superTypeDeclarer : superTypeDeclarers) {
            final Class<?> superType = superTypeDeclarer.getSuperType();
            if (typeDesc.isAssignableTo(superType) || superTypeSet.contains(superType)) {
                continue;
            }
            newBuilder = superTypeDeclarer.resolve(superType, newBuilder);
            superTypeSet.add(superType);
        }
        return newBuilder;
    }

    /**
     * 检查类定义的所有方法，并尝试增强：
     * <pre>
     *     1.初次增强时，添加必要参数，见{@link #defineEssentialFields}
     *     2.遍历所有定义的方法，并尝试增强，见{@link #enhanceMethod}
     * </pre>
     * 注意，native方法，抽象方法，及父类定义的方法不会被检查
     *
     * @param builder     构建器
     * @param typeDesc    类定义
     * @param classLoader 被增强类的类加载器
     * @param verifiedKey 校验键
     * @return 构建器
     */
    private DynamicType.Builder<?> enhanceMethods(DynamicType.Builder<?> builder, TypeDescription typeDesc,
            ClassLoader classLoader, int verifiedKey) {
        if (interceptDeclarers == null || interceptDeclarers.length <= 0) {
            return builder;
        }
        DynamicType.Builder<?> newBuilder = builder;
        Map<String, List<Interceptor>> interceptorMap = INTERCEPTOR_GLOBAL_MAP.get(verifiedKey);
        if (interceptorMap == null) {
            interceptorMap = new HashMap<>();
            INTERCEPTOR_GLOBAL_MAP.put(verifiedKey, interceptorMap);
            newBuilder = defineEssentialFields(newBuilder, interceptorMap);
        }
        for (MethodDescription.InDefinedShape methodDesc : typeDesc.getDeclaredMethods()) {
            if (methodDesc.isNative() || methodDesc.isAbstract()) {
                continue;
            }
            newBuilder = enhanceMethod(newBuilder, methodDesc, classLoader, interceptorMap);
        }
        return newBuilder;
    }

    /**
     * 为被增强类添加必要参数：
     * <pre>
     *     1.用于存放拦截器的集合
     *     2.用于存放额外静态属性的集合
     *     3.用于存放额外成员属性的集合
     * </pre>
     *
     * @param builder        构建器
     * @param interceptorMap 拦截器集合
     * @return 构建器
     */
    private DynamicType.Builder<?> defineEssentialFields(DynamicType.Builder<?> builder,
            Map<String, List<Interceptor>> interceptorMap) {
        return builder.defineField("_INTERCEPTOR_MAP_$SERMANT", Map.class, Visibility.PRIVATE, Ownership.STATIC)
                .initializer(new LoadedTypeInitializer.ForStaticField("_INTERCEPTOR_MAP_$SERMANT", interceptorMap))
                .defineField("_EXT_STATIC_FIELDS_$SERMANT", Map.class, Visibility.PRIVATE, Ownership.STATIC)
                .defineField("_EXT_MEMBER_FIELDS_$SERMANT", Map.class, Visibility.PRIVATE, Ownership.MEMBER);
    }

    /**
     * 对单个方法进行增强
     *
     * @param builder        构建器
     * @param methodDesc     方法定义
     * @param classLoader    加载被增强类的类加载器
     * @param interceptorMap 拦截器集合
     * @return 构建器
     */
    private DynamicType.Builder<?> enhanceMethod(DynamicType.Builder<?> builder,
            MethodDescription.InDefinedShape methodDesc, ClassLoader classLoader,
            Map<String, List<Interceptor>> interceptorMap) {
        final List<Interceptor> declaredInterceptors = getInterceptors(methodDesc, classLoader);
        if (declaredInterceptors.isEmpty()) {
            return builder;
        }
        final String methodKey = MethodKeyCreator.getMethodDescKey(methodDesc);
        final List<Interceptor> interceptors = interceptorMap.get(methodKey);
        DynamicType.Builder<?> newBuilder = builder;
        if (interceptors == null) {
            if (methodDesc.isStatic()) {
                newBuilder = newBuilder.visit(Advice.to(AdviceStaticTemplate.class).on(ElementMatchers.is(methodDesc)));
            } else if (methodDesc.isConstructor()) {
                newBuilder = newBuilder.visit(Advice.to(AdviceConstTemplate.class).on(ElementMatchers.is(methodDesc)));
            } else {
                newBuilder = newBuilder.visit(Advice.to(AdviceMemberTemplate.class).on(ElementMatchers.is(methodDesc)));
            }
            interceptorMap.put(methodKey, declaredInterceptors);
        } else {
            interceptors.addAll(declaredInterceptors);
        }
        return newBuilder;
    }

    /**
     * 从拦截声明器中获取所有符合条件的拦截器
     *
     * @param methodDesc  方法定义
     * @param classLoader 被增强类的类加载器
     * @return 拦截器集合
     */
    private List<Interceptor> getInterceptors(MethodDescription.InDefinedShape methodDesc, ClassLoader classLoader) {
        final List<Interceptor> interceptors = new ArrayList<>();
        for (InterceptDeclarer declarer : interceptDeclarers) {
            if (!declarer.getMethodMatcher().matches(methodDesc)) {
                continue;
            }
            interceptors.addAll(Arrays.asList(declarer.getInterceptors(classLoader)));
        }
        return interceptors;
    }
}
