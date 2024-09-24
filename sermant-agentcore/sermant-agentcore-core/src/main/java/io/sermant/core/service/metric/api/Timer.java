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
 * Based on io/micrometer/core/instrument/Timer.java
 * from the Micrometer project.
 */

package io.sermant.core.service.metric.api;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * A timer interface for measuring the duration of events.
 *
 * @author zwmagic
 * @since 2024-08-16
 */
public interface Timer {
    /**
     * Starts the timer.
     *
     * @return The Timer object for further operations.
     */
    Timer start();

    /**
     * Stops the timer and returns the elapsed time.
     *
     * @return The elapsed time in milliseconds.
     */
    long stop();

    /**
     * Records a duration.
     *
     * @param duration The duration object to be recorded.
     */
    void record(Duration duration);

    /**
     * Updates the statistics stored by the timer with a specified amount.
     *
     * @param amount The duration of a single event; if amount is less than 0, the value will be removed.
     * @param unit The time unit.
     */
    void record(long amount, TimeUnit unit);

    /**
     * Gets the number of times that stop has been called on this timer.
     *
     * @return The number of stop operations.
     */
    long count();

    /**
     * Gets the total time of recorded events.
     *
     * @param unit The base unit of time to scale the total to.
     * @return The total time of recorded events.
     */
    double totalTime(TimeUnit unit);
}
