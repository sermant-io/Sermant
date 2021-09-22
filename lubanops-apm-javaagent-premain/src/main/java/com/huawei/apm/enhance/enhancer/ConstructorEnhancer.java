package com.huawei.apm.enhance.enhancer;

import com.huawei.apm.bootstrap.interceptors.ConstructorInterceptor;
import com.lubanops.apm.bootstrap.log.LogFactory;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * 多Interceptor构造方法增强委派类
 */
public final class ConstructorEnhancer {

    private final static Logger LOGGER = LogFactory.getLogger();

    private final List<ConstructorInterceptor> interceptors;

    public ConstructorEnhancer(List<ConstructorInterceptor> interceptors) {
        this.interceptors = Collections.unmodifiableList(interceptors);
    }

    /**
     * 增强委派方法
     *
     * @param obj       增强实例
     * @param arguments 原构造方法参数
     */
    @RuntimeType
    public void intercept(@This Object obj, @AllArguments Object[] arguments) {
        for (ConstructorInterceptor interceptor : interceptors) {
            try {
                interceptor.onConstruct(obj, arguments);
            } catch (Throwable t) {
                LOGGER.severe(String.format("An error occurred on construct [{%s}] in interceptor [{%s}]: [{%s}]",
                        obj.getClass().getName(), interceptor.getClass().getName(), t.getMessage()));
            }
        }
    }
}
