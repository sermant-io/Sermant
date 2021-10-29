/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.premain.agent;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

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

import com.huawei.apm.bootstrap.common.VersionChecker;
import com.huawei.apm.bootstrap.definition.EnhanceDefinition;
import com.huawei.apm.bootstrap.definition.MethodInterceptPoint;
import com.huawei.apm.bootstrap.interceptors.ConstructorInterceptor;
import com.huawei.apm.bootstrap.interceptors.InstanceMethodInterceptor;
import com.huawei.apm.bootstrap.interceptors.Interceptor;
import com.huawei.apm.bootstrap.interceptors.StaticMethodInterceptor;
import com.huawei.apm.bootstrap.lubanops.Listener;
import com.huawei.apm.bootstrap.lubanops.TransformerMethod;
import com.huawei.apm.bootstrap.lubanops.log.LogFactory;
import com.huawei.apm.bootstrap.lubanops.utils.Util;
import com.huawei.apm.premain.agent.template.BootstrapConstTemplate;
import com.huawei.apm.premain.agent.template.BootstrapInstTemplate;
import com.huawei.apm.premain.agent.template.BootstrapStaticTemplate;
import com.huawei.apm.premain.enhance.InterceptorLoader;

/**
 * 启动类加载器加载类的Transformer
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/10/27
 */
public class BootstrapTransformer implements AgentBuilder.Transformer {
    /**
     * 日志
     */
    private static final Logger LOGGER = LogFactory.getLogger();

    /**
     * 增强定义加载器
     */
    private final EnhanceDefinitionLoader enhanceDefinitionLoader;

    public BootstrapTransformer(EnhanceDefinitionLoader enhanceDefinitionLoader) {
        this.enhanceDefinitionLoader = enhanceDefinitionLoader;
    }

    @Override
    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription,
            ClassLoader classLoader, JavaModule module) {
        final List<Listener> nameListeners = enhanceDefinitionLoader.findNameListeners(typeDescription);
        final List<EnhanceDefinition> definitions = enhanceDefinitionLoader.findDefinitions(typeDescription);
        if (nameListeners.isEmpty() && definitions.isEmpty()) {
            return builder;
        }
        DynamicType.Builder<?> newBuilder = builder;
        final ClassLoader loader = Thread.currentThread().getContextClassLoader(); // 使用当前线程加载模板类
        final MethodList<MethodDescription.InDefinedShape> declaredMethods = typeDescription.getDeclaredMethods();
        for (MethodDescription.InDefinedShape method : declaredMethods) {
            if (method.isNative()) {
                continue;
            }
            final VisitGuide guide = new VisitGuide(method, loader);
            guide.addOriginInterceptors(nameListeners); // 添加luban插件的拦截器
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
    private static class VisitGuide {
        /**
         * 被增强类的方法
         */
        private final MethodDescription.InDefinedShape method;

        /**
         * 加载模板类的ClassLoader
         */
        private final ClassLoader classLoader;

        /**
         * luban插件增强当前方法的拦截器名称
         */
        private final List<String> originInterceptorNames;

        /**
         * 增强当前方法的拦截器名称
         */
        private final List<String> interceptorNames;

        private VisitGuide(MethodDescription.InDefinedShape method, ClassLoader classLoader) {
            this.method = method;
            this.classLoader = classLoader;
            this.originInterceptorNames = new ArrayList<String>();
            this.interceptorNames = new ArrayList<String>();
        }

        /**
         * 添加luban插件增强当前方法的拦截器
         *
         * @param listeners luban的监听器列表
         */
        private void addOriginInterceptors(List<Listener> listeners) {
            for (Listener listener : listeners) {
                String version = Util.getJarVersionFromProtectionDomain(listener.getClass().getProtectionDomain());
                if (!new VersionChecker(version, listener).check()) {
                    continue;
                }
                listener.addTag();
                for (TransformerMethod transformerMethod : listener.getTransformerMethod()) {
                    // 同为构造函数且参数列表一致、或同为普通方法且方法名和参数列表一致时，通过
                    if (((method.isConstructor() && transformerMethod.isConstructor()) ||
                            (!method.isConstructor() && !transformerMethod.isConstructor() &&
                                    method.getName().equals(transformerMethod.getMethod()))) &&
                            (isParamsMatch(transformerMethod))) {
                        originInterceptorNames.add(transformerMethod.getMethod());
                    }
                }
            }
        }

        /**
         * 检查参数类型是否匹配，当luban监听器中没有声明参数类型时同样表示匹配
         *
         * @param transformerMethod luban声明的方法对象
         * @return 参数类型是否匹配
         */
        private boolean isParamsMatch(TransformerMethod transformerMethod) {
            final List<String> params = transformerMethod.getParams();
            if (params.isEmpty()) {
                return true;
            }
            final ParameterList<ParameterDescription.InDefinedShape> parameters = method.getParameters();
            if (params.size() != parameters.size()) {
                return false;
            }
            for (int i = 0; i < params.size(); i++) {
                if (!params.get(i).equals(parameters.get(i).getType().getTypeName())) {
                    return false;
                }
            }
            return true;
        }

        /**
         * 添加当前方法的拦截器
         *
         * @param enhanceDefinitions 增强定义列表
         */
        private void addInterceptors(List<EnhanceDefinition> enhanceDefinitions) {
            for (EnhanceDefinition definition : enhanceDefinitions) {
                for (MethodInterceptPoint point : definition.getMethodInterceptPoints()) {
                    // 方法类型相同且满足匹配条件时通过
                    if (((point.isStaticMethod() && method.isStatic()) ||
                            (point.isConstructor() && method.isConstructor()) ||
                            (point.isInstanceMethod() && !method.isStatic() && !method.isConstructor())) &&
                            (point.getMatcher().matches(method))) {
                        interceptorNames.add(point.getInterceptor());
                    }
                }
            }
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
            if (originInterceptorNames.isEmpty() && interceptorNames.isEmpty()) {
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
        private DynamicType.Builder<?> doResolve(DynamicType.Builder<?> builder, Class<?> templateCls,
                Class<? extends Interceptor> interceptorType) throws Exception {
            final String adviceClsName = getAdviceClassName(templateCls, method);
            final byte[] adviceClsBytes = createAdviceClass(templateCls, adviceClsName);
            final Class<?> adviceCls = defineAdviceClass(adviceClsName, adviceClsBytes);
            final List<? extends Interceptor> originInterceptors = InterceptorLoader.getInterceptors(
                    originInterceptorNames.toArray(new String[0]), classLoader,
                    com.huawei.apm.bootstrap.lubanops.Interceptor.class);
            final List<? extends Interceptor> interceptors = InterceptorLoader.getInterceptors(
                    interceptorNames.toArray(new String[0]), classLoader, interceptorType);
            prepareAdviceClass(adviceCls, originInterceptors, interceptors);
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
         * @param adviceCls          动态Advice增强器类
         * @param originInterceptors luban拦截器列表
         * @param interceptors       拦截器列表
         * @throws Exception 执行prepare方法失败
         */
        private void prepareAdviceClass(Class<?> adviceCls, List<? extends Interceptor> originInterceptors,
                List<? extends Interceptor> interceptors) throws Exception {
            adviceCls.getDeclaredField("ORIGIN_INTERCEPTORS").set(null, originInterceptors);
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
