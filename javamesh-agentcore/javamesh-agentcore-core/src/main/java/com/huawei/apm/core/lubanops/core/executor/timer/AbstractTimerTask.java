package com.huawei.apm.core.lubanops.core.executor.timer;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;

/**
 * Abstract Timer Task.
 *
 * @author
 */
public abstract class AbstractTimerTask implements TimerTask {
    private static final Logger LOGGER = LogFactory.getLogger();

    protected volatile boolean cancel = false;

    private volatile Long tick;

    public AbstractTimerTask(Long tick) {
        this.tick = tick;
    }

    static Long now() {
        return System.currentTimeMillis();
    }

    public void cancel() {
        this.cancel = true;
    }

    public void reput(Timeout timeout, Long tick) {
        if (timeout == null || tick == null) {
            throw new IllegalArgumentException();
        }

        if (cancel) {
            return;
        }

        Timer timer = timeout.timer();
        if (timer.isStop() || timeout.isCancelled()) {
            return;
        }

        timer.newTimeout(timeout.task(), tick, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run(Timeout timeout) throws Exception {
        LOGGER.info(String.format("task[%s]tick[%s] running.", getName(), tick));
        doTask();
        reput(timeout, tick);
    }

    public abstract void doTask();

    public Long getTick() {
        return tick;
    }

    public void setTick(Long tick) {
        this.tick = tick;
    }
}
