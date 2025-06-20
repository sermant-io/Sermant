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

package io.sermant.implement.service.xds.utils;

import io.envoyproxy.envoy.config.core.v3.CidrRange;
import io.envoyproxy.envoy.config.route.v3.HeaderMatcher;
import io.envoyproxy.envoy.type.matcher.v3.RegexMatcher;
import io.sermant.core.service.xds.entity.match.IpMatcher;
import io.sermant.core.service.xds.entity.match.StringMatcher;
import io.sermant.core.service.xds.entity.match.StringMatcher.MatcherStrategy;
import io.sermant.core.utils.StringUtils;

/**
 * Utility class for converting various xDS matcher types to Sermant's internal matcher representation.
 * <p>
 * This class provides static methods to convert between Envoy's matcher types (StringMatcher, HeaderMatcher, etc.) and
 * Sermant's internal matcher types.
 * </p>
 *
 * @since 2025-06-17
 */
public class MatcherUtil {
    private MatcherUtil() {
    }

    /**
     * Converts Envoy StringMatcher to Sermant StringMatcher.
     *
     * @param envoyStringMatcher the Envoy StringMatcher to convert
     * @return converted Sermant StringMatcher, or null if input is null
     */
    public static StringMatcher convertEnvoyStringMatcher(
            io.envoyproxy.envoy.type.matcher.v3.StringMatcher envoyStringMatcher) {
        if (envoyStringMatcher == null) {
            return null;
        }
        boolean ignoreCase = envoyStringMatcher.getIgnoreCase();
        String exactValue = envoyStringMatcher.getExact();
        if (!StringUtils.isBlank(exactValue)) {
            return new StringMatcher(exactValue, MatcherStrategy.EXACT, ignoreCase);
        }
        String prefixValue = envoyStringMatcher.getPrefix();
        if (!StringUtils.isBlank(prefixValue)) {
            return new StringMatcher(prefixValue, MatcherStrategy.PREFIX, ignoreCase);
        }
        String suffixValue = envoyStringMatcher.getSuffix();
        if (!StringUtils.isBlank(suffixValue)) {
            return new StringMatcher(suffixValue, MatcherStrategy.SUFFIX, ignoreCase);
        }
        String containsValue = envoyStringMatcher.getContains();
        if (!StringUtils.isBlank(containsValue)) {
            return new StringMatcher(containsValue, MatcherStrategy.CONTAIN, ignoreCase);
        }
        String regexValue = envoyStringMatcher.getSafeRegex().getRegex();
        if (!StringUtils.isBlank(regexValue)) {
            return new StringMatcher(regexValue, MatcherStrategy.REGEX, ignoreCase);
        }
        return null;
    }

    /**
     * Converts Envoy CidrRange to Sermant IpMatcher.
     *
     * @param cidrRange the CidrRange to convert
     * @return converted IpMatcher
     */
    public static IpMatcher convertCidrRangeToIpMatcher(CidrRange cidrRange) {
        return new IpMatcher(cidrRange.getPrefixLen().getValue(), cidrRange.getAddressPrefix());
    }

    /**
     * Converts Envoy HeaderMatcher to Sermant StringMatcher.
     *
     * @param headerMatcher the HeaderMatcher to convert
     * @return converted StringMatcher, or null if input is null
     */
    public static StringMatcher convertHeaderMatcher(HeaderMatcher headerMatcher) {
        return convertEnvoyStringMatcher(convertHeaderMatcherToEnvoyString(headerMatcher));
    }

    /**
     * Converts Envoy HeaderMatcher to Envoy StringMatcher.
     *
     * @param headerMatcher the HeaderMatcher to convert
     * @return converted StringMatcher, or null if input is null
     */
    private static io.envoyproxy.envoy.type.matcher.v3.StringMatcher convertHeaderMatcherToEnvoyString(
            HeaderMatcher headerMatcher) {
        if (headerMatcher == null) {
            return null;
        }

        if (headerMatcher.getPresentMatch()) {
            return io.envoyproxy.envoy.type.matcher.v3.StringMatcher.newBuilder()
                    .setSafeRegex(RegexMatcher.newBuilder().build())
                    .setIgnoreCase(true)
                    .build();
        }

        if (!headerMatcher.hasStringMatch()) {
            io.envoyproxy.envoy.type.matcher.v3.StringMatcher.Builder builder =
                    io.envoyproxy.envoy.type.matcher.v3.StringMatcher.newBuilder();
            String exactMatch = headerMatcher.getExactMatch();
            String containsMatch = headerMatcher.getContainsMatch();
            String prefixMatch = headerMatcher.getPrefixMatch();
            String suffixMatch = headerMatcher.getSuffixMatch();
            RegexMatcher safeRegex = headerMatcher.getSafeRegexMatch();

            if (!StringUtils.isEmpty(exactMatch)) {
                builder.setExact(exactMatch);
            } else if (!StringUtils.isEmpty(containsMatch)) {
                builder.setContains(containsMatch);
            } else if (!StringUtils.isEmpty(prefixMatch)) {
                builder.setPrefix(prefixMatch);
            } else if (!StringUtils.isEmpty(suffixMatch)) {
                builder.setSuffix(suffixMatch);
            } else if (safeRegex.isInitialized()) {
                builder.setSafeRegex(safeRegex);
            }
            return builder.setIgnoreCase(true).build();
        }
        return headerMatcher.getStringMatch();
    }
}
