package com.lubanops.apm.plugin.threadlocal;

import com.alibaba.ttl.TtlCallable;
import com.alibaba.ttl.TtlRunnable;
import com.huawei.apm.core.agent.common.BeforeResult;
import com.huawei.apm.core.agent.interceptor.InstanceMethodInterceptor;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * 线程池增强，替换Runnable和Callable参数
 *
 * @author y00556973
 * @since 2021/10/11
 */
public class ThreadPoolInterceptor implements InstanceMethodInterceptor {
    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) {
        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i] instanceof Runnable) {
                arguments[i] = TtlRunnable.get((Runnable) arguments[i], false, true);
                continue;
            }
            if (arguments[i] instanceof Callable) {
                arguments[i] = TtlCallable.get((Callable<?>) arguments[i], false, true);
            }
        }
    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) {
        return result;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable throwable) {
    }
}
