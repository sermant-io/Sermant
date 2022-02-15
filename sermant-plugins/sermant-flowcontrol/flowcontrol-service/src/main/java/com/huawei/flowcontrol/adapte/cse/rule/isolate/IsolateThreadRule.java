/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.flowcontrol.adapte.cse.rule.isolate;

import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 隔离仓规则 基于信号量实现
 *
 * @author zhouss
 * @since 2021-12-04
 */
public class IsolateThreadRule extends AbstractRule {
    private static final long DEFAULT_WAIT_TIME_MS = 1000L;

    private static final int DEFAULT_CONCURRENT_CALLS = 5;

    private Semaphore semaphore;

    /**
     * 获取许可的最大等待时间 单位MS
     */
    private long maxWaitDuration;

    /**
     * 最大并发调用数
     */
    private int maxConcurrentCalls;

    public IsolateThreadRule() {
        this(null);
    }

    public IsolateThreadRule(String resource) {
        this(resource, DEFAULT_CONCURRENT_CALLS, DEFAULT_WAIT_TIME_MS);
    }

    public IsolateThreadRule(String resource, int permitNum, long waitTimeMs) {
        this(resource, permitNum, waitTimeMs, true);
    }

    public IsolateThreadRule(String resource, int permitNum, long waitTimeMs, boolean isForUse) {
        super();
        super.setResource(resource);
        super.setLimitApp(RuleConstant.LIMIT_APP_DEFAULT);
        this.maxWaitDuration = waitTimeMs;
        this.maxConcurrentCalls = permitNum;
        if (isForUse) {
            this.semaphore = new Semaphore(permitNum);
        }
    }

    /**
     * 尝试获取通行证
     *
     * @param permit 令牌数
     * @throws IsolateThreadException 获取通行证失败抛出
     */
    public void tryEntry(int permit) throws IsolateThreadException {
        if (!tryAcquirePermit(permit)) {
            throw new IsolateThreadException(super.getLimitApp(), "Exceeded max permit can acquire!", this);
        }
    }

    /**
     * 尝试获取许可
     *
     * @return 是否获取成功
     */
    private boolean tryAcquirePermit(int permit) {
        try {
            return semaphore.tryAcquire(permit, maxWaitDuration, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    /**
     * 请求结束, 释放通行证
     *
     * @param permit 令牌数
     */
    public void exit(int permit) {
        semaphore.release(permit);
    }

    public long getMaxWaitDuration() {
        return maxWaitDuration;
    }

    public IsolateThreadRule setMaxWaitDuration(long maxWaitDuration) {
        this.maxWaitDuration = maxWaitDuration;
        return this;
    }

    public int getAvailablePermits() {
        return semaphore.availablePermits();
    }

    public int getMaxConcurrentCalls() {
        return maxConcurrentCalls;
    }

    public synchronized IsolateThreadRule setMaxConcurrentCalls(int maxConcurrentCalls) {
        // 差值
        final int gap = this.maxConcurrentCalls - maxConcurrentCalls;
        if (gap == 0) {
            return this;
        } else if (gap > 0) {
            semaphore.acquireUninterruptibly(gap);
        } else {
            // 如果新配置比旧配置增加更多许可，则此处直接增加许可数
            semaphore.release(Math.abs(gap));
        }
        this.maxConcurrentCalls = maxConcurrentCalls;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }

        IsolateThreadRule rule = (IsolateThreadRule) obj;

        if (maxWaitDuration != rule.maxWaitDuration) {
            return false;
        }
        return maxConcurrentCalls == rule.maxConcurrentCalls;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (maxWaitDuration ^ (maxWaitDuration >>> 32));
        result = 31 * result + maxConcurrentCalls;
        return result;
    }
}
