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

package com.huawei.javamesh.core.lubanops.core.transfer.circuit;

import com.google.inject.Inject;
import com.huawei.javamesh.core.lubanops.bootstrap.api.CircuitBreaker;
import com.huawei.javamesh.core.lubanops.bootstrap.commons.LubanApmConstants;
import com.huawei.javamesh.core.lubanops.bootstrap.plugin.apm.APMCollector;
import com.huawei.javamesh.core.lubanops.core.executor.ExecuteRepository;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author
 * @date 2020/12/17 14:35
 */
public class ReportCircuitBreaker implements CircuitBreaker {

    public AtomicBoolean reset = new AtomicBoolean(true);

    public long resetTime = System.currentTimeMillis();

    @Inject
    ExecuteRepository executeRepository;

    private APMCollector apmCollector = APMCollector.INSTANCE;

    /**
     * open flag,default close.
     */
    private AtomicBoolean state = new AtomicBoolean(false);

    public ReportCircuitBreaker() {
    }

    @Override
    public boolean allowRequest() {
        if (isOpen()) {
            if (reset.get()) {
                return true;
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean isOpen() {
        return state.get();
    }

    @Override
    public void markSuccess() {
        if (state.get()) {
            state.compareAndSet(true, false);
        }
    }

    @Override
    public void markFailure() {
        if (!state.get()) {
            if (apmCollector.successPercent(LubanApmConstants.SPAN_EVENT_DATA_TYPE) > 0.5
                    && apmCollector.successPercent(LubanApmConstants.MONITOR_DATA_TYPE) > 0.5) {
                state.compareAndSet(false, true);
            }
        }
    }
}
