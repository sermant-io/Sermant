package com.huawei.javamesh.core.agent.transformer;

import com.huawei.javamesh.core.agent.EnhanceDefinitionLoader;
import com.huawei.javamesh.core.agent.common.OverrideArgumentsCall;
import com.huawei.javamesh.core.agent.definition.EnhanceDefinition;
import com.huawei.javamesh.core.agent.interceptor.ConstructorInterceptor;
import com.huawei.javamesh.core.agent.interceptor.InstanceMethodInterceptor;
import com.huawei.javamesh.core.agent.interceptor.StaticMethodInterceptor;
import com.huawei.javamesh.core.lubanops.bootstrap.AttributeAccess;
import com.huawei.javamesh.core.lubanops.bootstrap.Interceptor;
import com.huawei.javamesh.core.lubanops.bootstrap.TransformAccess;
import com.huawei.javamesh.core.agent.interceptor.InterceptorLoader;
import com.huawei.javamesh.core.agent.enhancer.ConstructorEnhancer;
import com.huawei.javamesh.core.agent.enhancer.InstanceMethodEnhancer;
import com.huawei.javamesh.core.agent.enhancer.MemberFieldsHandler;
import com.huawei.javamesh.core.agent.enhancer.StaticMethodEnhancer;
import com.huawei.javamesh.core.lubanops.bootstrap.Listener;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.named;

/**
 * 委派Transformer，支持多次增强
 */
public class DelegateTransformer implements AgentBuilder.Transformer {
    private static final String ENHANCED_FIELD_NAME = "_$lopsAttribute_enhanced";

    @Override
    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription,
            ClassLoader classLoader, JavaModule module) {
        final EnhanceDefinitionLoader loader = EnhanceDefinitionLoader.getInstance();
        final Listener listener = loader.findNameListener(typeDescription);
        final List<EnhanceDefinition> definitions = loader.findDefinitions(typeDescription);
        if (listener == null && definitions.isEmpty()) {
            return builder;
        }
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
                newBuilder = addListenerFields(newBuilder, listener.getFields());
            }
            methodHolder.addInterceptors(definitions);
            newBuilder = methodHolder.build(newBuilder, classLoader);
            flag |= methodHolder.hasOriginInterceptor();
        }
        if (flag) {
            newBuilder = addEnhancedField(newBuilder);
        }
        return newBuilder;
    }

    private static DynamicType.Builder<?> addEnhancedField(DynamicType.Builder<?> newBuilder) {
        return newBuilder.defineField(ENHANCED_FIELD_NAME, Object.class, Opcodes.ACC_PRIVATE)
                .implement(TransformAccess.class)
                .intercept(FieldAccessor.ofField(ENHANCED_FIELD_NAME));
    }

    /**
     * 添加成员变量
     *
     * @param newBuilder 构建器
     * @param fields     定义的成员变量属性
     * @return 构建器
     */
    private static DynamicType.Builder<?> addListenerFields(DynamicType.Builder<?> newBuilder, List<String> fields) {
        if (fields == null || fields.size() == 0) {
            return newBuilder;
        }
        return newBuilder.implement(AttributeAccess.class)
                .method(named("getLopsFileds"))
                .intercept(MethodDelegation.withDefaultConfiguration().to(new MemberFieldsHandler(fields)));
    }

    private static class MultiInterMethodHolder extends MethodInterceptorCollector {
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
            final Interceptor originInterceptor;
            if (originInterceptorName == null) {
                originInterceptor = null;
            } else {
                originInterceptor = InterceptorLoader.getInterceptor(
                        originInterceptorName, classLoader, Interceptor.class);
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
                Interceptor originInterceptor,
                ClassLoader classLoader, DynamicType.Builder<?> newBuilder) {
            final List<InstanceMethodInterceptor> instInterceptors =
                    InterceptorLoader.getInterceptors(interceptorNames, classLoader, InstanceMethodInterceptor.class);
            final MethodDelegation delegation = MethodDelegation.withDefaultConfiguration()
                    .withBinders(Morph.Binder.install(OverrideArgumentsCall.class))
                    .to(new InstanceMethodEnhancer(originInterceptor, instInterceptors));
            return newBuilder.method(ElementMatchers.is(method)).intercept(delegation);
        }

        private DynamicType.Builder<?> enhanceStaticMethod(
                Interceptor originInterceptor,
                ClassLoader classLoader, DynamicType.Builder<?> newBuilder) {
            final List<StaticMethodInterceptor> staticInterceptors =
                    InterceptorLoader.getInterceptors(interceptorNames, classLoader, StaticMethodInterceptor.class);
            final Implementation delegation = MethodDelegation.withDefaultConfiguration()
                    .withBinders(Morph.Binder.install(OverrideArgumentsCall.class))
                    .to(new StaticMethodEnhancer(originInterceptor, staticInterceptors));
            return newBuilder.method(ElementMatchers.is(method)).intercept(delegation);
        }

        private DynamicType.Builder<?> enhanceConstructor(
                Interceptor originInterceptor,
                ClassLoader classLoader, DynamicType.Builder<?> newBuilder) {
            final List<ConstructorInterceptor> constInterceptors =
                    InterceptorLoader.getInterceptors(interceptorNames, classLoader, ConstructorInterceptor.class);
            final Implementation delegation = SuperMethodCall.INSTANCE.andThen(MethodDelegation
                    .withDefaultConfiguration().to(new ConstructorEnhancer(originInterceptor, constInterceptors)));
            return newBuilder.constructor(ElementMatchers.is(method)).intercept(delegation);
        }
    }
}
