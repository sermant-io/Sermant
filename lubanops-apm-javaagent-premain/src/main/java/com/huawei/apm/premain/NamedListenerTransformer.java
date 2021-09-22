package com.huawei.apm.premain;

import com.huawei.apm.bootstrap.common.VersionChecker;
import com.huawei.apm.bootstrap.definition.MethodInterceptPoint;
import com.huawei.apm.common.OverrideArgumentsCall;
import com.huawei.apm.enhance.enhancer.OriginConstructorEnhancer;
import com.huawei.apm.enhance.enhancer.OriginInstanceMethodOriginEnhancer;
import com.huawei.apm.enhance.enhancer.OriginStaticMethodOriginEnhancer;
import com.lubanops.apm.bootstrap.Interceptor;
import com.lubanops.apm.bootstrap.Listener;
import com.lubanops.apm.bootstrap.TransformerMethod;
import com.lubanops.apm.bootstrap.utils.StringUtils;
import com.lubanops.apm.bootstrap.utils.Util;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.method.ParameterList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.huawei.apm.enhance.InterceptorLoader.getInterceptor;
import static net.bytebuddy.matcher.ElementMatchers.any;
import static net.bytebuddy.matcher.ElementMatchers.isStatic;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.not;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

/**
 * 增强NamedListener
 * 单独抽出
 */
public class NamedListenerTransformer {
    private static final NamedListenerTransformer INSTANCE = new NamedListenerTransformer();

    public static NamedListenerTransformer getInstance() {
        return INSTANCE;
    }

    public DynamicType.Builder<?> enhanceNamedListener(Listener listener,
            DynamicType.Builder<?> newBuilder,
            TypeDescription typeDescription,
            ClassLoader classLoader) {
        // 判断版本
        String version = Util.getJarVersionFromProtectionDomain(listener.getClass().getProtectionDomain());
        if (!new VersionChecker(version, listener).check()) {
            return newBuilder;
        }
        listener.addTag();
        List<MethodInterceptPoint> methodInterceptPoints = buildMethodPoints(listener, typeDescription);
        // 增强方法
        for (MethodInterceptPoint methodInterceptPoint : methodInterceptPoints) {
            if (methodInterceptPoint.isInstanceMethod()) {
                newBuilder = enhanceOriginInstMethod(methodInterceptPoint, newBuilder, classLoader);
            } else if (methodInterceptPoint.isStaticMethod()) {
                newBuilder = enhanceOriginStaticMethod(methodInterceptPoint, newBuilder, classLoader);
            } else {
                // 构造方法
                newBuilder = enhanceOriginConstructor(methodInterceptPoint, newBuilder, classLoader);
            }
        }
        // 增加fields
        return BuilderHelpers.addListenerFields(newBuilder, listener.getFields());
    }


    private DynamicType.Builder<?> enhanceOriginInstMethod(MethodInterceptPoint methodInterceptPoint,
            DynamicType.Builder<?> builder,
            ClassLoader classLoader) {
        ElementMatcher.Junction<MethodDescription> junction = not(isStatic())
                .and(methodInterceptPoint.getMatcher());
        return builder.method(junction)
                .intercept(MethodDelegation.withDefaultConfiguration()
                        .withBinders(Morph.Binder.install(OverrideArgumentsCall.class))
                        .to(new OriginInstanceMethodOriginEnhancer(
                                getInterceptor(methodInterceptPoint.getInterceptor(), classLoader, Interceptor.class))));
    }

    private DynamicType.Builder<?> enhanceOriginStaticMethod(MethodInterceptPoint methodInterceptPoint,
            DynamicType.Builder<?> builder,
            ClassLoader classLoader) {
        ElementMatcher.Junction<MethodDescription> junction = isStatic()
                .and(methodInterceptPoint.getMatcher());
        return builder.method(junction)
                .intercept(MethodDelegation.withDefaultConfiguration()
                        .withBinders(Morph.Binder.install(OverrideArgumentsCall.class))
                        .to(new OriginStaticMethodOriginEnhancer(
                                getInterceptor(
                                        methodInterceptPoint.getInterceptor(), classLoader, Interceptor.class))));
    }

    private DynamicType.Builder<?> enhanceOriginConstructor(MethodInterceptPoint methodInterceptPoint,
            DynamicType.Builder<?> builder,
            ClassLoader classLoader) {
        return builder.constructor(methodInterceptPoint.getMatcher())
                .intercept(SuperMethodCall.INSTANCE.andThen(MethodDelegation.withDefaultConfiguration()
                        .to(new OriginConstructorEnhancer(
                                getInterceptor(
                                        methodInterceptPoint.getInterceptor(), classLoader, Interceptor.class)))));
    }

    private List<MethodInterceptPoint> buildMethodPoints(Listener listener, TypeDescription typeDescription) {
        List<MethodInterceptPoint> points = new ArrayList<MethodInterceptPoint>();
        List<TransformerMethod> transformerMethods = listener.getTransformerMethod();
        if (transformerMethods == null || transformerMethods.size() == 0) {
            return points;
        }
        MethodList<MethodDescription.InDefinedShape> declaredMethods = typeDescription.getDeclaredMethods();
        for (TransformerMethod method : transformerMethods) {
            ElementMatcher.Junction<MethodDescription> junction = any();
            if (method.getMethod() == null) {
                Set<String> excludeMethods = method.getExcludeMethods();
                if (excludeMethods == null || excludeMethods.size() == 0) {
                    continue;
                }
                // 增强所有未排除的方法
                for (MethodDescription.InDefinedShape methodDescription : declaredMethods) {
                    if (excludeMethods.contains(methodDescription.getName())) {
                        continue;
                    }
                    points.add(buildMethodInterceptorPoint(junction.and(named(methodDescription.getName())),
                            method.getInterceptor(), methodDescription));
                }
            } else {
                if (method.isConstructor()) {
                    points.add(MethodInterceptPoint.newConstructorInterceptPoint(
                            method.getInterceptor(), junction));
                } else {
                    junction = junction.and(named(method.getMethod()));
                    // 参数转换
                    List<String> params = convertParams(method.getParams());
                    // 构造参数条件
                    junction = buildParamsJunction(method, junction, params);
                    ElementMatcher.Junction<MethodDescription> methodJunction = junction;
                    for (MethodDescription.InDefinedShape methodDescription : declaredMethods) {
                        if (!StringUtils.equals(methodDescription.getName(), method.getMethod())
                                || !isMatchParams(params, methodDescription)) {
                            continue;
                        }
                        points.add(buildMethodInterceptorPoint(methodJunction, method.getInterceptor(), methodDescription));
                    }
                }
            }
        }
        return points;
    }

    private ElementMatcher.Junction<MethodDescription> buildParamsJunction(TransformerMethod method, ElementMatcher.Junction<MethodDescription> junction, List<String> params) {
        if (params.size() > 0) {
            junction = junction.and(ElementMatchers.takesArguments(method.getParams().size()));
            for (int i = 0; i < params.size(); i++) {
                try {
                    junction = junction.and(takesArgument(i, Class.forName(params.get(i))));
                } catch (ClassNotFoundException e) {
                    // 若未加载到 则用户应用无该类，无需增强
                }
            }
        }
        return junction;
    }

    private MethodInterceptPoint buildMethodInterceptorPoint(ElementMatcher.Junction<MethodDescription> junction, String interceptor, MethodDescription.InDefinedShape methodDescription) {
        if (methodDescription.isStatic()) {
            return MethodInterceptPoint.newStaticMethodInterceptPoint(interceptor, junction);
        }
        return MethodInterceptPoint.newInstMethodInterceptPoint(interceptor, junction);
    }

    private boolean isMatchParams(List<String> params, MethodDescription.InDefinedShape methodDescription) {
        ParameterList<ParameterDescription.InDefinedShape> parameters = methodDescription.getParameters();
        if (parameters.size() == params.size()) {
            // 比较参数类型
            for (int i = 0; i < parameters.size(); i++) {
                if (!StringUtils.equals(parameters.get(i).getType().asRawType().getTypeName(), params.get(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 转换基本参数类型定义
     *
     * @param params 待转换参数类型
     * @return 转换后参数类型
     */
    private List<String> convertParams(List<String> params) {
        List<String> convertedParams = new ArrayList<String>();
        if (params == null) {
            return convertedParams;
        }
        for (String param : params) {
            if ("long".equalsIgnoreCase(param)) {
                convertedParams.add(Long.class.getName());
            } else if ("string".equalsIgnoreCase(param)) {
                convertedParams.add(String.class.getName());
            } else if ("int".equalsIgnoreCase(param)) {
                convertedParams.add(Integer.class.getName());
            } else if ("float".equalsIgnoreCase(param)) {
                convertedParams.add(Float.class.getName());
            } else if ("double".equalsIgnoreCase(param)) {
                convertedParams.add(Double.class.getName());
            } else {
                convertedParams.add(param);
            }
        }
        return convertedParams;
    }
}
