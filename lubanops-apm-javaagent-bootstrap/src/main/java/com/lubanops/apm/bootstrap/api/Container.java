package com.lubanops.apm.bootstrap.api;

/**
 * Container. (Singleton, ThreadSafe)
 *
 * @author
 * @date 2020/10/15 20:54
 */
public interface Container {

    /**
     * start method to load the container.
     */
    void start();

    /**
     * stop method to unload the container.
     */
    void stop();

    /**
     * get agent service from container.
     *
     * @param clazz
     * @param <T>
     * @return
     */
    <T> T getService(Class<T> clazz);
}
