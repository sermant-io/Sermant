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
 * Based on io/micrometer/core/instrument/MeterRegistry.java
 * from the Micrometer project.
 */

package io.sermant.core.service.metric.api;

import java.util.Collection;
import java.util.Map;
import java.util.function.ToDoubleFunction;

/**
 * metric
 *
 * @author zwmagic
 * @since 2024-08-16
 */
public interface Gauge {
    /**
     * Register a gauge that reports the value of the {@link Number}.
     *
     * @param number Thread-safe implementation of {@link Number} used to access the value.
     * @param <T> The type of the state object from which the gauge value is extracted.
     * @return The number that was passed in so the registration can be done as part of an assignment statement.
     */
    default <T extends Number> T gaugeNumber(Number number) {
        return (T) gaugeState(number, Number::doubleValue);
    }

    /**
     * Register a gauge that reports the size of the {@link Collection}. The registration will keep a weak reference to
     * the collection so it will not prevent garbage collection. The collection implementation used should be thread
     * safe. Note that calling {@link Collection#size()} can be expensive for some collection implementations and should
     * be considered before registering.
     *
     * @param collection Thread-safe implementation of {@link Collection} used to access the value.
     * @param <T> The type of the state object from which the gauge value is extracted.
     * @return The Collection that was passed in so the registration can be done as part of an assignment statement.
     */
    default <T extends Collection<?>> T gaugeCollectionSize(T collection) {
        return gaugeState(collection, Collection::size);
    }

    /**
     * Register a gauge that reports the size of the {@link Map}. The registration will keep a weak reference to the
     * collection so it will not prevent garbage collection. The collection implementation used should be thread safe.
     * Note that calling {@link Map#size()} can be expensive for some collection implementations and should be
     * considered before registering.
     *
     * @param map Thread-safe implementation of {@link Map} used to access the value.
     * @param <T> The type of the state object from which the gauge value is extracted.
     * @return The Map that was passed in so the registration can be done as part of an assignment statement.
     */
    default <T extends Map<?, ?>> T gaugeMapSize(T map) {
        return gaugeState(map, Map::size);
    }

    /**
     * Register a gauge that reports the value of the object after the function {@code valueFunction} is applied. The
     * registration will keep a weak reference to the object so it will not prevent garbage collection. Applying
     * {@code valueFunction} on the object should be thread safe.
     *
     * @param stateObject State object used to compute a value.
     * @param valueFunction Function that produces an instantaneous gauge value from the state object.
     * @param <T> The type of the state object from which the gauge value is extracted.
     * @return The state object that was passed in so the registration can be done as part of an assignment statement.
     */
    <T> T gaugeState(T stateObject, ToDoubleFunction<T> valueFunction);
}
