package com.huawei.apm.premain.enhance.enhancer;

import com.huawei.apm.bootstrap.common.BeforeResult;
import com.huawei.apm.bootstrap.exception.FlowControlException;
import com.huawei.apm.bootstrap.interceptors.InstanceMethodInterceptor;
import com.huawei.apm.bootstrap.lubanops.Interceptor;
import com.huawei.apm.premain.common.OverrideArgumentsCall;
import com.huawei.apm.bootstrap.lubanops.log.LogFactory;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * 多Interceptor实例方法增强委派类
 */
public final class InstanceMethodEnhancer extends AbstractAroundEnhancer {

    private final static Logger LOGGER = LogFactory.getLogger();

    private final List<InstanceMethodInterceptor> interceptors;

    public InstanceMethodEnhancer(Interceptor originInterceptor, List<InstanceMethodInterceptor> interceptors) {
        super(originInterceptor);
        this.interceptors = Collections.unmodifiableList(interceptors);
    }

    /**
     * 增强委派方法
     *
     * @param obj       增强实例
     * @param method    原方法
     * @param callable  原始调用
     * @param arguments 原方法参数
     * @return 增强后返回值
     * @throws Throwable 增强过程中产生的异常，当前实现使用原方法执行产生异常
     */
    @RuntimeType
    public Object intercept(@This Object obj,
            @Origin Method method,
            @Morph OverrideArgumentsCall callable,
            @AllArguments Object[] arguments) throws Throwable {
        return doIntercept(obj, method, callable, arguments);
    }

    @Override
    protected BeforeResult doBefore(final EnhanceContext context) throws Throwable {
        BeforeResult beforeResult = new BeforeResult();
        for (InstanceMethodInterceptor interceptor : interceptors) {
            context.increaseInvokedIndex();
            execBefore(interceptor, context, beforeResult);
            if (!beforeResult.isContinue()) {
                break;
            }
        }
        return beforeResult;
    }

    @Override
    protected void doOnThrow(final EnhanceContext context,
            final Throwable originThrowable) {
        for (int i = context.getInvokedIndex() - 1; i >= 0; i--) {
            InstanceMethodInterceptor interceptor = interceptors.get(i);
            execOnThrow(interceptor, context, originThrowable);
        }
    }

    @Override
    protected Object doAfter(final EnhanceContext context, final Object result) {
        Object returnResult = result;
        for (int i = context.getInvokedIndex() - 1; i >= 0; i--) {
            InstanceMethodInterceptor interceptor = interceptors.get(i);
            returnResult = execAfter(interceptor, context, returnResult);
        }
        return returnResult;
    }

    private void execBefore(final InstanceMethodInterceptor interceptor,
            final EnhanceContext context,
            final BeforeResult beforeResult) throws Throwable {
        Object origin = context.getOrigin();
        Method method = context.getMethod();
        try {
            interceptor.before(origin, method, context.getArguments(), beforeResult);
        } catch (Throwable t) {
            LOGGER.severe(String.format("An error occurred before [{%s}#{%s}] in interceptor [{%s}]: [{%s}]",
                    origin.getClass().getName(), method.getName(), interceptor.getClass().getName(), t.getMessage()));
            if (t instanceof FlowControlException) {
                // 流控异常特别梳理，需将异常抛给用户，让用户自身做处理
                throw t;
            }
        }
    }

    private void execOnThrow(final InstanceMethodInterceptor interceptor,
            final EnhanceContext context,
            final Throwable originThrowable) {
        Object origin = context.getOrigin();
        Method method = context.getMethod();
        try {
            interceptor.onThrow(origin, method, context.getArguments(), originThrowable);
        } catch (Throwable t) {
            LOGGER.severe(String.format("An error occurred while handling throwable thrown by"
                            + " [{%s}#{%s}] in interceptor [{%s}]: [{%s}].",
                    origin.getClass().getName(), method.getName(), interceptor.getClass().getName(), t.getMessage()));
        }
    }

    private Object execAfter(final InstanceMethodInterceptor interceptor,
            final EnhanceContext context,
            final Object result) {
        Object returnResult = result;
        Object origin = context.getOrigin();
        Method method = context.getMethod();
        try {
            returnResult = interceptor.after(origin, method, context.getArguments(), returnResult);
        } catch (Throwable t) {
            LOGGER.severe(String.format("An error occurred after [{%s}#{%s}] in interceptor [{%s}]: [{%s}].",
                    origin.getClass().getName(), method.getName(), interceptor.getClass().getName(), t.getMessage()));
        }
        return returnResult;
    }
}
