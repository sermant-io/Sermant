package com.huawei.flowrecord;

import java.util.concurrent.ThreadFactory;

public class FlowrecordThreadFactory implements ThreadFactory {
    private final String threadName;

    public FlowrecordThreadFactory(String threadName) {
        this.threadName = threadName;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        return new Thread(runnable, threadName);
    }
}
