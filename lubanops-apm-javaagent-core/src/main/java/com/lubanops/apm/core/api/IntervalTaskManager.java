package com.lubanops.apm.core.api;

import java.util.Map;

/**
 * @author
 * @date 2020/10/21 11:10
 */
public interface IntervalTaskManager<T extends Runnable> {

    void register(T task, int interval);

    T unRegister(int interval);

    T getTask(int interval);

    Map<Integer, T> getAllTask();

    void stopTask(int interval);

    void stopAllTasks();
}
