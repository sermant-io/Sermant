/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.sermant.dubbo.registry.service;

import io.sermant.core.common.LoggerFactory;
import io.sermant.dubbo.registry.factory.RegistryNotifyThreadFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sign up for the listening service
 *
 * @since 2022-10-25
 */
public abstract class RegistryNotifier {
    private static final int DEFAULT_DELAY_EXECUTE_TIMES = 10;

    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final ScheduledExecutorService SCHEDULER = new ScheduledThreadPoolExecutor(1,
            new RegistryNotifyThreadFactory("dubbo-registry-notify-thread"));

    private volatile long lastExecuteTime;

    private volatile long lastEventTime;

    private Object rawAddresses;

    private final long delayTime;

    private final AtomicBoolean shouldDelay = new AtomicBoolean(false);

    private final AtomicInteger executeTime = new AtomicInteger(0);

    /**
     * Constructor
     *
     * @param delayTime Delay time
     */
    public RegistryNotifier(long delayTime) {
        this.delayTime = delayTime;
    }

    /**
     * Listen to downstream instances
     *
     * @param rawAddress Address
     */
    public synchronized void notify(Object rawAddress) {
        this.rawAddresses = rawAddress;
        long notifyTime = System.currentTimeMillis();
        this.lastEventTime = notifyTime;
        long delta = (System.currentTimeMillis() - lastExecuteTime) - delayTime;
        boolean delay = shouldDelay.get() && delta < 0;
        if (delay) {
            SCHEDULER.schedule(new NotificationTask(this, notifyTime), -delta, TimeUnit.MILLISECONDS);
        } else {
            if (!shouldDelay.get() && executeTime.incrementAndGet() > DEFAULT_DELAY_EXECUTE_TIMES) {
                shouldDelay.set(true);
            }
            SCHEDULER.submit(new NotificationTask(this, notifyTime));
        }
    }

    /**
     * Listen to the instance
     *
     * @param rawAddresses Address
     */
    protected abstract void doNotify(Object rawAddresses);

    /**
     * Listen to tasks
     *
     * @since 2022-10-25
     */
    public static class NotificationTask implements Runnable {
        private final RegistryNotifier listener;

        private final long time;

        /**
         * Constructor
         *
         * @param listener Listening
         * @param time Time
         */
        public NotificationTask(RegistryNotifier listener, long time) {
            this.listener = listener;
            this.time = time;
        }

        @Override
        public void run() {
            try {
                if (this.time == listener.lastEventTime) {
                    doNotify();
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error occurred when notify directory. ", e);
            }
        }

        private void doNotify() {
            listener.doNotify(listener.rawAddresses);
            listener.lastExecuteTime = System.currentTimeMillis();
            synchronized (listener) {
                if (this.time == listener.lastEventTime) {
                    listener.rawAddresses = null;
                }
            }
        }
    }
}
