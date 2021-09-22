package com.huawei.apm.premain;

import com.huawei.apm.classloader.PluginClassLoader;
import com.huawei.apm.common.OverrideArgumentsCall;
import com.huawei.apm.enhance.InterceptorLoader;
import com.huawei.apm.enhance.enhancer.OriginConstructorEnhancer;
import com.huawei.apm.enhance.enhancer.OriginInstanceMethodOriginEnhancer;
import com.huawei.apm.enhance.enhancer.OriginStaticMethodOriginEnhancer;
import com.lubanops.apm.bootstrap.Interceptor;
import com.lubanops.apm.bootstrap.NoneNamedListener;
import com.lubanops.apm.premain.classloader.LopsUrlClassLoader;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeList;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * NoneNamedListener增强构建
 */
public class NoneNamedListenerBuilder {
    private static final Set<String> IGNORED_METHODS = new HashSet<String>();

    static {
        IGNORED_METHODS.add("getClass");
        IGNORED_METHODS.add("hashCode");
        IGNORED_METHODS.add("wait");
        IGNORED_METHODS.add("equals");
        IGNORED_METHODS.add("clone");
        IGNORED_METHODS.add("toString");
        IGNORED_METHODS.add("toJSONString");
        IGNORED_METHODS.add("notify");
        IGNORED_METHODS.add("notifyAll");
        IGNORED_METHODS.add("finalize");
        IGNORED_METHODS.add("main");
    }

    public static void initialize(Instrumentation instrumentation) {
        final ServiceLoader<NoneNamedListener> noneNameListenerServiceLoader = AgentSpiLoader.load(NoneNamedListener.class);
        initNoneNameListener(noneNameListenerServiceLoader);
        AgentBuilder.Transformer transformer = new Transformer(noneNameListenerServiceLoader);
        new AgentBuilder.Default(new ByteBuddy())
            .ignore(BuilderHelpers.buildIgnoreClassNamePrefixMatch())
            .type(ElementMatchers.not(EnhanceDefinitionLoader.INSTANCE.buildMatch()))
            .and(ElementMatchers.not(ElementMatchers.isInterface()))
            .and(buildMatch(noneNameListenerServiceLoader))
            .transform(transformer)
            .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
            .with(new LoadListener())
            .installOn(instrumentation);
    }

    private static void initNoneNameListener(ServiceLoader<NoneNamedListener> noneNameListenerServiceLoader) {
        for (NoneNamedListener noneNamedListener : noneNameListenerServiceLoader) {
            noneNamedListener.init();
        }
    }

    private static boolean isNeedTransform(String className, ClassLoader loader) {
        if (className == null) {
            return false;
        }
        return isNeededTransformClassLoader(loader);
    }

    private static AgentBuilder.RawMatcher buildMatch(
        final ServiceLoader<NoneNamedListener> noneNameListenerServiceLoader) {
        return new AgentBuilder.RawMatcher() {
            @Override
            public boolean matches(TypeDescription typeDescription, ClassLoader classLoader,
                JavaModule module, Class<?> classBeingRedefined, ProtectionDomain protectionDomain) {
                if (!isNeedTransform(typeDescription.getTypeName(), classLoader)) {
                    return false;
                }
                if (classLoader != null && classLoader.getClass().getName().endsWith("DelegatingClassLoader")) {
                    return false;
                }
                // 针对listener 自身条件匹配
                List<String> methods = matchListenerMethods(typeDescription, noneNameListenerServiceLoader);
                if (methods.isEmpty()) {
                    return false;
                }
                return !findValidMethods(typeDescription, methods).isEmpty();
            }
        };
    }

    private static List<String> matchListenerMethods(TypeDescription typeDescription,
        ServiceLoader<NoneNamedListener> noneNameListenerServiceLoader) {
        List<String> methods = new ArrayList<String>();
        if (typeDescription.isEnum()
            || typeDescription.isInterface()
            || typeDescription.isAnnotation()
            || typeDescription.isArray()) {
            return methods;
        }
        methods.addAll(matchJavaMethodListener(typeDescription, noneNameListenerServiceLoader));
        methods.addAll(matchRunnableListener(typeDescription));
        return methods;
    }

    private static List<String> matchJavaMethodListener(TypeDescription typeDescription,
        ServiceLoader<NoneNamedListener> noneNameListenerServiceLoader) {
        List<String> methods = null;
        for (NoneNamedListener noneNamedListener : noneNameListenerServiceLoader) {
            // JavaMethodListener
            if (noneNamedListener.getClass().getName().equals("com.lubanops.apm.plugin.method.JavaMethodListener")) {
                methods = noneNamedListener.matchClass(typeDescription.getName(), null);
            }
        }
        if (methods == null) {
            methods = new ArrayList<String>();
        }
        return methods;
    }

    private static List<String> matchRunnableListener(TypeDescription typeDescription) {
        // 黑名单
        if ("io/grpc/internal/DnsNameResolver$Resolve".equals(typeDescription.getTypeName())) {
            return Collections.emptyList();
        }
        if (!isSubTypeOfRunnable(typeDescription)) {
            return Collections.emptyList();
        }
        return Collections.singletonList("run");
    }

    /**
     * 是否是Runnable的子类
     * 相比原生方法，耗时少50ms左右
     *
     * @param typeDescription 类描述
     * @return 是否为其子类
     */
    private static boolean isSubTypeOfRunnable(TypeDescription typeDescription) {
        Queue<TypeDescription.Generic> queue = new LinkedList<TypeDescription.Generic>();
        queue.add(typeDescription.asGenericType());
        while (!queue.isEmpty()) {
            TypeDescription.Generic typeDefinition = queue.poll();
            if (typeDefinition.getTypeName().equals(Runnable.class.getName())) {
                return true;
            }
            if (!typeDefinition.isInterface()) {
                TypeList.Generic interfaces = typeDefinition.getInterfaces();
                if (!interfaces.isEmpty()) {
                    queue.addAll(interfaces);
                }
            }
            TypeDescription.Generic superClass = typeDefinition.getSuperClass();
            if (superClass != null) {
                queue.add(superClass);
            }
        }
        return false;
    }

    private static boolean isNeededTransformClassLoader(ClassLoader classLoader) {
        return !LopsUrlClassLoader.class.getName().equals(classLoader.getClass().getName())
            && !PluginClassLoader.class.getName().equals(classLoader.getClass().getName());
    }

    private static boolean ifAddInterceptor(List<String> methodPatterns,
        MethodDescription.InDefinedShape method) {
        if (method.isAbstract()) { // method is abstract
            return false;
        }
        String methodName = method.getName();
        if (methodName == null || methodName.contains("$")) { // 对于本省是代理的方法，不拦截
            return false;
        }
        if (method.isConstructor() || method.isTypeInitializer()) {
            return false;
        }
        if (method.isBridge()) {
            return false;
        }
        int mod = method.getModifiers();
        if (Modifier.isAbstract((mod)) || Modifier.isNative((mod))) {
            return false;
        }
        if (IGNORED_METHODS.contains(method.getName())) {
            return false;
        }
        if (methodPatterns.size() == 0) {
            return method.isPublic();
        }
        return methodPatterns.contains(method.getName());
    }

    private static List<MethodDescription.InDefinedShape> findValidMethods(TypeDescription typeDescription,
        List<String> methodPatterns) {
        List<MethodDescription.InDefinedShape> allMethodToAddInterceptor =
            new ArrayList<MethodDescription.InDefinedShape>();
        Set<String> getMethods = new HashSet<String>();
        Set<String> setMethods = new HashSet<String>();
        Set<String> isMethods = new HashSet<String>();

        for (MethodDescription.InDefinedShape method : typeDescription.getDeclaredMethods()) {
            String methodName = method.getName();
            if (ifAddInterceptor(methodPatterns, method)) {
                allMethodToAddInterceptor.add(method);
                if (methodName.startsWith("get")) {
                    getMethods.add(methodName);
                } else if (methodName.startsWith("set")) {
                    setMethods.add(methodName);
                } else if (methodName.startsWith("is")) {
                    isMethods.add(methodName);
                }
            }
        }
        Iterator<MethodDescription.InDefinedShape> iterator = allMethodToAddInterceptor.iterator();
        while (iterator.hasNext()) {
            MethodDescription.InDefinedShape next = iterator.next();
            String methodName = next.getName();
            if (methodName.startsWith("get")) { // 判断是否javabean方法，这种方法不加拦截
                String setterName = methodName.replaceFirst("get", "set");
                if (setMethods.contains(setterName)) {
                    iterator.remove();
                }
            } else if (methodName.startsWith("is")) {
                String setterName = methodName.replaceFirst("is", "set");
                if (setMethods.contains(setterName)) {
                    iterator.remove();
                }
            } else if (methodName.startsWith("set")) {
                String getterName = methodName.replaceFirst("set", "get");
                if (getMethods.contains(getterName)) {
                    iterator.remove();
                }

                String predicationName = methodName.replaceFirst("set", "is");
                if (isMethods.contains(predicationName)) {
                    iterator.remove();
                }
            }
        }
        return allMethodToAddInterceptor;
    }

    static class Transformer implements AgentBuilder.Transformer {
        private final ServiceLoader<NoneNamedListener> noneNameListenerServiceLoader;

        Transformer(ServiceLoader<NoneNamedListener> noneNameListenerServiceLoader) {
            this.noneNameListenerServiceLoader = noneNameListenerServiceLoader;
        }

        @Override
        public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription,
            ClassLoader classLoader, JavaModule module) {
            for (NoneNamedListener listener : noneNameListenerServiceLoader) {
                // 匹配class是否需要拦截
                String className = typeDescription.getName();
                byte[] classFileBuffer = builder.make().getBytes();

                List<String> methodPatterns = listener.matchClass(className, classFileBuffer);
                if (methodPatterns != null) {
                    if (typeDescription.isInterface() || typeDescription.isAnnotation()
                        || typeDescription.isEnum() || typeDescription.isArray()) {
                        return builder;
                    }
                    return interceptorMethods(builder, typeDescription, methodPatterns,
                        classLoader, listener);
                }
            }
            return builder;
        }

        private DynamicType.Builder<?> interceptorMethods(DynamicType.Builder<?> builder,
            TypeDescription typeDescription,
            List<String> methodPatterns,
            ClassLoader classLoader,
            NoneNamedListener listener) {
            MethodList<MethodDescription.InDefinedShape> methods = typeDescription.getDeclaredMethods();

            if (methods == null || methods.isEmpty()) {
                return builder;
            }
            // 找到所有的需要拦截的方法，以及对应的get方法和set方法
            List<MethodDescription.InDefinedShape> allMethodToAddInterceptor = findValidMethods(typeDescription,
                methodPatterns);
            DynamicType.Builder<?> newBuilder = builder;
            for (MethodDescription.InDefinedShape method : allMethodToAddInterceptor) {
                newBuilder = interceptorGetAndSet(newBuilder, method, classLoader, listener);
            }
            // 增加字段
            if (listener.hasAttribute()) {
                BuilderHelpers.addEnhancedField(builder);
            }
            return newBuilder;
        }

        private DynamicType.Builder<?> interceptorGetAndSet(DynamicType.Builder<?> builder,
            MethodDescription.InDefinedShape method,
            ClassLoader classLoader,
            NoneNamedListener listener) {
            DynamicType.Builder.MethodDefinition.ImplementationDefinition<?> newBuilder =
                builder.method(ElementMatchers.is(method));
            if (method.isStatic()) {
                return newBuilder.intercept(MethodDelegation.withDefaultConfiguration()
                    .withBinders(Morph.Binder.install(OverrideArgumentsCall.class))
                    .to(new OriginStaticMethodOriginEnhancer(
                        InterceptorLoader.getInterceptor(listener.getInterceptor(), classLoader, Interceptor.class))));
            } else if (method.isConstructor()) {
                return newBuilder.intercept(MethodDelegation.withDefaultConfiguration()
                    .to(new OriginConstructorEnhancer(
                        InterceptorLoader.getInterceptor(listener.getInterceptor(), classLoader, Interceptor.class))));
            } else {
                // 实例方法
                return newBuilder.intercept(MethodDelegation.withDefaultConfiguration()
                    .withBinders(Morph.Binder.install(OverrideArgumentsCall.class))
                    .to(new OriginInstanceMethodOriginEnhancer(
                        InterceptorLoader.getInterceptor(listener.getInterceptor(), classLoader, Interceptor.class))));
            }
        }
    }
}
