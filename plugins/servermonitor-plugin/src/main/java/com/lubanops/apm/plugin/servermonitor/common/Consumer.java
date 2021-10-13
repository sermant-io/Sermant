package com.lubanops.apm.plugin.servermonitor.common;

/**
 * JDK: java.util.function.Consumer
 *
 * @param <T>
 */
public interface Consumer<T> {

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     */
    void accept(T t);
}
