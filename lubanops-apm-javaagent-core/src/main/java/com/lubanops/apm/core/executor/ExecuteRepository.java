package com.lubanops.apm.core.executor;

import java.util.concurrent.ExecutorService;

import com.lubanops.apm.core.executor.timer.Timer;

/**
 * @author
 * @date 2020/11/4 20:35
 */
public interface ExecuteRepository {

    /**
     * get shared timer.
     *
     * @return
     */
    Timer getSharedTimer();

    /**
     * get shared executor.
     *
     * @return
     */
    ExecutorService getSharedExecutor();

}
