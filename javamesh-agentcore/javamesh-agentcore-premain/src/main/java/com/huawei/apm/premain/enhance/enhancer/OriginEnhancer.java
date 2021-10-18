package com.huawei.apm.premain.enhance.enhancer;

import com.huawei.apm.premain.common.OverrideArgumentsCall;
import com.huawei.apm.bootstrap.lubanops.Interceptor;
import com.huawei.apm.bootstrap.lubanops.log.LogFactory;
import com.huawei.apm.bootstrap.lubanops.utils.StringUtils;

import java.lang.reflect.Method;
import java.util.logging.Logger;

/**
 * 原生插件适配委托类
 */
public abstract class OriginEnhancer {
    private static final Logger LOGGER = LogFactory.getLogger();

    protected final Interceptor interceptor;

    protected OriginEnhancer(Interceptor interceptor) {
        this.interceptor = interceptor;
    }

    protected Object process(Object obj, Object[] allArguments, OverrideArgumentsCall callable, Method method) throws Exception {
        return handleRequest(obj, allArguments, callable, method);
    }

    private Object handleRequest(Object obj, Object[] allArguments, OverrideArgumentsCall callable, Method method) throws Exception {
        String className = resolveClassName(obj);
        try {
            Object[] newArguments = interceptor.onStart(obj, allArguments, className, method.getName());
            if (newArguments != null && newArguments.length == allArguments.length) {
                allArguments = newArguments;
            }
        } catch (Throwable t) {
            LOGGER.severe(String.format("invoke onStart method failed, class name:[{%s}], method name:[{%s}], reason:[{%s}]",
                    className, method.getName(), t.getMessage()));
        }

        Object result = null;
        try {
            result = callable.call(allArguments);
        } catch (Exception t) {
            try {
                interceptor.onError(obj, allArguments, t, className, method.getName());
            } catch (Throwable t1) {
                LOGGER.severe(String.format("invoke onError method failed, class name:[{%s}], method name:[{%s}], reason:[{%s}]",
                        className, method.getName(), t.getMessage()));
            }
            throw t;
        } finally {
            try {
                interceptor.onFinally(obj, allArguments, result, className, method.getName());
                // 返回结果不生效！
            } catch (Exception t) {
                LOGGER.severe(String.format("invoke onFinally method failed, class name:[{%s}], method name:[{%s}], reason:[{%s}]",
                        className, method.getName(), t.getMessage()));
            }
        }
        return result;
    }

    private String resolveClassName(Object obj) {
        if (obj == null) {
            return "";
        }
        String className = obj.getClass().getName();
        if (StringUtils.equals("java.lang.Class", className)) {
            return ((Class<?>) obj).getName();
        }
        return className;
    }
}
