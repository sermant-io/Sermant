package com.huawei.apm.bootstrap.lubanops.collector.interceptor;

/**
 * 无主键拦截器
 */
public interface StatsAroundInterceptor extends AroundInterceptor {
    /**
     * 在方法前执行
     *
     * @return 开始时间，单位纳秒
     */
    long onStart();

    /**
     * 在异常中执行
     *
     * @param t 异常
     */
    void onThrowable(Throwable t);

    /**
     * 在方法后执行
     *
     * @param timeInNanos 间隔时间，单位纳秒
     * @return 是否成功设置为最大耗时时间
     */
    boolean onFinally(long timeInNanos);
}
