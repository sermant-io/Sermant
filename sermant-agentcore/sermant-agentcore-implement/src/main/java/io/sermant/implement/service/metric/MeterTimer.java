/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.implement.service.metric;

import io.micrometer.core.instrument.MeterRegistry;
import io.sermant.core.service.metric.api.Timer;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * metric
 *
 * @author zwmagic
 * @since 2024-08-19
 */
public class MeterTimer implements Timer {
    private final MeterRegistry registry;

    private final io.micrometer.core.instrument.Timer timer;

    private io.micrometer.core.instrument.Timer.Sample sample;

    /**
     * Constructs a timer associated with the specified MeterRegistry and Timer. This constructor initializes a
     * MeterTimer object to integrate with the MeterRegistry and Timer from the Micrometer library.
     *
     * @param registry An instance of MeterRegistry used for creating and managing metrics.
     * @param timer An instance of io.micrometer.core.instrument.Timer used for timing and recording durations.
     */
    public MeterTimer(MeterRegistry registry, io.micrometer.core.instrument.Timer timer) {
        this.registry = registry;
        this.timer = timer;
    }

    @Override
    public Timer start() {
        this.sample = io.micrometer.core.instrument.Timer.start(registry);
        return this;
    }

    @Override
    public long stop() {
        if (sample == null) {
            return -1L;
        }
        return sample.stop(timer);
    }

    @Override
    public void record(Duration duration) {
        timer.record(duration);
    }

    @Override
    public void record(long amount, TimeUnit unit) {
        timer.record(amount, unit);
    }

    @Override
    public long count() {
        return timer.count();
    }

    @Override
    public double totalTime(TimeUnit unit) {
        return timer.totalTime(unit);
    }
}
