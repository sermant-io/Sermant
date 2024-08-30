/*
 * Copyright 2017 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Based on io/micrometer/core/instrument/DistributionSummary.java
 * from the Micrometer project.
 */

package io.sermant.core.service.metric.api;

/**
 * The Summary interface is used to maintain statistical information about a series of events.
 *
 * @author zwmagic
 * @since 2024-08-17
 */
public interface Summary {
    /**
     * Updates the statistics kept by the summary with the specified amount.
     *
     * @param amount Amount for an event being measured. For example, if the size in bytes of responses from a server.
     * If the amount is less than 0, the value will be dropped.
     */
    void record(double amount);

    /**
     * Returns the number of times that record has been called since this timer was created.
     *
     * @return The number of recorded events.
     */
    long count();

    /**
     * Returns the total amount of all recorded events.
     *
     * @return The total amount.
     */
    double totalAmount();

    /**
     * Returns the distribution average for all recorded events.
     *
     * @return The average event size.
     */
    default double mean() {
        long count = count();
        return count == 0 ? 0 : totalAmount() / count;
    }

    /**
     * Returns the maximum time of a single event.
     *
     * @return The maximum event size.
     */
    double max();
}
