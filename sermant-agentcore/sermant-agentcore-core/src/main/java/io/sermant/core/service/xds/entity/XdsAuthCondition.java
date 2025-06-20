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

package io.sermant.core.service.xds.entity;

import io.sermant.core.service.xds.entity.match.XdsAuthorizationMatcher;

import java.util.Objects;

/**
 * XDS authentication condition class, defines conditions required for xDS service authentication
 * <p>
 * Contains authentication type, key for authentication and matcher, used to verify if request meets authentication
 * conditions
 * </p>
 *
 * @since 2025-06-17
 */
public class XdsAuthCondition {
    /**
     * Authentication type, determines which part of request to get authentication info from
     */
    private final XdsAuthorizationType authType;

    /**
     * When authentication type is HEADER or CLAIM, specifies the key to get authentication info
     */
    private final String key;

    /**
     * Matcher for validating incoming requests
     */
    private final XdsAuthorizationMatcher matcher;

    /**
     * Constructor
     *
     * @param authType authentication type
     * @param matcher matcher for validating incoming requests
     */
    public XdsAuthCondition(XdsAuthorizationType authType, XdsAuthorizationMatcher matcher) {
        this.authType = authType;
        this.matcher = matcher;
        this.key = null;
    }

    /**
     * Constructor
     *
     * @param authType authentication type
     * @param key key for validating incoming requests
     * @param matcher matcher for validating incoming requests
     */
    public XdsAuthCondition(XdsAuthorizationType authType, String key, XdsAuthorizationMatcher matcher) {
        this.authType = authType;
        this.matcher = matcher;
        this.key = key;
    }

    /**
     * Get authentication type
     *
     * @return authentication type enum value
     */
    public XdsAuthorizationType getAuthType() {
        return authType;
    }

    /**
     * Get authentication key
     *
     * @return key string, may be null
     */
    public String getKey() {
        return key;
    }

    /**
     * Get matcher
     *
     * @return matcher instance
     */
    public XdsAuthorizationMatcher getMatcher() {
        return matcher;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof XdsAuthCondition)) {
            return false;
        }
        XdsAuthCondition that = (XdsAuthCondition) object;
        return authType == that.authType
                && Objects.equals(key, that.key)
                && Objects.equals(matcher, that.matcher);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authType, key, matcher);
    }
}
