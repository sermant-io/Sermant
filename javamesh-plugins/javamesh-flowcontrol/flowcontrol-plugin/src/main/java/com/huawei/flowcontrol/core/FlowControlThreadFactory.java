package com.huawei.flowcontrol.core;

import java.util.concurrent.ThreadFactory;

/**
 * 自定义线程工厂
 * 区分线程
 */
public class FlowControlThreadFactory implements ThreadFactory {
    private final String threadName;

    public FlowControlThreadFactory(String threadName) {
        this.threadName = threadName;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        return new Thread(runnable, threadName);
    }
}
