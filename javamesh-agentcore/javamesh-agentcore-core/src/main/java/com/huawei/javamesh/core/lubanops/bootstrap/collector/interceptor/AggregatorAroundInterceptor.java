package com.huawei.javamesh.core.lubanops.bootstrap.collector.interceptor;

/**
 * 单主键拦截器
 */
public interface AggregatorAroundInterceptor extends AroundInterceptor {
    /**
     * 在方法前执行
     *
     * @param key 主键
     * @return 开始时间，单位纳秒
     */
    long onStart(String key);

    /**
     * 在异常中执行
     *
     * @param t 异常
     */
    void onThrowable(Throwable t);

    /**
     * 在方法后执行
     *
     * @return 耗时间隔，单位毫秒
     */
    long onFinally();
}
