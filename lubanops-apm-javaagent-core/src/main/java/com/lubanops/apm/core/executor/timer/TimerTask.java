package com.lubanops.apm.core.executor.timer;

import java.util.concurrent.TimeUnit;

/**
 * A task which is executed after the delay specified with
 * {@link Timer#newTimeout(TimerTask, long, TimeUnit)} (TimerTask, long, TimeUnit)}.
 */
public interface TimerTask {

    void run(Timeout timeout) throws Exception;

    String getName();
}