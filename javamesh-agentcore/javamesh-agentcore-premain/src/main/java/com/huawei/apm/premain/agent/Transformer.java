package com.huawei.apm.premain.agent;

import com.huawei.apm.bootstrap.definition.EnhanceDefinition;
import com.huawei.apm.bootstrap.interceptors.ConstructorInterceptor;
import com.huawei.apm.bootstrap.interceptors.InstanceMethodInterceptor;
import com.huawei.apm.bootstrap.interceptors.StaticMethodInterceptor;
import com.huawei.apm.premain.common.OverrideArgumentsCall;
import com.huawei.apm.premain.enhance.InterceptorLoader;
import com.huawei.apm.premain.enhance.enhancer.ConstructorEnhancer;
import com.huawei.apm.premain.enhance.enhancer.InstanceMethodEnhancer;
import com.huawei.apm.premain.enhance.enhancer.StaticMethodEnhancer;
import com.huawei.apm.premain.agent.boot.PluginServiceManager;
import com.huawei.apm.bootstrap.lubanops.Listener;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.util.List;

/**
 * 多次增强Transformer
 */
public class Transformer implements AgentBuilder.Transformer {

    private final EnhanceDefinitionLoader enhanceDefinitionLoader;

    public Transformer(EnhanceDefinitionLoader enhanceDefinitionLoader) {
        this.enhanceDefinitionLoader = enhanceDefinitionLoader;
    }

    @Override
    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription,
            ClassLoader classLoader, JavaModule module) {
        if (classLoader == null) {
            return new BootstrapTransformer(enhanceDefinitionLoader).transform(builder, typeDescription, null, module);
        }
        final Listener listener = enhanceDefinitionLoader.findNameListener(typeDescription);
        final List<EnhanceDefinition> definitions = enhanceDefinitionLoader.findDefinitions(typeDescription);
        if (listener == null && definitions.isEmpty()) {
            return builder;
        }
        // 初始化插件, 只会调用一次, 目的是使用增强类的类加载器对插件初始化, 这样可保证拦截器以及初始化的内容数据可共享
        PluginServiceManager.INSTANCE.init(classLoader);
        return enhanceMethods(listener, definitions, builder, typeDescription, classLoader);
    }

    private DynamicType.Builder<?> enhanceMethods(
            Listener listener,
            List<EnhanceDefinition> definitions,
            final DynamicType.Builder<?> builder,
            TypeDescription typeDescription,
            ClassLoader classLoader) {
        DynamicType.Builder<?> newBuilder = builder;

        // 找出所有满足条件的方法以及其所对应的所有拦截器
        boolean flag = false;
        for (MethodDescription.InDefinedShape method : typeDescription.getDeclaredMethods()) {
            final MultiInterMethodHolder methodHolder = new MultiInterMethodHolder(method);
            if (listener != null && methodHolder.setOriginInterceptor(listener)) {
                newBuilder = BuilderHelpers.addListenerFields(newBuilder, listener.getFields());
            }
            methodHolder.addInterceptors(definitions);
            newBuilder = methodHolder.build(newBuilder, classLoader);
            flag |= methodHolder.hasOriginInterceptor();
        }
        if (flag) {
            newBuilder = BuilderHelpers.addEnhancedField(newBuilder);
        }
        return newBuilder;
    }

    private static class MultiInterMethodHolder extends InterceptorCollector {
        private MultiInterMethodHolder(MethodDescription.InDefinedShape method) {
            super(method);
        }

        private boolean hasOriginInterceptor() {
            return originInterceptorName != null;
        }

        private DynamicType.Builder<?> build(DynamicType.Builder<?> builder, ClassLoader classLoader) {
            if (originInterceptorName == null && interceptorNames.isEmpty()) {
                return builder;
            }
            final com.huawei.apm.bootstrap.lubanops.Interceptor originInterceptor;
            if (originInterceptorName == null) {
                originInterceptor = null;
            } else {
                originInterceptor = InterceptorLoader.getInterceptor(
                        originInterceptorName, classLoader, com.huawei.apm.bootstrap.lubanops.Interceptor.class);
            }
            if (method.isStatic()) {
                return enhanceStaticMethod(originInterceptor, classLoader, builder);
            } else if (method.isConstructor()) {
                return enhanceConstructor(originInterceptor, classLoader, builder);
            } else {
                return enhanceInstanceMethod(originInterceptor, classLoader, builder);
            }
        }

        private DynamicType.Builder<?> enhanceInstanceMethod(
                com.huawei.apm.bootstrap.lubanops.Interceptor originInterceptor,
                ClassLoader classLoader, DynamicType.Builder<?> newBuilder) {
            final List<InstanceMethodInterceptor> instInterceptors =
                    InterceptorLoader.getInterceptors(interceptorNames, classLoader, InstanceMethodInterceptor.class);
            final MethodDelegation delegation = MethodDelegation.withDefaultConfiguration()
                    .withBinders(Morph.Binder.install(OverrideArgumentsCall.class))
                    .to(new InstanceMethodEnhancer(originInterceptor, instInterceptors));
            return newBuilder.method(ElementMatchers.is(method)).intercept(delegation);
        }

        private DynamicType.Builder<?> enhanceStaticMethod(
                com.huawei.apm.bootstrap.lubanops.Interceptor originInterceptor,
                ClassLoader classLoader, DynamicType.Builder<?> newBuilder) {
            final List<StaticMethodInterceptor> staticInterceptors =
                    InterceptorLoader.getInterceptors(interceptorNames, classLoader, StaticMethodInterceptor.class);
            final Implementation delegation = MethodDelegation.withDefaultConfiguration()
                    .withBinders(Morph.Binder.install(OverrideArgumentsCall.class))
                    .to(new StaticMethodEnhancer(originInterceptor, staticInterceptors));
            return newBuilder.method(ElementMatchers.is(method)).intercept(delegation);
        }

        private DynamicType.Builder<?> enhanceConstructor(
                com.huawei.apm.bootstrap.lubanops.Interceptor originInterceptor,
                ClassLoader classLoader, DynamicType.Builder<?> newBuilder) {
            final List<ConstructorInterceptor> constInterceptors =
                    InterceptorLoader.getInterceptors(interceptorNames, classLoader, ConstructorInterceptor.class);
            final Implementation delegation = SuperMethodCall.INSTANCE.andThen(MethodDelegation
                    .withDefaultConfiguration().to(new ConstructorEnhancer(originInterceptor, constInterceptors)));
            return newBuilder.constructor(ElementMatchers.is(method)).intercept(delegation);
        }
    }
}
