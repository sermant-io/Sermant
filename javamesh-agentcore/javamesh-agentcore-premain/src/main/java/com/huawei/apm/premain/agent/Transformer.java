package com.huawei.apm.premain.agent;

import com.huawei.apm.bootstrap.definition.EnhanceDefinition;
import com.huawei.apm.bootstrap.definition.MethodInterceptPoint;
import com.huawei.apm.bootstrap.interceptors.ConstructorInterceptor;
import com.huawei.apm.bootstrap.interceptors.InstanceMethodInterceptor;
import com.huawei.apm.bootstrap.interceptors.StaticMethodInterceptor;
import com.huawei.apm.premain.common.OverrideArgumentsCall;
import com.huawei.apm.premain.enhance.enhancer.ConstructorEnhancer;
import com.huawei.apm.premain.enhance.enhancer.InstanceMethodEnhancer;
import com.huawei.apm.premain.enhance.enhancer.StaticMethodEnhancer;
import com.huawei.apm.premain.agent.boot.PluginServiceManager;
import com.huawei.apm.bootstrap.lubanops.Listener;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.huawei.apm.premain.enhance.InterceptorLoader.getInterceptors;

/**
 * 多次增强Transformer
 */
public class Transformer implements AgentBuilder.Transformer {

    private final EnhanceDefinitionLoader enhanceDefinitionLoader;

    public Transformer(EnhanceDefinitionLoader enhanceDefinitionLoader) {
        this.enhanceDefinitionLoader = enhanceDefinitionLoader;
    }

    @Override
    public DynamicType.Builder<?> transform(final DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module) {
        final List<Listener> nameListeners = enhanceDefinitionLoader.findNameListeners(typeDescription);
        final List<EnhanceDefinition> definitions = enhanceDefinitionLoader.findDefinitions(typeDescription);

        if (nameListeners.isEmpty() && definitions.isEmpty()) {
            return builder;
        }

        DynamicType.Builder<?> newBuilder = builder;
        for (Listener listener : nameListeners) {
            newBuilder = NamedListenerTransformer.getInstance()
                .enhanceNamedListener(listener, newBuilder, typeDescription, classLoader);
        }

        // 当前实现方式，新功能会覆盖同一个拦截点下的老功能增强器
        if (!definitions.isEmpty()) {
            newBuilder = enhanceMethods(definitions, newBuilder, typeDescription, classLoader);
        }
        // 初始化插件, 只会调用一次, 目的是使用增强类的类加载器对插件初始化, 这样可保证拦截器以及初始化的内容数据可共享
        PluginServiceManager.INSTANCE.init(classLoader);
        return BuilderHelpers.addEnhancedField(newBuilder);
    }

    private DynamicType.Builder<?> enhanceMethods(List<EnhanceDefinition> definitions,
            final DynamicType.Builder<?> builder,
            TypeDescription typeDescription,
            ClassLoader classLoader) {
        DynamicType.Builder<?> newBuilder = builder;

        List<MultiInterMethodHolder> methodHolders = getMethodsToBeEnhanced(definitions, typeDescription);
        for (MultiInterMethodHolder methodHolder : methodHolders) {
            if (methodHolder.isConstructor()) {
                newBuilder = enhanceConstructor(classLoader, newBuilder, methodHolder);
            } else if (methodHolder.isStatic()) {
                newBuilder = enhanceStaticMethod(classLoader, newBuilder, methodHolder);
            } else {
                newBuilder = enhanceInstanceMethod(classLoader, newBuilder, methodHolder);
            }
        }
        return newBuilder;
    }

    private DynamicType.Builder<?> enhanceInstanceMethod(ClassLoader classLoader,
            DynamicType.Builder<?> newBuilder,
            MultiInterMethodHolder methodHolder) {
        return newBuilder.method(methodHolder.getMatcher())
            .intercept(MethodDelegation.withDefaultConfiguration()
                .withBinders(Morph.Binder.install(OverrideArgumentsCall.class))
                .to(new InstanceMethodEnhancer(
                    getInterceptors(methodHolder.getInterceptors(), classLoader, InstanceMethodInterceptor.class))));
    }

    private DynamicType.Builder<?> enhanceStaticMethod(ClassLoader classLoader,
            DynamicType.Builder<?> newBuilder,
            MultiInterMethodHolder methodHolder) {
        return newBuilder.method(methodHolder.getMatcher())
            .intercept(MethodDelegation.withDefaultConfiguration()
                .withBinders(Morph.Binder.install(OverrideArgumentsCall.class))
                .to(new StaticMethodEnhancer(
                    getInterceptors(methodHolder.getInterceptors(), classLoader, StaticMethodInterceptor.class))));
    }

    private DynamicType.Builder<?> enhanceConstructor(ClassLoader classLoader,
            DynamicType.Builder<?> newBuilder,
            MultiInterMethodHolder methodHolder) {
        return newBuilder.constructor(methodHolder.getMatcher())
                .intercept(SuperMethodCall.INSTANCE.andThen(
                        MethodDelegation.withDefaultConfiguration().to(new ConstructorEnhancer(
                                getInterceptors(methodHolder.getInterceptors(), classLoader, ConstructorInterceptor.class)))));
    }

    private List<String> getMatchedInterceptors(List<EnhanceDefinition> definitions,
            MethodDescription.InDefinedShape method) {
        List<String> matchedInterceptors = new ArrayList<String>();
        for (EnhanceDefinition definition : definitions) {
            for (MethodInterceptPoint methodInterceptPoint : definition.getMethodInterceptPoints()) {
                if (((methodInterceptPoint.isStaticMethod() && method.isStatic()) ||
                        (methodInterceptPoint.isConstructor() && method.isConstructor()) ||
                        (methodInterceptPoint.isInstanceMethod() && !method.isStatic() &&
                                !method.isConstructor())) && methodInterceptPoint.getMatcher().matches(method)) {
                    matchedInterceptors.add(methodInterceptPoint.getInterceptor());
                }
            }
        }
        return matchedInterceptors;
    }

    private List<MultiInterMethodHolder> getMethodsToBeEnhanced(List<EnhanceDefinition> definitions,
            TypeDescription typeDescription) {
        MethodList<MethodDescription.InDefinedShape> declaredMethods = typeDescription.getDeclaredMethods();
        // 找出所有满足条件的方法以及其所对应的所有拦截器
        List<MultiInterMethodHolder> methodHolders = new LinkedList<MultiInterMethodHolder>();
        for (MethodDescription.InDefinedShape method : declaredMethods) {
            List<String> matchedInterceptors = getMatchedInterceptors(definitions, method);
            if (!matchedInterceptors.isEmpty()) {
                methodHolders.add(new MultiInterMethodHolder(method, matchedInterceptors.toArray(new String[0])));
            }
        }
        return methodHolders;
    }

    private static class MultiInterMethodHolder {

        private final MethodDescription.InDefinedShape method;

        private final String[] interceptors;

        public MultiInterMethodHolder(MethodDescription.InDefinedShape method, String[] interceptors) {
            this.method = method;
            this.interceptors = interceptors;
        }

        public ElementMatcher.Junction<MethodDescription> getMatcher() {
            return ElementMatchers.is(method);
        }

        public boolean isConstructor() {
            return method.isConstructor();
        }

        public boolean isStatic() {
            return method.isStatic();
        }

        public String[] getInterceptors() {
            return interceptors;
        }

    }
}
