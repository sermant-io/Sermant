/*
 * Copyright (C) 2025-2025 Sermant Authors. All rights reserved.
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

package io.sermant.core.service.xds.entity.match;

/**
 * Generic interface for XDS authorization matchers. Defines the contract for matching objects of type T against
 * authorization criteria.
 *
 * @param <T> the type of objects this matcher can match against
 * @since 2025-06-17
 */
public interface XdsAuthorizationMatcher<T> {
    /**
     * Matches the given object against the authorization criteria.
     *
     * @param obj the object to be matched
     * @return true if the object matches the criteria, false otherwise
     */
    boolean match(T obj);
}
