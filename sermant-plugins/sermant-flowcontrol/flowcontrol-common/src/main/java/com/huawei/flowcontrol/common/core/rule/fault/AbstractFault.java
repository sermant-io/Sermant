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

/**
 * 抽象层fault, 做一层公共处理
 *
 * @author zhouss
 * @since 2022-08-05
 */
public abstract class AbstractFault implements Fault {
    private static final int PERCENT_UNIT = 100;

    private final FaultRule rule;

    private long reqCount;

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
        reqCount++;
        if (isNeedFault()) {
            exeFault(rule);
        }
    }

    private boolean isNeedFault() {
        if (rule.isForceClosed()) {
            return false;
        }
        return checkPercent();
    }

    /**
     * 朴素贝叶斯概率模型
     *
     * @return 核对触发概率
     */
    private boolean checkPercent() {
        final int percentage = rule.getPercentage();
        long reqOld = (reqCount - 1) * percentage / PERCENT_UNIT;
        long reqNew = reqCount * percentage / PERCENT_UNIT;
        return reqOld != reqNew;
    }

    /**
     * 执行错误注入
     *
     * @param faultRule 错误注入规则
     */
    protected abstract void exeFault(FaultRule faultRule);
}
