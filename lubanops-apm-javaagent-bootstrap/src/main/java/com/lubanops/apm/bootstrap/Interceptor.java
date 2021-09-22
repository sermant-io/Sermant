package com.lubanops.apm.bootstrap;

public interface Interceptor extends com.huawei.apm.bootstrap.interceptors.Interceptor {

    /**
     * 重要：该方法一般都是返回null，否则可能会影响用户业务
     *
     * @param object 拦截方法的this对象
     * @param args   拦截方法的参数
     * @return
     */
    Object[] onStart(Object object, Object[] args, String className, String methodName);

    void onError(Object object, Object[] args, Throwable e, String className, String methodName);

    void onFinally(Object object, Object[] args, Object result, String className, String methodName);

}
