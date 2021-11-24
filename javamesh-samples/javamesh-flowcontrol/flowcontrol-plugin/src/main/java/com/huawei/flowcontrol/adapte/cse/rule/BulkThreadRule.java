/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adapte.cse.rule;

import com.alibaba.csp.sentinel.slots.block.Rule;

/**
 * 隔离仓规则
 *
 * @author zhouss
 * @since 2021-11-15
 */
public class BulkThreadRule extends AbstractRule {
    /**
     * 最大并发数
     */
    public static final int DEFAULT_MAX_CONCURRENT_CALLS = 1000;

    /**
     * 默认最大等待时间
     */
    public static final long DEFAULT_MAX_WAIT_DURATION_MS = 0L;

    /**
     * 最大并发数
     */
    private int maxConcurrentCalls = DEFAULT_MAX_CONCURRENT_CALLS;

    /**
     * 最大等待时间
     */
    private String maxWaitDuration = String.valueOf(DEFAULT_MAX_WAIT_DURATION_MS);

    /**
     * 转换后的最大等待时间
     */
    private long parsedMaxWaitDuration = DEFAULT_MAX_WAIT_DURATION_MS;

    @Override
    public boolean isValid() {
        if (maxConcurrentCalls < 0) {
            return true;
        }
        if (parsedMaxWaitDuration < 0) {
            return true;
        }
        return super.isValid();
    }

    @Override
    public Rule convertToSentinelRule() {
        return null;
    }

    public long getParsedMaxWaitDuration() {
        return parsedMaxWaitDuration;
    }

    public int getMaxConcurrentCalls() {
        return maxConcurrentCalls;
    }

    public void setMaxConcurrentCalls(int maxConcurrentCalls) {
        this.maxConcurrentCalls = maxConcurrentCalls;
    }

    public String getMaxWaitDuration() {
        return maxWaitDuration;
    }

    public void setMaxWaitDuration(String maxWaitDuration) {
        this.maxWaitDuration = maxWaitDuration;
        this.parsedMaxWaitDuration = parseLongTime(maxWaitDuration, DEFAULT_MAX_WAIT_DURATION_MS);
    }
}
