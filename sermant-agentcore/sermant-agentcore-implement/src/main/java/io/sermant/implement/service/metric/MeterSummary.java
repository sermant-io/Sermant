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

import io.micrometer.core.instrument.DistributionSummary;
import io.sermant.core.service.metric.api.Summary;

/**
 * The MeterSummary class implements the Summary interface and represents a metered summary, capable of recording,
 * aggregating, and reporting the distribution of a certain metric.
 *
 * @author zwmagic
 * @since 2024-08-19
 */
public class MeterSummary implements Summary {
    private final DistributionSummary summary;

    /**
     * Constructor that creates a MeterSummary instance.
     *
     * @param distributionSummary A DistributionSummary instance used internally to record and aggregate metric data.
     */
    public MeterSummary(DistributionSummary distributionSummary) {
        this.summary = distributionSummary;
    }

    /**
     * Records a new metric value.
     *
     * @param amount The metric value to be recorded.
     */
    @Override
    public void record(double amount) {
        summary.record(amount);
    }

    /**
     * Gets the total number of recorded metric values.
     *
     * @return The total number of recorded metric values.
     */
    @Override
    public long count() {
        return summary.count();
    }

    /**
     * Gets the sum of all recorded metric values.
     *
     * @return The sum of all recorded metric values.
     */
    @Override
    public double totalAmount() {
        return summary.totalAmount();
    }

    /**
     * Gets the maximum value among the recorded metric values.
     *
     * @return The maximum value among the recorded metric values.
     */
    @Override
    public double max() {
        return summary.max();
    }
}
