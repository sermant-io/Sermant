package com.huawei.javamesh.core.agent.enhancer;

import com.huawei.javamesh.core.agent.common.BeforeResult;
import com.huawei.javamesh.core.agent.common.OverrideArgumentsCall;
import com.huawei.javamesh.core.exception.BizException;
import com.huawei.javamesh.core.lubanops.bootstrap.Interceptor;

import java.lang.reflect.Method;

/**
 * 抽象环绕增强委派类
 */
public abstract class AbstractAroundEnhancer  extends OriginEnhancer {
    protected AbstractAroundEnhancer(Interceptor originInterceptor) {
        super(originInterceptor);
    }

    /**
     * 环绕增强方法
     *
     * @param origin    增强实例或其class，由具体子类型指定
     * @param method    原方法
     * @param callable  原始调用
     * @param arguments 原方法参数
     * @return 增强后返回值
     * @throws Throwable 增强过程中产生的异常，当前实现使用原方法执行产生异常
     */
    protected Object doIntercept(final Object origin,
            final Method method,
            final OverrideArgumentsCall callable,
            Object[] arguments) throws Throwable {
        arguments = onStart(origin, arguments, method);
        final EnhanceContext context = new EnhanceContext(origin, method, arguments);
        BeforeResult beforeResult = doBefore(context);
        Object result = null;
        try {
            if (beforeResult.isContinue()) {
                result = callable.call(arguments);
            } else {
                result = beforeResult.getResult();
            }
        } catch (Throwable t) {
            doOnThrow(context, t);
            onError(origin, arguments, method, t);
            throw t;
        } finally {
            result = doAfter(context, result);
            onFinally(origin, arguments, method, result);
        }
        return result;
    }

    /**
     * 抛出业务异常
     *
     * @param throwable 拦截器执行异常
     */
    protected void throwBizException(Throwable throwable) {
        if (throwable instanceof BizException) {
            throw (BizException) throwable;
        }
    }

    /**
     * 执行拦截器前置方法，由子类实现
     *
     * @param context 增强上下文实例
     * @return 前置结果
     */
    protected abstract BeforeResult doBefore(final EnhanceContext context) throws Throwable;

    /**
     * 执行拦截器异常处理方法，由子类实现
     *
     * @param context 增强上下文实例
     * @param t       原方法异常
     */
    protected abstract void doOnThrow(final EnhanceContext context, final Throwable t);

    /**
     * 执行拦截器后置方法，由子类实现
     *
     * @param context 增强上下文实例
     * @param result  原方法返回值或前置方法执行结果
     * @return 最终返回结果
     */
    protected abstract Object doAfter(final EnhanceContext context, final Object result);

    /**
     * 增强上下文，封装了增强过程中的参数
     */
    protected static class EnhanceContext {

        private int invokedIndex;

        private final Object origin;

        private final Method method;

        private final Object[] arguments;

        public EnhanceContext(Object origin, Method method, Object[] arguments) {
            this.origin = origin;
            this.method = method;
            this.arguments = arguments;
        }

        public void increaseInvokedIndex() {
            invokedIndex++;
        }

        public int getInvokedIndex() {
            return invokedIndex;
        }

        public Object getOrigin() {
            return origin;
        }

        public Method getMethod() {
            return method;
        }

        public Object[] getArguments() {
            return arguments;
        }
    }
}
