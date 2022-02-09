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

package com.huawei.sermant.core.agent.transformer;

import com.huawei.sermant.core.agent.EnhanceDefinitionLoader;
import com.huawei.sermant.core.agent.annotations.AboutDelete;
import com.huawei.sermant.core.agent.definition.EnhanceDefinition;
import com.huawei.sermant.core.agent.interceptor.ConstructorInterceptor;
import com.huawei.sermant.core.agent.interceptor.InstanceMethodInterceptor;
import com.huawei.sermant.core.agent.interceptor.Interceptor;
import com.huawei.sermant.core.agent.interceptor.InterceptorLoader;
import com.huawei.sermant.core.agent.interceptor.StaticMethodInterceptor;
import com.huawei.sermant.core.agent.template.BootstrapConstTemplate;
import com.huawei.sermant.core.agent.template.BootstrapInstTemplate;
import com.huawei.sermant.core.agent.template.BootstrapStaticTemplate;
import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.lubanops.bootstrap.Listener;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.method.ParameterList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * 启动类加载器加载类的Transformer
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/10/27
 */
@AboutDelete
@Deprecated
public class BootstrapTransformer implements AgentBuilder.Transformer {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    @SuppressWarnings("checkstyle:ParameterAssignment")
    @Override
    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription,
            ClassLoader classLoader, JavaModule module) {
        final EnhanceDefinitionLoader loader = EnhanceDefinitionLoader.getInstance();
        final Listener listener = loader.findNameListener(typeDescription);
        final List<EnhanceDefinition> definitions = loader.findDefinitions(typeDescription);
        if (listener == null && definitions.isEmpty()) {
            return builder;
        }
        classLoader = ClassLoader.getSystemClassLoader();
        return enhanceMethods(listener, definitions, builder, typeDescription, classLoader);
    }

    /**
     * 判断并增强该类所有方法
     *
     * @param listener        luban监听器
     * @param definitions     增强定义
     * @param builder         动态构建器
     * @param typeDescription 被增强类描述
     * @param classLoader     用于增强的类加载器
     * @return 动态构建器
     */
    @SuppressWarnings("checkstyle:IllegalCatch")
    private DynamicType.Builder<?> enhanceMethods(Listener listener, List<EnhanceDefinition> definitions,
            DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader) {
        DynamicType.Builder<?> newBuilder = builder;
        final MethodList<MethodDescription.InDefinedShape> declaredMethods = typeDescription.getDeclaredMethods();
        for (MethodDescription.InDefinedShape method : declaredMethods) {
            if (method.isNative()) {
                continue;
            }
            final VisitGuide guide = new VisitGuide(method, classLoader);
            if (listener != null) {
                guide.setOriginInterceptor(listener); // 添加luban插件的拦截器
            }
            guide.addInterceptors(definitions); // 添加插件的拦截器
            try {
                newBuilder = guide.resolve(newBuilder, method);
            } catch (Exception e) {
                LOGGER.warning(String.format(Locale.ROOT, "Enhance [%s] failed for [%s], caused by [%s]. ",
                        typeDescription.getTypeName(), e.getClass().getName(), e.getMessage()));
            }
        }
        return newBuilder;
    }

    /**
     * 被增强类方法的visit向导
     */
    private static class VisitGuide extends MethodInterceptorCollector {
        /**
         * 加载模板类的ClassLoader
         */
        private final ClassLoader classLoader;

        private VisitGuide(MethodDescription.InDefinedShape method, ClassLoader classLoader) {
            super(method);
            this.classLoader = classLoader;
        }


        /**
         * 尝试使用Advice的方式增强目标类，此处只做分类，操作见{@link #doResolve(DynamicType.Builder, Class, Class)}
         *
         * @param builder 动态构造器
         * @param method  被增强方法的描述
         * @return 动态构造器
         * @throws Exception 增强失败
         */
        private DynamicType.Builder<?> resolve(DynamicType.Builder<?> builder, MethodDescription.InDefinedShape method)
                throws Exception {
            if (originInterceptorName == null && interceptorNames.isEmpty()) {
                return builder;
            }
            if (method.isStatic()) {
                return doResolve(builder, BootstrapStaticTemplate.class, StaticMethodInterceptor.class);
            } else if (method.isConstructor()) {
                return doResolve(builder, BootstrapConstTemplate.class, ConstructorInterceptor.class);
            } else {
                return doResolve(builder, BootstrapInstTemplate.class, InstanceMethodInterceptor.class);
            }
        }

        /**
         * 尝试使用Advice的方式增强目标类，这里将使用模板类进行增强
         *
         * @param builder         动态构造器
         * @param templateCls     模板类
         * @param interceptorType 拦截器的类型
         * @return 动态构造器
         * @throws Exception 增强失败
         */
        @SuppressWarnings("checkstyle:OperatorWrap")
        private DynamicType.Builder<?> doResolve(DynamicType.Builder<?> builder, Class<?> templateCls,
                Class<? extends Interceptor> interceptorType) throws Exception {
            final String adviceClsName = getAdviceClassName(templateCls, method);
            final byte[] adviceClsBytes = createAdviceClass(templateCls, adviceClsName);
            final Class<?> adviceCls = defineAdviceClass(adviceClsName, adviceClsBytes);
            final Interceptor originInterceptor = originInterceptorName == null ? null :
                    InterceptorLoader.getInterceptor(originInterceptorName, classLoader,
                            com.huawei.sermant.core.lubanops.bootstrap.Interceptor.class);
            final List<? extends Interceptor> interceptors = InterceptorLoader.getInterceptors(
                    interceptorNames, classLoader, interceptorType);
            prepareAdviceClass(adviceCls, originInterceptor, interceptors);
            return visitAdvice(builder, adviceCls, adviceClsBytes);
        }

        /**
         * 依据模板类名称、被拦截类名称、被拦截方法名称和被拦截方法参数的类型，创建动态Advice增强器的名称
         *
         * @param templateCls 模板类
         * @param method      被拦截方法
         * @return 动态Advice增强器的名称
         */
        private String getAdviceClassName(Class<?> templateCls, MethodDescription.InDefinedShape method) {
            final StringBuilder builder = new StringBuilder().append(method.getDeclaringType().getTypeName());
            if (!method.isConstructor()) {
                builder.append('#').append(method.getName());
            }
            builder.append("(");
            final ParameterList<ParameterDescription.InDefinedShape> parameters = method.getParameters();
            for (int i = 0; i < parameters.size(); i++) {
                if (i > 0) {
                    builder.append(',');
                }
                builder.append(parameters.get(i).getType().getTypeName());
            }
            builder.append(')');
            return templateCls.getName() + "_" + Integer.toHexString(builder.toString().hashCode());
        }

        /**
         * 使用byte buddy字节码技术动态创建Advice增强器
         *
         * @param templateCls   模板类
         * @param adviceClsName 动态Advice增强器的名称
         * @return 动态Advice增强器的字节码
         */
        @SuppressWarnings("checkstyle:RegexpSinglelineJava")
        private byte[] createAdviceClass(Class<?> templateCls, String adviceClsName) {
            return new ByteBuddy()
                    .redefine(templateCls)
                    .name(adviceClsName)
                    .make()
                    .getBytes();
        }

        /**
         * 使用当前类加载器定义动态Advice增强器类
         *
         * @param adviceClsName  动态Advice增强器的名称
         * @param adviceClsBytes 动态Advice增强器的字节码
         * @return 动态Advice增强器类
         * @throws Exception 定义动态Advice增强器类失败
         */
        private Class<?> defineAdviceClass(String adviceClsName, byte[] adviceClsBytes) throws Exception {
            final Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class,
                    int.class, int.class);
            defineClass.setAccessible(true);
            return (Class<?>) defineClass.invoke(classLoader, adviceClsName, adviceClsBytes, 0, adviceClsBytes.length);
        }

        /**
         * 将两组拦截器赋值为动态Advice增强器类的ORIGIN_INTERCEPTORS和INTERCEPTORS
         *
         * @param adviceCls         动态Advice增强器类
         * @param originInterceptor luban拦截器列表
         * @param interceptors      拦截器列表
         * @throws Exception 执行prepare方法失败
         */
        private void prepareAdviceClass(Class<?> adviceCls, Interceptor originInterceptor,
                List<? extends Interceptor> interceptors) throws Exception {
            adviceCls.getDeclaredField("ORIGIN_INTERCEPTOR").set(null, originInterceptor);
            adviceCls.getDeclaredField("INTERCEPTORS").set(null, interceptors);
        }

        /**
         * 调用visit方法增强目标类
         *
         * @param builder        动态构造器
         * @param adviceCls      动态Advice增强器类
         * @param adviceClsBytes 动态Advice增强器字节码
         * @return 动态构造器
         */
        private DynamicType.Builder<?> visitAdvice(DynamicType.Builder<?> builder, Class<?> adviceCls,
                final byte[] adviceClsBytes) {
            return builder.visit(Advice.to(adviceCls, new ClassFileLocator() {
                @Override
                public void close() {
                }

                @Override
                public Resolution locate(String name) {
                    return new Resolution.Explicit(adviceClsBytes);
                }
            }).on(ElementMatchers.<MethodDescription>is(method)));
        }
    }
}
