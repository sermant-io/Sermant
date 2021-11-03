package com.huawei.apm.core.agent.enhancer;

import java.lang.reflect.Method;
import java.util.logging.Logger;

import com.huawei.apm.core.lubanops.bootstrap.Interceptor;
import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;

/**
 * 原生插件适配委托类
 */
public abstract class OriginEnhancer {
    private static final Logger LOGGER = LogFactory.getLogger();

    protected final Interceptor interceptor;

    protected OriginEnhancer(Interceptor interceptor) {
        this.interceptor = interceptor;
    }

    protected Object[] onStart(Object obj, Object[] allArguments, Method method) {
        if (interceptor == null) {
            return allArguments;
        }
        String className = resolveClassName(obj);
        try {
            Object[] newArguments = interceptor.onStart(obj, allArguments, className, method.getName());
            if (newArguments != null && newArguments.length == allArguments.length) {
                return newArguments;
            }
        } catch (Throwable t) {
            LOGGER.severe(
                    String.format("invoke onStart method failed, class name:[{%s}], method name:[{%s}], reason:[{%s}]",
                            className, method.getName(), t.getMessage()));
        }
        return allArguments;
    }

    protected void onFinally(Object obj, Object[] allArguments, Method method, Object result) {
        if (interceptor == null) {
            return;
        }
        String className = resolveClassName(obj);
        final String methodName = method == null ? "constructor" : method.getName();
        try {
            interceptor.onFinally(obj, allArguments, result, className, methodName);
            // 返回结果不生效！
        } catch (Exception t) {
            LOGGER.severe(String.format(
                    "invoke onFinally method failed, class name:[{%s}], method name:[{%s}], reason:[{%s}]",
                    className, methodName, t.getMessage()));
        }
    }

    protected void onError(Object obj, Object[] allArguments, Method method, Throwable throwable) {
        if (interceptor == null) {
            return;
        }
        String className = resolveClassName(obj);
        try {
            interceptor.onError(obj, allArguments, throwable, className, method.getName());
        } catch (Throwable t) {
            LOGGER.severe(String.format(
                    "invoke onError method failed, class name:[{%s}], method name:[{%s}], reason:[{%s}]",
                    className, method.getName(), t.getMessage()));
        }
    }

    private String resolveClassName(Object obj) {
        if (obj == null) {
            return "";
        }
        if (obj instanceof Class) {
            return ((Class<?>) obj).getName();
        }
        return obj.getClass().getName();
    }
}
