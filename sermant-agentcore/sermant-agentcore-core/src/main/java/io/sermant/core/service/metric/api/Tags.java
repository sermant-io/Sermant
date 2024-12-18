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
 * Based on io.micrometer.core.instrument/Tags.java
 * from the Micrometer project.
 */

package io.sermant.core.service.metric.api;

import io.sermant.core.utils.MapUtils;
import io.sermant.core.utils.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * metric tags
 *
 * @author zwmagic
 * @since 2024-08-16
 */
public final class Tags {
    /**
     * Key for the scope tag
     */
    public static final String SCOPE = "scope";
    /**
     * Value for the core scope tag
     */
    public static final String SCOPE_VALUE_CORE = "core";
    private final Map<String, String> tags = new HashMap<>();

    private Tags() {
    }

    /**
     * Creates an empty Tags object.
     *
     * @return An empty Tags object.
     */
    public static Tags of() {
        return new Tags();
    }

    /**
     * Creates a Tags object and adds a key-value pair.
     *
     * @param key The key.
     * @param value The value.
     * @return A Tags object with the added key-value pair.
     */
    public static Tags of(String key, Object value) {
        return of().add(key, value);
    }

    /**
     * Creates a Tags object and copies the content from another Tags object.
     *
     * @param tags The Tags object to copy.
     * @return A new Tags object with copied content.
     */
    public static Tags of(Tags tags) {
        return of(tags.getTags());
    }

    /**
     * Creates a Tags object initialized with a given map of key-value pairs.
     *
     * @param tags The map of key-value pairs.
     * @return An initialized Tags object.
     */
    public static Tags of(Map<String, String> tags) {
        Tags result = of();
        if (!MapUtils.isEmpty(tags)) {
            result.tags.putAll(tags);
        }
        return result;
    }

    /**
     * Adds a core scope tag.
     *
     * @return Tags Returns the current Tags instance to support method chaining.
     */
    public Tags addCoreScope() {
        return addScope(SCOPE_VALUE_CORE);
    }

    /**
     * Adds a scope tag.
     *
     * @param scopeValue scope value
     * @return Tags Returns the current Tags instance to support method chaining.
     */
    public Tags addScope(String scopeValue) {
        tags.put(SCOPE, StringUtils.isEmpty(scopeValue) ? "undefined" : scopeValue);
        return this;
    }

    /**
     * Adds a key-value pair to the current Tags object.
     *
     * @param key The key.
     * @param value The value.
     * @return The current Tags object, for chaining calls.
     */
    public Tags add(String key, Object value) {
        if (StringUtils.isEmpty(key) || value == null) {
            return this;
        }
        String valueStr = value instanceof String ? (String) value : String.valueOf(value);
        if (StringUtils.isEmpty(valueStr)) {
            return this;
        }
        tags.put(key, valueStr);
        return this;
    }

    /**
     * Gets the number of key-value pairs in the current Tags object.
     *
     * @return The number of key-value pairs.
     */
    public int getSize() {
        return tags.size();
    }

    /**
     * Retrieves all key-value pairs in the current Tags object as an unmodifiable map.
     *
     * @return An unmodifiable map of key-value pairs.
     */
    public Map<String, String> getTags() {
        return Collections.unmodifiableMap(tags);
    }
}
