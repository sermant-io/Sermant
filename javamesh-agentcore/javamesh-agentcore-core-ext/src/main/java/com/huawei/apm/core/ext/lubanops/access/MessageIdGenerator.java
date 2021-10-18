package com.huawei.apm.core.ext.lubanops.access;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 在一个jvm里面消息的id的产生器，只会递增
 *
 * @author
 * @since 2020/5/5
 **/
public class MessageIdGenerator {
    private static AtomicLong atomicLong = new AtomicLong(0L);

    public static long generateMessageId() {
        return atomicLong.incrementAndGet();
    }
}
