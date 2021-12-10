package com.huawei.javamesh.backend.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class BackendThreadFactory implements ThreadFactory {
    private final static AtomicInteger FACTORY_NUMBER = new AtomicInteger(0);

    private final AtomicInteger threadNumber = new AtomicInteger(0);

    private final String threadPrefix;

    private final boolean daemon;

    public BackendThreadFactory() {
        this("backend", true);
    }

    public BackendThreadFactory(String threadName) {
        this(threadName, true);
    }

    public BackendThreadFactory(String threadName, boolean daemon) {
        this.threadPrefix = prefix(threadName, FACTORY_NUMBER.getAndIncrement());
        this.daemon = daemon;
    }

    public static ThreadFactory createThreadFactory(String threadName) {
        return createThreadFactory(threadName, false);
    }

    public static ThreadFactory createThreadFactory(String threadName, boolean daemon) {
        return new BackendThreadFactory(threadName, daemon);
    }

    private String prefix(String threadName, int factoryId) {
        return threadName + '(' + factoryId + ')';
    }

    @Override
    public Thread newThread(Runnable job) {
        String newThreadName = createThreadName();
        Thread thread = new Thread(job, newThreadName);
        if (daemon) {
            thread.setDaemon(true);
        }
        return thread;
    }

    private String createThreadName() {
        return threadPrefix + threadNumber.getAndIncrement() + ')';
    }
}
