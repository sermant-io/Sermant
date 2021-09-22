package com.lubanops.apm.bootstrap.api;

/**
 * @author
 * @date 2020/12/17 14:35
 */
public interface CircuitBreaker {

    /**
     * allow request pass?
     *
     * @return
     */
    public boolean allowRequest();

    /**
     * is circuit open
     *
     * @return
     */
    public boolean isOpen();

    /**
     * mark success.
     */
    void markSuccess();

    /**
     * mark failure.
     */
    void markFailure();
}
