package com.lubanops.apm.core.master;

/**
 * Notify Listener.
 *
 * @author
 * @date 2020/10/21 15:52
 */
public interface NotifyListener<T> {

    void notify(T value);
}
