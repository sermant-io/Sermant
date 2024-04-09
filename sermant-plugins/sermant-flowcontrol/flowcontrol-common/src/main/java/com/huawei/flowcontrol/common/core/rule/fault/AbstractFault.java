/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.flowcontrol.common.core.rule.fault;

import java.util.concurrent.atomic.AtomicLong;

/**
 * The abstraction layer is fault and does a common layer of processing
 *
 * @author zhouss
 * @since 2022-08-05
 */
public abstract class AbstractFault implements Fault {
    private static final int PERCENT_UNIT = 100;

    /**
     * Request base buffering prevents requests from overflowing in multiple concurrent scenarios
     * (Request overflow does not cause problems, but the data is reset and some thread statistics are ignored)
     */
    private static final long COUNT_BUFFER = 10000000L;

    private final AtomicLong reqCount = new AtomicLong();

    private final FaultRule rule;

    /**
     * create error injection
     *
     * @param rule error injection rule
     * @throws IllegalArgumentException thrown if rule is null
     */
    protected AbstractFault(FaultRule rule) {
        if (rule == null) {
            throw new IllegalArgumentException("Fault rule can not be empty!");
        }
        this.rule = rule;
    }

    @Override
    public void acquirePermission() {
        long curReqCount = reqCount.incrementAndGet();
        if (curReqCount == Long.MAX_VALUE - COUNT_BUFFER) {
            reqCount.set(0L);
        }
        if (isNeedFault(curReqCount)) {
            exeFault(rule);
        }
    }

    private boolean isNeedFault(long curReqCount) {
        if (rule.isForceClosed()) {
            return false;
        }
        return checkPercent(curReqCount);
    }

    /**
     * naive bayesian probability model
     *
     * @param curReqCount current number of requests
     * @return check trigger probability
     */
    private boolean checkPercent(long curReqCount) {
        final int percentage = rule.getPercentage();
        long reqOld = (curReqCount - 1) * percentage / PERCENT_UNIT;
        long reqNew = curReqCount * percentage / PERCENT_UNIT;
        return reqOld != reqNew;
    }

    /**
     * execution error injection
     *
     * @param faultRule error injection rule
     */
    protected abstract void exeFault(FaultRule faultRule);
}
