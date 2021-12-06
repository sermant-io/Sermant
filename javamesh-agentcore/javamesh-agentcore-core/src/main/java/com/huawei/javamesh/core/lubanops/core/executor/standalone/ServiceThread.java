/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.javamesh.core.lubanops.core.executor.standalone;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.huawei.javamesh.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.javamesh.core.lubanops.core.common.CountDownLatch2;

public abstract class ServiceThread implements Runnable {

    private static final Logger LOGGER = LogFactory.getLogger();

    private static final long JOIN_TIME = 90 * 1000;

    protected final Thread thread;

    protected final CountDownLatch2 waitPoint = new CountDownLatch2(1);

    private final Object startupShutdownMonitor = new Object();

    protected volatile AtomicBoolean hasNotified = new AtomicBoolean(false);

    protected volatile boolean stopped = true;

    protected volatile boolean active;

    protected volatile long defer = 60 * 1000;

    public ServiceThread() {
        this.thread = new Thread(this, this.getServiceName());
        this.thread.setDaemon(true);
    }

    public abstract String getServiceName();

    public void start(final int defer) {
        synchronized (this.startupShutdownMonitor) {
            if (!this.active && this.stopped) {
                this.stopped = false;
                this.active = true;
                this.defer = defer;
                this.thread.start();
            }
        }
    }

    public void start() {
        synchronized (this.startupShutdownMonitor) {
            if (!this.active && this.stopped) {
                this.stopped = false;
                this.active = true;
                this.thread.start();
            }
        }
    }

    public void shutdown() {
        synchronized (this.startupShutdownMonitor) {
            if (this.active && !this.stopped) {
                this.active = false;
                this.shutdown(false);
            }
        }
    }

    public void shutdown(final boolean interrupt) {
        this.stopped = true;
        LOGGER.info("shutdown thread " + this.getServiceName() + " interrupt " + interrupt);

        if (hasNotified.compareAndSet(false, true)) {
            waitPoint.countDown(); // notify
        }

        try {
            if (interrupt) {
                this.thread.interrupt();
            }

            long beginTime = System.currentTimeMillis();
            if (!this.thread.isDaemon()) {
                this.thread.join(this.getJointime());
            }
            long eclipseTime = System.currentTimeMillis() - beginTime;
            LOGGER.info(
                "join thread " + this.getServiceName() + " eclipse time(ms) " + eclipseTime + " " + this.getJointime());
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "Interrupted", e);
        }
    }

    public long getJointime() {
        return JOIN_TIME;
    }

    public void stop() {
        this.stop(false);
    }

    public void stop(final boolean interrupt) {
        this.stopped = true;
        this.active = false;
        LOGGER.info("stop thread " + this.getServiceName() + " interrupt " + interrupt);

        if (hasNotified.compareAndSet(false, true)) {
            waitPoint.countDown(); // notify
        }

        if (interrupt) {
            this.thread.interrupt();
        }
    }

    public void makeStop() {
        this.stopped = true;
        LOGGER.info("makestop thread " + this.getServiceName());
    }

    public void wakeup() {
        if (hasNotified.compareAndSet(false, true)) {
            waitPoint.countDown(); // notify
        }
    }

    public void restart(final int defer) {
        this.stop(false);
        this.defer = defer;
        this.start();
    }

    protected void waitForRunning(long interval) {
        if (hasNotified.compareAndSet(true, false)) {
            this.onWaitEnd();
            return;
        }

        // entry to wait
        waitPoint.reset();

        try {
            waitPoint.await(interval, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "Interrupted", e);

        } finally {
            hasNotified.set(false);
            this.onWaitEnd();
        }
    }

    protected abstract void onWaitEnd();

    public boolean isStopped() {
        return stopped;
    }

}
