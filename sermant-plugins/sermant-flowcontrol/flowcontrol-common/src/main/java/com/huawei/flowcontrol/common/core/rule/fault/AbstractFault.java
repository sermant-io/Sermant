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
 * 抽象层fault, 做一层公共处理
 *
 * @author zhouss
 * @since 2022-08-05
 */
public abstract class AbstractFault implements Fault {
    private static final int PERCENT_UNIT = 100;

    /**
     * 请求基数缓冲, 防止多并发场景导致请求溢出(请求溢出不会导致问题, 只是数据会重置, 存在部分线程统计数据被忽略)
     */
    private static final long COUNT_BUFFER = 10000000L;

    private final AtomicLong reqCount = new AtomicLong();

    private final FaultRule rule;

    /**
     * 创建错误注入
     *
     * @param rule 错误注入规则
     * @throws IllegalArgumentException rule为空抛出
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
     * 朴素贝叶斯概率模型
     *
     * @return 核对触发概率
     */
    private boolean checkPercent(long curReqCount) {
        final int percentage = rule.getPercentage();
        long reqOld = (curReqCount - 1) * percentage / PERCENT_UNIT;
        long reqNew = curReqCount * percentage / PERCENT_UNIT;
        return reqOld != reqNew;
    }

    /**
     * 执行错误注入
     *
     * @param faultRule 错误注入规则
     */
    protected abstract void exeFault(FaultRule faultRule);
}
