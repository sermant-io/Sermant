/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huawei.sermant.core.plugin.agent.interceptor.Interceptor;
import com.huawei.sermant.core.plugin.agent.template.BootstrapConstTemplate;
import com.huawei.sermant.core.plugin.agent.template.BootstrapMemberTemplate;
import com.huawei.sermant.core.plugin.agent.template.BootstrapStaticTemplate;
import com.huawei.sermant.core.plugin.agent.template.MethodKeyCreator;
import com.huawei.sermant.core.utils.ClassLoaderUtils;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.modifier.Ownership;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 启动类加载器加载类的Transformer，advice风格
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-10-27
 */
public class BootstrapTransformer implements AgentBuilder.Transformer {
    /**
     * bootstrap模板的字段名称
     */
    public static final String INTERCEPTORS_FIELD_NAME = "_INTERCEPTORS_$SERMANT";

    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 拦截器全局集
     */
    private static final Map<String, List<Interceptor>> INTERCEPTOR_GLOBAL_MAP = new HashMap<>();

    /**
     * 拦截定义数组
     */
    private final InterceptDeclarer[] interceptDeclarers;

    public BootstrapTransformer(InterceptDeclarer[] interceptDeclarers) {
        this.interceptDeclarers = interceptDeclarers;
    }

    @Override
    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDesc,
            ClassLoader classLoader, JavaModule module) {
        if (interceptDeclarers == null || interceptDeclarers.length <= 0) {
            return builder;
        }
        return enhanceMethods(builder, typeDesc, ClassLoader.getSystemClassLoader());
    }

    /**
     * 检查类定义的所有方法，并尝试增强，见{@link #enhanceMethod}
     * <p>注意，native方法，抽象方法，及父类定义的方法不会被检查
     *
     * @param builder     构建器
     * @param typeDesc    类定义
     * @param classLoader 加载被增强类的类加载器
     * @return 构建器
     */
    private DynamicType.Builder<?> enhanceMethods(DynamicType.Builder<?> builder, TypeDescription typeDesc,
            ClassLoader classLoader) {
        final MethodList<MethodDescription.InDefinedShape> declaredMethods = typeDesc.getDeclaredMethods();
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
     * 对单个方法进行增强
     *
     * @param builder     构建器
     * @param methodDesc  方法定义
     * @param classLoader 加载被增强类的类加载器
     * @return 构建器
     */
    private DynamicType.Builder<?> enhanceMethod(DynamicType.Builder<?> builder,
            MethodDescription.InDefinedShape methodDesc, ClassLoader classLoader) {
        final List<Interceptor> interceptors = getInterceptors(methodDesc, classLoader);
        if (interceptors.isEmpty()) {
            return builder;
        }
        try {
            if (methodDesc.isStatic()) {
                return resolve(builder, methodDesc, interceptors, BootstrapStaticTemplate.class, classLoader);
            } else if (methodDesc.isConstructor()) {
                return resolve(builder, methodDesc, interceptors, BootstrapConstTemplate.class, classLoader);
            } else {
                return resolve(builder, methodDesc, interceptors, BootstrapMemberTemplate.class, classLoader);
            }
        } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            LOGGER.warning(String.format(Locale.ROOT, "Enhance [%s] failed for [%s], caused by [%s]. ",
                    MethodKeyCreator.getMethodDescKey(methodDesc), e.getClass().getName(), e.getMessage()));
        }
        return builder;
    }

    /**
     * 获取单个方法有关的拦截器列表
     *
     * @param methodDesc  方法定义
     * @param classLoader 类加载器
     * @return 拦截器列表
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

    /**
     * 处理方法增强
     * <pre>
     *     1.依模板类创建增强Adviser
     *     2.使用被增强类的类加载器定义该Adviser
     *     3.为该Adviser添加增强的拦截器
     *     4.在构建器中定义增强逻辑
     * </pre>
     *
     * @param builder      构建器
     * @param methodDesc   方法定义
     * @param interceptors 拦截器列表
     * @param templateCls  增强模板类
     * @param classLoader  被增强类的类加载器
     * @return 构建器
     * @throws InvocationTargetException 调用方法错误
     * @throws IllegalAccessException    无法访问属性或方法，正常不会报出
     * @throws NoSuchMethodException     无法找到方法，正常不会报出
     * @throws NoSuchFieldException      找不到属性
     */
    private DynamicType.Builder<?> resolve(DynamicType.Builder<?> builder, MethodDescription.InDefinedShape methodDesc,
            List<Interceptor> interceptors, Class<?> templateCls, ClassLoader classLoader)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, NoSuchFieldException {
        final String adviceClassName = getAdviceClassName(templateCls, methodDesc);
        List<Interceptor> globalInterceptors = INTERCEPTOR_GLOBAL_MAP.get(adviceClassName);
        if (globalInterceptors == null) {
            globalInterceptors = new ArrayList<>(interceptors);
            INTERCEPTOR_GLOBAL_MAP.put(adviceClassName, globalInterceptors);
            final byte[] adviceClsBytes = createAdviceClass(templateCls, adviceClassName);
            final Class<?> adviceCls = defineAdviceClass(adviceClassName, classLoader, adviceClsBytes);
            prepareAdviceClass(adviceCls, interceptors);
            return visitAdvice(builder, methodDesc, adviceCls, adviceClsBytes);
        } else {
            globalInterceptors.addAll(interceptors);
            return builder;
        }
    }

    /**
     * 获取增强Adviser的全限定名
     * <p>由模板类全限定名拼接被增强方法元信息的hash值所得，见于{@link MethodKeyCreator#getMethodDescKey}
     *
     * @param templateCls 增强模板类
     * @param methodDesc  方法定义
     * @return 增强Adviser全限定名
     */
    private String getAdviceClassName(Class<?> templateCls, MethodDescription.InDefinedShape methodDesc) {
        return templateCls.getName() + "_"
                + Integer.toHexString(MethodKeyCreator.getMethodDescKey(methodDesc).hashCode());
    }

    /**
     * 使用byte-buddy依照增强模板类动态生成增强Adviser
     *
     * @param templateCls   增强模板类
     * @param adviceClsName 增强Adviser全限定名
     * @return 增强Adviser的字节码
     */
    @SuppressWarnings("checkstyle:RegexpSinglelineJava")
    private byte[] createAdviceClass(Class<?> templateCls, String adviceClsName) {
        return new ByteBuddy().redefine(templateCls)
                .name(adviceClsName)
                .defineField(INTERCEPTORS_FIELD_NAME, List.class, Visibility.PUBLIC, Ownership.STATIC)
                .make()
                .getBytes();
    }

    /**
     * 通过字节码，使用ClassLoader定义增强Adviser
     *
     * @param adviceClassName 增强Advice的全限定名
     * @param classLoader    被增强类的ClassLoader
     * @param adviceClsBytes 增强Adviser的字节码
     * @return 增强Adviser的Class
     * @throws InvocationTargetException 调用defineClass方法错误
     * @throws IllegalAccessException    无法访问defineClass方法，正常不会报出
     * @throws NoSuchMethodException     无法找到defineClass方法，正常不会报出
     */
    private Class<?> defineAdviceClass(String adviceClassName, ClassLoader classLoader, byte[] adviceClsBytes)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        return ClassLoaderUtils.defineClass(adviceClassName, classLoader, adviceClsBytes);
    }

    /**
     * 初始化增强Adviser，为其添加方法拦截器集合
     *
     * @param adviceCls    增强Adviser的Class
     * @param interceptors 拦截器集合
     * @throws NoSuchFieldException   找不到属性
     * @throws IllegalAccessException 无法访问属性
     */
    private void prepareAdviceClass(Class<?> adviceCls, List<Interceptor> interceptors)
            throws NoSuchFieldException, IllegalAccessException {
        adviceCls.getDeclaredField(INTERCEPTORS_FIELD_NAME).set(null, interceptors);
    }

    /**
     * 在构建器中定义增强逻辑
     *
     * @param builder        构建器
     * @param methodDesc     方法定义
     * @param adviceCls      增强Adviser的Class
     * @param adviceClsBytes 增强Adviser的字节码
     * @return 构建器
     */
    private DynamicType.Builder<?> visitAdvice(DynamicType.Builder<?> builder,
            MethodDescription.InDefinedShape methodDesc, Class<?> adviceCls, final byte[] adviceClsBytes) {
        return builder.visit(Advice.to(adviceCls, new ClassFileLocator() {
            @Override
            public void close() {
            }

            @Override
            public Resolution locate(String name) {
                return new Resolution.Explicit(adviceClsBytes);
            }
        }).on(ElementMatchers.<MethodDescription>is(methodDesc)));
    }
}
