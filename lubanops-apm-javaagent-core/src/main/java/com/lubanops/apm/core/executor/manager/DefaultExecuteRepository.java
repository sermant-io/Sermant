package com.lubanops.apm.core.executor.manager;

import com.google.inject.Singleton;
import com.lubanops.apm.bootstrap.exception.ApmRuntimeException;
import com.lubanops.apm.core.api.AgentService;
import com.lubanops.apm.core.common.NamedThreadFactory;
import com.lubanops.apm.core.executor.ExecuteRepository;
import com.lubanops.apm.core.executor.timer.HashedWheelTimer;
import com.lubanops.apm.core.executor.timer.Timer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author
 * @date 2020/11/4 20:39
 */
@Singleton
public class DefaultExecuteRepository implements ExecuteRepository, AgentService {

    public final static int TICKS_PER_WHEEL = 128;

    private ExecutorService sharedExecutor;

    private HashedWheelTimer heartbeatTimer;

    private volatile boolean closed = false;

    @Override
    public Timer getSharedTimer() {
        return heartbeatTimer;
    }

    @Override
    public ExecutorService getSharedExecutor() {
        return sharedExecutor;
    }

    @Override
    public void init() throws ApmRuntimeException {
        sharedExecutor = new ThreadPoolExecutor(50, 100, 0, TimeUnit.MICROSECONDS,
            new LinkedBlockingQueue<Runnable>(128), new NamedThreadFactory("ApmSharedExecutor", true),
            new ThreadPoolExecutor.AbortPolicy());
        heartbeatTimer = new HashedWheelTimer(new NamedThreadFactory("ApmWheelTimer", true), 1, TimeUnit.SECONDS,
            TICKS_PER_WHEEL);
    }

    @Override
    public void dispose() throws ApmRuntimeException {
        if (closed) {
            return;
        }
        closed = true;
        if (null != sharedExecutor) {
            sharedExecutor.shutdown();
            heartbeatTimer.stop();
        }
    }

    @Override
    public int getPriority() {
        return EARLY_EXPOSE;
    }
}
