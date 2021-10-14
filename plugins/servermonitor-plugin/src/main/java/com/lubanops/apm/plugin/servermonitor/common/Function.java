package com.lubanops.apm.plugin.servermonitor.common;

/**
 * JDK: java.util.function.Function
 *
 * @param <T>
 * @param <R>
 */
public interface Function<T, R> {

    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     */
    R apply(T t);
}
