package com.huawei.apm.core.agent.enhancer;

import com.huawei.apm.core.agent.interceptor.ConstructorInterceptor;
import com.huawei.apm.core.lubanops.bootstrap.Interceptor;
import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * 多Interceptor构造方法增强委派类
 */
public final class ConstructorEnhancer extends OriginEnhancer {

    private final static Logger LOGGER = LogFactory.getLogger();

    private final List<ConstructorInterceptor> interceptors;

    public ConstructorEnhancer(Interceptor originInterceptor, List<ConstructorInterceptor> interceptors) {
        super(originInterceptor);
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
        onFinally(obj, arguments, null, null);
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
