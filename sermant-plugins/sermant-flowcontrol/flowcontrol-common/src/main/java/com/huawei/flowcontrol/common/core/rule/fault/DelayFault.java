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

import com.huaweicloud.sermant.core.common.LoggerFactory;

import java.util.Locale;
import java.util.logging.Logger;

/**
 * 延迟错误注入
 *
 * @author zhouss
 * @since 2022-08-08
 */
public class DelayFault extends AbstractFault {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 延迟时间
     */
    private final long delayTimeMs;

    /**
     * 创建错误注入
     *
     * @param rule 错误注入规则
     * @throws IllegalArgumentException rule为空抛出
     */
    public DelayFault(FaultRule rule) {
        super(rule);
        this.delayTimeMs = rule.getParsedDelayTime();
    }

    @Override
    protected void exeFault(FaultRule faultRule) {
        try {
            LOGGER.fine(String.format(Locale.ENGLISH,
                    "Start delay request by delay fault, delay time is [%s]ms", delayTimeMs));
            Thread.sleep(delayTimeMs);
        } catch (InterruptedException ignored) {
            // ignored
        }
    }
}
