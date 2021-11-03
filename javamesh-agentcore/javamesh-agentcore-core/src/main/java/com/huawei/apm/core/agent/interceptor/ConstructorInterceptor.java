package com.huawei.apm.core.agent.interceptor;

/**
 * 构造方法拦截器接口
 */
public interface ConstructorInterceptor extends Interceptor {

    void onConstruct(Object obj, Object[] allArguments);
}
