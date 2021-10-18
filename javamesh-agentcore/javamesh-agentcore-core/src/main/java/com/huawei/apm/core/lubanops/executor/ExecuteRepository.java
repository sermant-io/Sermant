package com.huawei.apm.core.lubanops.executor;

import java.util.concurrent.ExecutorService;

import com.huawei.apm.core.lubanops.executor.timer.Timer;

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
