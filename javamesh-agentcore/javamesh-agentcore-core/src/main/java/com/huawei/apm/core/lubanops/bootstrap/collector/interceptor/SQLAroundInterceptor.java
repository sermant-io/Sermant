package com.huawei.apm.core.lubanops.bootstrap.collector.interceptor;

/**
 * 单主键拦截器
 */
public interface SQLAroundInterceptor extends AroundInterceptor {
    /**
     * 在方法前执行
     *
     * @param sql 主键
     * @return 开始时间，单位纳秒
     */
    long onStart(String sql);

    /**
     * 在异常中执行
     *
     * @param sql SQL语句
     * @param t   异常
     */
    void onThrowable(String sql, Throwable t);

    /**
     * 在方法后执行
     *
     * @param sql             SQL语句
     * @param updatedRowCount 更新行数
     * @param readRowCount    读取行数
     * @return 耗时，单位纳秒
     */
    long onFinally(String sql, int updatedRowCount, int readRowCount);
}
