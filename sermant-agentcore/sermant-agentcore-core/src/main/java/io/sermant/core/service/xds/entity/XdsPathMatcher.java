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

package io.sermant.core.service.xds.entity;

import io.sermant.core.service.xds.entity.match.MatchStrategy;

import java.util.Locale;

/**
 * XdsPathMatcher
 *
 * @author daizhenyu
 * @since 2024-08-06
 **/
public class XdsPathMatcher {
    private final MatchStrategy matchStrategy;

    private final boolean caseSensitive;

    /**
     * parameterized constructor
     *
     * @param matchStrategy match strategy
     * @param caseSensitive case sensitive
     */
    public XdsPathMatcher(MatchStrategy matchStrategy, boolean caseSensitive) {
        this.matchStrategy = matchStrategy;
        this.caseSensitive = caseSensitive;
    }

    /**
     * the request path matches the route configuration path or not.
     *
     * @param path request path
     * @return isMatch
     */
    public boolean isMatch(String path) {
        return path != null && matchStrategy.isMatch(caseSensitive ? path : path.toLowerCase(Locale.ROOT));
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }
}
