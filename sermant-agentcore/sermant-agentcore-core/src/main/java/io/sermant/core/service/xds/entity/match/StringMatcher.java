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

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.utils.StringUtils;

import java.util.Locale;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * String matcher implementation for XDS authorization, supports various matching strategies including exact, prefix,
 * suffix, contain and regex matching with case-insensitive option.
 *
 * @since 2025-06-16
 */
public class StringMatcher implements XdsAuthorizationMatcher<String> {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final String patternToMatch;

    private final MatcherStrategy matchingStrategy;

    private final boolean isCaseNotInsensitive;

    /**
     * Constructs a StringMatcher with specified pattern, matching strategy and case sensitivity
     *
     * @param patternToMatch the pattern string to match against
     * @param matchingStrategy the matching strategy to use
     * @param isCaseNotInsensitive whether matching should be case-insensitive
     * @throws IllegalArgumentException if patternToMatch is null or empty
     */
    public StringMatcher(String patternToMatch, MatcherStrategy matchingStrategy, boolean isCaseNotInsensitive) {
        if (StringUtils.isEmpty(patternToMatch)) {
            LOGGER.warning("Pattern to match cannot be null or empty");
            throw new IllegalArgumentException("Pattern to match cannot be null or empty");
        }
        this.patternToMatch = isCaseNotInsensitive ? patternToMatch.toLowerCase(Locale.ROOT) : patternToMatch;
        this.matchingStrategy = matchingStrategy;
        this.isCaseNotInsensitive = isCaseNotInsensitive;
    }

    @Override
    public boolean match(String inputString) {
        if (StringUtils.isEmpty(inputString)) {
            LOGGER.fine("Input string is null or empty, matching failed");
            return false;
        }
        String caseString = isCaseNotInsensitive ? inputString.toLowerCase(Locale.ROOT) : inputString;
        switch (matchingStrategy) {
            case EXACT:
                return caseString.equals(patternToMatch);
            case PREFIX:
                return caseString.startsWith(patternToMatch);
            case SUFFIX:
                return caseString.endsWith(patternToMatch);
            case CONTAIN:
                return caseString.contains(patternToMatch);
            case REGEX:
                try {
                    return Pattern.matches(patternToMatch, inputString);
                } catch (Exception e) {
                    LOGGER.warning("Regex pattern matching failed: " + e.getMessage());
                    return false;
                }
            default:
                throw new UnsupportedOperationException(
                        "Unsupported string matching operation: " + matchingStrategy);
        }
    }

    public String getPattern() {
        return patternToMatch;
    }

    public boolean isCaseNotInsensitive() {
        return isCaseNotInsensitive;
    }

    public MatcherStrategy getStrategy() {
        return matchingStrategy;
    }

    /**
     * Matcher Strategy
     *
     * @since 2025-06-16
     */
    public enum MatcherStrategy {
        /**
         * Exact match strategy
         */
        EXACT("exact"),
        /**
         * Prefix match strategy
         */
        PREFIX("prefix"),
        /**
         * Suffix match strategy
         */
        SUFFIX("suffix"),
        /**
         * Regex match strategy
         */
        REGEX("regex"),
        /**
         * Contain match strategy
         */
        CONTAIN("contain");

        private final String strategyName;

        MatcherStrategy(String strategyName) {
            this.strategyName = strategyName;
        }
    }
}
